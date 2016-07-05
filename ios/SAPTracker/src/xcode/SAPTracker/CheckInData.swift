//
//  CheckInData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class CheckInData: NSObject {
    
    private struct ItemNames {
        static let EventID = "event_id"
        static let LeaderboardName = "leaderboard_name"
        static let CompetitorID = "competitor_id"
    }
    
    public let serverURL: String
    public let eventID: String
    public let leaderboardName: String
    public let competitorID: String

    init(serverURL: String = "",
         eventID: String = "",
         leaderboardName: String = "",
         competitorID: String = "")
    {
        self.serverURL = serverURL
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.competitorID = competitorID
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
        guard let eventID = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.EventID) else { return nil }
        guard let leaderboardName = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.LeaderboardName) else { return nil }
        guard let competitorID = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.CompetitorID) else { return nil }
        
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
    
    // MARK: - Helper
    
    private class func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
