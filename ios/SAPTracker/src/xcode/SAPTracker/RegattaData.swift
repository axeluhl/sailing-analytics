//
//  RegattaData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class RegattaData: NSObject {
    
    private struct ItemNames {
        static let EventID = "event_id"
        static let LeaderboardName = "leaderboard_name"
        static let CompetitorID = "competitor_id"
    }
    
    let serverURL: String
    let eventID: String
    let leaderboardName: String
    let competitorID: String

    var eventData = EventData()
    var leaderboardData = LeaderboardData()
    var competitorData = CompetitorData()
    var teamImageURL: String?
    
    override init() {
        serverURL = ""
        eventID = ""
        leaderboardName = ""
        competitorID = ""
        super.init()
    }
    
    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         competitorID: String)
    {
        self.serverURL = serverURL
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.competitorID = competitorID
        super.init()
    }
    
    init(regatta: Regatta) {
        serverURL = regatta.serverURL
        eventID = regatta.event.eventID
        competitorID = regatta.competitor.competitorID
        leaderboardName = regatta.leaderboard.name
        super.init()
    }
    
    convenience init?(url: NSURL) {
        guard let host = url.host else { return nil }
        guard let components = NSURLComponents(string: url.absoluteString) else { return nil }
        
        // Set server URL
        let serverURL = url.scheme + "://" + host + (url.port != nil ? ":" + url.port!.stringValue : "")
        
        // In query component replace '+' occurrences with '%20' as a workaround for bug 3664
        components.percentEncodedQuery = components.percentEncodedQuery?.stringByReplacingOccurrencesOfString("+", withString: "%20")
        let queryItems = components.queryItems
        
        // Get check-in items
        guard let eventID = RegattaData.queryItemValue(queryItems, itemName: ItemNames.EventID) else { return nil }
        guard let leaderboardName = RegattaData.queryItemValue(queryItems, itemName: ItemNames.LeaderboardName) else { return nil }
        guard let competitorID = RegattaData.queryItemValue(queryItems, itemName: ItemNames.CompetitorID) else { return nil }
        
        // Init with values
        self.init(serverURL: serverURL,
                  eventID: eventID,
                  leaderboardName: leaderboardName,
                  competitorID: competitorID)
    }
    
    convenience init?(urlString: String) {
        guard let url = NSURL(string: urlString) else { return nil }
        
        // Init with URL
        self.init(url: url)
    }    
    
    // Properties
    
    var welcomeString: String {
        get {
            return String(format:NSLocalizedString("Hello %@. Welcome to %@. You are registered as %@.", comment: ""),
                          competitorData.name,
                          leaderboardData.name,
                          competitorData.sailID)
        }
    }
    
    // MARK: - Helper
    
    class private func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
