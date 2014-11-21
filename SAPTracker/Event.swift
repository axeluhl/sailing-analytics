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
class Event: NSManagedObject {

    @NSManaged var serverUrl: String
    @NSManaged var eventId: String
    @NSManaged var name: String
    @NSManaged var startDate: NSDate
    @NSManaged var endDate: NSDate
    @NSManaged var imageUrl: String?
    @NSManaged var leaderBoard: LeaderBoard?
    @NSManaged var userImage: NSData?
    @NSManaged var gpsFixes: NSSet?
    @NSManaged var lastSyncDate: NSDate?
    
    func initWithDictionary(dictionary: [String: AnyObject]) {
        eventId = dictionary["id"] as String
        name = dictionary["name"] as String
        startDate = NSDate(timeIntervalSince1970: (dictionary["startDate"] as Double)/1000)
        endDate = NSDate(timeIntervalSince1970: (dictionary["endDate"] as Double)/1000)
        if (dictionary["imageURLs"] != nil) {
            let imageUrls = dictionary["imageURLs"] as Array<String>
            if (imageUrls.count > 0) {
                imageUrl = imageUrls[0]
            }
        }
    }

}
