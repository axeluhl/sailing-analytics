//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class CheckInData {
    
    struct ItemNames {
        static let eventId = "event_id"
        static let leaderboardName = "leaderboard_name"
        static let competitorId = "competitor_id"
    }
    
    struct Competitor {
        static let id = "id"
        static let name = "name"
        static let sailId = "sailID"
    }
    
    struct Leaderboard {
        static let name = "name"
    }
    
    public let serverURL: String!
    public let eventId: String!
    public let leaderboardName: String!
    public let competitorId: String!

    public var eventDictionary: [String: AnyObject]?
    public var leaderboardDictionary: [String: AnyObject]?
    public var competitorDictionary: [String: AnyObject]?
    public var teamImageURI: String?
    
    public init?(urlString: String!) {
        
        // Init fails if URL is invalid
        let url = NSURL(string: urlString)
        if (url == nil || url!.host == nil || url!.query == nil) {
            return nil
        }
        
        // Get query items and replace '+' occurrences with '%20' as a workaround for bug 3664
        let components = NSURLComponents(string: (url!.absoluteString))
        components!.percentEncodedQuery = components!.percentEncodedQuery!.stringByReplacingOccurrencesOfString("+", withString: "%20")
        let queryItems = components?.queryItems
        
        // Set server URL and check-in items
        serverURL = url!.scheme + "://" + url!.host! + (url!.port == nil ? "" : ":" + url!.port!.stringValue)
        eventId = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.eventId)
        leaderboardName = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.leaderboardName)
        competitorId = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.competitorId)
        
        // Init fails if one check-in item is missing
        if eventId == nil || leaderboardName == nil || competitorId == nil {
            return nil
        }
    }
    
    // MARK: - Leaderboard
    
    func dictionaryLeaderboardName() -> String! {
        return valueFromDictionary(leaderboardDictionary, forKey: Leaderboard.name)
    }
    
    // MARK: - Competitor
    
    func dictionaryCompetitorId() -> String {
        return valueFromDictionary(competitorDictionary, forKey: Competitor.id)
    }
    
    func dictionaryCompetitorName() -> String! {
        return valueFromDictionary(competitorDictionary, forKey: Competitor.name)
    }
    
    func dictionaryCompetitorSailId() -> String! {
        return valueFromDictionary(competitorDictionary, forKey: Competitor.sailId)
    }
    
    // MARK: - Helper
    
    private class func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
    private func valueFromCompetitor(forKey key: String) -> String {
        return valueFromDictionary(competitorDictionary, forKey: key)
    }
    
    private func valueFromDictionary(dictionary: [String: AnyObject]?, forKey key: String!) -> String {
        if let value = dictionary?[key] {
            return value as! String
        }
        return ""
    }
    
}
