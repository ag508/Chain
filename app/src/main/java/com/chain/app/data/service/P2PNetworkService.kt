package com.chain.app.data.service

import com.chain.app.domain.model.NetworkStatus
import com.chain.app.domain.usecase.p2p.GetNetworkStatusUseCase
import com.chain.app.domain.usecase.p2p.StartP2PNetworkUseCase
import com.chain.app.domain.usecase.p2p.StopP2PNetworkUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing P2P network lifecycle and monitoring.
 * Provides high-level control over the P2P network and network state monitoring.
 */
@Singleton
class P2PNetworkService @Inject constructor(
    private val startP2PNetworkUseCase: StartP2PNetworkUseCase,
    private val stopP2PNetworkUseCase: StopP2PNetworkUseCase,
    private val getNetworkStatusUseCase: GetNetworkStatusUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _networkStatus = MutableStateFlow(
        NetworkStatus(
            isConnected = false,
            connectedPeers = 0,
            availablePeers = 0,
            localDiscoveredPeers = 0,
            globalDiscoveredPeers = 0,
            activeConnections = 0,
            lastUpdate = System.currentTimeMillis()
        )
    )
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    init {
        // Monitor network status
        scope.launch {
            getNetworkStatusUseCase().collect { status ->
                _networkStatus.value = status
                Timber.v("Network status updated: ${status.connectedPeers} peers connected")
            }
        }
    }

    /**
     * Start the P2P network.
     */
    suspend fun start(): Result<Unit> {
        return try {
            if (_isRunning.value) {
                Timber.w("P2P network already running")
                return Result.success(Unit)
            }

            Timber.i("Starting P2P network service...")
            startP2PNetworkUseCase().getOrThrow()

            _isRunning.value = true
            Timber.i("P2P network service started successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start P2P network service")
            _isRunning.value = false
            Result.failure(e)
        }
    }

    /**
     * Stop the P2P network.
     */
    suspend fun stop(): Result<Unit> {
        return try {
            if (!_isRunning.value) {
                Timber.w("P2P network not running")
                return Result.success(Unit)
            }

            Timber.i("Stopping P2P network service...")
            stopP2PNetworkUseCase().getOrThrow()

            _isRunning.value = false
            Timber.i("P2P network service stopped successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop P2P network service")
            Result.failure(e)
        }
    }

    /**
     * Restart the P2P network.
     */
    suspend fun restart(): Result<Unit> {
        return try {
            Timber.i("Restarting P2P network service...")
            stop()
            kotlinx.coroutines.delay(1000) // Wait 1 second before restarting
            start()
        } catch (e: Exception) {
            Timber.e(e, "Failed to restart P2P network service")
            Result.failure(e)
        }
    }

    /**
     * Check if network is healthy (has peers connected).
     */
    fun isHealthy(): Boolean {
        val status = _networkStatus.value
        return status.isConnected && status.connectedPeers > 0
    }

    /**
     * Get a human-readable status message.
     */
    fun getStatusMessage(): String {
        val status = _networkStatus.value
        return when {
            !_isRunning.value -> "Network Offline"
            !status.isConnected -> "Connecting..."
            status.connectedPeers == 0 -> "No Peers"
            status.connectedPeers == 1 -> "1 Peer Connected"
            else -> "${status.connectedPeers} Peers Connected"
        }
    }
}
