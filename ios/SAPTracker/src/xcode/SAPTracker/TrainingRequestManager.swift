//
//  TrainingRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 24.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingRequestManager: NSObject {
    
    fileprivate let basePathString = "/sailingserver/api/v1"
    
    let baseURLString: String
    let manager: AFHTTPRequestOperationManager
    let sessionManager: AFURLSessionManager
    
    init(baseURLString: String = "") {
        self.baseURLString = baseURLString
        manager = AFHTTPRequestOperationManager(baseURL: URL(string: baseURLString))
        manager.requestSerializer = AFHTTPRequestSerializer()
        manager.responseSerializer = AFHTTPResponseSerializer()
        sessionManager = AFURLSessionManager(sessionConfiguration: URLSessionConfiguration.default)
        sessionManager.responseSerializer = AFHTTPResponseSerializer()
        super.init()
    }
    
    // MARK: - CreateEvent
    
    func postCreateEvent(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let urlString = "/sailingserver/api/v1/events/createEvent"
        
        var body = [String: AnyObject]()
        //        body["eventname"] = "raimund" as AnyObject
        //        body["eventdescription"] = "raimund" as AnyObject
        //        body["startdate"] = "" as AnyObject
        //        body["startdateasmillis"] = "" as AnyObject
        //        body["enddate"] = "" as AnyObject
        //        body["enddateasmillis"] = "" as AnyObject
        body["venuename"] = "hamburg" as AnyObject
        //        body["venuelat"] = "" as AnyObject
        //        body["venuelng"] = "" as AnyObject
        //        body["ispublic"] = "" as AnyObject
        //        body["officialwebsiteurl"] = "" as AnyObject
        //        body["baseurl"] = "" as AnyObject
        //        body["leaderboardgroupids"] = "" as AnyObject
        //        body["createleaderboardgroup"] = "" as AnyObject
        body["createregatta"] = "true" as AnyObject
        body["boatclassname"] = "J80" as AnyObject
        //        body["numberofraces"] = "" as AnyObject

        manager.post(urlString, parameters: body, success: { (requestOperation, responseObject) in
            success()
        }) { (requestOperation, error) in
            failure(error, self.stringForError(error))
        }
    }
    
    // MARK: - Helper
    
    fileprivate func stringForError(_ error: Error) -> String? {
        guard let data = ((error as NSError).userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] as? NSData) else { return nil }
        return String(data: data as Data, encoding: String.Encoding.utf8)
    }
    
}
