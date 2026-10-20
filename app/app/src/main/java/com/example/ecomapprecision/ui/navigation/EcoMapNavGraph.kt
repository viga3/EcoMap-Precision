package com.example.ecomapprecision.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecomapprecision.location.LocationResultData
import com.example.ecomapprecision.ui.components.AggiornamentiDialog
import com.example.ecomapprecision.ui.components.AppDrawerContent
import com.example.ecomapprecision.ui.components.InfoDialog
import com.example.ecomapprecision.ui.components.ScreenTab
import com.example.ecomapprecision.ui.screen.addpostazione.AddPostazioneScreen
import com.example.ecomapprecision.ui.screen.addpostazione.AddPostazioneViewModel
import com.example.ecomapprecision.ui.screen.map.MapScreen
import com.example.ecomapprecision.ui.screen.profile.ProfileScreen
import com.example.ecomapprecision.ui.screen.segnalazione.SegnalazioneScreen
import com.example.ecomapprecision.ui.screen.segnalazione.SegnalazioneViewModel
import com.example.ecomapprecision.ui.screen.segnalazionericevute.SegnalazioniRicevuteScreen
import com.example.ecomapprecision.ui.screen.selectlocation.SelectLocationScreen
import com.example.ecomapprecision.ui.screen.settings.ImpostazioniScreen
import com.example.ecomapprecision.ui.screen.storico.StoricoScreen
import kotlinx.coroutines.launch

import androidx.navigation.NavType
import androidx.navigation.navArgument

object NavDestinations {
    const val MAP = "map"
    const val STORICO = "storico"
    const val PROFILO = "profilo"
    const val ADD_POSTAZIONE = "add_postazione"
    const val SEGNALAZIONE = "segnalazione"
    const val SEGNALAZIONI_RICEVUTE = "segnalazioni_ricevute"
    const val IMPOSTAZIONI = "impostazioni"
    const val SELECT_LOCATION = "select_location"
}

@Composable
fun EcoMapNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavDestinations.MAP
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showUpdatesDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    fun openDrawer() {
        coroutineScope.launch {
            drawerState.open()
        }
    }

    fun closeDrawer() {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun navigateToProfile() {
        navController.navigate(NavDestinations.PROFILO) {
            launchSingleTop = true
        }
    }

    fun handleTabNavigation(tab: ScreenTab) {
        val route = when (tab) {
            ScreenTab.Map -> NavDestinations.MAP
            ScreenTab.Storico -> NavDestinations.STORICO
            ScreenTab.Segnalazioni -> NavDestinations.SEGNALAZIONI_RICEVUTE
            ScreenTab.Profilo -> NavDestinations.PROFILO
        }
        navController.navigate(route) {
            popUpTo(NavDestinations.MAP) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            AppDrawerContent(
                onNavigateToSettings = {
                    navController.navigate(NavDestinations.IMPOSTAZIONI)
                },
                onNavigateToProfile = { navigateToProfile() },
                onShowUpdatesDialog = { showUpdatesDialog = true },
                onShowInfoDialog = { showInfoDialog = true },
                onCloseDrawer = { closeDrawer() }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(NavDestinations.MAP) {
                MapScreen(
                    onNavigateTab = { tab -> handleTabNavigation(tab) },
                    onNavigateToAddPostazione = { lat, lng ->
                        navController.navigate("${NavDestinations.ADD_POSTAZIONE}?lat=$lat&lng=$lng")
                    },
                    onNavigateToSegnalazione = { lat, lng ->
                        if (lat != null && lng != null) {
                            navController.navigate("${NavDestinations.SEGNALAZIONE}?lat=$lat&lng=$lng")
                        } else {
                            navController.navigate(NavDestinations.SEGNALAZIONE)
                        }
                    },
                    onNavigateToSegnalazioniRicevute = { navController.navigate(NavDestinations.SEGNALAZIONI_RICEVUTE) },
                    onMenuClick = { openDrawer() },
                    onProfileClick = { navigateToProfile() }
                )
            }

            composable(
                route = "${NavDestinations.ADD_POSTAZIONE}?lat={lat}&lng={lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("lng") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val addVm: AddPostazioneViewModel = viewModel(backStackEntry)
                AddPostazioneScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSelectLocation = { navController.navigate(NavDestinations.SELECT_LOCATION) },
                    viewModel = addVm
                )
            }

            composable(NavDestinations.SELECT_LOCATION) {
                val previousEntry = navController.previousBackStackEntry
                val addVm: AddPostazioneViewModel = viewModel(previousEntry ?: it)
                val currentLoc = addVm.uiState.value.locationData

                SelectLocationScreen(
                    initialLocation = currentLoc,
                    onNavigateBack = { navController.popBackStack() },
                    onLocationConfirmed = { newLocation ->
                        addVm.updateManualLocation(newLocation)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "${NavDestinations.SEGNALAZIONE}?lat={lat}&lng={lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("lng") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val segnalazioneVm: SegnalazioneViewModel = viewModel(backStackEntry)
                SegnalazioneScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = segnalazioneVm
                )
            }

            composable(NavDestinations.SEGNALAZIONI_RICEVUTE) {
                SegnalazioniRicevuteScreen(
                    onNavigateTab = { tab -> handleTabNavigation(tab) },
                    onNavigateToNewSegnalazione = { navController.navigate(NavDestinations.SEGNALAZIONE) },
                    onNavigateBack = { navController.popBackStack() },
                    onMenuClick = { openDrawer() },
                    onProfileClick = { navigateToProfile() }
                )
            }

            composable(NavDestinations.STORICO) {
                StoricoScreen(
                    onNavigateTab = { tab -> handleTabNavigation(tab) },
                    onNavigateToAddPostazione = { navController.navigate(NavDestinations.ADD_POSTAZIONE) },
                    onMenuClick = { openDrawer() },
                    onProfileClick = { navigateToProfile() }
                )
            }

            composable(NavDestinations.PROFILO) {
                ProfileScreen(
                    onNavigateTab = { tab -> handleTabNavigation(tab) },
                    onNavigateBack = { navController.popBackStack() },
                    onMenuClick = { openDrawer() }
                )
            }

            composable(NavDestinations.IMPOSTAZIONI) {
                ImpostazioniScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        if (showUpdatesDialog) {
            AggiornamentiDialog(onDismiss = { showUpdatesDialog = false })
        }

        if (showInfoDialog) {
            InfoDialog(onDismiss = { showInfoDialog = false })
        }
    }
}
