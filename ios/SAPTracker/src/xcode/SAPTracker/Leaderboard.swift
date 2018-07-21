//
//  Leaderboard.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(Leaderboard)
class Leaderboard: NSManagedObject {

    func updateWithLeaderboardData(leaderboardData: LeaderboardData) {
        name = leaderboardData.name
    }
    
    func nameWithQueryAllowedCharacters() -> String? {
        return name.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
    }
    
}
