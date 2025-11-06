package com.chain.app.domain.usecase.contact

import com.chain.app.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Use case to delete a contact from the user's contact list.
 */
class DeleteContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contactId: String): Result<Unit> {
        return contactRepository.deleteContact(contactId)
    }
}
