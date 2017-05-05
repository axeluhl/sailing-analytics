//
//  CheckIn.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(CheckIn)
class CheckIn: NSManagedObject {

    func initialize() {
        event = CoreDataManager.sharedManager.newEvent(self)
        leaderboard = CoreDataManager.sharedManager.newLeaderboard(self)
    }

    func updateWithRegattaData(regattaData: RegattaData) {
        serverURL = regattaData.serverURL
        event.updateWithEventData(regattaData.eventData)
        leaderboard.updateWithLeaderboardData(regattaData.leaderboardData)
    }

    func eventURL() -> NSURL? {
        return NSURL(string: "\(serverURL)/gwt/Home.html?navigationTab=Regattas#EventPlace:eventId=\(event.eventID)")
    }
    
    func leaderboardURL() -> NSURL? {
        guard let name = leaderboard.nameWithQueryAllowedCharacters() else { return nil }
        return NSURL(string: "\(serverURL)/gwt/Leaderboard.html?name=\(name)&showRaceDetails=false&embedded=true&hideToolbar=true")
    }
    
}
