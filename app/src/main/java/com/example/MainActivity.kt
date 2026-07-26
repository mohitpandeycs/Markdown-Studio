package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarkdownReaderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DocumentViewModel
import com.example.viewmodel.ReaderViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MarkdownStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialUri = intent?.data

        setContent {
            val documentViewModel: DocumentViewModel = viewModel()
            val settings by documentViewModel.readerSettings.collectAsStateWithLifecycle()

            MarkdownStudioTheme(appThemeMode = settings.appThemeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MarkdownReaderApp(initialUri = initialUri, documentViewModel = documentViewModel)
                }
            }
        }
    }
}

@Composable
fun MarkdownReaderApp(
    initialUri: Uri?,
    documentViewModel: DocumentViewModel = viewModel()
) {
    val navController = rememberNavController()
    val readerViewModel: ReaderViewModel = viewModel()

    LaunchedEffect(initialUri) {
        if (initialUri != null) {
            val registered = documentViewModel.registerAndGetOpenedDoc(initialUri)
            val encodedUri = URLEncoder.encode(registered.uriString, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(registered.fileName, StandardCharsets.UTF_8.toString())
            navController.navigate("reader?uri=$encodedUri&title=$encodedTitle")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = documentViewModel,
                onOpenDocument = { uriStr, title ->
                    val encodedUri = URLEncoder.encode(uriStr, StandardCharsets.UTF_8.toString())
                    val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                    navController.navigate("reader?uri=$encodedUri&title=$encodedTitle")
                }
            )
        }

        composable(
            route = "reader?uri={uri}&title={title}",
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType; defaultValue = "" },
                navArgument("title") { type = NavType.StringType; defaultValue = "Document.md" }
            )
        ) { backStackEntry ->
            val uriArg = backStackEntry.arguments?.getString("uri") ?: ""
            val titleArg = backStackEntry.arguments?.getString("title") ?: "Document.md"

            val decodedUri = URLDecoder.decode(uriArg, StandardCharsets.UTF_8.toString())
            val decodedTitle = URLDecoder.decode(titleArg, StandardCharsets.UTF_8.toString())

            MarkdownReaderScreen(
                uriString = decodedUri,
                titleFallback = decodedTitle,
                viewModel = readerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
