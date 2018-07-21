//
//  LocationData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 06.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreLocation

class LocationData: NSObject {

    struct LocationValidation {
        static let requiredAccuracy: CLLocationAccuracy = 10
        static let maxElapsedTime: TimeInterval = 10
    }
    
    var location: CLLocation
    
    init(location: CLLocation) {
        self.location = location
        super.init()
    }
    
    var isValid: Bool {
        get {
            guard location.horizontalAccuracy >= 0 && location.horizontalAccuracy <= LocationValidation.requiredAccuracy else { return false }
            guard location.timestamp.timeIntervalSince(Date()) <= LocationValidation.maxElapsedTime else { return false }
            return true;
        }
    }
    
}
