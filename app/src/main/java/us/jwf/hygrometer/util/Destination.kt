package us.jwf.hygrometer.util

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.Navigator
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.Serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

sealed interface Destination{
    data class Simple(val name: String) : Destination {
        val route: String = name
    }

    data class WithArgs<T : Parcelable>(
        val name: String,
        val argsClass: Class<T>,
        val serializer: KSerializer<T>,
    ) : Destination {
        val routeTemplate = "$name/{args}"

        fun route(args: T): String =
            "$name/${URLEncoder.encode(Json.encodeToString(serializer, args), Charset.forName("UTF-8"))}"

        fun fromArgString(argString: String): T =
            Json.decodeFromString(serializer, URLDecoder.decode(argString, Charset.forName("UTF-8")))
    }
}

fun NavGraphBuilder.destination(dest: Destination.Simple, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(dest.route) { content(it) }
}

fun <T : Parcelable> NavGraphBuilder.destination(
    dest: Destination.WithArgs<T>,
    content: @Composable (NavBackStackEntry, T) -> Unit,
) {
    composable(
        route = dest.routeTemplate,
        arguments = listOf(
            navArgument("args") {
                type = NavType.StringType
            }
        )
    ) {
        val argString = requireNotNull(it.arguments?.getString("args")) {
            "No args specified"
        }
        content(it, dest.fromArgString(argString = argString))
    }
}

fun NavController.navigate(destination: Destination.Simple) {
    navigate(route = destination.route)
}

fun <T : Parcelable> NavController.navigate(
    destination: Destination.WithArgs<T>,
    args: T,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(route = destination.route(args), builder = builder)
}

private class ArgsDirections(val args: Parcelable) : NavDirections {
    override val actionId: Int
        get() = TODO("Not yet implemented")
    override val arguments: Bundle
        get() = TODO("Not yet implemented")

}
