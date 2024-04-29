package cn.edu.cidp.www.myapplication.navigation

const val AUTHENTICATION_ROUTE = "authentication"
const val ROOT_ROUTE = "root"
sealed class Screen(val route: String) {
    object Photo : Screen(route = "photo_screen")
    object Preview : Screen(route = "preview_screen")
}