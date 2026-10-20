//
//  Postazione.swift
//  EcoMapPrecision
//

import Foundation
import CoreLocation

struct Postazione: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var nome: String
    var indirizzo: String
    var latitudine: Double
    var longitudine: Double
    
    var numIndifferenziato: Int = 0
    var numCarta: Int = 0
    var numMultimateriale: Int = 0
    var numVetro: Int = 0
    var numOrganico: Int = 0
    var numRaee: Int = 0
    var numAbitiUsati: Int = 0
    
    var dataCreazione: Date = Date()
    var dataUltimoAggiornamento: Date = Date()
    
    var totaleBins: Int {
        numIndifferenziato + numCarta + numMultimateriale + numVetro + numOrganico + numRaee + numAbitiUsati
    }
    
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitudine, longitude: longitudine)
    }
}
