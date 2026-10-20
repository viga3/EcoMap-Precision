//
//  DataStore.swift
//  EcoMapPrecision
//

import Foundation
import CoreLocation
import Combine

class DataStore: ObservableObject {
    @Published var postazioni: [Postazione] = []
    @Published var segnalazioni: [Segnalazione] = []
    
    private let postazioniFileName = "postazioni.json"
    private let segnalazioniFileName = "segnalazioni.json"
    
    init() {
        loadData()
        if postazioni.isEmpty {
            loadDemoData()
        }
    }
    
    // MARK: - Persistence Paths
    private func getDocumentsDirectory() -> URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }
    
    private var postazioniURL: URL {
        getDocumentsDirectory().appendingPathComponent(postazioniFileName)
    }
    
    private var segnalazioniURL: URL {
        getDocumentsDirectory().appendingPathComponent(segnalazioniFileName)
    }
    
    // MARK: - Load & Save
    func loadData() {
        // Load Postazioni
        if let data = try? Data(contentsOf: postazioniURL) {
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            if let decoded = try? decoder.decode([Postazione].self, from: data) {
                self.postazioni = decoded
            }
        }
        
        // Load Segnalazioni
        if let data = try? Data(contentsOf: segnalazioniURL) {
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            if let decoded = try? decoder.decode([Segnalazione].self, from: data) {
                self.segnalazioni = decoded
            }
        }
    }
    
    func savePostazioni() {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = .prettyPrinted
        if let encoded = try? encoder.encode(postazioni) {
            try? encoded.write(to: postazioniURL)
        }
    }
    
    func saveSegnalazioni() {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = .prettyPrinted
        if let encoded = try? encoder.encode(segnalazioni) {
            try? encoded.write(to: segnalazioniURL)
        }
    }
    
    // MARK: - Postazioni Operations
    func addPostazione(_ postazione: Postazione) {
        postazioni.insert(postazione, at: 0)
        savePostazioni()
    }
    
    func updatePostazione(_ postazione: Postazione) {
        if let index = postazioni.firstIndex(where: { $0.id == postazione.id }) {
            postazioni[index] = postazione
            savePostazioni()
        }
    }
    
    func deletePostazione(at offsets: IndexSet) {
        postazioni.remove(atOffsets: offsets)
        savePostazioni()
    }
    
    func deletePostazione(_ postazione: Postazione) {
        postazioni.removeAll { $0.id == postazione.id }
        savePostazioni()
    }
    
    func deleteAllPostazioni() {
        postazioni.removeAll()
        savePostazioni()
    }
    
    // MARK: - Segnalazioni Operations
    func addSegnalazione(_ segnalazione: Segnalazione) {
        segnalazioni.insert(segnalazione, at: 0)
        saveSegnalazioni()
    }
    
    func updateSegnalazioneStato(id: String, nuovoStato: StatoSegnalazione) {
        if let index = segnalazioni.firstIndex(where: { $0.id == id }) {
            segnalazioni[index].stato = nuovoStato
            saveSegnalazioni()
        }
    }
    
    func deleteSegnalazione(_ segnalazione: Segnalazione) {
        segnalazioni.removeAll { $0.id == segnalazione.id }
        saveSegnalazioni()
    }
    
    // MARK: - Distance & Spatial Queries
    func distanceMeters(from lat1: Double, lon1: Double, to lat2: Double, lon2: Double) -> Double {
        let loc1 = CLLocation(latitude: lat1, longitude: lon1)
        let loc2 = CLLocation(latitude: lat2, longitude: lon2)
        return loc1.distance(from: loc2)
    }
    
    func findNearbyPostazione(latitude: Double, longitude: Double, thresholdMeters: Double = 15.0) -> Postazione? {
        var closest: Postazione? = nil
        var minDistance = Double.greatestFiniteMagnitude
        
        for p in postazioni {
            let d = distanceMeters(from: latitude, lon1: longitude, to: p.latitudine, lon2: p.longitudine)
            if d <= thresholdMeters && d < minDistance {
                minDistance = d
                closest = p
            }
        }
        return closest
    }
    
    // MARK: - Backup & Export
    func exportBackupData() -> Data? {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = .prettyPrinted
        return try? encoder.encode(postazioni)
    }
    
    func importBackupData(_ data: Data) -> Int {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        guard let imported = try? decoder.decode([Postazione].self, from: data) else { return 0 }
        
        var added = 0
        for item in imported {
            let exists = postazioni.contains { p in
                distanceMeters(from: item.latitudine, lon1: item.longitudine, to: p.latitudine, lon2: p.longitudine) < 15.0
            }
            if !exists {
                postazioni.append(item)
                added += 1
            }
        }
        if added > 0 {
            savePostazioni()
        }
        return added
    }
    
    // MARK: - Initial Demo Data
    private func loadDemoData() {
        postazioni = [
            Postazione(
                nome: "Piazza Garibaldi",
                indirizzo: "Piazza Giuseppe Garibaldi 12",
                latitudine: 40.8518,
                longitudine: 14.2681,
                numIndifferenziato: 2,
                numCarta: 2,
                numMultimateriale: 2,
                numVetro: 1,
                numOrganico: 2,
                numRaee: 0,
                numAbitiUsati: 1
            ),
            Postazione(
                nome: "Via Toledo Centro",
                indirizzo: "Via Toledo 156",
                latitudine: 40.8415,
                longitudine: 14.2496,
                numIndifferenziato: 1,
                numCarta: 1,
                numMultimateriale: 2,
                numVetro: 1,
                numOrganico: 1,
                numRaee: 0,
                numAbitiUsati: 0
            )
        ]
        savePostazioni()
    }
}
