//
//  RegattaRaceColumnAddData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 12.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaRaceColumnAddData: BaseData {
    
    fileprivate struct Keys {
        static let FleetName = "seriesname"
        static let RaceColumnName = "racename"
    }
    
    var fleetName: String { get { return stringValue(forKey: Keys.FleetName) } }
    var raceColumnName: String { get { return stringValue(forKey: Keys.RaceColumnName) } }
    
}
