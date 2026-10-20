//
//  EcoMapPrecisionApp.swift
//  EcoMapPrecision
//
//  Main application entry point for EcoMap Precision iOS.
//

import SwiftUI

@main
struct EcoMapPrecisionApp: App {
    @StateObject private var dataStore = DataStore()
    @StateObject private var locationManager = LocationManager()
    
    var body: some Scene {
        WindowGroup {
            MainTabView()
                .environmentObject(dataStore)
                .environmentObject(locationManager)
        }
    }
}
