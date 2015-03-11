//
//  Event.swift
//  SAPTracker
//
//  Created by computing on 14/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(Event)
public class Event: NSManagedObject {

    @NSManaged public var eventId: String
    @NSManaged public var name: String
    @NSManaged public var startDate: NSDate
    @NSManaged public var endDate: NSDate
    @NSManaged public var checkIn: CheckIn?

    public func initWithDictionary(dictionary: [String: AnyObject]) {
        eventId = dictionary["id"] as String
        name = dictionary["name"] as String
        startDate = NSDate(timeIntervalSince1970: (dictionary["startDate"] as Double)/1000)
        endDate = NSDate(timeIntervalSince1970: (dictionary["endDate"] as Double)/1000)
        if (dictionary["imageURLs"] != nil) {
            let imageUrls = dictionary["imageURLs"] as Array<String>
            if (imageUrls.count > 0) {
                checkIn!.imageUrl = imageUrls[0]
            }
        }
    }

}
