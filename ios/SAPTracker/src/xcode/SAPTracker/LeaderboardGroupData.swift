//
//  LeaderboardGroupData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 20.12.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class LeaderboardGroupData: BaseData {

    func raceName(trainingRaceData: TrainingRaceData) -> String? {
        var raceName: String?
        if let leaderboards = dictionary["leaderboards"] as? [[String: AnyObject]] {

            // Leaderboards
            leaderboards.forEach({ (leaderboard) in
                if leaderboard["name"] as? String == trainingRaceData.leaderboardName {

                    // Series
                    if let series = leaderboard["series"] as? [[String: AnyObject]] {
                        series.forEach({ (serie) in

                            // Fleets
                            if let fleets = serie["fleets"] as? [[String: AnyObject]] {
                                fleets.forEach({ (fleet) in
                                    if fleet["name"] as? String == trainingRaceData.fleetName {

                                        // Races
                                        if let races = fleet["races"] as? [[String: AnyObject]] {
                                            races.forEach({ (race) in
                                                if race["name"] as? String == trainingRaceData.raceColumnName {
                                                    raceName = race["trackedRaceName"] as? String
                                                }
                                            })
                                        }
                                    }
                                })
                            }
                        })
                    }
                }
            })
        }
        return raceName
    }

}
