package com.mtunes.app.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtunes.app.data.repository.FriendRepository
import com.mtunes.app.domain.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _myFriendCode = MutableStateFlow("")
    val myFriendCode: StateFlow<String> = _myFriendCode.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        viewModelScope.launch {
            friendRepository.getAllFriends().collect { friends ->
                _friends.value = friends
            }
        }
        viewModelScope.launch {
            friendRepository.getMyFriendCode().collect { code ->
                _myFriendCode.value = code
            }
        }
    }

    fun showAddFriendDialog() {
        _showAddDialog.value = true
    }

    fun hideAddFriendDialog() {
        _showAddDialog.value = false
    }

    fun addFriend(friendCode: String) {
        viewModelScope.launch {
            friendRepository.addFriend(friendCode)
            _showAddDialog.value = false
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            friendRepository.removeFriend(friendId)
        }
    }
}
