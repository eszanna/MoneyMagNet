package hu.bme.aut.android.hw.ui.nav

sealed class Screen(val route: String) {
    object Home        : Screen("home")
    object Summary     : Screen("summary")
    object Settings     : Screen("settings")
    object ExchangeRates : Screen("rates")
    object Transactions: Screen("transactions")
    object PieChart    : Screen("piechart")
    object BarChart    : Screen("barchart")
    object Detail      : Screen("detail/{id}") {
        // helper to build the real route with ID:
        fun createRoute(id: Long) = "detail/$id"
    }
    object Edit        : Screen("edit/{id}") {
        fun createRoute(id: Long) = "edit/$id"
    }
    object ChartsMenu  : Screen("charts")
    object Goals       : Screen("goals")
}