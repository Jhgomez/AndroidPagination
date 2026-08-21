package com.demo.pagination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.demo.pagination.ui.theme.PaginationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm = viewModel<MainVm>()
            // this was just a test to confirm I was able to communicate with the backend
//            LaunchedEffect(Unit) {
//                vm.getNextPage()
//            }

            val shows = vm.tvShowsPagingFlow.collectAsLazyPagingItems()

            PaginationTheme {
                Column(Modifier.fillMaxSize()) {
                    Button({
                        vm.invalidate()
                        shows.refresh()
                    }) {
                        Text("Invalidate")
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (shows.loadState.refresh == LoadState.Loading) { // for invalidations and refreshes
                            // careful here as rendering this UI in the lazy list when this is ture
                            // will make recomposoe the below list all over again, we could gain
                            // from already visible keys, so you might like to show this loading without
                            // removing the whole list when it happens
                            item {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        } else {
                            if (shows.loadState.source.prepend == LoadState.Loading) { // could use shows.loadState.prepend
                                item {
                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }

                            items(
                                count = shows.itemCount,
                                key = shows.itemKey { it.id }
                            ) { index ->
                                val show = shows[index]
                                if (show != null) {
                                    Text(
                                        text = index.toString(),
                                        color = Color.White,
                                        modifier = Modifier
                                            .height(78.dp)
                                            .background(Color.Blue),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text("Placeholder")
                                }
                            }

                            if (
                                shows.loadState.source.append == LoadState.Loading ||
                                shows.loadState.source.refresh == LoadState.Loading
                            ) {
                                item {
                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PaginationTheme {
        Greeting("Android")
    }
}