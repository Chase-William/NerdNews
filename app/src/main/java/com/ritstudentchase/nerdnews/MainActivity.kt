package com.ritstudentchase.nerdnews

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritstudentchase.nerdnews.dao.NerdNewsDatabase
import com.ritstudentchase.nerdnews.ui.theme.NerdNewsTheme
import com.ritstudentchase.nerdnews.viewmodels.ChocolateyFeedViewModel
import kotlinx.coroutines.*
import androidx.compose.material3.* // Material Theme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.ritstudentchase.nerdnews.models.ChocolateyItem
import androidx.compose.material.Icon
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Alignment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create database instance
        val db = NerdNewsDatabase.getDatabase(applicationContext)
        // create viewModel with access to database
        val chocolatey = ChocolateyFeedViewModel(db)

        setContent {
            NerdNewsTheme {
                FeedView(chocolatey)
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedView(chocolateyFeed: ChocolateyFeedViewModel) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            // Load local
//            chocolateyFeed.loadLocal() // Must load local otherwise an insertion will take place with the same link causing unique key failure in backing SQLite
//        }
//    }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Button(onClick = {
                navController.navigate("chocolatey")
                coroutineScope.launch {
                    scaffoldState.drawerState.close()
                }
            }) {
                Text(text = "Choco")
            }
            
            Button(onClick = {
                navController.navigate("microsoft")
                coroutineScope.launch {
                    scaffoldState.drawerState.close()
                }
            }) {
                Text(text = "Micro")
            }
        },
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colorScheme.surface,
                title = {
                    val channel = chocolateyFeed.getChannel().value
                    if (channel != null) {
                        Column {
                            Text(
                                text = channel.title,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight(700),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = channel.link,
                                fontFamily = FontFamily.Cursive,
                                fontWeight = FontWeight(200),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Column {
                            Text("Failed to load Channel")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // open the drawer on click
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Drawer Navigation")
                    }
                }
            )
        },
        content = {
//            LaunchedEffect(Unit, block = {
//                withContext(Dispatchers.IO) {
//                    val r = chocolateyFeed.mergeRemoteFeedItems()
//                    chocolateyFeed.loadLocal()
////                    if (!r.success) {
////                        // Load local
////                        chocolateyFeed.loadLocal() // Must load local otherwise an insertion will take place with the same link causing unique key failure in backing SQLite
////                    }
//                }
//            })
//
//            Button(onClick = {
//                coroutineScope.launch {
//                    withContext(Dispatchers.IO) {
//                        chocolateyFeed.loadLocal()
//                    }
//                }
//            }) {
//                Text(text = ("asdasd"))
//            }
//
//            Column(
//                modifier = Modifier
//                    .padding(2.dp)
//                    .background(MaterialTheme.colorScheme.surface)
//
//            ) {
//                if (chocolateyFeed.getItems().isNotEmpty()) {
//                    // Documentation used to setup refresh
//                    // https://developer.android.com/reference/kotlin/androidx/compose/material/pullrefresh/package-summary
//                    // State variables for refresh
//                    val refreshScope = rememberCoroutineScope()
//                    var refreshing by remember { mutableStateOf(false) }
//
//                    fun refresh() = refreshScope.launch {
//                        // -- Must perform web request on IO dispatcher
//                        withContext(Dispatchers.IO) {
//                            refreshing = true
//                            chocolateyFeed.mergeRemoteFeedItems()
//                            refreshing = false
//                        }
//                    }
//                    val refreshState = rememberPullRefreshState(refreshing, ::refresh)
//
//                    Box(Modifier.pullRefresh(refreshState)) {
//                        LazyColumn(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(4.dp),
//
//                            ) {
//                            if (!refreshing) {
//                                items(chocolateyFeed.getItems()) { item ->
//                                    Box(
//                                        modifier = Modifier.padding(top = 10.dp)
//                                    ) {
//                                        Column(
//                                            modifier = Modifier
//                                                .border(
//                                                    2.dp,
//                                                    MaterialTheme.colorScheme.surface, // make shadow instead
//                                                    RoundedCornerShape(1.dp)
//                                                )
//                                        ) {
//                                            Column(
//                                                modifier = Modifier
//                                                    .background(MaterialTheme.colorScheme.surface)
//                                                    .padding(10.dp)
//                                            ) {
//                                                Text(
//                                                    item!!.title,
//                                                    fontSize = 20.sp,
//                                                    fontFamily = FontFamily.Monospace,
//                                                    color = MaterialTheme.colorScheme.onSurface,
//                                                    fontWeight = FontWeight(700)
//                                                )
//                                                Text(
//                                                    item.pubDate,
//                                                    fontSize = 12.sp,
//                                                    fontFamily = FontFamily.Cursive,
//                                                    color = MaterialTheme.colorScheme.onSurface
//                                                )
//                                            } // Column
//
//                                            val test = isSystemInDarkTheme()
//                                            if (test) {
//                                                WebViewWrapper(item = item!!, backgroundColorId = R.color.outer_space_crayola)
//                                            } else {
//                                                WebViewWrapper(item = item!!, backgroundColorId = R.color.light_ghost_white)
//                                            }
//                                        } // Column
//                                    } // Box
//                                } // items
//                            } // if
//                        } // LazyColumn
//                        PullRefreshIndicator(refreshing, refreshState, Modifier.align(Alignment.TopCenter))
//                    } // Box
//                } // if
//                else {
//                    Column() {
//                        // Display default content when nothing is available
//                        Text("Nothing to display.")
//                    }
//                }
//            } // Column
            NavHost(
                navController, "chocolatey"
            ) {
                composable("chocolatey") {
                    ChocolateyFeedScreen(chocolateyFeed)
                }
                composable("microsoft") {
                    MicrosoftFeedScreen()
                }
            }
        } // content
    )
}

@Composable
fun WebViewWrapper(item: ChocolateyItem, backgroundColorId: Int) {
    AndroidView(
        modifier = Modifier
            .padding(top = 10.dp),
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.loadWithOverviewMode = true
                settings.blockNetworkImage = true
                setBackgroundColor(0x0)
                val arr: String = Base64.encodeToString(item.content.toByteArray(), Base64.NO_PADDING)
                loadData(arr, "text/html", "base64")
            }
        })
}