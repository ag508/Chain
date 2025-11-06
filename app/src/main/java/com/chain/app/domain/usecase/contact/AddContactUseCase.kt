package com.chain.app.domain.usecase.contact

import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.ContactRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to add a contact to the user's contact list.
 */
class AddContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        userId: String,
        phoneNumber: String,
        displayName: String,
        avatar: String? = null,
        publicKey: String? = null
    ): Result<Contact> {
        return try {
            // Check if contact already exists
            val existingContact = contactRepository.getContactByUserId(userId)
            if (existingContact != null) {
                return Result.failure(Exception("Contact already exists"))
            }

            // Create new contact
            val contact = Contact(
                id = UUID.randomUUID().toString(),
                userId = userId,
                phoneNumber = phoneNumber,
                displayName = displayName,
                avatar = avatar,
                publicKey = publicKey,
                addedAt = Date(),
                isBlocked = false
            )

            // Add to repository
            contactRepository.addContact(contact).fold(
                onSuccess = { Result.success(contact) },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
