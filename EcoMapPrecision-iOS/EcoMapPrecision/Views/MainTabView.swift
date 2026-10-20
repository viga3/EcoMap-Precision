//
//  MainTabView.swift
//  EcoMapPrecision
//

import SwiftUI
import CoreLocation

enum AppTab: Int {
    case mappa = 0
    case storico = 1
    case segnalazioni = 2
    case profilo = 3
}

struct MainTabView: View {
    @State private var selectedTab: AppTab = .mappa
    @State private var isDrawerOpen: Bool = false
    
    @State private var showAddPostazione: Bool = false
    @State private var addPostazioneCoord: CLLocationCoordinate2D? = nil
    
    @State private var showAddSegnalazione: Bool = false
    @State private var addSegnalazioneCoord: CLLocationCoordinate2D? = nil
    
    @State private var showSettings: Bool = false
    
    var body: some View {
        ZStack {
            // Main Bottom Tab Bar
            TabView(selection: $selectedTab) {
                MapView(
                    isDrawerOpen: $isDrawerOpen,
                    onNavigateToAddPostazione: { coord in
                        self.addPostazioneCoord = coord
                        self.showAddPostazione = true
                    },
                    onNavigateToAddSegnalazione: { coord in
                        self.addSegnalazioneCoord = coord
                        self.showAddSegnalazione = true
                    }
                )
                .tabItem {
                    Label("Mappa", systemImage: "map.fill")
                }
                .tag(AppTab.mappa)
                
                StoricoPostazioniView(
                    isDrawerOpen: $isDrawerOpen,
                    onNavigateToAddPostazione: {
                        self.addPostazioneCoord = nil
                        self.showAddPostazione = true
                    }
                )
                .tabItem {
                    Label("Storico", systemImage: "list.bullet.rectangle.portrait.fill")
                }
                .tag(AppTab.storico)
                
                SegnalazioniRicevuteView(
                    isDrawerOpen: $isDrawerOpen,
                    onNavigateToAddSegnalazione: {
                        self.addSegnalazioneCoord = nil
                        self.showAddSegnalazione = true
                    }
                )
                .tabItem {
                    Label("Segnalazioni", systemImage: "exclamationmark.triangle.fill")
                }
                .tag(AppTab.segnalazioni)
                
                ProfileView(isDrawerOpen: $isDrawerOpen)
                .tabItem {
                    Label("Profilo", systemImage: "person.crop.circle.fill")
                }
                .tag(AppTab.profilo)
            }
            .tint(AppColors.primaryGreen)
            
            // Side Drawer Overlay
            DrawerMenu(
                isOpen: $isDrawerOpen,
                onSelectSettings: {
                    showSettings = true
                },
                onSelectProfile: {
                    selectedTab = .profilo
                }
            )
        }
        .sheet(isPresented: $showAddPostazione) {
            AddPostazioneView(initialCoordinate: addPostazioneCoord)
        }
        .sheet(isPresented: $showAddSegnalazione) {
            AddSegnalazioneView(initialCoordinate: addSegnalazioneCoord)
        }
        .sheet(isPresented: $showSettings) {
            SettingsView()
        }
    }
}
