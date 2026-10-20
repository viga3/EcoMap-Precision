//
//  TipoSegnalazione.swift
//  EcoMapPrecision
//

import Foundation

enum TipoSegnalazione: String, CaseIterable, Identifiable, Codable {
    case abbandoni = "Abbandoni"
    case ingombranti = "Ingombranti"
    case bombole = "Bombole"
    case sacchiATerra = "Sacchi a terra"
    case cassonettoBloccato = "Cassonetto bloccato o danneggiato"
    case rifiutiSpeciali = "Rifiuti Speciali"
    
    var id: String { rawValue }
    var title: String { rawValue }
    
    var icon: String {
        switch self {
        case .abbandoni: return "exclamationmark.triangle.fill"
        case .ingombranti: return "shippingbox.and.arrow.backward.fill"
        case .bombole: return "flame.fill"
        case .sacchiATerra: return "bag.fill"
        case .cassonettoBloccato: return "lock.trianglebadge.exclamationmark.fill"
        case .rifiutiSpeciali: return "allergens.fill"
        }
    }
}
