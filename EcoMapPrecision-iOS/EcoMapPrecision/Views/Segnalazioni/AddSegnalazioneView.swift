//
//  AddSegnalazioneView.swift
//  EcoMapPrecision
//

import SwiftUI
import CoreLocation

struct AddSegnalazioneView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var dataStore: DataStore
    @EnvironmentObject var locationManager: LocationManager
    
    var initialCoordinate: CLLocationCoordinate2D?
    
    @State private var tipo: TipoSegnalazione = .abbandoni
    @State private var indirizzo: String = ""
    @State private var latitudine: Double = 0.0
    @State private var longitudine: Double = 0.0
    @State private var note: String = ""
    
    // Bombole counters
    @State private var bomboleRossa: Int = 0
    @State private var bomboleBlu: Int = 0
    @State private var bomboleGrigia: Int = 0
    @State private var bomboleVerde: Int = 0
    
    // Cassonetto bloccato
    @State private var cassonettoBloccatoTipo: String = "Indifferenziato"
    
    var body: some View {
        NavigationStack {
            Form {
                // Section 1: Type of Issue
                Section(header: Text("Tipo di Segnalazione")) {
                    Picker("Categoria", selection: $tipo) {
                        ForEach(TipoSegnalazione.allCases) { item in
                            Label(item.title, systemImage: item.icon).tag(item)
                        }
                    }
                }
                
                // Section 2: Specific Fields
                if tipo == .bombole {
                    Section(header: Text("Conteggio Bombole per Colore")) {
                        Stepper("Bombole Rosse: \(bomboleRossa)", value: $bomboleRossa, in: 0...50)
                        Stepper("Bombole Blu: \(bomboleBlu)", value: $bomboleBlu, in: 0...50)
                        Stepper("Bombole Grigie: \(bomboleGrigia)", value: $bomboleGrigia, in: 0...50)
                        Stepper("Bombole Verdi: \(bomboleVerde)", value: $bomboleVerde, in: 0...50)
                    }
                } else if tipo == .cassonettoBloccato {
                    Section(header: Text("Dettaglio Cassonetto")) {
                        Picker("Cassonetto interessato", selection: $cassonettoBloccatoTipo) {
                            Text("Indifferenziato").tag("Indifferenziato")
                            Text("Carta e Cartone").tag("Carta e Cartone")
                            Text("Multimateriale").tag("Multimateriale")
                            Text("Vetro").tag("Vetro")
                            Text("Organico").tag("Organico")
                        }
                    }
                }
                
                // Section 3: Location
                Section(header: Text("Posizione")) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Indirizzo Rilevato:")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        TextField("Indirizzo", text: $indirizzo)
                    }
                    
                    Text(String(format: "Coordinate: %.5f, %.5f", latitudine, longitudine))
                        .font(.system(size: 12, design: .monospaced))
                        .foregroundColor(.secondary)
                    
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
                
                // Section 4: Notes
                Section(header: Text("Note / Descrizione")) {
                    TextEditor(text: $note)
                        .frame(minHeight: 80)
                }
                
                // Section 5: Submit Action
                Section {
                    Button(action: saveSegnalazione) {
                        Text("Invia Segnalazione")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                    }
                    .listRowBackground(Color.red)
                    .disabled(latitudine == 0.0)
                }
            }
            .navigationTitle("Nuova Segnalazione")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annulla") { dismiss() }
                }
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
    
    private func saveSegnalazione() {
        let newSeg = Segnalazione(
            tipo: tipo,
            stato: .nuovo,
            indirizzo: indirizzo.isEmpty ? "Posizione segnalata" : indirizzo,
            latitudine: latitudine,
            longitudine: longitudine,
            fotoFileName: nil,
            note: note.isEmpty ? nil : note,
            bomboleRossa: bomboleRossa,
            bomboleBlu: bomboleBlu,
            bomboleGrigia: bomboleGrigia,
            bomboleVerde: bomboleVerde,
            cassonettoBloccatoTipo: tipo == .cassonettoBloccato ? cassonettoBloccatoTipo : nil
        )
        dataStore.addSegnalazione(newSeg)
        dismiss()
    }
}
