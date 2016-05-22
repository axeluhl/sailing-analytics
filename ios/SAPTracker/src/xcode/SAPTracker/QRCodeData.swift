//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class QRCodeData {
    
    struct Keys {
        static let eventId = "event_id"
        static let leaderBoardName = "leaderboard_name"
        static let competitorId = "competitor_id"
    }
    
    public var serverUrl: String?
    public var eventId: String?
    public var leaderBoardName: String?
    public var competitorId: String?

    public init() {
    }
    
    public func parseString(urlString: String) -> Bool {
        let url = NSURL(string: urlString)
        if (url == nil || url!.host == nil || url!.query == nil) {
            return false
        }
        
        // Set server url
        serverUrl = url!.scheme + "://" + url!.host!
        if ((url!.port) != nil) {
            serverUrl! += ":" + url!.port!.stringValue
        }
        
        // Get query items and replace '+' occurrences with '%20' as a workaround for bug 3664
        let components = NSURLComponents(string: (url?.absoluteString)!)
        components!.percentEncodedQuery = components!.percentEncodedQuery!.stringByReplacingOccurrencesOfString("+", withString: "%20")
        let queryItems = components?.queryItems
        
        // Get query parameter
        eventId = queryItemValue(queryItems, itemName: QRCodeData.Keys.eventId)
        leaderBoardName = queryItemValue(queryItems, itemName: QRCodeData.Keys.leaderBoardName)
        competitorId = queryItemValue(queryItems, itemName: QRCodeData.Keys.competitorId)
        
        return eventId != nil && leaderBoardName != nil && competitorId != nil
    }
    
    func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
