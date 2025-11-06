package com.chain.app.domain.usecase.contact

import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Use case to search for a contact by phone number.
 * Searches both local contacts and P2P network.
 * Returns null if contact not found.
 */
class SearchContactByPhoneUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(phoneNumber: String): Contact? {
        // Validate phone number format
        val cleanedPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (cleanedPhone.isEmpty()) {
            return null
        }

        return contactRepository.searchContactByPhone(cleanedPhone)
    }
}
