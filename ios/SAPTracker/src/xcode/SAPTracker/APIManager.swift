//
//  APIManager.swift
//  SAPTracker
//
//  Created by computing on 24/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

public class APIManager: NSObject {
    
    struct NotificationType {
        static let networkAvailabilityChanged = "networkAvailabilityChanged"
    }
    
    /* Constants */
    struct Constants {
        /* Max number of fix objects to be sent per POST request. */
        static let maxSendGPSFix = 100
    }
    
    /* Server URL */
    private var serverUrlString: String?
    
    /* Base url of all requests, contains serverUrlString */
    private let baseUrlString = "/sailingserver/api/v1"
    
    /* AFNetworking operation manager */
    private var manager: AFHTTPRequestOperationManager?
    
    var networkAvailable: Bool {
        get {
            if manager == nil {
                return false
            }
            return !manager!.operationQueue.suspended
        }
    }
    
    /* Singleton */
    public class var sharedManager: APIManager {
        struct Singleton {
            static let sharedManager = APIManager()
        }
        return Singleton.sharedManager
    }
    
    /* Sets base URL for all requests. Request and response are JSON. Starts reachability listener. Starts timer for uploading data. */
    public func initManager(serverUrlString: String) {
        if self.serverUrlString == serverUrlString {
            return
        }
        
        self.serverUrlString = serverUrlString
        
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: self.serverUrlString!)) // baseUrl needed for checking reachability
        
        // encode/decode body as JSON
        manager!.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager!.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        
        // set up reachability
        let operationQueue = manager!.operationQueue
        manager!.reachabilityManager.setReachabilityStatusChangeBlock({(AFNetworkReachabilityStatus status) -> Void in
            switch (status) {
            case AFNetworkReachabilityStatus.ReachableViaWWAN, AFNetworkReachabilityStatus.ReachableViaWiFi:
                operationQueue?.suspended = false
                break
            default:
                operationQueue?.suspended = true
            }
            
            // send notification (e.g. to tracker view controller)
            let notification = NSNotification(name: NotificationType.networkAvailabilityChanged, object: self)
            NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
        })
        manager!.reachabilityManager.startMonitoring()
    }
    
    // MARK: - REST API
    
    /* Get event */
    public func getEvent(eventId: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let urlString = baseUrlString + "/events/\(eventId)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    /* Get leader board */
    public func getLeaderBoard(leaderBoardName: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let urlString = baseUrlString + "/leaderboards/\(leaderBoardName.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    /* Get competitor */
    public func getCompetitor(competitorId: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let urlString = baseUrlString + "/competitors/\(competitorId)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    /* Map a device to competitor. */
    public func checkIn(leaderBoardName: String!, competitorId: String!, deviceUuid: String!, pushDeviceId: String!, fromMillis: Int64!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        
        let urlString = baseUrlString + "/leaderboards/\(leaderBoardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/start"
        
        var body = [String: AnyObject]()
        body["competitorId"] = competitorId
        body["deviceType"] = "ios"
        body["deviceUuid"] = deviceUuid
        body["pushDeviceId"] = pushDeviceId
        body["fromMillis"] = NSNumber(longLong: fromMillis)
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    /* Disconnect a device from competitor. */
    public func checkOut(leaderBoardName: String!, competitorId: String!, deviceUuid: String!, toMillis: Int64!, success:(AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        
        let urlString = baseUrlString + "/leaderboards/\(leaderBoardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/end"
        
        var body = [String: AnyObject]()
        body["competitorId"] = competitorId
        body["deviceUuid"] = deviceUuid
        body["toMillis"] = NSNumber(longLong: toMillis)
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    /* Send GPS location to server. Delete row from cache. */
    public func postGPSFixes(deviceUuid: String!, gpsFixes: [GPSFix]!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        if gpsFixes.count == 0 {
            return
        }
        
        let urlString = baseUrlString + "/gps_fixes"
        
        var body = [String: AnyObject]()
        body["deviceUuid"] = deviceUuid
        var array: [[String: AnyObject]] = []
        for gpsFix in gpsFixes {
            array.append(gpsFix.dictionary())
        }
        body["fixes"] = array
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    
    /* Send GPS location to server. Delete row from cache. */
    public func postTeamImage(competitorId: String!, imageData: NSData!, success: (AnyObject!) -> Void, failure: (AnyObject!) -> Void) {
        let urlString = baseUrlString + "/competitors/\(competitorId)/team/image"
        let configuration = NSURLSessionConfiguration.defaultSessionConfiguration()
        let sessionManager = AFURLSessionManager(sessionConfiguration: configuration)
        sessionManager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        let url = NSURL(string: self.serverUrlString! + urlString)
        let request = NSMutableURLRequest(URL: url!)
        request.setValue("image/jpeg", forHTTPHeaderField: "Content-Type")
        request.HTTPBody = imageData;
        request.HTTPMethod = "POST"
        let dataTask = sessionManager.dataTaskWithRequest(request, completionHandler:{ (response, responseObject, error) in
            if (error != nil) {
                failure(error)
            }
            else {
                success(responseObject)
            }
        })
        dataTask.resume();

    }
    
}