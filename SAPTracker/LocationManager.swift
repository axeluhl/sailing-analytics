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
        static let locationServicesDisabled = "location_services_disabled"
        static let trackingStarted = "tracking_started"
        static let trackingStopped = "tracking_stopped"
        static let newLocation = "new_location"
        static let locationManagerFailed = "location_manager_failed"
    }

    class var sharedManager: LocationManager {
        struct Singleton {
            static let sharedLocationManager = LocationManager()
        }
        return Singleton.sharedLocationManager
    }
    
    private var coreLocationManager: CLLocationManager = CLLocationManager()
    
    var isTracking: Bool = false
    
    override init() {
        super.init()
        coreLocationManager.delegate = self
    }
    
    func startTracking() -> String? {
        if (!CLLocationManager.locationServicesEnabled()) {
            return "Please enable location services."
        }
        if (CLLocationManager.authorizationStatus() == CLAuthorizationStatus.Denied) {
            return "Please enable location services for this app."
        }
        if(coreLocationManager.respondsToSelector("requestAlwaysAuthorization")) {
            coreLocationManager.requestAlwaysAuthorization()
        }
        coreLocationManager.startUpdatingLocation()
        coreLocationManager.startUpdatingHeading()
        coreLocationManager.delegate = self
        isTracking = true;
        let notification = NSNotification(name: NotificationType.trackingStarted, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
        return nil
    }

    func stopTracking() {
        coreLocationManager.stopUpdatingLocation()
        coreLocationManager.stopUpdatingHeading()
        isTracking = false;
        let notification = NSNotification(name: NotificationType.trackingStopped, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func locationManager(manager: CLLocationManager!, didUpdateLocations locations: [AnyObject]!) {
        let location = locations.last as CLLocation
        let notification = NSNotification(name: NotificationType.newLocation, object: self, userInfo:LocationManager.dictionaryForLocation(location))
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func locationManager(manager: CLLocationManager!, didFailWithError error: NSError!) {
        let notification = NSNotification(name: NotificationType.locationManagerFailed, object: self, userInfo: ["error": error])
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    class func dictionaryForLocation(location: CLLocation) -> Dictionary<String, AnyObject> {
        return [
            "timestamp": location.timestamp.timeIntervalSince1970,
            "latitude" : location.coordinate.latitude,
            "longitude": location.coordinate.longitude,
            "speed": location.speed,
            "course": location.course
        ]
    }
}