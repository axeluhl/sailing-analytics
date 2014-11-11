//
//  Event.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class Event: NSManagedObject {

    @NSManaged var eventId: String
    @NSManaged var name: String
    @NSManaged var startDate: NSDate
    @NSManaged var endDate: NSDate
    @NSManaged var imageUrl: String

}
