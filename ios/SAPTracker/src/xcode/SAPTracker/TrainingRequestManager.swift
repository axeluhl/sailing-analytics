//
//  TrainingRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 24.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum TrainingRequestManagerError: Error {
    case invalidResponse
}

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
        boatClassName: String,
        success: @escaping (_ createEventData: CreateEventData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let urlString = "\(basePathString)/events/createEvent"
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
        body["boatclassname"] = boatClassName as AnyObject
        body["numberofraces"] = "1" as AnyObject
        manager.post(urlString, parameters: body, success: { (requestOperation, responseObject) in
            self.postCreateEventSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postCreateEventFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postCreateEventSuccess(responseObject: Any?, success: (_ createEventData: CreateEventData) -> Void, failure: (_ error: Error, _ message: String?) -> Void) {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: fixJSON(data: data))
                success(CreateEventData(dictionary: jsonObject as? [String: AnyObject]))
            } catch {
                postCreateEventFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
            }
        } else {
            postCreateEventFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
        }
    }
    
    fileprivate func postCreateEventFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - CompetitorCreateAndAdd
    
    func postCompetitorCreateAndAdd(
        regatta: String,
        boatClassName: String,
        sailID: String,
        nationality: String,
        success: @escaping (_ competitorCreateAndAddData: CompetitorCreateAndAddData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedRegatta = regatta.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedBoatClassName = boatClassName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedSailID = sailID.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedNationality = nationality.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/regattas/\(encodedRegatta)/competitors/createandadd?boatclass=\(encodedBoatClassName)&sailid=\(encodedSailID)&nationalityIOC=\(encodedNationality)"
        manager.post(urlString, parameters: nil, success: { (requestOperation, responseObject) in
            self.postCompetitorCreateAndAddSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postCompetitorCreateAndAddFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postCompetitorCreateAndAddSuccess(
        responseObject: Any?,
        success: @escaping (_ competitorCreateAndAddData: CompetitorCreateAndAddData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                success(CompetitorCreateAndAddData(dictionary: jsonObject as? [String: AnyObject]))
            } catch {
                self.postCompetitorCreateAndAddFailure(error: error, failure: failure)
            }
        } else {
            self.postCompetitorCreateAndAddFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
        }
    }
    
    fileprivate func postCompetitorCreateAndAddFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - SetTrackingTimes
    
    func postSetTrackingTimes(
        leaderboardName: String,
        raceName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedRaceName = raceName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedFleetName = fleetName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/settrackingtimes?race_column=\(encodedRaceName)&fleet=\(encodedFleetName)&startoftrackingasmillis=\(millisSince1970())"
        manager.responseSerializer = AFJSONResponseSerializer()
        manager.post(urlString, parameters: nil, success: { (requestOperation, responseObject) in
            self.postSetTrackingTimesSuccess(responseObject: responseObject, success: success)
        }) { (requestOperation, error) in
            self.postSetTrackingTimesFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postSetTrackingTimesSuccess(responseObject: Any?, success: @escaping () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postSetTrackingTimesFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - Helper
    
    fileprivate func millisSince1970() -> NSNumber {
        return NSNumber(value: Int64(Date().timeIntervalSince1970 * 1000) as Int64)
    }
    
    fileprivate func responseObjectToString(responseObject: Any?) -> String {
        return (responseObject as? String) ?? "response object is empty or cannot be casted"
    }
    
    fileprivate func stringForError(_ error: Error) -> String? {
        guard let data = ((error as NSError).userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] as? NSData) else { return nil }
        return String(data: data as Data, encoding: String.Encoding.utf8)
    }
    
    func fixJSON(data: Data) -> Data {
        guard let json = String(data: data, encoding: String.Encoding.utf8) else { return data }
        guard let newData = fixJSON(string: json).data(using: String.Encoding.utf8) else { return data }
        return newData
    }
    
    func fixJSON(string: String) -> String {
        let pattern = "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"
        let regex = try! NSRegularExpression(pattern: pattern, options: [])
        let matches = regex.matches(in: string, options: [], range: NSRange(location: 0, length: string.characters.count))
        var newString = string
        matches.forEach { (match) in
            let uuid = (string as NSString).substring(with: match.range)
            newString = newString.replacingOccurrences(of: uuid, with: "\"\(uuid)\"")
        }
        return newString
    }

}
