//
//  MapView.swift
//  EcoMapPrecision
//

import SwiftUI
import MapKit

enum MapFilter: String, CaseIterable, Identifiable {
    case tutte = "Tutte"
    case postazioni = "Postazioni"
    case segnalazioni = "Segnalazioni"
    var id: String { rawValue }
}

struct MapView: View {
    @EnvironmentObject var dataStore: DataStore
    @EnvironmentObject var locationManager: LocationManager
    
    @Binding var isDrawerOpen: Bool
    var onNavigateToAddPostazione: (CLLocationCoordinate2D?) -> Void
    var onNavigateToAddSegnalazione: (CLLocationCoordinate2D?) -> Void
    
    @State private var position: MapCameraPosition = .userLocation(fallback: .camera(
        MapCamera(centerCoordinate: CLLocationCoordinate2D(latitude: 40.8518, longitude: 14.2681), distance: 2500)
    ))
    
    @State private var selectedFilter: MapFilter = .tutte
    @State private var selectedPostazione: Postazione? = nil
    @State private var selectedSegnalazione: Segnalazione? = nil
    @State private var mapStyleStandard: Bool = true
    
    var body: some View {
        ZStack(alignment: .top) {
            // Apple MapKit Map
            Map(position: $position) {
                UserAnnotation()
                
                // Postazioni Pins
                if selectedFilter == .tutte || selectedFilter == .postazioni {
                    ForEach(dataStore.postazioni) { item in
                        Annotation(item.nome, coordinate: item.coordinate) {
                            Button(action: {
                                selectedPostazione = item
                            }) {
                                ZStack {
                                    Circle()
                                        .fill(AppColors.primaryGreen)
                                        .frame(width: 36, height: 36)
                                        .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
                                    Image(systemName: "trash.fill")
                                        .font(.system(size: 16))
                                        .foregroundColor(.white)
                                }
                            }
                        }
                    }
                }
                
                // Segnalazioni Pins
                if selectedFilter == .tutte || selectedFilter == .segnalazioni {
                    ForEach(dataStore.segnalazioni) { seg in
                        Annotation(seg.tipo.title, coordinate: seg.coordinate) {
                            Button(action: {
                                selectedSegnalazione = seg
                            }) {
                                ZStack {
                                    Circle()
                                        .fill(Color.red)
                                        .frame(width: 36, height: 36)
                                        .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
                                    Image(systemName: seg.tipo.icon)
                                        .font(.system(size: 16))
                                        .foregroundColor(.white)
                                }
                            }
                        }
                    }
                }
            }
            .mapStyle(mapStyleStandard ? .standard(elevation: .realistic) : .hybrid(elevation: .realistic))
            .ignoresSafeArea()
            
            // Top Controls Bar (Menu, Filters, Map Type)
            VStack(spacing: 8) {
                HStack {
                    // Menu Button
                    Button(action: {
                        withAnimation { isDrawerOpen.toggle() }
                    }) {
                        Image(systemName: "line.3.horizontal")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(AppColors.primaryGreen)
                            .padding(12)
                            .background(Color.white)
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.15), radius: 4)
                    }
                    
                    Spacer()
                    
                    // Filter Picker
                    Picker("Filtra", selection: $selectedFilter) {
                        ForEach(MapFilter.allCases) { filter in
                            Text(filter.rawValue).tag(filter)
                        }
                    }
                    .pickerStyle(.segmented)
                    .frame(maxWidth: 220)
                    .background(Color.white.opacity(0.9))
                    .cornerRadius(8)
                    .shadow(color: .black.opacity(0.1), radius: 3)
                    
                    Spacer()
                    
                    // Toggle Map Style (Satellite / Normal)
                    Button(action: {
                        mapStyleStandard.toggle()
                    }) {
                        Image(systemName: mapStyleStandard ? "map.fill" : "globe.europe.africa.fill")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(AppColors.primaryGreen)
                            .padding(12)
                            .background(Color.white)
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.15), radius: 4)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 10)
            }
            
            // Floating Action Buttons (Right Bottom)
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    VStack(spacing: 12) {
                        // Recenter GPS
                        Button(action: {
                            if let loc = locationManager.currentLocation {
                                withAnimation {
                                    position = .camera(MapCamera(centerCoordinate: loc.coordinate, distance: 1500))
                                }
                            } else {
                                locationManager.requestPermission()
                            }
                        }) {
                            Image(systemName: "location.fill")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 48, height: 48)
                                .background(AppColors.secondaryBlue)
                                .clipShape(Circle())
                                .shadow(color: .black.opacity(0.25), radius: 4)
                        }
                        
                        // Add Segnalazione Button
                        Button(action: {
                            onNavigateToAddSegnalazione(locationManager.currentLocation?.coordinate)
                        }) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 52, height: 52)
                                .background(Color.red)
                                .clipShape(Circle())
                                .shadow(color: .black.opacity(0.25), radius: 5)
                        }
                        
                        // Add Postazione Button
                        Button(action: {
                            onNavigateToAddPostazione(locationManager.currentLocation?.coordinate)
                        }) {
                            Image(systemName: "plus")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 58, height: 58)
                                .background(AppColors.primaryGreen)
                                .clipShape(Circle())
                                .shadow(color: .black.opacity(0.3), radius: 6)
                        }
                    }
                    .padding(.trailing, 16)
                    .padding(.bottom, 24)
                }
            }
        }
        .onAppear {
            locationManager.requestPermission()
        }
        // Sheet for Postazione Detail
        .sheet(item: $selectedPostazione) { postazione in
            PostazioneDetailSheet(postazione: postazione)
        }
        // Sheet for Segnalazione Detail
        .sheet(item: $selectedSegnalazione) { seg in
            SegnalazioneDetailSheet(segnalazione: seg)
        }
    }
}

// MARK: - Postazione Detail Sheet
struct PostazioneDetailSheet: View {
    let postazione: Postazione
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(postazione.nome)
                            .font(.title2.bold())
                        Text("📍 \(postazione.indirizzo)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    
                    Divider()
                    
                    Text("Cassonetti Presenti (Totale: \(postazione.totaleBins))")
                        .font(.headline)
                    
                    VStack(spacing: 8) {
                        DetailBadgeRow(tipo: .indifferenziato, count: postazione.numIndifferenziato)
                        DetailBadgeRow(tipo: .carta, count: postazione.numCarta)
                        DetailBadgeRow(tipo: .multimateriale, count: postazione.numMultimateriale)
                        DetailBadgeRow(tipo: .vetro, count: postazione.numVetro)
                        DetailBadgeRow(tipo: .organico, count: postazione.numOrganico)
                        if postazione.numRaee > 0 {
                            DetailBadgeRow(tipo: .raee, count: postazione.numRaee)
                        }
                        if postazione.numAbitiUsati > 0 {
                            DetailBadgeRow(tipo: .abitiUsati, count: postazione.numAbitiUsati)
                        }
                    }
                    
                    Spacer()
                }
                .padding()
            }
            .navigationTitle("Dettagli Postazione")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Chiudi") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

private struct DetailBadgeRow: View {
    let tipo: TipoRifiuto
    let count: Int
    
    var body: some View {
        HStack {
            Circle()
                .fill(tipo.color)
                .frame(width: 12, height: 12)
            Text(tipo.labelName)
                .font(.system(size: 15))
            Spacer()
            Text("\(count)")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(count > 0 ? .primary : .secondary)
        }
        .padding(.vertical, 4)
    }
}

// MARK: - Segnalazione Detail Sheet
struct SegnalazioneDetailSheet: View {
    let segnalazione: Segnalazione
    @EnvironmentObject var dataStore: DataStore
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Image(systemName: segnalazione.tipo.icon)
                        .font(.title)
                        .foregroundColor(.red)
                    VStack(alignment: .leading) {
                        Text(segnalazione.tipo.title)
                            .font(.title3.bold())
                        Text(segnalazione.stato.label)
                            .font(.caption.bold())
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(segnalazione.stato.backgroundColor)
                            .foregroundColor(segnalazione.stato.textColor)
                            .cornerRadius(6)
                    }
                    Spacer()
                }
                
                Text("📍 \(segnalazione.indirizzo)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                if let note = segnalazione.note, !note.isEmpty {
                    Text("Note:")
                        .font(.headline)
                    Text(note)
                        .font(.body)
                        .padding(10)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(8)
                }
                
                Divider()
                
                Text("Modifica Stato:")
                    .font(.headline)
                
                HStack(spacing: 8) {
                    ForEach(StatoSegnalazione.allCases) { st in
                        Button(action: {
                            dataStore.updateSegnalazioneStato(id: segnalazione.id, nuovoStato: st)
                            dismiss()
                        }) {
                            Text(st.label)
                                .font(.caption.bold())
                                .padding(8)
                                .frame(maxWidth: .infinity)
                                .background(st.backgroundColor)
                                .foregroundColor(st.textColor)
                                .cornerRadius(8)
                        }
                    }
                }
                
                Spacer()
            }
            .padding()
            .navigationTitle("Dettaglio Segnalazione")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Chiudi") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
