//
//  GPSFix.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(GPSFix)
class GPSFix: NSManagedObject {

    func updateWithLocationData(locationData: LocationData) {
        course = locationData.location.course
        latitude = locationData.location.coordinate.latitude
        longitude = locationData.location.coordinate.longitude
        speed = locationData.location.speed
        timestamp = round(locationData.location.timestamp.timeIntervalSince1970 * 1000)
    }
    
}
