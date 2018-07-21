//
//  EventData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class EventData: BaseData {

    fileprivate struct Keys {
        static let EndDate = "endDate"
        static let ID = "id"
        static let ImageURLs = "imageURLs"
        static let Name = "name"
        static let StartDate = "startDate"
    }
    
    var endDate: Double { get { return doubleValue(forKey: Keys.EndDate) / 1000 } }
    var eventID: String { get { return stringValue(forKey: Keys.ID) } }
    var name: String { get { return stringValue(forKey: Keys.Name) } }
    var startDate: Double { get { return doubleValue(forKey: Keys.StartDate) / 1000 } }
    
}
