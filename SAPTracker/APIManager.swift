//
//  APIManager.swift
//  SAPTracker
//
//  Created by computing on 24/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class APIManager {
    
    private var baseUrlString: String?
    private var manager: AFHTTPRequestOperationManager?
 
    class var sharedManager: APIManager {
        struct Singleton {
            static let sharedAPIManager = APIManager()
        }
        return Singleton.sharedAPIManager
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
    }
    
    func postDeviceMapping(qrcodeData: QRCodeData!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {

        // resource path contains query parameters, note QR code data is already URL encoded
        var urlString = baseUrlString! + "/sailingserver/rc/racelog"
        urlString += "?leaderboard=\(qrcodeData.leaderBoard!)"
        urlString += "&raceColumn=\(qrcodeData.raceColumn!)"
        urlString += "&fleet=\(qrcodeData.fleet!)"
        urlString += "&clientuuid=\(qrcodeData.competitor!)"
        
        var body = DeviceCompetitorMappingEvent(deviceId: DeviceUDIDManager.UDID, competitor: qrcodeData.competitor!, from: qrcodeData.from!, to: qrcodeData.to!).dictionary()
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
     }
    
}