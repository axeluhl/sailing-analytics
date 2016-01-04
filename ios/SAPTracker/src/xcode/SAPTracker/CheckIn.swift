//
//  CheckIn.swift
//  SAPTracker
//
//  Created by computing on 19/01/15.
//  Copyright (c) 2015 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(CheckIn)
public class CheckIn: NSManagedObject {
    
    @NSManaged public var serverUrl: String
    @NSManaged public var eventId: String
    @NSManaged public var leaderBoardName: String
    @NSManaged public var competitorId: String
    
    @NSManaged public var imageUrl: String?
    @NSManaged public var userImage: NSData?
    @NSManaged public var lastSyncDate: NSDate?

    @NSManaged public var event: Event?
    @NSManaged public var leaderBoard: LeaderBoard?
    @NSManaged public var competitor: Competitor?
    @NSManaged public var gpsFixes: NSSet?
    
}