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
        static let locationServicesDisabledNotificationKey = "location_services_disabled"
        static let trackingStartedNotificationKey = "tracking_started"
        static let trackingStoppedNotificationKey = "tracking_stopped"
        static let newLocationNotificationKey = "new_location"
        static let locationManagerFailedNotificationKey = "location_manager_failed"
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
    
    func startTracking() {
        if (!CLLocationManager.locationServicesEnabled())
        {
            let notification = NSNotification(name: NotificationType.locationServicesDisabledNotificationKey, object: self)
            NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
            return
        }

        if(coreLocationManager.respondsToSelector("requestAlwaysAuthorization")) {
            coreLocationManager.requestAlwaysAuthorization()
        }
        coreLocationManager.startUpdatingLocation()
        coreLocationManager.startUpdatingHeading()
        coreLocationManager.delegate = self
        isTracking = true;
        let notification = NSNotification(name: NotificationType.trackingStartedNotificationKey, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

    func stopTracking() {
        coreLocationManager.stopUpdatingLocation()
        coreLocationManager.stopUpdatingHeading()
        isTracking = false;
        let notification = NSNotification(name: NotificationType.trackingStoppedNotificationKey, object: self)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func locationManager(manager: CLLocationManager!, didUpdateLocations locations: [AnyObject]!) {
        let location = locations.last as CLLocation
        println(location.coordinate)
        let notification = NSNotification(name: NotificationType.newLocationNotificationKey, object: self, userInfo:LocationManager.dictionaryForLocation(location))
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    func locationManager(manager: CLLocationManager!, didFailWithError error: NSError!) {
        let notification = NSNotification(name: NotificationType.locationManagerFailedNotificationKey, object: self, userInfo: ["error": error])
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