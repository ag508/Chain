package com.chain.app.presentation.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.ContactRepository
import com.chain.app.domain.usecase.contact.AddContactUseCase
import com.chain.app.domain.usecase.contact.GetContactsUseCase
import com.chain.app.domain.usecase.contact.SearchContactByPhoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for contact search screen.
 * Handles searching for contacts by phone number and adding them.
 */
@HiltViewModel
class ContactSearchViewModel @Inject constructor(
    private val searchContactByPhoneUseCase: SearchContactByPhoneUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ContactSearchResult>>(emptyList())
    val searchResults: StateFlow<List<ContactSearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Cache of existing contacts for quick lookup
    private var existingContacts: List<Contact> = emptyList()

    init {
        // Load existing contacts on initialization
        viewModelScope.launch {
            existingContacts = getContactsUseCase().first()
        }
    }

    /**
     * Search for contacts by phone number.
     */
    fun searchContacts(phoneNumber: String) {
        Log.d("ContactSearchViewModel", "Searching for: $phoneNumber")
        _searchQuery.value = phoneNumber

        // Clear results if query is too short
        if (phoneNumber.length < 3) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isSearching.value = true

                // Search for the contact
                val contact = searchContactByPhoneUseCase(phoneNumber)

                if (contact != null) {
                    // Check if contact is already added
                    val isAdded = existingContacts.any { it.phoneNumber == contact.phoneNumber }

                    _searchResults.value = listOf(
                        ContactSearchResult(
                            phoneNumber = contact.phoneNumber,
                            name = contact.displayName,
                            avatar = contact.avatar,
                            isAdded = isAdded
                        )
                    )
                } else {
                    // No contact found
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ContactSearchViewModel", "Error searching contacts", e)
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * Add a contact by phone number.
     */
    fun addContact(
        phoneNumber: String,
        onSuccess: (Contact) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Log.d("ContactSearchViewModel", "Adding contact: $phoneNumber")

        viewModelScope.launch {
            try {
                // First search for the contact to get their details
                val contact = searchContactByPhoneUseCase(phoneNumber)

                if (contact == null) {
                    onError("Contact not found")
                    return@launch
                }

                // Add the contact
                addContactUseCase(
                    userId = contact.userId,
                    phoneNumber = contact.phoneNumber,
                    displayName = contact.displayName,
                    avatar = contact.avatar,
                    publicKey = contact.publicKey
                ).fold(
                    onSuccess = {
                        Log.d("ContactSearchViewModel", "Contact added successfully")
                        // Refresh existing contacts
                        existingContacts = getContactsUseCase().first()
                        // Update search results to reflect added state
                        searchContacts(_searchQuery.value)
                        onSuccess(it)
                    },
                    onFailure = { error ->
                        Log.e("ContactSearchViewModel", "Failed to add contact", error)
                        onError(error.message ?: "Failed to add contact")
                    }
                )
            } catch (e: Exception) {
                Log.e("ContactSearchViewModel", "Exception adding contact", e)
                onError(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Clear search results.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}
