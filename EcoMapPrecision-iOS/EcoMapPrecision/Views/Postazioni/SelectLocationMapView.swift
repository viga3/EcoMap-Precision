//
//  SelectLocationMapView.swift
//  EcoMapPrecision
//

import SwiftUI
import MapKit

struct SelectLocationMapView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var locationManager: LocationManager
    
    var onConfirmLocation: (CLLocationCoordinate2D, String) -> Void
    
    @State private var position: MapCameraPosition
    @State private var selectedCoordinate: CLLocationCoordinate2D
    @State private var resolvedAddress: String = "Caricamento indirizzo..."
    
    init(initialCoordinate: CLLocationCoordinate2D, onConfirmLocation: @escaping (CLLocationCoordinate2D, String) -> Void) {
        self.onConfirmLocation = onConfirmLocation
        _selectedCoordinate = State(initialValue: initialCoordinate)
        _position = State(initialValue: .camera(MapCamera(centerCoordinate: initialCoordinate, distance: 1000)))
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                // Interactive Map
                MapReader { proxy in
                    Map(position: $position) {
                        Annotation("Nuova Posizione", coordinate: selectedCoordinate) {
                            ZStack {
                                Circle()
                                    .fill(AppColors.primaryGreen)
                                    .frame(width: 32, height: 32)
                                Image(systemName: "mappin")
                                    .foregroundColor(.white)
                                    .font(.system(size: 16, weight: .bold))
                            }
                        }
                    }
                    .onTapGesture { screenPoint in
                        if let coord = proxy.convert(screenPoint, from: .local) {
                            selectedCoordinate = coord
                            locationManager.reverseGeocode(coordinate: coord) { addr in
                                resolvedAddress = addr
                            }
                        }
                    }
                }
                .ignoresSafeArea(edges: .bottom)
                
                // Bottom Confirmation Card
                VStack {
                    Spacer()
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Tocca la mappa per spostare il marcatore:")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        HStack(spacing: 8) {
                            Image(systemName: "mappin.circle.fill")
                                .foregroundColor(AppColors.primaryGreen)
                                .font(.title3)
                            Text(resolvedAddress)
                                .font(.subheadline.bold())
                                .lineLimit(2)
                        }
                        
                        Button(action: {
                            onConfirmLocation(selectedCoordinate, resolvedAddress)
                            dismiss()
                        }) {
                            Text("Conferma Posizione")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(AppColors.primaryGreen)
                                .cornerRadius(12)
                        }
                    }
                    .padding()
                    .background(Color(UIColor.systemBackground))
                    .cornerRadius(16)
                    .shadow(radius: 6)
                    .padding()
                }
            }
            .navigationTitle("Scegli su Mappa")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annulla") { dismiss() }
                }
            }
            .onAppear {
                locationManager.reverseGeocode(coordinate: selectedCoordinate) { addr in
                    resolvedAddress = addr
                }
            }
        }
    }
}
