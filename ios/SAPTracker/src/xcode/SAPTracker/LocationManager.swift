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
    
    enum LocationManagerError: ErrorType {
        case LocationServicesDenied
        case LocationServicesDisabled
        var description: String {
            switch self {
            case .LocationServicesDenied: return Translation.LocationManager.LocationServicesDeniedError.String
            case .LocationServicesDisabled: return Translation.LocationManager.LocationServicesDisabledError.String
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
        setup()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocationManager()
    }
    
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
        let notification = NSNotification(name: NotificationType.Started, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func stopTracking() {
        
        // Stop location updates
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
        isTracking = false;
        
        // Send notification
        let notification = NSNotification(name: NotificationType.Stopped, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
}

// MARK: - CLLocationManagerDelegate

extension LocationManager: CLLocationManagerDelegate {
        
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        let locationData = LocationData(location: location)
        let userInfo = [UserInfo.LocationData: locationData]
        let notification = NSNotification(name: NotificationType.Updated, object: self, userInfo: userInfo)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
        let userInfo = [UserInfo.Error: error]
        let notification = NSNotification(name: NotificationType.Failed, object: self, userInfo: userInfo)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
}
