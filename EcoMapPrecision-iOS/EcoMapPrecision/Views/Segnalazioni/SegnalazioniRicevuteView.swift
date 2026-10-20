//
//  SegnalazioniRicevuteView.swift
//  EcoMapPrecision
//

import SwiftUI
import UIKit

struct SegnalazioniRicevuteView: View {
    @EnvironmentObject var dataStore: DataStore
    @Binding var isDrawerOpen: Bool
    var onNavigateToAddSegnalazione: () -> Void
    
    @State private var selectedFilter: String = "Tutte"
    @State private var selectedSegnalazione: Segnalazione? = nil
    @State private var showShareSheet: Bool = false
    @State private var pdfDataToShare: Data? = nil
    
    let filterOptions = ["Tutte", "NUOVO", "IN CARICO", "RISOLTO"]
    
    var filteredSegnalazioni: [Segnalazione] {
        if selectedFilter == "Tutte" {
            return dataStore.segnalazioni
        } else {
            return dataStore.segnalazioni.filter { $0.stato.rawValue == selectedFilter }
        }
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Top Filter Segment
                Picker("Stato", selection: $selectedFilter) {
                    ForEach(filterOptions, id: \.self) { opt in
                        Text(opt).tag(opt)
                    }
                }
                .pickerStyle(.segmented)
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                
                // List of Reports
                if filteredSegnalazioni.isEmpty {
                    VStack(spacing: 12) {
                        Spacer()
                        Image(systemName: "checkmark.shield.fill")
                            .font(.system(size: 48))
                            .foregroundColor(AppColors.primaryGreen)
                        Text("Nessuna segnalazione trovata")
                            .font(.headline)
                        Text("Tutte le postazioni sono in regola.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                } else {
                    List {
                        ForEach(filteredSegnalazioni) { seg in
                            Button(action: {
                                selectedSegnalazione = seg
                            }) {
                                HStack(alignment: .top, spacing: 12) {
                                    ZStack {
                                        Circle()
                                            .fill(Color.red.opacity(0.15))
                                            .frame(width: 44, height: 44)
                                        Image(systemName: seg.tipo.icon)
                                            .font(.system(size: 18))
                                            .foregroundColor(.red)
                                    }
                                    
                                    VStack(alignment: .leading, spacing: 4) {
                                        HStack {
                                            Text(seg.tipo.title)
                                                .font(.system(size: 16, weight: .semibold))
                                                .foregroundColor(.primary)
                                            Spacer()
                                            Text(seg.stato.label)
                                                .font(.caption2.bold())
                                                .padding(.horizontal, 6)
                                                .padding(.vertical, 2)
                                                .background(seg.stato.backgroundColor)
                                                .foregroundColor(seg.stato.textColor)
                                                .cornerRadius(4)
                                        }
                                        
                                        Text("📍 \(seg.indirizzo)")
                                            .font(.system(size: 13))
                                            .foregroundColor(.secondary)
                                            .lineLimit(1)
                                        
                                        if let note = seg.note, !note.isEmpty {
                                            Text(note)
                                                .font(.system(size: 12))
                                                .foregroundColor(.gray)
                                                .lineLimit(2)
                                        }
                                    }
                                }
                                .padding(.vertical, 4)
                            }
                        }
                        .onDelete { indexSet in
                            indexSet.forEach { idx in
                                let item = filteredSegnalazioni[idx]
                                dataStore.deleteSegnalazione(item)
                            }
                        }
                    }
                    .listStyle(.plain)
                }
                
                // Bottom PDF Export Bar
                if !dataStore.segnalazioni.isEmpty {
                    VStack {
                        Button(action: exportAndSharePdf) {
                            HStack {
                                Image(systemName: "doc.text.fill")
                                Text("Esporta Report PDF (\(dataStore.segnalazioni.count))")
                                    .fontWeight(.semibold)
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(AppColors.darkTeal)
                            .cornerRadius(12)
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 8)
                    }
                    .background(Color(UIColor.systemBackground))
                }
            }
            .navigationTitle("Segnalazioni Rifiuti")
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
                    Button(action: onNavigateToAddSegnalazione) {
                        Image(systemName: "plus")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.red)
                    }
                }
            }
            .sheet(item: $selectedSegnalazione) { seg in
                SegnalazioneDetailSheet(segnalazione: seg)
            }
            .sheet(isPresented: $showShareSheet) {
                if let data = pdfDataToShare {
                    ShareActivityView(activityItems: [data])
                }
            }
        }
    }
    
    private func exportAndSharePdf() {
        let pdf = PdfReportGenerator.generatePdf(segnalazioni: dataStore.segnalazioni, operatore: "Michele Villani")
        self.pdfDataToShare = pdf
        self.showShareSheet = true
    }
}

// iOS Native Share Sheet wrapper
struct ShareActivityView: UIViewControllerRepresentable {
    let activityItems: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
        return controller
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
