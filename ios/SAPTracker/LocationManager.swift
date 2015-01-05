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
        static let newHeading = "newHeading"
        static let locationManagerFailed = "locationManagerFailed"
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
    
    func startTracking() -> String? {
        if (!CLLocationManager.locationServicesEnabled()) {
            return "Please enable location services."
        }
        if (CLLocationManager.authorizationStatus() == CLAuthorizationStatus.Denied) {
            return "Please enable location services for this app."
        }
        if (coreLocationManager.respondsToSelector("requestAlwaysAuthorization")) {
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
    
    func locationManager(manager: CLLocationManager!, didUpdateHeading newHeading: CLHeading!) {
        let notification = NSNotification(name: NotificationType.newHeading, object: self, userInfo:LocationManager.dictionaryForHeading(newHeading))
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

    func locationManager(manager: CLLocationManager!, didFailWithError error: NSError!) {
        let notification = NSNotification(name: NotificationType.locationManagerFailed, object: self, userInfo: ["error": error])
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    /* User preference for heading type. */
    private let HeadingDefaultsKey = "heading"
    
    enum Heading: Int {
        case Magnetic = 1, True
    }
    
    var headingPreference: Int {
        get {
            let preferences = NSUserDefaults.standardUserDefaults()
            var heading = preferences.integerForKey(HeadingDefaultsKey)
            if heading == 0 {
                heading = Heading.Magnetic.rawValue
                self.headingPreference = heading
            }
            return heading
        }
        set {
            let preferences = NSUserDefaults.standardUserDefaults()
            preferences.setInteger(newValue, forKey: HeadingDefaultsKey)
            preferences.synchronize()
        }
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
        ]
    }
    
    /* Create dictionary for heading. */
    class func dictionaryForHeading(heading: CLHeading) -> [String: AnyObject] {
        return [
            "timestamp": heading.timestamp,
            "magneticHeading": heading.magneticHeading,
            "trueHeading": heading.trueHeading,
            "description": heading.description,
            "headingAccuracy": heading.headingAccuracy,
        ]
    }
}