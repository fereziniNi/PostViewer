package br.edu.ifsp.scl.sc3044025.postviewer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.edu.ifsp.scl.sc3044025.postviewer.PostViewerApplication
import br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postdetail.PostDetailScreen
import br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postdetail.PostDetailViewModel
import br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postlist.PostListScreen
import br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postlist.PostListViewModel

private const val ROUTE_POST_LIST = "post_list"
private const val ROUTE_POST_DETAIL = "post_detail/{postId}"
private const val ARG_POST_ID = "postId"

@Composable
fun AppNavGraph(application: PostViewerApplication) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_POST_LIST) {
        composable(ROUTE_POST_LIST) {
            val viewModel: PostListViewModel = viewModel(
                factory = PostListViewModel.Factory(application.postRepository, application.commentRepository)
            )
            PostListScreen(
                viewModel = viewModel,
                onPostClick = { postId -> navController.navigate("post_detail/$postId") }
            )
        }

        composable(
            route = ROUTE_POST_DETAIL,
            arguments = listOf(navArgument(ARG_POST_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt(ARG_POST_ID) ?: return@composable
            val viewModel: PostDetailViewModel = viewModel(
                factory = PostDetailViewModel.Factory(postId, application.commentRepository)
            )
            PostDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}