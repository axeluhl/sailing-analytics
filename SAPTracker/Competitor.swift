//
//  Competitor.swift
//  SAPTracker
//
//  Created by computing on 14/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(Competitor)
class Competitor: NSManagedObject {

    @NSManaged var competitorId: String
    @NSManaged var countryCode: String
    @NSManaged var displayName: String
    @NSManaged var name: String
    @NSManaged var nationality: String
    @NSManaged var sailId: String
    @NSManaged var leaderBoard: LeaderBoard?
    
    func initWithDictionary(dictionary: Dictionary<NSObject, AnyObject>) {
        competitorId = dictionary["id"] as String
        displayName = dictionary["displayName"] as String
        name = dictionary["name"] as String
        sailId = dictionary["sailID"] as String
        countryCode = dictionary["countryCode"] as String
        nationality = dictionary["nationality"] as String
    }

}
