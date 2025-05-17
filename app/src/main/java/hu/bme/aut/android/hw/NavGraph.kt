package hu.bme.aut.android.hw

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hu.bme.aut.android.hw.domain.model.Transaction
import hu.bme.aut.android.hw.ui.nav.Screen
import hu.bme.aut.android.hw.ui.screen.ExchangeRatesScreen
import hu.bme.aut.android.hw.ui.screen.HomeScreen
import hu.bme.aut.android.hw.ui.screen.MainSummaryScreen
import hu.bme.aut.android.hw.ui.screen.charts.BarChartScreen
import hu.bme.aut.android.hw.ui.screen.charts.ChartMenuScreen
import hu.bme.aut.android.hw.ui.screen.charts.ChartsScreen
import hu.bme.aut.android.hw.ui.screen.goals.GoalScreen
import hu.bme.aut.android.hw.ui.screen.transactions.MoneyRainOverlay
import hu.bme.aut.android.hw.ui.screen.transactions.TransactionEditScreen
import hu.bme.aut.android.hw.ui.screen.transactions.TransactionReadOnlyScreen
import hu.bme.aut.android.hw.ui.screen.transactions.TransactionScreenHost
import hu.bme.aut.android.hw.ui.settings.SettingsScreen
import hu.bme.aut.android.hw.utils.SortOption
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    //viewModel: TransactionViewModel,
) {
    val navController = rememberNavController()

    val viewModel: TransactionViewModel = hiltViewModel()
    val currencyVm: CurrencyViewModel = hiltViewModel()

    val totalSum    by viewModel.totalSum.collectAsState()
    val filteredSum by viewModel.filteredSum.collectAsState()

    var langMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = (context as? Activity)

    Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)           // take all remaining space
                                .padding(end = 8.dp)  // give a little breathing room before your actions
                        )
                        IconButton(onClick = { langMenuExpanded = true }) {
                            Icon(Icons.Default.Language, contentDescription = null)
                        }
                    }
                   },
                actions = {

                    IconButton(onClick={ navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription=null)
                    }

                    DropdownMenu(
                        expanded = langMenuExpanded,
                        onDismissRequest = { langMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.lang_english)) },
                            onClick = {
                                val list = LocaleListCompat.forLanguageTags("en")
                                AppCompatDelegate.setApplicationLocales(list)
                                langMenuExpanded = false
                                activity?.recreate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.lang_hungarian)) },
                            onClick = {
                                val list = LocaleListCompat.forLanguageTags("hu")
                                AppCompatDelegate.setApplicationLocales(list)
                                langMenuExpanded = false
                                activity?.recreate()     // ← this forces MainActivity to restart with the new locale
                            }
                        )
                    }
                }
            )
        }
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            //modifier = Modifier.weight(1f)
        ) {

            composable(Screen.Settings.route) {
                val ratesState by currencyVm.rates.collectAsState()
                val selectedCurrency by currencyVm.defaultCurrency.collectAsState()
                val lastFetch by currencyVm.lastFetchTime.collectAsState()

                SettingsScreen(
                    rates = ratesState.keys.sorted(),
                    selected = selectedCurrency,
                    onSelect = currencyVm::setDefaultCurrency,
                    onBack = { navController.popBackStack() } ,
                    lastFetchedTime = lastFetch
                )
            }


            composable(Screen.ExchangeRates.route) {
                ExchangeRatesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Summary.route) {
                MainSummaryScreen(
                    totalBalance = totalSum,
                    //filteredBalance = filteredSum,
                    onViewTransactionsClick = {
                        navController.navigate(Screen.Transactions.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onSummary = { navController.navigate(Screen.Summary.route) },
                    onTransactions = { navController.navigate(Screen.Transactions.route) },
                    onCharts = { navController.navigate(Screen.ChartsMenu.route) },
                    onGoals = { navController.navigate(Screen.Goals.route) },
                    onExchangeRates = { navController.navigate(Screen.ExchangeRates.route)}
                )
            }

            composable(Screen.ChartsMenu.route) {
                ChartMenuScreen(
                    onPie = { navController.navigate(Screen.PieChart.route) },
                    onBar = { navController.navigate(Screen.BarChart.route) },
                    onBack = { navController.popBackStack() }
                )

            }

            composable(Screen.PieChart.route) {
                ChartsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BarChart.route) {
                BarChartScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }


            composable(Screen.Transactions.route) {
                val vm: TransactionViewModel = hiltViewModel()

                val sort by vm.sort.collectAsState()
                var menuExpanded by remember { mutableStateOf(false) }
                var showMoneyRain by rememberSaveable { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.transactions_title)) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Sort,
                                            contentDescription = "Sort"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        SortOption.entries.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(stringResource(option.labelRes)) },
                                                onClick = {
                                                    vm.setSort(option)
                                                    menuExpanded = false
                                                },
                                                leadingIcon = {
                                                    if (option == sort)
                                                        Icon(Icons.Default.Check, null)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )


                    },
                    contentWindowInsets = WindowInsets(0)
                ) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)    // keeps status-bar / top-bar inset
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()

                        ) {

                            // render exactly one composable that owns dialog + list + overlay
                            TransactionScreenHost(
                                viewModel = vm,
                                onTransactionClick = { id ->
                                    navController.navigate(Screen.Detail.createRoute(id))
                                },
                                onMoneyRainTrigger = { showMoneyRain = true }
                            )
                        }

                    }
                    MoneyRainOverlay(
                        visible = showMoneyRain,
                        onFinished = { showMoneyRain = false }
                    )

                }

            }

            composable(
                Screen.Detail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("id")
                val vm: TransactionViewModel = hiltViewModel()

                val tx by vm.getTransactionFlow(id).collectAsState(initial = null)

                tx?.let { transaction ->
                    TransactionReadOnlyScreen(
                        transaction = transaction,
                        onEditClick = { navController.navigate(Screen.Edit.createRoute(id)) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            composable(
                Screen.Edit.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                val transaction by produceState<Transaction?>(null, id) {
                    value = viewModel.get(id)
                }

                transaction?.let { _ ->
                    TransactionEditScreen(
                        transaction = transaction!!,
                        viewModel = viewModel,
                        onSave = { updated ->
                            viewModel.add(updated)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Goals.route) {
                val vm: TransactionViewModel = hiltViewModel()
                GoalScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

        }
    }

