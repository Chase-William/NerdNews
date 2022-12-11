package com.ritstudentchase.nerdnews

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
import com.ritstudentchase.nerdnews.util.RssFeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
// Don't like using Any but, don't want to abstract to create another interface just to relate the two feeds
fun FeedItemView(feed: RssFeed<Any>, feedItem: @Composable (item: Any?) -> Unit) {
    LaunchedEffect(Unit, block = {
        withContext(Dispatchers.IO) {
            feed.merge()
            feed.loadLocal()
        }
    })

    Column(
        modifier = Modifier
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.surface)

    ) {
        if (feed.getItems().isNotEmpty()) {
            // Documentation used to setup refresh
            // https://developer.android.com/reference/kotlin/androidx/compose/material/pullrefresh/package-summary
            // State variables for refresh
            val refreshScope = rememberCoroutineScope()
            var refreshing by remember { mutableStateOf(false) }

            fun refresh() = refreshScope.launch {
                // -- Must perform web request on IO dispatcher
                withContext(Dispatchers.IO) {
                    refreshing = true
                    feed.merge()
                    refreshing = false
                }
            }
            val refreshState = rememberPullRefreshState(refreshing, ::refresh)

            Box(Modifier.pullRefresh(refreshState)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),

                    ) {
                    if (!refreshing) {
                        items(feed.getItems()) { item ->
                            feedItem(item)
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
}