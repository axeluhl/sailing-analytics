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
        static let Started = "LocationManager.Started"
        static let Stopped = "LocationManager.Stopped"
        static let Updated = "LocationManager.Updated"
        static let Failed = "LocationManager.Failed"
    }
    
    struct UserInfo {
        static let Error = "NSError"
        static let LocationData = "LocationData"
        static let Status = "Status"
    }
    
    enum Status: String {
        case Tracking
        case NotTracking
        var description: String {
            switch self {
            case .Tracking: return Translation.LocationManager.Status.Tracking.String
            case .NotTracking: return Translation.LocationManager.Status.NotTracking.String
            }
        }
    }
    
    enum LocationManagerError: Error {
        case locationServicesDenied
        case locationServicesDisabled
        var description: String {
            switch self {
            case .locationServicesDenied: return Translation.LocationManager.LocationServicesDeniedError.String
            case .locationServicesDisabled: return Translation.LocationManager.LocationServicesDisabledError.String
            }
        }
    }
    
    fileprivate let locationManager: CLLocationManager!
    fileprivate (set) var isTracking: Bool = false
    
    class var sharedManager: LocationManager {
        struct Singleton {
            static let sharedManager = LocationManager()
        }
        return Singleton.sharedManager
    }
    
    override init() {
        locationManager = CLLocationManager()
        super.init()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupLocationManager()
    }
    
    fileprivate func setupLocationManager() {
        locationManager.pausesLocationUpdatesAutomatically = false;
        if #available(iOS 9, *) {
            locationManager.allowsBackgroundLocationUpdates = true;
        }
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.delegate = self
    }
    
    // MARK: - Methods
    
    func startTracking() throws {
        guard CLLocationManager.locationServicesEnabled() else {
            throw LocationManagerError.locationServicesDisabled
        }
        guard CLLocationManager.authorizationStatus() != CLAuthorizationStatus.denied else {
            throw LocationManagerError.locationServicesDenied
        }

        // Ask user to always track location
        locationManager.requestAlwaysAuthorization()
        
        // Start location updates
        locationManager.startUpdatingLocation()
        locationManager.startUpdatingHeading()
        isTracking = true;
        
        // Send notification
        let notification = Notification(name: Notification.Name(rawValue: NotificationType.Started), object: self)
        NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
    }
    
    func stopTracking() {
        
        // Stop location updates
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
        isTracking = false;
        
        // Send notification
        let notification = Notification(name: Notification.Name(rawValue: NotificationType.Stopped), object: self)
        NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
    }
    
}

// MARK: - CLLocationManagerDelegate

extension LocationManager: CLLocationManagerDelegate {
        
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        let locationData = LocationData(location: location)
        let userInfo = [UserInfo.LocationData: locationData]
        let notification = Notification(name: Notification.Name(rawValue: NotificationType.Updated), object: self, userInfo: userInfo)
        NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        let userInfo = [UserInfo.Error: error]
        let notification = Notification(name: Notification.Name(rawValue: NotificationType.Failed), object: self, userInfo: userInfo)
        NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
    }
    
}
