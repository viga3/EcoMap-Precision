//
//  Segnalazione.swift
//  EcoMapPrecision
//

import Foundation
import CoreLocation

struct Segnalazione: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var tipo: TipoSegnalazione
    var stato: StatoSegnalazione = .nuovo
    var indirizzo: String
    var latitudine: Double
    var longitudine: Double
    var fotoFileName: String? = nil
    var note: String? = nil
    var dataCreazione: Date = Date()
    
    // Campi specifici
    var bomboleRossa: Int = 0
    var bomboleBlu: Int = 0
    var bomboleGrigia: Int = 0
    var bomboleVerde: Int = 0
    var cassonettoBloccatoTipo: String? = nil
    
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitudine, longitude: longitudine)
    }
}
