//
//  Event+CoreDataProperties.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension Event {

    @NSManaged var endDate: TimeInterval
    @NSManaged var eventID: String
    @NSManaged var name: String
    @NSManaged var startDate: TimeInterval
    @NSManaged var checkIn: CheckIn

}
