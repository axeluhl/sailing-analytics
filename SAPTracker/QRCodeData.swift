//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class QRCodeData {
    
    struct Keys {
        static let leaderBoard = "leaderboard"
        static let raceColumn = "raceColumn"
        static let fleet = "fleet"
        static let competitor = "competitor"
        static let mark = "mark"
        static let from = "from"
        static let to = "to"
    }
    
    var server: String?
    var leaderBoard: String?
    var raceColumn: String?
    var fleet: String?
    var competitor: String?
    var mark: String?
    var from: Double?
    var to: Double?
 
    init() {
        
    }
    
    func parseString(urlString: String) -> Bool {
        let url = NSURL(string: urlString)
        
        if (url == nil || url!.scheme == nil || url!.host == nil) {
            return false
        }
        
        server = url!.scheme! + "://" + url!.host!
        if ((url!.port) != nil) {
            server! += ":" + url!.port!.stringValue
        }
        
        let queryString = url!.query
        if (queryString == nil) {
            return false
        }
        let urlComponents = queryString!.componentsSeparatedByString("&")
        var queryStringDictionary = [String:String]()
        for keyValuePair in urlComponents
        {
            let pairComponents = keyValuePair.componentsSeparatedByString("=")
            let key = pairComponents[0]
            let value = pairComponents[1]
            queryStringDictionary[key] = value
        }
       
        leaderBoard = queryStringDictionary[QRCodeData.Keys.leaderBoard]
        raceColumn = queryStringDictionary[QRCodeData.Keys.raceColumn]
        fleet = queryStringDictionary[QRCodeData.Keys.fleet]
        competitor = queryStringDictionary[QRCodeData.Keys.competitor]
        mark = queryStringDictionary[QRCodeData.Keys.mark]
        if queryStringDictionary[QRCodeData.Keys.from] != nil {
            from = (queryStringDictionary[QRCodeData.Keys.from]! as NSString).doubleValue
        }
        if queryStringDictionary[QRCodeData.Keys.to] != nil {
            to = (queryStringDictionary[QRCodeData.Keys.to]! as NSString).doubleValue
        }

        // TODO: make sure minimum values set
        
        return true
    }
}
