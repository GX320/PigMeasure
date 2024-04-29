package cn.edu.cidp.www.myapplication.navigation.nav_graph

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import cn.edu.cidp.www.myapplication.navigation.AUTHENTICATION_ROUTE
import cn.edu.cidp.www.myapplication.navigation.Screen
import cn.edu.cidp.www.myapplication.photoCompose
import cn.edu.cidp.www.myapplication.previewCompose

fun NavGraphBuilder.AuthenticationNavGraph(
    navController: NavController,
    lifecycleScope: LifecycleCoroutineScope
){
    navigation(
        startDestination = Screen.Photo.route,
        route = AUTHENTICATION_ROUTE
    ){
        composable(Screen.Photo.route){
            photoCompose(navController)
        }
        composable(Screen.Preview.route){
            previewCompose(navController, lifecycleScope)
        }
    }
}