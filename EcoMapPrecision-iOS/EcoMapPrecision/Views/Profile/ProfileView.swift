//
//  ProfileView.swift
//  EcoMapPrecision
//

import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var dataStore: DataStore
    @Binding var isDrawerOpen: Bool
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Operator Avatar Card
                    VStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .fill(AppColors.primaryGreen.opacity(0.15))
                                .frame(width: 90, height: 90)
                            Image(systemName: "person.crop.circle.fill")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 80, height: 80)
                                .foregroundColor(AppColors.primaryGreen)
                        }
                        
                        VStack(spacing: 4) {
                            Text("Michele Villani")
                                .font(.title2.bold())
                            Text("Operatore Monitoraggio Ambientale")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 16)
                    
                    // Stats Grid
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Statistiche Attività")
                            .font(.headline)
                            .padding(.horizontal)
                        
                        let totalBins = dataStore.postazioni.reduce(0) { $0 + $1.totaleBins }
                        let risolteCount = dataStore.segnalazioni.filter { $0.stato == .risolto }.count
                        
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            StatCard(
                                title: "Postazioni Mappate",
                                value: "\(dataStore.postazioni.count)",
                                icon: "map.fill",
                                color: AppColors.primaryGreen
                            )
                            StatCard(
                                title: "Cassonetti Censiti",
                                value: "\(totalBins)",
                                icon: "trash.fill",
                                color: AppColors.secondaryBlue
                            )
                            StatCard(
                                title: "Segnalazioni Totali",
                                value: "\(dataStore.segnalazioni.count)",
                                icon: "exclamationmark.triangle.fill",
                                color: .orange
                            )
                            StatCard(
                                title: "Casi Risolti",
                                value: "\(risolteCount)",
                                icon: "checkmark.circle.fill",
                                color: AppColors.darkTeal
                            )
                        }
                        .padding(.horizontal)
                    }
                    
                    // Device & App info
                    VStack(spacing: 12) {
                        InfoRow(label: "Versione Applicazione", value: "2.1.0b (iOS Native)")
                        InfoRow(label: "Motore Grafico", value: "SwiftUI + MapKit")
                        InfoRow(label: "Sistema Operativo", value: "iOS 17+")
                        InfoRow(label: "Precisione GPS", value: "Alta (Sub-metrica)")
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(16)
                    .padding(.horizontal)
                }
                .padding(.bottom, 24)
            }
            .navigationTitle("Profilo Operatore")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        withAnimation { isDrawerOpen.toggle() }
                    }) {
                        Image(systemName: "line.3.horizontal")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(AppColors.primaryGreen)
                    }
                }
            }
        }
    }
}

private struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(color)
                Spacer()
            }
            Text(value)
                .font(.system(size: 26, weight: .bold, design: .rounded))
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(14)
    }
}

private struct InfoRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 14))
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.system(size: 14, weight: .medium))
        }
    }
}
