//
//  CheckIn+CoreDataProperties.swift
//  SAPTracker
//
//  Created by Raimund Wege on 01.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension CheckIn {

    @NSManaged var competitorID: String?
    @NSManaged var eventID: String?
    @NSManaged var teamImageURL: String?
    @NSManaged var lastSyncDate: NSTimeInterval
    @NSManaged var leaderboardName: String?
    @NSManaged var serverURL: String?
    @NSManaged var teamImageData: NSData?
    @NSManaged var teamImageRetry: Bool
    @NSManaged var competitor: Competitor?
    @NSManaged var event: Event?
    @NSManaged var gpsFixes: NSSet?
    @NSManaged var leaderboard: Leaderboard?

}
