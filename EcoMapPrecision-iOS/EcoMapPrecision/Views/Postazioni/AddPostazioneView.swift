//
//  AddPostazioneView.swift
//  EcoMapPrecision
//

import SwiftUI
import CoreLocation

struct AddPostazioneView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var dataStore: DataStore
    @EnvironmentObject var locationManager: LocationManager
    
    var initialCoordinate: CLLocationCoordinate2D?
    
    @State private var nome: String = ""
    @State private var indirizzo: String = ""
    @State private var latitudine: Double = 0.0
    @State private var longitudine: Double = 0.0
    
    // Waste counters
    @State private var numIndifferenziato: Int = 0
    @State private var numCarta: Int = 0
    @State private var numMultimateriale: Int = 0
    @State private var numVetro: Int = 0
    @State private var numOrganico: Int = 0
    @State private var numRaee: Int = 0
    @State private var numAbitiUsati: Int = 0
    
    @State private var showMapPicker: Bool = false
    @State private var showDuplicateAlert: Bool = false
    @State private var duplicateItemName: String = ""
    
    var body: some View {
        NavigationStack {
            Form {
                // Section 1: Location & Info
                Section(header: Text("Dati Postazione")) {
                    TextField("Nome Postazione (es. Piazza Dante)", text: $nome)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Indirizzo Rilevato:")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        TextField("Indirizzo", text: $indirizzo)
                    }
                    
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Coordinate GPS:")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(String(format: "Lat: %.5f, Lon: %.5f", latitudine, longitudine))
                                .font(.system(size: 13, design: .monospaced))
                        }
                        Spacer()
                    }
                    
                    Button(action: {
                        showMapPicker = true
                    }) {
                        Label("Scegli Posizione su Mappa", systemImage: "map.fill")
                            .foregroundColor(AppColors.primaryGreen)
                    }
                    
                    Button(action: {
                        if let loc = locationManager.currentLocation {
                            latitudine = loc.coordinate.latitude
                            longitudine = loc.coordinate.longitude
                            indirizzo = locationManager.currentAddress
                        }
                    }) {
                        Label("Aggiorna con GPS Attuale", systemImage: "location.fill")
                            .foregroundColor(AppColors.secondaryBlue)
                    }
                }
                
                // Section 2: Waste Bins Counters
                Section(header: Text("Cassonetti Presenti")) {
                    WasteCounterRow(tipo: .indifferenziato, count: $numIndifferenziato)
                    WasteCounterRow(tipo: .carta, count: $numCarta)
                    WasteCounterRow(tipo: .multimateriale, count: $numMultimateriale)
                    WasteCounterRow(tipo: .vetro, count: $numVetro)
                    WasteCounterRow(tipo: .organico, count: $numOrganico)
                    WasteCounterRow(tipo: .raee, count: $numRaee)
                    WasteCounterRow(tipo: .abitiUsati, count: $numAbitiUsati)
                }
                
                // Section 3: Summary & Action
                Section {
                    let total = numIndifferenziato + numCarta + numMultimateriale + numVetro + numOrganico + numRaee + numAbitiUsati
                    HStack {
                        Text("Totale Cassonetti:")
                            .font(.headline)
                        Spacer()
                        Text("\(total)")
                            .font(.title3.bold())
                            .foregroundColor(AppColors.primaryGreen)
                    }
                    
                    Button(action: savePostazione) {
                        Text("Salva Postazione")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                    }
                    .listRowBackground(AppColors.primaryGreen)
                    .disabled(nome.trimmingCharacters(in: .whitespaces).isEmpty || latitudine == 0.0)
                }
            }
            .navigationTitle("Nuova Postazione")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annulla") { dismiss() }
                }
            }
            .sheet(isPresented: $showMapPicker) {
                SelectLocationMapView(
                    initialCoordinate: CLLocationCoordinate2D(latitude: latitudine != 0 ? latitudine : 40.8518, longitude: longitudine != 0 ? longitudine : 14.2681)
                ) { newCoord, newAddr in
                    latitudine = newCoord.latitude
                    longitudine = newCoord.longitude
                    indirizzo = newAddr
                }
            }
            .alert("Postazione Vicina Esistente", isPresented: $showDuplicateAlert) {
                Button("Salva Comunque") {
                    performSave()
                }
                Button("Annulla", role: .cancel) {}
            } message: {
                Text("Esiste già una postazione denominata '\(duplicateItemName)' entro 15 metri da questa posizione. Vuoi salvarla comunque?")
            }
            .onAppear {
                if let initial = initialCoordinate {
                    latitudine = initial.latitude
                    longitudine = initial.longitude
                    locationManager.reverseGeocode(coordinate: initial) { addr in
                        indirizzo = addr
                    }
                } else if let loc = locationManager.currentLocation {
                    latitudine = loc.coordinate.latitude
                    longitudine = loc.coordinate.longitude
                    indirizzo = locationManager.currentAddress
                }
            }
        }
    }
    
    private func savePostazione() {
        if let nearby = dataStore.findNearbyPostazione(latitude: latitudine, longitude: longitudine, thresholdMeters: 15.0) {
            duplicateItemName = nearby.nome
            showDuplicateAlert = true
        } else {
            performSave()
        }
    }
    
    private func performSave() {
        let newPostazione = Postazione(
            nome: nome.trimmingCharacters(in: .whitespaces),
            indirizzo: indirizzo.isEmpty ? "Indirizzo non specificato" : indirizzo,
            latitudine: latitudine,
            longitudine: longitudine,
            numIndifferenziato: numIndifferenziato,
            numCarta: numCarta,
            numMultimateriale: numMultimateriale,
            numVetro: numVetro,
            numOrganico: numOrganico,
            numRaee: numRaee,
            numAbitiUsati: numAbitiUsati
        )
        dataStore.addPostazione(newPostazione)
        dismiss()
    }
}
