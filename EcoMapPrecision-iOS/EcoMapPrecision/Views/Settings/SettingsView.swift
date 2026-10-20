//
//  SettingsView.swift
//  EcoMapPrecision
//

import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var dataStore: DataStore
    @EnvironmentObject var locationManager: LocationManager
    
    @State private var showDeleteAllAlert = false
    @State private var showShareBackup = false
    @State private var backupDataToShare: Data? = nil
    @State private var backupStatusMessage: String? = nil
    @State private var showFileImporter = false
    
    var body: some View {
        NavigationStack {
            Form {
                // Section 1: GPS & Accuracy
                Section(header: Text("Geolocalizzazione")) {
                    Toggle("Precisione Elevata (Navigazione)", isOn: $locationManager.isHighAccuracy)
                        .tint(AppColors.primaryGreen)
                    
                    HStack {
                        Text("Stato Autorizzazione:")
                            .font(.subheadline)
                        Spacer()
                        Text("Attiva")
                            .font(.subheadline.bold())
                            .foregroundColor(AppColors.primaryGreen)
                    }
                }
                
                // Section 2: Backup & Restore
                Section(header: Text("Backup Dati")) {
                    Button(action: exportBackup) {
                        Label("Esporta Backup Postazioni (JSON)", systemImage: "square.and.arrow.up")
                            .foregroundColor(AppColors.primaryGreen)
                    }
                    
                    Button(action: {
                        showFileImporter = true
                    }) {
                        Label("Importa Backup da File", systemImage: "square.and.arrow.down")
                            .foregroundColor(AppColors.secondaryBlue)
                    }
                    
                    if let msg = backupStatusMessage {
                        Text(msg)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                
                // Section 3: Danger Zone
                Section(header: Text("Zona di Pericolo")) {
                    Button(role: .destructive, action: {
                        showDeleteAllAlert = true
                    }) {
                        Label("Cancella Tutte le Postazioni", systemImage: "trash.fill")
                    }
                }
            }
            .navigationTitle("Impostazioni")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Fine") { dismiss() }
                }
            }
            .alert("Cancellare tutte le postazioni?", isPresented: $showDeleteAllAlert) {
                Button("Elimina Tutto", role: .destructive) {
                    dataStore.deleteAllPostazioni()
                    backupStatusMessage = "Tutte le postazioni sono state eliminate."
                }
                Button("Annulla", role: .cancel) {}
            } message: {
                Text("Questa azione è irreversibile. Tutte le postazioni salvate verranno rimosse dal dispositivo.")
            }
            .sheet(isPresented: $showShareBackup) {
                if let data = backupDataToShare {
                    ShareActivityView(activityItems: [data])
                }
            }
            .fileImporter(isPresented: $showFileImporter, allowedContentTypes: [.json]) { result in
                switch result {
                case .success(let url):
                    if url.startAccessingSecurityScopedResource() {
                        defer { url.stopAccessingSecurityScopedResource() }
                        if let data = try? Data(contentsOf: url) {
                            let count = dataStore.importBackupData(data)
                            backupStatusMessage = "Importate con successo \(count) nuove postazioni."
                        }
                    }
                case .failure(let error):
                    backupStatusMessage = "Errore durante l'importazione: \(error.localizedDescription)"
                }
            }
        }
    }
    
    private func exportBackup() {
        if let data = dataStore.exportBackupData() {
            self.backupDataToShare = data
            self.showShareBackup = true
            self.backupStatusMessage = "Backup esportato correttamente."
        }
    }
}
