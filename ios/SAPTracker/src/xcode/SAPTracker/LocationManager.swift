//
//  LocationController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreLocation

class LocationManager: NSObject, CLLocationManagerDelegate {
    
    struct NotificationType {
        static let locationServicesDisabled = "locationServicesDisabled"
        static let trackingStarted = "trackingStarted"
        static let trackingStopped = "trackingStopped"
        static let newLocation = "newLocation"
        static let locationManagerFailed = "locationManagerFailed"
    }
    
    struct UserInfo {
        static let timestamp = "timestamp"
        static let latitude = "latitude"
        static let longitude = "longitude"
        static let speed = "speed"
        static let course = "course"
        static let horizontalAccuracy = "horizontalAccuracy"
        static let isValid = "isValid"
    }
    
    enum TrackingError: ErrorType {
        case LocationServicesDisabled
        case LocationServicesDenied
        var description: String {
            switch self {
            case TrackingError.LocationServicesDenied: return NSLocalizedString("Please enable location services for this app.", comment: "")
            case TrackingError.LocationServicesDisabled: return NSLocalizedString("Please enable location services.", comment: "")
            }
        }
    }
    
    class var sharedManager: LocationManager {
        struct Singleton {
            static let sharedManager = LocationManager()
        }
        return Singleton.sharedManager
    }
    
    private var coreLocationManager: CLLocationManager = CLLocationManager()
    
    var isTracking: Bool = false
    
    override init() {
        super.init()
        coreLocationManager.delegate = self
    }
    
    func startTracking() throws {
        guard CLLocationManager.locationServicesEnabled() else {
            throw TrackingError.LocationServicesDisabled
        }
        guard CLLocationManager.authorizationStatus() != CLAuthorizationStatus.Denied else {
            throw TrackingError.LocationServicesDenied
        }
        
        // Allow the app to use the GPS sensor while in background
        coreLocationManager.requestAlwaysAuthorization()
        coreLocationManager.pausesLocationUpdatesAutomatically = false;
        if #available(iOS 9, *) {
            coreLocationManager.allowsBackgroundLocationUpdates = true;
        }

        // Start location updates
        coreLocationManager.startUpdatingLocation()
        coreLocationManager.startUpdatingHeading()
        coreLocationManager.delegate = self
        isTracking = true;
        let notification = NSNotification(name: NotificationType.trackingStarted, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func stopTracking() {
        coreLocationManager.stopUpdatingLocation()
        coreLocationManager.stopUpdatingHeading()
        isTracking = false;
        let notification = NSNotification(name: NotificationType.trackingStopped, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let location = locations.last
        let notification = NSNotification(name: NotificationType.newLocation, object: self, userInfo:LocationManager.dictionaryForLocation(location!))
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

    func locationManager(manager: CLLocationManager, didFailWithError error: NSError) {
        let notification = NSNotification(name: NotificationType.locationManagerFailed, object: self, userInfo: ["error": error])
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

    // MARK: -
    /* Create dictionary for location. */
    class func dictionaryForLocation(location: CLLocation) -> [String: AnyObject] {
        return [
            "timestamp": location.timestamp.timeIntervalSince1970,
            "latitude" : location.coordinate.latitude,
            "longitude": location.coordinate.longitude,
            "speed": location.speed,
            "course": location.course,
            "horizontalAccuracy": location.horizontalAccuracy,
            "isValid": isLocationValid(location)
        ]
    }
    
    static let REQ_ACCURACY : CLLocationAccuracy = 10
    static let REQ_TIME : NSTimeInterval = 10
    class func isLocationValid(location: CLLocation) -> Bool {
        let accuracy = location.horizontalAccuracy
        let time = location.timestamp
        let elapsed = time.timeIntervalSinceDate(NSDate())
        if elapsed > REQ_TIME {
            return false
        }
        if accuracy < 0 || accuracy > REQ_ACCURACY {
            return false
        }
        return true;
    }
    
}
