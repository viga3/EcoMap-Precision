//
//  PdfReportGenerator.swift
//  EcoMapPrecision
//

import UIKit
import PDFKit

struct PdfReportGenerator {
    
    static func generatePdf(segnalazioni: [Segnalazione], operatore: String = "Michele Villani") -> Data {
        let pdfMetaData = [
            kCGPDFContextCreator: "EcoMap Precision iOS",
            kCGPDFContextAuthor: operatore,
            kCGPDFContextTitle: "Report Segnalazioni Rifiuti"
        ]
        
        let format = UIGraphicsPDFRendererFormat()
        format.documentInfo = pdfMetaData as [String: Any]
        
        // A4 format: 595.2 x 841.8 points
        let pageWidth: CGFloat = 595.2
        let pageHeight: CGFloat = 841.8
        let pageRect = CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight)
        
        let renderer = UIGraphicsPDFRenderer(bounds: pageRect, format: format)
        
        let data = renderer.pdfData { context in
            context.beginPage()
            guard let cgContext = UIGraphicsGetCurrentContext() else { return }
            
            // 1. Header Banner (Dark Teal)
            let headerRect = CGRect(x: 0, y: 0, width: pageWidth, height: 90)
            cgContext.setFillColor(UIColor(red: 15/255, green: 118/255, blue: 110/255, alpha: 1.0).cgColor)
            cgContext.fill(headerRect)
            
            // 2. Header Title
            let titleFont = UIFont.boldSystemFont(ofSize: 22)
            let titleAttributes: [NSAttributedString.Key: Any] = [
                .font: titleFont,
                .foregroundColor: UIColor.white
            ]
            let titleString = "EcoMap - Report Segnalazioni"
            titleString.draw(at: CGPoint(x: 30, y: 24), withAttributes: titleAttributes)
            
            // 3. Header Subtitle (Date & Operator)
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "dd/MM/yyyy HH:mm"
            let nowStr = dateFormatter.string(from: Date())
            
            let subFont = UIFont.systemFont(ofSize: 11)
            let subAttributes: [NSAttributedString.Key: Any] = [
                .font: subFont,
                .foregroundColor: UIColor(white: 1.0, alpha: 0.9)
            ]
            let subString = "Generato il: \(nowStr)   |   Operatore: \(operatore)"
            subString.draw(at: CGPoint(x: 30, y: 56), withAttributes: subAttributes)
            
            // 4. Section Summary Title
            var currentY: CGFloat = 115
            let sectionFont = UIFont.boldSystemFont(ofSize: 14)
            let sectionAttributes: [NSAttributedString.Key: Any] = [
                .font: sectionFont,
                .foregroundColor: UIColor(red: 31/255, green: 41/255, blue: 55/255, alpha: 1.0)
            ]
            let summaryString = "Riepilogo Segnalazioni Rifiuti (\(segnalazioni.count) Totali)"
            summaryString.draw(at: CGPoint(x: 30, y: currentY), withAttributes: sectionAttributes)
            currentY += 30
            
            // 5. Draw Items
            let bodyFont = UIFont.systemFont(ofSize: 10)
            let boldBodyFont = UIFont.boldSystemFont(ofSize: 10)
            
            for (index, seg) in segnalazioni.prefix(12).enumerated() {
                // Card background
                let cardRect = CGRect(x: 30, y: currentY, width: pageWidth - 60, height: 48)
                cgContext.setFillColor(UIColor(red: 248/255, green: 250/255, blue: 252/255, alpha: 1.0).cgColor)
                cgContext.fill(cardRect)
                
                // Border
                cgContext.setStrokeColor(UIColor(red: 226/255, green: 232/255, blue: 240/255, alpha: 1.0).cgColor)
                cgContext.setLineWidth(1)
                cgContext.stroke(cardRect)
                
                // Index & Type
                let numTypeStr = "\(index + 1). \(seg.tipo.title)"
                numTypeStr.draw(at: CGPoint(x: 40, y: currentY + 8), withAttributes: [
                    .font: boldBodyFont,
                    .foregroundColor: UIColor(red: 17/255, green: 24/255, blue: 39/255, alpha: 1.0)
                ])
                
                // Status Badge
                let statoStr = "[\(seg.stato.label)]"
                let statoColor: UIColor
                switch seg.stato {
                case .nuovo: statoColor = UIColor(red: 186/255, green: 26/255, blue: 26/255, alpha: 1.0)
                case .inCarico: statoColor = UIColor(red: 117/255, green: 88/255, blue: 0/255, alpha: 1.0)
                case .risolto: statoColor = UIColor(red: 0/255, green: 83/255, blue: 32/255, alpha: 1.0)
                }
                statoStr.draw(at: CGPoint(x: 230, y: currentY + 8), withAttributes: [
                    .font: boldBodyFont,
                    .foregroundColor: statoColor
                ])
                
                // Date
                let itemDateStr = dateFormatter.string(from: seg.dataCreazione)
                itemDateStr.draw(at: CGPoint(x: pageWidth - 160, y: currentY + 8), withAttributes: [
                    .font: bodyFont,
                    .foregroundColor: UIColor.gray
                ])
                
                // Address & Details
                var detail = "📍 \(seg.indirizzo)"
                if let note = seg.note, !note.isEmpty {
                    detail += " • Note: \(note)"
                }
                detail.draw(at: CGPoint(x: 40, y: currentY + 26), withAttributes: [
                    .font: bodyFont,
                    .foregroundColor: UIColor(red: 75/255, green: 85/255, blue: 99/255, alpha: 1.0)
                ])
                
                currentY += 56
            }
            
            // 6. Footer
            let footerString = "Documento generato dall'app EcoMap Precision • Sistema di Monitoraggio Ambientale"
            footerString.draw(at: CGPoint(x: 30, y: pageHeight - 35), withAttributes: [
                .font: UIFont.systemFont(ofSize: 9),
                .foregroundColor: UIColor.lightGray
            ])
        }
        
        return data
    }
}
