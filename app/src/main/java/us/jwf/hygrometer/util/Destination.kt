package us.jwf.hygrometer.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.destination(route: String, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(route) { content(it) }
}
