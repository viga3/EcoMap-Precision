//
//  AppColors.swift
//  EcoMapPrecision
//
//  Color tokens for EcoMap Precision matching Android Material Design 3 tokens.
//

import SwiftUI

struct AppColors {
    // Primary Brand Colors
    static let primaryGreen = Color(red: 0x00 / 255.0, green: 0x6B / 255.0, blue: 0x2C / 255.0)
    static let primaryContainer = Color(red: 0x00 / 255.0, green: 0x87 / 255.0, blue: 0x3A / 255.0)
    static let secondaryBlue = Color(red: 0x00 / 255.0, green: 0x58 / 255.0, blue: 0xBE / 255.0)
    static let darkTeal = Color(red: 0x0F / 255.0, green: 0x76 / 255.0, blue: 0x6E / 255.0)
    
    // Backgrounds & Surfaces
    static let background = Color(red: 0xF9 / 255.0, green: 0xF9 / 255.0, blue: 0xFF / 255.0)
    static let surface = Color.white
    static let surfaceContainer = Color(red: 0xE7 / 255.0, green: 0xEE / 255.0, blue: 0xFE / 255.0)
    static let surfaceVariant = Color(red: 0xDC / 255.0, green: 0xE2 / 255.0, blue: 0xF3 / 255.0)
    
    // Waste Categories Colors (Standardized)
    static let wasteGrigio = Color(red: 0x6B / 255.0, green: 0x72 / 255.0, blue: 0x80 / 255.0)      // Indifferenziato
    static let wasteBlu = Color(red: 0x00 / 255.0, green: 0x58 / 255.0, blue: 0xBE / 255.0)         // Carta
    static let wasteGiallo = Color(red: 0xEA / 255.0, green: 0xB3 / 255.0, blue: 0x08 / 255.0)      // Multimateriale
    static let wasteVerde = Color(red: 0x00 / 255.0, green: 0x6B / 255.0, blue: 0x2C / 255.0)       // Vetro
    static let wasteMarrone = Color(red: 0x92 / 255.0, green: 0x40 / 255.0, blue: 0x0E / 255.0)     // Organico
    static let wasteRaee = Color(red: 0x8B / 255.0, green: 0x5C / 255.0, blue: 0xF6 / 255.0)        // RAEE
    static let wasteAbiti = Color(red: 0x0D / 255.0, green: 0x94 / 255.0, blue: 0x88 / 255.0)       // Abiti Usati

    // Status Badges
    static let statusNuovoBg = Color(red: 0xFF / 255.0, green: 0xDA / 255.0, blue: 0xD6 / 255.0)
    static let statusNuovoText = Color(red: 0xBA / 255.0, green: 0x1A / 255.0, blue: 0x1A / 255.0)
    
    static let statusInCaricoBg = Color(red: 0xFF / 255.0, green: 0xDF / 255.0, blue: 0x9A / 255.0)
    static let statusInCaricoText = Color(red: 0x75 / 255.0, green: 0x58 / 255.0, blue: 0x00 / 255.0)
    
    static let statusRisoltoBg = Color(red: 0x7F / 255.0, green: 0xFC / 255.0, blue: 0x97 / 255.0)
    static let statusRisoltoText = Color(red: 0x00 / 255.0, green: 0x53 / 255.0, blue: 0x20 / 255.0)
}
