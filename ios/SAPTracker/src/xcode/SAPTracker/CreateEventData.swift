//
//  CreateEventData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CreateEventData: BaseData {
    
    fileprivate struct Keys {
        static let EndDate = "eventenddate"
        static let ID = "eventid"
        static let LeaderboardName = "leaderboard"
        static let LeaderboardGroupID = "leaderboardgroupid"
        static let Name = "eventname"
        static let Regatta = "regatta"
        static let StartDate = "eventstartdate"
    }
    
    var endDate: Double { get { return doubleValue(forKey: Keys.EndDate) / 1000 } }
    var eventID: String { get { return stringValue(forKey: Keys.ID) } }
    var name: String { get { return stringValue(forKey: Keys.Name) } }
    var leaderboardName: String { get { return stringValue(forKey: Keys.LeaderboardName) } }
    var leaderboardGroupID: String { get { return stringValue(forKey: Keys.LeaderboardGroupID) } }
    var regatta: String { get { return stringValue(forKey: Keys.Regatta) } }
    var startDate: Double { get { return doubleValue(forKey: Keys.StartDate) / 1000 } }

}
