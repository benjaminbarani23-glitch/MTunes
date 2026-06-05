package com.mtunes.app.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mtunes.app.domain.model.Friend
import com.mtunes.app.domain.model.FriendStatus
import com.mtunes.app.ui.theme.CyberGreen
import com.mtunes.app.ui.theme.CyberGreenDim
import com.mtunes.app.ui.theme.DarkCard
import com.mtunes.app.ui.theme.DarkSurfaceVariant
import com.mtunes.app.ui.theme.PureBlack
import com.mtunes.app.ui.theme.TextGray

@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val myFriendCode by viewModel.myFriendCode.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()

    Scaffold(
        containerColor = PureBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddFriendDialog() },
                containerColor = CyberGreen,
                contentColor = PureBlack
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add Friend")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CyberGreen
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // My Friend Code card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Friend Code",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = myFriendCode,
                                style = MaterialTheme.typography.headlineMedium,
                                color = CyberGreen,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { /* Copy to clipboard */ }) {
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = CyberGreen
                                )
                            }
                            IconButton(onClick = { /* Show QR */ }) {
                                Icon(
                                    Icons.Filled.QrCode,
                                    contentDescription = "QR Code",
                                    tint = CyberGreen
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (friends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No friends yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Add friends to share music",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            items(friends) { friend ->
                FriendItem(friend = friend)
            }
        }
    }

    if (showAddDialog) {
        AddFriendDialog(
            onDismiss = { viewModel.hideAddFriendDialog() },
            onAdd = { code -> viewModel.addFriend(code) }
        )
    }
}

@Composable
private fun FriendItem(friend: Friend) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name.first().uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = CyberGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (friend.status == FriendStatus.ONLINE) CyberGreen
                                else TextGray
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (friend.status == FriendStatus.ONLINE) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${friend.sharedSongCount} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var friendCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text("Add Friend", color = CyberGreen)
        },
        text = {
            Column {
                Text(
                    "Enter your friend's code to connect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = friendCode,
                    onValueChange = { friendCode = it },
                    label = { Text("Friend Code") },
                    placeholder = { Text("MT-XXXXXXXX") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGreen,
                        unfocusedBorderColor = TextGray,
                        cursorColor = CyberGreen,
                        focusedLabelColor = CyberGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(friendCode) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                enabled = friendCode.isNotBlank()
            ) {
                Text("Add", color = PureBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
