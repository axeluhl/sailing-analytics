//
//  CheckInData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

open class CheckInData: NSObject {
    
    fileprivate struct ItemNames {
        static let EventID = "event_id"
        static let LeaderboardName = "leaderboard_name"
        static let CompetitorID = "competitor_id"
        static let MarkID = "mark_id"
    }
    
    public enum CheckInDataType {
        case competitor
        case mark
    }
    
    let serverURL: String
    let eventID: String
    let leaderboardName: String
    let competitorID: String?
    let markID: String?
    let type: CheckInDataType

    var eventData = EventData()
    var leaderboardData = LeaderboardData()
    var competitorData = CompetitorData()
    var markData = MarkData()
    var teamImageURL: String?
    
    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         competitorID: String)
    {
        self.competitorID = competitorID
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = nil
        self.serverURL = serverURL
        self.type = CheckInDataType.competitor
        super.init()
    }

    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         markID: String)
    {
        self.competitorID = nil
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = markID
        self.serverURL = serverURL
        type = CheckInDataType.mark
        super.init()
    }

    convenience init(competitorCheckIn: CompetitorCheckIn) {
        self.init(
            serverURL: competitorCheckIn.serverURL,
            eventID: competitorCheckIn.event.eventID,
            leaderboardName: competitorCheckIn.leaderboard.name,
            competitorID: competitorCheckIn.competitorID
        )
    }

    convenience init(markCheckIn: MarkCheckIn) {        
        self.init(
            serverURL: markCheckIn.serverURL,
            eventID: markCheckIn.event.eventID,
            leaderboardName: markCheckIn.leaderboard.name,
            markID: markCheckIn.markID
        )
    }

    convenience init?(url: URL) {
        guard let host = url.host else { return nil }
        
        // Set server URL
        let serverURL = url.scheme! + "://" + host + (url.port != nil ? ":\(url.port!)" : "")
        
        // In query component replace '+' occurrences with '%20' as a workaround for bug 3664
        var components = URLComponents(string: url.absoluteString)
        components?.percentEncodedQuery = components?.percentEncodedQuery?.replacingOccurrences(of: "+", with: "%20")
        guard let queryItems = components?.queryItems else { return nil }
        
        // Get check-in items
        guard let eventID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.EventID) else { return nil }
        guard let leaderboardName = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.LeaderboardName) else { return nil }
        if let competitorID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.CompetitorID) {
            self.init(
                serverURL: serverURL,
                eventID: eventID,
                leaderboardName: leaderboardName,
                competitorID: competitorID
            )
        } else if let markID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.MarkID) {
            self.init(
                serverURL: serverURL,
                eventID: eventID,
                leaderboardName: leaderboardName,
                markID: markID
            )
        } else {
            logError(name: "\(#function)", error: "unknown check-in type")
            return nil
        }
    }
    
    convenience init?(urlString: String) {
        guard let url = URL(string: urlString) else { return nil }
        
        // Init with URL
        self.init(url: url)
    }

    convenience init?(checkIn: CheckIn) {
        if let competitorCheckIn = checkIn as? CompetitorCheckIn {
            self.init(competitorCheckIn: competitorCheckIn)
        } else if let markCheckIn = checkIn as? MarkCheckIn {
            self.init(markCheckIn: markCheckIn)
        } else {
            logError(name: "\(#function)", error: "unknown check-in type")
            return nil
        }
    }

    // MARK: - Helper
    
    class fileprivate func queryItemValue(queryItems: [URLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
