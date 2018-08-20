//
//  TrainingRaceTrackingData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 12.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingRaceData: NSObject, NSCoding {
    
    struct Keys {
        static let LeaderboardName = "LeaderboardName"
        static let FleetName = "FleetName"
        static let RaceColumnName = "RaceColumnName"
        static let RegattaName = "RegattaName"
    }
    
    var leaderboardName: String
    var regattaName: String
    var raceColumnName: String
    var fleetName: String
    
    init(
        leaderboardName: String,
        regattaName: String,
        raceColumnName: String,
        fleetName: String)
    {
        self.leaderboardName = leaderboardName
        self.regattaName = regattaName
        self.raceColumnName = raceColumnName
        self.fleetName = fleetName
    }
    
    // MARK: - NSCoding
    
    required convenience init?(coder decoder: NSCoder) {
        guard
            let leaderboardName = decoder.decodeObject(forKey: Keys.LeaderboardName) as? String,
            let regattaName = decoder.decodeObject(forKey: Keys.RegattaName) as? String,
            let raceColumnName = decoder.decodeObject(forKey: Keys.RaceColumnName) as? String,
            let fleetName = decoder.decodeObject(forKey: Keys.FleetName) as? String
            else { return nil }
        self.init(
            leaderboardName: leaderboardName,
            regattaName: regattaName,
            raceColumnName: raceColumnName,
            fleetName: fleetName
        )
    }
    
    func encode(with coder: NSCoder) {
        coder.encode(leaderboardName, forKey: Keys.LeaderboardName)
        coder.encode(regattaName, forKey: Keys.RegattaName)
        coder.encode(raceColumnName, forKey: Keys.RaceColumnName)
        coder.encode(fleetName, forKey: Keys.FleetName)
    }
    
}
