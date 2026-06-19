package br.edu.ifsp.scl.sc3044025.postviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.ifsp.scl.sc3044025.postviewer.ui.navigation.AppNavGraph
import br.edu.ifsp.scl.sc3044025.postviewer.ui.theme.PostViewerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as PostViewerApplication
        setContent {
            PostViewerTheme {
                AppNavGraph(application = application)
            }
        }
    }
}