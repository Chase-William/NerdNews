package com.ritstudentchase.nerdnews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritstudentchase.nerdnews.dao.NerdNewsDatabase
import com.ritstudentchase.nerdnews.ui.theme.NerdNewsTheme
import com.ritstudentchase.nerdnews.viewmodels.ChocolateyFeedViewModel
import kotlinx.coroutines.*
import androidx.compose.material3.* // Material Theme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create database instance
        val db = NerdNewsDatabase.getDatabase(applicationContext)
        // create viewModel with access to database
        val chocolatey = ChocolateyFeedViewModel(db, applicationContext)

        setContent {
            NerdNewsTheme {
                FeedView(chocolatey)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedView(chocolateyFeed: ChocolateyFeedViewModel) {
    LaunchedEffect(Unit, block = {
        withContext(Dispatchers.IO) {
            // Load local
            chocolateyFeed.loadLocal() // Must load local otherwise an insertion will take place with the same link causing unique key failure in backing SQLite
            // Then fetch remote
            chocolateyFeed.mergeRemoteFeedItems()
        }
    })

    Scaffold(
        topBar = {

        },
        content = {
            Column(modifier = Modifier.padding(it)) {
                if (chocolateyFeed.getItems().isNotEmpty()) {
                    // Documentation used to setup refresh
                    // https://developer.android.com/reference/kotlin/androidx/compose/material/pullrefresh/package-summary
                    // State variables for refresh
                    val refreshScope = rememberCoroutineScope()
                    var refreshing by remember { mutableStateOf(false) }

                    fun refresh() = refreshScope.launch {
                        // -- Must perform web request on IO dispatcher
                        withContext(Dispatchers.IO) {
                            refreshing = true
                            chocolateyFeed.mergeRemoteFeedItems()
                            refreshing = false
                        }
                    }
                    val refreshState = rememberPullRefreshState(refreshing, ::refresh)

                    Box(Modifier.pullRefresh(refreshState)) {
                        val channel = chocolateyFeed.getChannel().value!!
                        Text(channel.title)
                        Text(channel.description)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            if (!refreshing) {
                                items(chocolateyFeed.getItems()) { item ->
                                    Column() {
                                        Text(
                                            item!!.title,
                                            fontSize = 20.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            item.pubDate,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Cursive
                                        )
                                        Text(
                                            item.content,
                                            modifier = Modifier
                                                .padding(top = 4.dp, bottom = 4.dp)
                                        )
                                    } // Column
                                } // items
                            } // if
                        } // LazyColumn
                        PullRefreshIndicator(refreshing, refreshState, Modifier.align(Alignment.TopCenter))
                    } // Box
                } // if
                else {
                    Column() {
                        // Display default content when nothing is available
                        Text("Nothing to display.")
                    }
                }
            } // Column
        } // content
    )
}
