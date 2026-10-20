//
//  StatoSegnalazione.swift
//  EcoMapPrecision
//

import SwiftUI

enum StatoSegnalazione: String, CaseIterable, Identifiable, Codable {
    case nuovo = "NUOVO"
    case inCarico = "IN CARICO"
    case risolto = "RISOLTO"
    
    var id: String { rawValue }
    var label: String { rawValue }
    
    var backgroundColor: Color {
        switch self {
        case .nuovo: return AppColors.statusNuovoBg
        case .inCarico: return AppColors.statusInCaricoBg
        case .risolto: return AppColors.statusRisoltoBg
        }
    }
    
    var textColor: Color {
        switch self {
        case .nuovo: return AppColors.statusNuovoText
        case .inCarico: return AppColors.statusInCaricoText
        case .risolto: return AppColors.statusRisoltoText
        }
    }
}
