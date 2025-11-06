package com.chain.app.data.p2p.mdns

import android.content.Context
import android.net.wifi.WifiManager
import com.chain.app.domain.model.Peer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton
import org.jmdns.JmDNS
import org.jmdns.ServiceEvent
import org.jmdns.ServiceInfo
import org.jmdns.ServiceListener

/**
 * Manages mDNS (Multicast DNS) for local network peer discovery.
 * Discovers peers on the same WiFi/LAN network.
 */
@Singleton
class MDNSManager @Inject constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var jmdns: JmDNS? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    // Service type for Chain app
    private val serviceType = "_chain-p2p._tcp.local."

    // Local service info
    private var localServiceInfo: ServiceInfo? = null

    // Discovered peers channel
    private val _discoveredPeers = Channel<Peer>(Channel.BUFFERED)
    val discoveredPeers: Flow<Peer> = _discoveredPeers.receiveAsFlow()

    // Active peers
    private val activePeers = mutableMapOf<String, Peer>()

    /**
     * Start mDNS discovery and advertise local peer.
     */
    suspend fun startDiscovery(localPeerId: String, localPublicKey: String, port: Int = 5353): Result<Unit> {
        return try {
            // Acquire multicast lock for WiFi
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("chain_mdns").apply {
                setReferenceCounted(true)
                acquire()
            }

            // Get local IP address
            val localAddress = getLocalIpAddress()

            // Initialize JmDNS
            jmdns = JmDNS.create(localAddress, "Chain-$localPeerId").also { jmdns ->

                // Create local service info
                val serviceInfo = ServiceInfo.create(
                    serviceType,
                    localPeerId,
                    port,
                    0,
                    0,
                    mapOf(
                        "peerId" to localPeerId,
                        "publicKey" to localPublicKey,
                        "version" to "1.0"
                    )
                )

                // Register service
                jmdns.registerService(serviceInfo)
                localServiceInfo = serviceInfo

                // Add service listener
                jmdns.addServiceListener(serviceType, ChainServiceListener())

                Timber.d("mDNS discovery started for peer: $localPeerId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start mDNS discovery")
            stopDiscovery()
            Result.failure(e)
        }
    }

    /**
     * Stop mDNS discovery.
     */
    suspend fun stopDiscovery(): Result<Unit> {
        return try {
            localServiceInfo?.let { service ->
                jmdns?.unregisterService(service)
            }

            jmdns?.close()
            jmdns = null

            multicastLock?.release()
            multicastLock = null

            activePeers.clear()

            Timber.d("mDNS discovery stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop mDNS discovery")
            Result.failure(e)
        }
    }

    /**
     * Get list of discovered local peers.
     */
    fun getActivePeers(): List<Peer> {
        return activePeers.values.toList()
    }

    // ========== Private Methods ==========

    private fun getLocalIpAddress(): InetAddress {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipInt = wifiInfo.ipAddress

        return InetAddress.getByAddress(
            byteArrayOf(
                (ipInt and 0xff).toByte(),
                (ipInt shr 8 and 0xff).toByte(),
                (ipInt shr 16 and 0xff).toByte(),
                (ipInt shr 24 and 0xff).toByte()
            )
        )
    }

    // ========== Service Listener ==========

    private inner class ChainServiceListener : ServiceListener {
        override fun serviceAdded(event: ServiceEvent?) {
            event?.let {
                Timber.d("mDNS service added: ${it.name}")
                // Request service info
                jmdns?.requestServiceInfo(it.type, it.name, 1000)
            }
        }

        override fun serviceRemoved(event: ServiceEvent?) {
            event?.let {
                Timber.d("mDNS service removed: ${it.name}")
                activePeers.remove(it.name)
            }
        }

        override fun serviceResolved(event: ServiceEvent?) {
            event?.info?.let { info ->
                val peerId = info.getPropertyString("peerId") ?: return
                val publicKey = info.getPropertyString("publicKey") ?: return

                val addresses = info.inet4Addresses.map { it.hostAddress }.joinToString(",")

                val peer = Peer(
                    id = peerId,
                    address = addresses,
                    publicKey = publicKey,
                    displayName = null,
                    lastSeen = System.currentTimeMillis(),
                    reliability = 1.0,
                    isOnline = true,
                    latency = 0
                )

                activePeers[peerId] = peer

                scope.launch {
                    _discoveredPeers.send(peer)
                }

                Timber.d("mDNS peer discovered: $peerId at $addresses")
            }
        }
    }
}
