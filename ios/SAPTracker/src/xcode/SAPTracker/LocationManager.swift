//
//  LocationManager.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreLocation

class LocationManager: NSObject {
    
    struct NotificationType {
        static let LocationManagerStarted = "LocationManagerStarted"
        static let LocationManagerStopped = "LocationManagerStopped"
        static let LocationManagerUpdated = "LocationManagerUpdated"
        static let LocationManagerFailed = "LocationManagerFailed"
    }
    
    enum LocationManagerError: ErrorType {
        case LocationServicesDisabled
        case LocationServicesDenied
        var description: String {
            switch self {
            case LocationManagerError.LocationServicesDenied: return NSLocalizedString("Please enable location services for this app.", comment: "")
            case LocationManagerError.LocationServicesDisabled: return NSLocalizedString("Please enable location services.", comment: "")
            }
        }
    }
    
    private let locationManager: CLLocationManager!
    private (set) var isTracking: Bool = false
    
    class var sharedManager: LocationManager {
        struct Singleton {
            static let sharedManager = LocationManager()
        }
        return Singleton.sharedManager
    }
    
    override init() {
        locationManager = CLLocationManager()
        super.init()
        setupLocationManager()
    }
    
    // MARK: - Setups
    
    private func setupLocationManager() {
        locationManager.pausesLocationUpdatesAutomatically = false;
        if #available(iOS 9, *) {
            locationManager.allowsBackgroundLocationUpdates = true;
        }
        locationManager.delegate = self
    }
    
    // MARK: - Methods
    
    func startTracking() throws {
        guard CLLocationManager.locationServicesEnabled() else {
            throw LocationManagerError.LocationServicesDisabled
        }
        guard CLLocationManager.authorizationStatus() != CLAuthorizationStatus.Denied else {
            throw LocationManagerError.LocationServicesDenied
        }

        // Ask user to always track location
        locationManager.requestAlwaysAuthorization()
        
        // Start location updates
        locationManager.startUpdatingLocation()
        locationManager.startUpdatingHeading()
        isTracking = true;
        
        // Send notification
        let notification = NSNotification(name: NotificationType.LocationManagerStarted, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func stopTracking() {
        
        // Stop location updates
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
        isTracking = false;
        
        // Send notification
        let notification = NSNotification(name: NotificationType.LocationManagerStopped, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
}

// MARK: - CLLocationManagerDelegate

extension LocationManager: CLLocationManagerDelegate {
    
    struct UserInfo {
        static let Error = "error"
        static let Course = "course"
        static let HorizontalAccuracy = "horizontalAccuracy"
        static let IsValid = "isValid"
        static let Latitude = "latitude"
        static let Longitude = "longitude"
        static let Speed = "speed"
        static let Timestamp = "timestamp"
    }
    
    struct LocationValidation {
        static let requiredAccuracy: CLLocationAccuracy = 10
        static let maxElapsedTime: NSTimeInterval = 10
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let notification = NSNotification(name: NotificationType.LocationManagerUpdated, object: self, userInfo:dictionaryForLocation(locations.last!))
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
        let notification = NSNotification(name: NotificationType.LocationManagerFailed, object: self, userInfo: [UserInfo.Error: error])
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    // MARK: - Helper
    
    private func dictionaryForLocation(location: CLLocation) -> [String: AnyObject] {
        return [
            UserInfo.Timestamp: location.timestamp.timeIntervalSince1970,
            UserInfo.Latitude : location.coordinate.latitude,
            UserInfo.Longitude: location.coordinate.longitude,
            UserInfo.Speed: location.speed,
            UserInfo.Course: location.course,
            UserInfo.HorizontalAccuracy: location.horizontalAccuracy,
            UserInfo.IsValid: isLocationValid(location)
        ]
    }
    
    private func isLocationValid(location: CLLocation) -> Bool {
        guard location.horizontalAccuracy >= 0 && location.horizontalAccuracy <= LocationValidation.requiredAccuracy else { return false }
        guard location.timestamp.timeIntervalSinceDate(NSDate()) <= LocationValidation.maxElapsedTime else { return false }
        return true;
    }
    
}
