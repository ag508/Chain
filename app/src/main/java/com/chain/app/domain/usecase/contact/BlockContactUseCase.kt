package com.chain.app.domain.usecase.contact

import com.chain.app.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Use case to block a contact.
 */
class BlockContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contactId: String): Result<Unit> {
        return contactRepository.blockContact(contactId)
    }
}
