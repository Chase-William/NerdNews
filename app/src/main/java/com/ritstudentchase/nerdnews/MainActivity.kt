package com.ritstudentchase.nerdnews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.ritstudentchase.nerdnews.ui.theme.NerdNewsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            
            NerdNewsTheme {
                Test()
            }
        }
    }
}

@Composable
fun Test() {
    val scope = rememberCoroutineScope()
    var feed by remember {
        mutableStateOf<ChocolateyChannel?>(null)
    }
    Button(
        onClick = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val test = ChocolateyFeedService.getRSSFeed()
                    feed = test
                    Log.e("Data", test?.link ?: "channel is failing to parse")
                }
                Log.e("Test", "Btn was clicked")
            }
        }
    ) {
        Text(text = feed?.title ?: "Empty")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NerdNewsTheme {
        Test()
    }
}