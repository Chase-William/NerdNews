package com.ritstudentchase.nerdnews

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritstudentchase.nerdnews.models.ChocolateyItem
import com.ritstudentchase.nerdnews.viewmodels.ChocolateyFeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChocolateyFeedScreen(chocolateyFeed: ChocolateyFeedViewModel) {
//    LaunchedEffect(Unit, block = {
//        withContext(Dispatchers.IO) {
//            chocolateyFeed.mergeRemoteFeedItems()
//            chocolateyFeed.loadLocal()
//        }
//    })
//
//    Column(
//        modifier = Modifier
//            .padding(2.dp)
//            .background(MaterialTheme.colorScheme.surface)
//
//    ) {
//        if (chocolateyFeed.getItems().isNotEmpty()) {
//            // Documentation used to setup refresh
//            // https://developer.android.com/reference/kotlin/androidx/compose/material/pullrefresh/package-summary
//            // State variables for refresh
//            val refreshScope = rememberCoroutineScope()
//            var refreshing by remember { mutableStateOf(false) }
//
//            fun refresh() = refreshScope.launch {
//                // -- Must perform web request on IO dispatcher
//                withContext(Dispatchers.IO) {
//                    refreshing = true
//                    chocolateyFeed.mergeRemoteFeedItems()
//                    refreshing = false
//                }
//            }
//            val refreshState = rememberPullRefreshState(refreshing, ::refresh)
//
//            Box(Modifier.pullRefresh(refreshState)) {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(4.dp),
//
//                    ) {
//                    if (!refreshing) {
//                        items(chocolateyFeed.getItems()) { item ->
//                            Box(
//                                modifier = Modifier.padding(top = 10.dp)
//                            ) {
//                                Column(
//                                    modifier = Modifier
//                                        .border(
//                                            2.dp,
//                                            MaterialTheme.colorScheme.surface, // make shadow instead
//                                            RoundedCornerShape(1.dp)
//                                        )
//                                ) {
//                                    Column(
//                                        modifier = Modifier
//                                            .background(MaterialTheme.colorScheme.surface)
//                                            .padding(10.dp)
//                                    ) {
//                                        Text(
//                                            item!!.title,
//                                            fontSize = 20.sp,
//                                            fontFamily = FontFamily.Monospace,
//                                            color = MaterialTheme.colorScheme.onSurface,
//                                            fontWeight = FontWeight(700)
//                                        )
//                                        Text(
//                                            item.pubDate,
//                                            fontSize = 12.sp,
//                                            fontFamily = FontFamily.Cursive,
//                                            color = MaterialTheme.colorScheme.onSurface
//                                        )
//                                    } // Column
//
//                                    val test = isSystemInDarkTheme()
//                                    if (test) {
//                                        WebViewWrapper(item = item!!, backgroundColorId = R.color.outer_space_crayola)
//                                    } else {
//                                        WebViewWrapper(item = item!!, backgroundColorId = R.color.light_ghost_white)
//                                    }
//                                } // Column
//                            } // Box
//                        } // items
//                    } // if
//                } // LazyColumn
//                PullRefreshIndicator(refreshing, refreshState, Modifier.align(Alignment.TopCenter))
//            } // Box
//        } // if
//        else {
//            Column() {
//                // Display default content when nothing is available
//                Text("Nothing to display.")
//            }
//        }
//    } // Column

    FeedItemView(feed = chocolateyFeed) {
        val item = it as ChocolateyItem
        Box(
            modifier = Modifier.padding(top = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surface, // make shadow instead
                            RoundedCornerShape(1.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Text(
                            item.title,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight(700)
                        )
                        Text(
                            item.pubDate,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Cursive,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } // Column

                    val test = isSystemInDarkTheme()
                    if (test) {
                        WebViewWrapper(item = item, backgroundColorId = R.color.outer_space_crayola)
                    } else {
                        WebViewWrapper(item = item, backgroundColorId = R.color.light_ghost_white)
                    }
                } // Column
            } // Box
        }

//    LaunchedEffect(Unit, block = {
//        withContext(Dispatchers.IO) {
//            // Load local
//            chocolateyFeed.loadLocal() // Must load local otherwise an insertion will take place with the same link causing unique key failure in backing SQLite
//            // Then fetch remote
//            chocolateyFeed.merge()
//        }
//    })

//    Column(
//        modifier = Modifier
//            .padding(2.dp)
//            .background(MaterialTheme.colorScheme.surface)
//
//    ) {
//        if (chocolateyFeed.getItems().isNotEmpty()) {
//            // Documentation used to setup refresh
//            // https://developer.android.com/reference/kotlin/androidx/compose/material/pullrefresh/package-summary
//            // State variables for refresh
//            val refreshScope = rememberCoroutineScope()
//            var refreshing by remember { mutableStateOf(false) }
//
//            fun refresh() = refreshScope.launch {
//                // -- Must perform web request on IO dispatcher
//                withContext(Dispatchers.IO) {
//                    refreshing = true
//                    chocolateyFeed.merge()
//                    refreshing = false
//                }
//            }
//            val refreshState = rememberPullRefreshState(refreshing, ::refresh)
//
//            Box(Modifier.pullRefresh(refreshState)) {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(4.dp),
//
//                    ) {
//                    if (!refreshing) {
//                        items(chocolateyFeed.getItems()) { item ->
//                            Box(
//                                modifier = Modifier.padding(top = 10.dp)
//                            ) {
//                                Column(
//                                    modifier = Modifier
//                                        .border(
//                                            2.dp,
//                                            MaterialTheme.colorScheme.surface, // make shadow instead
//                                            RoundedCornerShape(1.dp)
//                                        )
//                                ) {
//                                    Column(
//                                        modifier = Modifier
//                                            .background(MaterialTheme.colorScheme.surface)
//                                            .padding(10.dp)
//                                    ) {
//                                        Text(
//                                            item!!.title,
//                                            fontSize = 20.sp,
//                                            fontFamily = FontFamily.Monospace,
//                                            color = MaterialTheme.colorScheme.onSurface,
//                                            fontWeight = FontWeight(700)
//                                        )
//                                        Text(
//                                            item.pubDate,
//                                            fontSize = 12.sp,
//                                            fontFamily = FontFamily.Cursive,
//                                            color = MaterialTheme.colorScheme.onSurface
//                                        )
//                                    } // Column
//
//                                    val test = isSystemInDarkTheme()
//                                    if (test) {
//                                        WebViewWrapper(item = item!!, backgroundColorId = R.color.outer_space_crayola)
//                                    } else {
//                                        WebViewWrapper(item = item!!, backgroundColorId = R.color.light_ghost_white)
//                                    }
//                                } // Column
//                            } // Box
//                        } // items
//                    } // if
//                } // LazyColumn
//                PullRefreshIndicator(refreshing, refreshState, Modifier.align(Alignment.TopCenter))
//            } // Box
//        } // if
//        else {
//            Column() {
//                // Display default content when nothing is available
//                Text("Nothing to display.")
//            }
//        }
//    } // Column
}
