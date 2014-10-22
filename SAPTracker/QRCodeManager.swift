//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class QRCodeManager: NSObject {
    
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
    var from: Int?
    var to: Int?
    
    class var sharedManager: QRCodeManager {
        struct Singleton {
            static let sharedQRCodeManager = QRCodeManager()
        }
        return Singleton.sharedQRCodeManager
    }
    
    
    override init() {
        super.init()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "qrcodeScanned:", name: QRCodeViewController.NotificationType.qrcodeScannedNotificationKey, object: nil)
    }
    
    // MARK: - notification callbacks
    
    func qrcodeScanned(notification: NSNotification) {
        parseString(notification.userInfo!["value"] as String)
    }
    
    // MARK: - methods
    
    func parseString(urlString: String) {
        let url = NSURL(string: urlString)
        
        server = url!.scheme! + "://" + url!.host! + ":" + url!.port!.stringValue

        let queryString = url!.query
        let urlComponents = queryString!.componentsSeparatedByString("&")
        var queryStringDictionary = [String:String]()
        for keyValuePair in urlComponents
        {
            let pairComponents = keyValuePair.componentsSeparatedByString("=")
            let key = pairComponents[0]
            let value = pairComponents[1]
            queryStringDictionary[key] = value
        }
       
        leaderBoard = queryStringDictionary[QRCodeManager.Keys.leaderBoard]
        raceColumn = queryStringDictionary[QRCodeManager.Keys.raceColumn]
        fleet = queryStringDictionary[QRCodeManager.Keys.fleet]
        competitor = queryStringDictionary[QRCodeManager.Keys.competitor]
        mark = queryStringDictionary[QRCodeManager.Keys.mark]
        from = queryStringDictionary[QRCodeManager.Keys.from]?.toInt()
        to = queryStringDictionary[QRCodeManager.Keys.to]?.toInt()
    }
}
