//
//  WasteCounterRow.swift
//  EcoMapPrecision
//

import SwiftUI

struct WasteCounterRow: View {
    let tipo: TipoRifiuto
    @Binding var count: Int
    
    var body: some View {
        HStack(spacing: 12) {
            // Icon Badge with Waste color
            ZStack {
                Circle()
                    .fill(tipo.color)
                    .frame(width: 36, height: 36)
                Image(systemName: tipo.systemIcon)
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .bold))
            }
            
            // Labels
            VStack(alignment: .leading, spacing: 2) {
                Text(tipo.labelName)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.primary)
                Text(tipo.categorySubtitle)
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            // Counter Stepper Controls
            HStack(spacing: 8) {
                Button(action: {
                    if count > 0 { count -= 1 }
                }) {
                    Image(systemName: "minus")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(count > 0 ? AppColors.primaryGreen : .gray)
                        .frame(width: 32, height: 32)
                        .background(Color(UIColor.secondarySystemBackground))
                        .clipShape(Circle())
                }
                .disabled(count <= 0)
                
                Text("\(count)")
                    .font(.system(size: 17, weight: .bold, design: .rounded))
                    .frame(minWidth: 28, alignment: .center)
                
                Button(action: {
                    count += 1
                }) {
                    Image(systemName: "plus")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 32, height: 32)
                        .background(AppColors.primaryGreen)
                        .clipShape(Circle())
                }
            }
        }
        .padding(.vertical, 6)
    }
}
