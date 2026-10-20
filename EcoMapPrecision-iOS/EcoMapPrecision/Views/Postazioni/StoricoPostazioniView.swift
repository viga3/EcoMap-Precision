//
//  StoricoPostazioniView.swift
//  EcoMapPrecision
//

import SwiftUI

struct StoricoPostazioniView: View {
    @EnvironmentObject var dataStore: DataStore
    @Binding var isDrawerOpen: Bool
    var onNavigateToAddPostazione: () -> Void
    
    @State private var searchText: String = ""
    @State private var selectedPostazione: Postazione? = nil
    
    var filteredPostazioni: [Postazione] {
        if searchText.isEmpty {
            return dataStore.postazioni
        } else {
            return dataStore.postazioni.filter {
                $0.nome.localizedCaseInsensitiveContains(searchText) ||
                $0.indirizzo.localizedCaseInsensitiveContains(searchText)
            }
        }
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Summary Bar
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Postazioni Totali")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("\(dataStore.postazioni.count)")
                            .font(.title2.bold())
                            .foregroundColor(AppColors.primaryGreen)
                    }
                    Spacer()
                    VStack(alignment: .trailing, spacing: 2) {
                        Text("Cassonetti Totali")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        let totalBins = dataStore.postazioni.reduce(0) { $0 + $1.totaleBins }
                        Text("\(totalBins)")
                            .font(.title2.bold())
                            .foregroundColor(AppColors.secondaryBlue)
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                
                // List of Postazioni
                List {
                    ForEach(filteredPostazioni) { item in
                        Button(action: {
                            selectedPostazione = item
                        }) {
                            HStack(alignment: .top, spacing: 12) {
                                ZStack {
                                    Circle()
                                        .fill(AppColors.primaryGreen.opacity(0.15))
                                        .frame(width: 44, height: 44)
                                    Image(systemName: "trash.fill")
                                        .font(.system(size: 18))
                                        .foregroundColor(AppColors.primaryGreen)
                                }
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(item.nome)
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(.primary)
                                    Text(item.indirizzo)
                                        .font(.system(size: 13))
                                        .foregroundColor(.secondary)
                                        .lineLimit(1)
                                    
                                    HStack(spacing: 6) {
                                        Text("\(item.totaleBins) cassonetti")
                                            .font(.caption.bold())
                                            .padding(.horizontal, 6)
                                            .padding(.vertical, 2)
                                            .background(AppColors.surfaceContainer)
                                            .cornerRadius(4)
                                    }
                                }
                                
                                Spacer()
                                
                                Image(systemName: "chevron.right")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                            .padding(.vertical, 4)
                        }
                    }
                    .onDelete(perform: dataStore.deletePostazione)
                }
                .listStyle(.plain)
                .searchable(text: $searchText, prompt: "Cerca postazione o via...")
            }
            .navigationTitle("Storico Postazioni")
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
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: onNavigateToAddPostazione) {
                        Image(systemName: "plus")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(AppColors.primaryGreen)
                    }
                }
            }
            .sheet(item: $selectedPostazione) { postazione in
                PostazioneDetailSheet(postazione: postazione)
            }
        }
    }
}
