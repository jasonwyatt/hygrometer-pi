package us.jwf.hygrometer.plant

import android.telephony.PhoneNumberUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import us.jwf.hygrometer.LocalNavController
import us.jwf.hygrometer.R
import us.jwf.hygrometer.common.Server
import us.jwf.hygrometer.home.HomeViewState
import us.jwf.hygrometer.ui.theme.Typography
import us.jwf.hygrometer.util.Destination
import us.jwf.hygrometer.util.destination
import us.jwf.hygrometer.util.navigate

val PlantDestination = Destination.WithArgs("plant", Server::class.java, Server.serializer())

fun NavGraphBuilder.plantDestination() {
    destination(PlantDestination) { _, server ->
        Plant(server)
    }
}

fun NavController.navigateToPlant(plant: Server) = navigate(PlantDestination, plant)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Plant(args: Server) {
    val viewModel: PlantViewModel = viewModel(factory = PlantViewModel.Factory(args))
    val viewState by viewModel.viewState.collectAsState()
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_up),
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(onClick = navController::navigateUp)
                    )
                },
                title = {
                    Text(
                        text = viewState.server.plantName,
                        style = Typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        when (val state = viewState) {
            is PlantViewState.Loading -> {
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
                        Text(text = stringResource(R.string.plant_loading, state.server.plantName))
                    }
                }
            }

            is PlantViewState.Loaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .wrapContentSize(),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = stringResource(R.string.plant_state_label),
                            style = Typography.titleLarge,
                        )
                        when (val reading = state.readingState) {
                            ReadingState.Error -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = stringResource(R.string.plant_reading_error),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            ReadingState.NotTaken -> {
                                Text(
                                    text = stringResource(R.string.plant_reading_unknown),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            ReadingState.Taking -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = stringResource(R.string.plant_reading_loading))
                                }
                            }
                            is ReadingState.Success -> {
                                if (reading.reading.needsWater) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = "",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.plant_reading_needs_water,
                                                reading.reading.voltage
                                            ),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "",
                                            tint = Color.Green
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.plant_reading_doing_fine,
                                                reading.reading.voltage
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.plant_sample_duration_title),
                                style = Typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.plant_threshold_voltage_title),
                                style = Typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.plant_sms_number_title),
                                style = Typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Column {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.plant_sample_duration,
                                    state.configFile.sampleDurationSeconds,
                                    state.configFile.sampleDurationSeconds,
                                ),
                                style = Typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(
                                    R.string.plant_threshold_voltage,
                                    state.configFile.thresholdVoltage
                                ),
                                style = Typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = PhoneNumberUtils.formatNumber(
                                    state.configFile.smsPhoneNumber,
                                    "US"
                                ),
                                style = Typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.takeReading() },
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        enabled = state.readingState !is ReadingState.Taking
                    ) {
                        Text(text = stringResource(id = R.string.plant_take_a_reading))
                    }
                }
            }
        }
    }
}


