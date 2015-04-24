//
//  GPSFix.swift
//  SAPTracker
//
//  Created by computing on 14/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(GPSFix)
public class GPSFix: NSManagedObject {

    @NSManaged var course: Double
    @NSManaged var latitude: Double
    @NSManaged var longitude: Double
    @NSManaged var speed: Double
    @NSManaged var timestamp: Double
    @NSManaged var checkIn: CheckIn?

    func initWithDictionary(dictionary: Dictionary<NSObject, AnyObject>) {
        timestamp = round(dictionary["timestamp"] as! Double * 1000)
        latitude = dictionary["latitude"] as! Double
        longitude = dictionary["longitude"] as! Double
        speed = dictionary["speed"] as! Double
        course = dictionary["course"] as! Double
    }
    
    func dictionary() -> [String: AnyObject] {
        var dictionary = [String: AnyObject]()
        dictionary["course"] = course
        dictionary["latitude"] = latitude
        dictionary["longitude"] = longitude
        dictionary["speed"] = speed
        dictionary["timestamp"] = timestamp
        return dictionary        
    }

}
