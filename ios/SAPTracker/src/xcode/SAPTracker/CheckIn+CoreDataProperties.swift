//
//  CheckIn+CoreDataProperties.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension CheckIn {

    @NSManaged var serverURL: String
    @NSManaged var name: String
    @NSManaged var isTraining: NSNumber
    @NSManaged var event: Event
    @NSManaged var gpsFixes: NSSet?
    @NSManaged var leaderboard: Leaderboard

}
