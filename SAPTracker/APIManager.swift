//
//  APIManager.swift
//  SAPTracker
//
//  Created by computing on 24/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class APIManager: NSObject {
    
    private var baseUrlString: String?
    private var manager: AFHTTPRequestOperationManager?
    private let postDeviceMapping = "/sailingserver/rc/racelog"
    private let postGPSFixPath = "/tracking/recordFixesFlatJson"
    
    class var sharedManager: APIManager {
        struct Singleton {
            static let sharedAPIManager = APIManager()
        }
        return Singleton.sharedAPIManager
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }

    /* Sets base URL for all requests. Starts reachability listener. */
    func initManager(baseUrlString: String) {
        if self.baseUrlString == baseUrlString {
            return
        }
        
        self.baseUrlString = baseUrlString;
        
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: baseUrlString)) // baseUrl needed for checking reachability
        
        // encode body as JSON
        manager!.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager!.responseSerializer = AFCompoundResponseSerializer() as AFHTTPResponseSerializer
        
        // set up reachability
        let operationQueue = manager!.operationQueue
        manager!.reachabilityManager.setReachabilityStatusChangeBlock({ (AFNetworkReachabilityStatus status) -> Void in
            switch (status) {
            case AFNetworkReachabilityStatus.ReachableViaWWAN, AFNetworkReachabilityStatus.ReachableViaWiFi:
                operationQueue?.suspended = false;
                break;
            default:
                operationQueue?.suspended = true;
            }
        })
        manager!.reachabilityManager.startMonitoring()
        
        // set up notifications
        /*
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "sendGPSFixes:", name: DataManager.NotificationType.newGPSFix, object: nil)
        */
    }
    
    /* Send a device to competitor mapping. */
    func postDeviceMapping(qrcodeData: QRCodeData!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        
        // resource path contains query parameters, note QR code data is already URL encoded
        var urlString = baseUrlString! + postDeviceMapping
        urlString += "?leaderboard=\(qrcodeData.leaderBoard!)"
        urlString += "&raceColumn=\(qrcodeData.raceColumn!)"
        urlString += "&fleet=\(qrcodeData.fleet!)"
        urlString += "&clientuuid=\(qrcodeData.competitor!)"
        
        var body = DeviceCompetitorMappingEvent(deviceId: DeviceUDIDManager.UDID, competitor: qrcodeData.competitor!, from: qrcodeData.from!, to: qrcodeData.to!).dictionary()
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    /* Send GPS location to server. Delete row from cache. */
    func postGPSFix(gpsFix: GPSFix!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        var urlString = baseUrlString! + postGPSFixPath
        manager!.POST(urlString, parameters: gpsFix.dictionary(), success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
            // delete GPS fix from database
            DataManager.sharedManager.managedObjectContext!.deleteObject(gpsFix)
            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                NSLog("failure")
        })
    }
    
}