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
public class Competitor: NSManagedObject {

    @NSManaged public var competitorId: String
    @NSManaged public var countryCode: String
    @NSManaged public var displayName: String
    @NSManaged public var name: String
    @NSManaged public var nationality: String
    @NSManaged public var sailId: String
    @NSManaged public var leaderBoard: LeaderBoard?
    
    public func initWithDictionary(dictionary: Dictionary<NSObject, AnyObject>) {
        competitorId = dictionary["id"] as String
        displayName = dictionary["displayName"] as String
        name = dictionary["name"] as String
        sailId = dictionary["sailID"] as String
        countryCode = dictionary["countryCode"] as String
        nationality = dictionary["nationality"] as String
    }

}
