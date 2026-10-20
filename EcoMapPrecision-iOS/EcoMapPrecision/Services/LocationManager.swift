//
//  LocationManager.swift
//  EcoMapPrecision
//

import Foundation
import CoreLocation
import Combine

class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    
    @Published var currentLocation: CLLocation?
    @Published var currentAddress: String = "Rilevamento posizione..."
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published var isHighAccuracy: Bool = true
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        locationManager.distanceFilter = 2.0 // updates every 2 meters
    }
    
    func requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    func startUpdating() {
        locationManager.startUpdatingLocation()
    }
    
    func stopUpdating() {
        locationManager.stopUpdatingLocation()
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorizationStatus = manager.authorizationStatus
        if authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways {
            startUpdating()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        self.currentLocation = location
        
        // Reverse geocoding
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, error in
            guard let self = self, let placemark = placemarks?.first, error == nil else { return }
            
            var components: [String] = []
            if let street = placemark.thoroughfare {
                if let subThoroughfare = placemark.subThoroughfare {
                    components.append("\(street) \(subThoroughfare)")
                } else {
                    components.append(street)
                }
            }
            if let city = placemark.locality {
                components.append(city)
            }
            
            DispatchQueue.main.async {
                self.currentAddress = components.joined(separator: ", ")
                if self.currentAddress.isEmpty {
                    self.currentAddress = String(format: "%.5f, %.5f", location.coordinate.latitude, location.coordinate.longitude)
                }
            }
        }
    }
    
    func reverseGeocode(coordinate: CLLocationCoordinate2D, completion: @escaping (String) -> Void) {
        let loc = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
        geocoder.reverseGeocodeLocation(loc) { placemarks, error in
            guard let placemark = placemarks?.first, error == nil else {
                completion(String(format: "Lat: %.5f, Lon: %.5f", coordinate.latitude, coordinate.longitude))
                return
            }
            var comps: [String] = []
            if let street = placemark.thoroughfare {
                if let num = placemark.subThoroughfare {
                    comps.append("\(street) \(num)")
                } else {
                    comps.append(street)
                }
            }
            if let city = placemark.locality {
                comps.append(city)
            }
            let res = comps.joined(separator: ", ")
            completion(res.isEmpty ? String(format: "Lat: %.5f, Lon: %.5f", coordinate.latitude, coordinate.longitude) : res)
        }
    }
}
