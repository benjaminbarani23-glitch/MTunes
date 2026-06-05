package com.mtunes.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mtunes.app.domain.model.Song
import com.mtunes.app.service.MusicPlayer
import com.mtunes.app.ui.navigation.Screen
import com.mtunes.app.ui.navigation.bottomNavItems
import com.mtunes.app.ui.screens.friends.FriendsScreen
import com.mtunes.app.ui.screens.home.HomeScreen
import com.mtunes.app.ui.screens.library.LibraryScreen
import com.mtunes.app.ui.screens.nowplaying.NowPlayingScreen
import com.mtunes.app.ui.screens.nowplaying.NowPlayingViewModel
import com.mtunes.app.ui.screens.search.SearchScreen
import com.mtunes.app.ui.screens.settings.SettingsScreen
import com.mtunes.app.ui.theme.CyberGreen
import com.mtunes.app.ui.theme.DarkCard
import com.mtunes.app.ui.theme.DarkSurfaceVariant
import com.mtunes.app.ui.theme.MTunesTheme
import com.mtunes.app.ui.theme.PureBlack
import com.mtunes.app.ui.theme.TextGray
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicPlayer: MusicPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MTunesTheme {
                MTunesApp(musicPlayer = musicPlayer)
            }
        }
    }
}

@Composable
fun MTunesApp(musicPlayer: MusicPlayer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentSong by musicPlayer.currentSong.collectAsState()
    val isPlaying by musicPlayer.isPlaying.collectAsState()

    val showBottomBar = currentRoute != Screen.NowPlaying.route
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()

    Scaffold(
        containerColor = PureBlack,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // Mini player
                    AnimatedVisibility(
                        visible = currentSong != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        MiniPlayer(
                            song = currentSong,
                            isPlaying = isPlaying,
                            onTogglePlay = { musicPlayer.togglePlayPause() },
                            onClick = {
                                navController.navigate(Screen.NowPlaying.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // Bottom navigation
                    NavigationBar(
                        containerColor = DarkCard,
                        contentColor = CyberGreen
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = navBackStackEntry?.destination?.hierarchy?.any {
                                it.route == screen.route
                            } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        screen.title,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberGreen,
                                    selectedTextColor = CyberGreen,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray,
                                    indicatorColor = CyberGreen.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route) {
                            launchSingleTop = true
                        }
                    },
                    onPlaySong = { song ->
                        nowPlayingViewModel.playSong(song)
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onPlaySong = { song ->
                        nowPlayingViewModel.playSong(song)
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onPlaySong = { song ->
                        nowPlayingViewModel.playSong(song)
                    }
                )
            }
            composable(Screen.Friends.route) {
                FriendsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.NowPlaying.route) {
                NowPlayingScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit
) {
    if (song == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 1
                )
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = CyberGreen
                )
            }
        }
    }
}
