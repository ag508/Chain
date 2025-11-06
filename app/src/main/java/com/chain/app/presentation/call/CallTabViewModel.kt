package com.chain.app.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Call
import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.CallRepository
import com.chain.app.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallTabViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _callHistory = MutableStateFlow<List<Call>>(emptyList())
    val callHistory: StateFlow<List<Call>> = _callHistory.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        loadCallHistory()
        loadContacts()
    }

    private fun loadCallHistory() {
        viewModelScope.launch {
            callRepository.getCallHistory().collect { calls ->
                // Sort by date, most recent first
                _callHistory.value = calls.sortedByDescending { it.startTime.time }
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contactList ->
                _contacts.value = contactList.filter { !it.isBlocked }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _isSearching.value = false
        } else {
            _isSearching.value = true
        }
    }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchQuery.value = ""
        }
    }
}
