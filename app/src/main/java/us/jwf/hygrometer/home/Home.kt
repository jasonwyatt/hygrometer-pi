package us.jwf.hygrometer.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import us.jwf.hygrometer.LocalApp
import us.jwf.hygrometer.LocalNavController
import us.jwf.hygrometer.R
import us.jwf.hygrometer.common.Server
import us.jwf.hygrometer.plant.navigateToPlant
import us.jwf.hygrometer.ui.theme.Typography
import us.jwf.hygrometer.util.Destination
import us.jwf.hygrometer.util.destination
import us.jwf.hygrometer.util.navigate

val HomeDest = Destination.Simple("home")

fun NavGraphBuilder.homeDestination() {
    destination(HomeDest) { _ ->
        val navController = LocalNavController.current
        Home(
            onPlantClicked = {
                navController.navigateToPlant(it)
            }
        )
    }
}

fun NavController.navigateToHome() = navigate(HomeDest)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    onPlantClicked: (Server) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(LocalApp.current)
    ),
) {
    val viewState by viewModel.viewState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(text = stringResource(id = R.string.app_name))
                }
            )
        }
    ) { paddingValues ->
        when (val state = viewState) {
            is HomeViewState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp)
                        )
                        Text(text = stringResource(id = state.message))
                    }
                }
            }

            is HomeViewState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(id = state.message))
                }
            }
            is HomeViewState.Loaded -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.houseplant))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            LottieAnimation(
                                composition = composition,
                                modifier = Modifier.fillMaxWidth(0.8f)
                                    .padding(25.dp),
                                restartOnPlay = true,
                                iterations = LottieConstants.IterateForever
                            )
                        }
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.home_pick_a_plant),
                            style = Typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    itemsIndexed(state.services) { i, service ->
                        if (i != 0) {
                            HorizontalDivider()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clickable { onPlantClicked(service) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = service.plantName, style = Typography.bodyLarge)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
                        }
                    }
                }
            }
        }
    }
}
