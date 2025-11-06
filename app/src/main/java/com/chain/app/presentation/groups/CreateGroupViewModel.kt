package com.chain.app.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Contact
import com.chain.app.domain.usecase.contact.GetAllContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for create group screen.
 * Handles loading contacts for group selection.
 */
@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val getAllContactsUseCase: GetAllContactsUseCase
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            getAllContactsUseCase().collect { contacts ->
                _contacts.value = contacts
                _isLoading.value = false
            }
        }
    }
}
