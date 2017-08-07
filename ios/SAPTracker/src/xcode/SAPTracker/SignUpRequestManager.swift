//
//  SignUpRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum SignUpRequestManagerError: Error {
    case userNameDoesNotMeetRequirements
}

extension SignUpRequestManagerError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .userNameDoesNotMeetRequirements:
            return "USERNAME DOES NOT MEET REQUIREMENTS"
        }
    }
}

class SignUpRequestManager: NSObject {
    
    fileprivate let basePathString = "/security/api/restsecurity"
    
    fileprivate enum BodyKeys {
        static let Company = "company"
        static let Email = "email"
        static let FullName = "fullName"
        static let Password = "password"
        static let UserName = "username"
    }
    
    let baseURLString: String
    let manager: AFHTTPRequestOperationManager
    let sessionManager: AFURLSessionManager
    
    init(baseURLString: String = "") {
        self.baseURLString = baseURLString
        manager = AFHTTPRequestOperationManager(baseURL: URL(string: baseURLString))
        manager.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        sessionManager = AFURLSessionManager(sessionConfiguration: URLSessionConfiguration.default)
        sessionManager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        super.init()
    }
    
    // MARK: - User

    func postUser(
        userName: String,
        email: String,
        fullName: String,
        company: String,
        password: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let urlString = "\(basePathString)/create_user?username=\(userName)&email=\(email)&fullName=\(fullName)&company=\(company)&password=\(password)"
        manager.post(
            urlString,
            parameters: nil,
            success: { (requestOperation, responseObject) in self.postUserSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.postUserFailure(error: error, failure: failure) }
        )
    }

    fileprivate func postUserSuccess(responseObject: Any, success: () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }

    fileprivate func postUserFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        logError(name: "\(#function)", error: String(data: ((error as NSError).userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] as! NSData) as Data, encoding: String.Encoding.utf8)!)
        failure(RequestManagerError.postGPSFixFailed)
    }

    // MARK: - Helper

    fileprivate func responseObjectToString(responseObject: Any?) -> String {
        return (responseObject as? String) ?? "response object is empty or cannot be casted"
    }

}
