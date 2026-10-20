//
//  TipoRifiuto.swift
//  EcoMapPrecision
//

import SwiftUI

enum TipoRifiuto: String, CaseIterable, Identifiable, Codable {
    case indifferenziato = "grigio"
    case carta = "blu"
    case multimateriale = "giallo"
    case vetro = "verde"
    case organico = "marrone"
    case raee = "raee"
    case abitiUsati = "abiti"
    
    var id: String { rawValue }
    
    var labelName: String {
        switch self {
        case .indifferenziato: return "Indifferenziato"
        case .carta: return "Carta e Cartone"
        case .multimateriale: return "Multimateriale"
        case .vetro: return "Vetro"
        case .organico: return "Organico"
        case .raee: return "RAEE"
        case .abitiUsati: return "Abiti Usati"
        }
    }
    
    var categorySubtitle: String {
        switch self {
        case .indifferenziato: return "Rifiuto Secco"
        case .carta: return "Carta & Cartone"
        case .multimateriale: return "Plastica & Metalli"
        case .vetro: return "Vetro & Bottiglie"
        case .organico: return "Umido Organico"
        case .raee: return "Radio & Elettronica"
        case .abitiUsati: return "Vestiti & Tessili"
        }
    }
    
    var color: Color {
        switch self {
        case .indifferenziato: return AppColors.wasteGrigio
        case .carta: return AppColors.wasteBlu
        case .multimateriale: return AppColors.wasteGiallo
        case .vetro: return AppColors.wasteVerde
        case .organico: return AppColors.wasteMarrone
        case .raee: return AppColors.wasteRaee
        case .abitiUsati: return AppColors.wasteAbiti
        }
    }
    
    var systemIcon: String {
        switch self {
        case .indifferenziato: return "trash.fill"
        case .carta: return "newspaper.fill"
        case .multimateriale: return "shippingbox.fill"
        case .vetro: return "wineglass.fill"
        case .organico: return "leaf.fill"
        case .raee: return "bolt.fill"
        case .abitiUsati: return "tshirt.fill"
        }
    }
}
