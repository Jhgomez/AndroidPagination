package com.demo.pagination

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.demo.pagination.ui.theme.PaginationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {

            }
        }

        setContent {
            val state = rememberLazyListState()

            val coroutineScope = rememberCoroutineScope()

            val vm = viewModel<MainVm>(
                initializer = {
                    MainVm(
                        application = application,
                        firstVisibleItemProducer = { // doesn't work, remote mediator design is not perfect its been experimental for too long
                            state.firstVisibleItemIndex
                        },
                        visibleItemsCountProducer = { // rename
                            coroutineScope.launch {
                                state.stopScroll()
                                state.scrollToItem(it)
                            }
                        }
                    )
                }
            )

            // this was just a test to confirm I was able to communicate with the backend
//            LaunchedEffect(Unit) {
//                vm.getNextPage()
//            }

            val shows = vm.tvShowsPagingFlow.collectAsLazyPagingItems()

            PaginationTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        Button({
                            vm.invalidate()
                            shows.refresh()
                            Log.d("countLazy", state.layoutInfo.visibleItemsInfo.size.toString())
                            Log.d("firstIndex", state.firstVisibleItemIndex.toString())
                        }) {
                            Text("Invalidate")
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            state = state
                        ) {
                            if (shows.loadState.source.prepend == LoadState.Loading && // could use shows.loadState.prepend
                                shows.loadState.refresh != LoadState.Loading) {  // so only one loading indicator is shown
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
                                key = shows.itemKey { it.tvShow.index }
                            ) { index ->
                                val show = shows[index]
                                if (show != null) {
                                    Text(
                                        text = show.toString(),
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

                            if (shows.loadState.source.append == LoadState.Loading) {
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

                    if (shows.loadState.refresh == LoadState.Loading) { // for invalidations and refreshes
                        // careful here as rendering this UI in the lazy list when this is ture
                        // will make recompose the below list all over again, we could gain
                        // from already visible keys, so you might like to show this loading without
                        // removing the whole list when it happens
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier
                                        .size(48.dp)
                            )
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