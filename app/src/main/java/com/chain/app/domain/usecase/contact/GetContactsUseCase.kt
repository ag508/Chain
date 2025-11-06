package com.chain.app.domain.usecase.contact

import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all contacts.
 * Returns a Flow that emits the list of contacts whenever it changes.
 */
class GetContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    operator fun invoke(): Flow<List<Contact>> {
        return contactRepository.getAllContacts()
    }
}
