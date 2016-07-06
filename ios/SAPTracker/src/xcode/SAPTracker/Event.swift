//
//  Event.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(Event)
class Event: NSManagedObject {

    func updateWithEventData(eventData: EventData) {
        endDate = eventData.endDate
        eventID = eventData.eventID
        name = eventData.name
        startDate = eventData.startDate
    }
    
}
