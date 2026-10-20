//
//  DrawerMenu.swift
//  EcoMapPrecision
//

import SwiftUI

struct DrawerMenu: View {
    @Binding var isOpen: Bool
    var onSelectSettings: () -> Void
    var onSelectProfile: () -> Void
    
    @State private var showInfoAlert = false
    @State private var showUpdatesAlert = false
    
    var body: some View {
        ZStack(alignment: .leading) {
            // Dimmed background
            if isOpen {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            isOpen = false
                        }
                    }
                
                // Drawer content
                VStack(alignment: .leading, spacing: 0) {
                    // Header
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            ZStack {
                                Circle()
                                    .fill(Color.white.opacity(0.2))
                                    .frame(width: 50, height: 50)
                                Image(systemName: "leaf.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.white)
                            }
                            Spacer()
                            Button(action: {
                                withAnimation { isOpen = false }
                            }) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.white.opacity(0.8))
                            }
                        }
                        
                        Text("EcoMap Precision")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)
                        
                        Text("v2.1.0 (iOS Edition)")
                            .font(.system(size: 13))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    .padding(24)
                    .background(AppColors.primaryGreen)
                    
                    // Menu Items
                    VStack(spacing: 8) {
                        DrawerButton(icon: "person.crop.circle", title: "Profilo Operatore") {
                            withAnimation { isOpen = false }
                            onSelectProfile()
                        }
                        
                        DrawerButton(icon: "gearshape.fill", title: "Impostazioni") {
                            withAnimation { isOpen = false }
                            onSelectSettings()
                        }
                        
                        Divider().padding(.vertical, 8)
                        
                        DrawerButton(icon: "info.circle", title: "Informazioni") {
                            showInfoAlert = true
                        }
                        
                        DrawerButton(icon: "arrow.clockwise.circle", title: "Verifica Aggiornamenti") {
                            showUpdatesAlert = true
                        }
                    }
                    .padding(.top, 16)
                    
                    Spacer()
                    
                    // Footer
                    Text("Sviluppato per la gestione ambientale e la mappatura di precisione del territorio.")
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)
                        .padding(24)
                }
                .frame(width: 290)
                .background(Color(UIColor.systemBackground))
                .transition(.move(edge: .leading))
                .alert("Informazioni App", isPresented: $showInfoAlert) {
                    Button("OK", role: .cancel) {}
                } message: {
                    Text("EcoMap Precision per iPhone.\nVersione: 2.1.0\nOperatore: Michele Villani\nMonitoraggio, rilevamento GPS e reportistica integrata.")
                }
                .alert("Aggiornamenti", isPresented: $showUpdatesAlert) {
                    Button("OK", role: .cancel) {}
                } message: {
                    Text("L'applicazione è aggiornata all'ultima versione disponibile.")
                }
            }
        }
    }
}

private struct DrawerButton: View {
    let icon: String
    let title: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(AppColors.primaryGreen)
                    .frame(width: 28)
                Text(title)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.primary)
                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
        }
    }
}
