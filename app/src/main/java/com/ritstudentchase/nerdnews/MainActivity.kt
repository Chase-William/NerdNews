package com.ritstudentchase.nerdnews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.ritstudentchase.nerdnews.dao.NerdNewsDatabase
import com.ritstudentchase.nerdnews.models.ChocolateyChannel
import com.ritstudentchase.nerdnews.ui.theme.NerdNewsTheme
import com.ritstudentchase.nerdnews.viewmodels.ChocolateyFeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create database instance
        val db = NerdNewsDatabase.getDatabase(applicationContext)
        // create viewModel with access to database
        val chocolatey = ChocolateyFeedViewModel(db, applicationContext)

        setContent {
            NerdNewsTheme {
                Test(chocolatey)
            }
        }
    }
}

@Composable
fun Test(chocolateyFeed: ChocolateyFeedViewModel) {
    LaunchedEffect(Unit, block = {
        chocolateyFeed.loadLocal()
    })

    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    // chocolateyFeed.mergeRemoteFeedItems()
                    Log.e("Data",  "After Remote Feed")
                }
                Log.e("Test", "Btn was clicked")
            }
        }
    ) {
        // Text(text = chocolateyFeed.getChannel().value?.title ?: "Failed to get feed title.")
    }
}