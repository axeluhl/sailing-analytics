//
//  GPSFix.swift
//  SAPTracker
//
//  Created by computing on 14/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class GPSFix: NSManagedObject {

    @NSManaged var bearingDeg: Double
    @NSManaged var latDeg: Double
    @NSManaged var lonDeg: Double
    @NSManaged var speedMperS: Double
    @NSManaged var timeMillis: Double

    func initWithDictionary(dictionary: Dictionary<NSObject, AnyObject>) {
        timeMillis = round(dictionary["timestamp"] as Double * 1000)
        latDeg = dictionary["latitude"] as Double
        lonDeg = dictionary["longitude"] as Double
        speedMperS = dictionary["speed"] as Double
        bearingDeg = dictionary["course"] as Double
    }
    
    func dictionary() -> [String: AnyObject] {
        var dictionary = [String: AnyObject]()
        dictionary["bearingDeg"] = bearingDeg
        dictionary["latDeg"] = latDeg
        dictionary["lonDeg"] = lonDeg
        dictionary["speedMperS"] = speedMperS
        dictionary["timeMillis"] = timeMillis
        return dictionary        
    }

}
