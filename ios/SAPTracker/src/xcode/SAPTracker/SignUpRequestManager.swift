//
//  SignUpRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum SignUpRequestManagerError: Error {
    case percentEncodingError
    case postUserFailed
    case postUserInvalidResponse
}

extension SignUpRequestManagerError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .percentEncodingError:
            return "PERCENT ENCODING ERROR"
        case .postUserFailed:
            return "POST USER FAILED"
        case .postUserInvalidResponse:
            return "POST USER INVALID RESPONSE"
        }
    }
}

class SignUpRequestManager: NSObject {
    
    fileprivate let basePathString = "/security/api/restsecurity"
    
    fileprivate enum BodyKeys {
        static let AccessToken = "access_token"
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
    
    // MARK: - CreateUser
    
    func postUser(
        userName: String,
        email: String,
        fullName: String,
        company: String,
        password: String,
        success: @escaping (_ userName: String, _ token: String) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let urlString = "\(basePathString)/create_user?username=\(userName)&email=\(email)&fullName=\(fullName)&company=\(company)&password=\(password)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            manager.post(
                urlString,
                parameters: nil,
                success: { (requestOperation, responseObject) in self.postUserSuccess(responseObject: responseObject, success: success, failure: failure) },
                failure: { (requestOperation, error) in self.postUserFailure(error: error, failure: failure) }
            )
        } else {
            failure(SignUpRequestManagerError.percentEncodingError, nil)
        }
    }
    
    fileprivate func postUserSuccess(
        responseObject: Any,
        success: (_ userName: String, _ token: String) -> Void,
        failure: (_ error: Error, _ message: String?) -> Void)
    {
        guard let response = responseObject as? Dictionary<String, AnyObject> else {
            postUserFailure(error: SignUpRequestManagerError.postUserInvalidResponse, failure: failure)
            return
        }
        guard let userName = response[BodyKeys.UserName] as? String else {
            postUserFailure(error: SignUpRequestManagerError.postUserInvalidResponse, failure: failure)
            return
        }
        guard let accessToken = response[BodyKeys.AccessToken] as? String else {
            postUserFailure(error: SignUpRequestManagerError.postUserInvalidResponse, failure: failure)
            return
        }
        logInfo(name: "\(#function)", info: response.description)
        success(userName, accessToken)
    }
    
    fileprivate func postUserFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(SignUpRequestManagerError.postUserFailed, stringForError(error))
    }
    
    // MARK: - Helper
    
    fileprivate func stringForError(_ error: Error) -> String? {
        guard let data = ((error as NSError).userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] as? NSData) as? Data else { return nil }
        return String(data: data, encoding: String.Encoding.utf8)
    }
    
}
