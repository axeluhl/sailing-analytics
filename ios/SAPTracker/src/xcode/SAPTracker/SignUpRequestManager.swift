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
    case invalidResponse
}

extension SignUpRequestManagerError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .percentEncodingError:
            return "PERCENT ENCODING ERROR"
        case .invalidResponse:
            return "INVALID RESPONSE"
        }
    }
}

class SignUpRequestManager: NSObject {
    
    fileprivate let basePathString = "/security/api/restsecurity"
    
    fileprivate enum BodyKeys {
        static let AccessToken = "access_token"
        static let Authenticated = "authenticated"
        static let Company = "company"
        static let Email = "email"
        static let FullName = "fullName"
        static let Password = "password"
        static let Principal = "principal"
        static let Remembered = "remembered"
        static let UserName = "username"
    }
    
    let baseURLString: String
    let manager: AFHTTPSessionManager
    let sessionManager: AFURLSessionManager
    
    init(baseURLString: String = "") {
        self.baseURLString = baseURLString
        manager = AFHTTPSessionManager(baseURL: URL(string: baseURLString))
        manager.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager.requestSerializer.timeoutInterval = Application.RequestTimeout
        manager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        sessionManager = AFURLSessionManager(sessionConfiguration: URLSessionConfiguration.default)
        sessionManager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        super.init()
    }
    
    // MARK: - AccessToken
    
    func postAccessToken(
        userName: String,
        password: String,
        success: @escaping (_ userName: String, _ accessToken: String) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let urlString = "\(basePathString)/access_token"
        manager.requestSerializer.setAuthorizationHeaderFieldWithUsername(userName, password: password)
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postAccessTokenSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postAccessTokenFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postAccessTokenSuccess(
        responseObject: Any,
        success: (_ userName: String, _ accessToken: String) -> Void,
        failure: (_ error: Error, _ message: String?) -> Void)
    {
        manager.requestSerializer.clearAuthorizationHeader()
        guard let response = responseObject as? Dictionary<String, AnyObject> else {
            postAccessTokenFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let userName = response[BodyKeys.UserName] as? String else {
            postAccessTokenFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let accessToken = response[BodyKeys.AccessToken] as? String else {
            postAccessTokenFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        manager.requestSerializer.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        logInfo(name: "\(#function)", info: response.description)
        success(userName, accessToken)
    }
    
    fileprivate func postAccessTokenFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        manager.requestSerializer.clearAuthorizationHeader()
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - CreateUser
    
    func postCreateUser(
        userName: String,
        email: String,
        fullName: String,
        company: String,
        password: String,
        success: @escaping (_ userName: String, _ accessToken: String) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let urlString = "\(basePathString)/create_user?username=\(userName)&email=\(email)&fullName=\(fullName)&company=\(company)&password=\(password)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
                self.postCreateUserSuccess(responseObject: responseObject, success: success, failure: failure)
            }) { (requestOperation, error) in
                self.postCreateUserFailure(error: error, failure: failure)
            }
        } else {
            failure(SignUpRequestManagerError.percentEncodingError, nil)
        }
    }
    
    fileprivate func postCreateUserSuccess(
        responseObject: Any,
        success: (_ userName: String, _ accessToken: String) -> Void,
        failure: (_ error: Error, _ message: String?) -> Void)
    {
        guard let response = responseObject as? Dictionary<String, AnyObject> else {
            postCreateUserFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let userName = response[BodyKeys.UserName] as? String else {
            postCreateUserFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let accessToken = response[BodyKeys.AccessToken] as? String else {
            postCreateUserFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        logInfo(name: "\(#function)", info: response.description)
        success(userName, accessToken)
    }
    
    fileprivate func postCreateUserFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - ForgotPassword
    
    func postForgotPassword(
        email: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let urlString = "\(basePathString)/forgot_password?email=\(email)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            self.postForgotPassword(urlString: urlString, success: success, failure: failure)
        } else {
            self.postForgotPasswordFailure(error: SignUpRequestManagerError.percentEncodingError, failure: failure)
        }
    }
    
    func postForgotPassword(
        userName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let urlString = "\(basePathString)/forgot_password?username=\(userName)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            self.postForgotPassword(urlString: urlString, success: success, failure: failure)
        } else {
            self.postForgotPasswordFailure(error: SignUpRequestManagerError.percentEncodingError, failure: failure)
        }
    }
    
    fileprivate func postForgotPassword(
        urlString: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postForgotPasswordSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postForgotPasswordFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postForgotPasswordSuccess(
        responseObject: Any,
        success: () -> Void,
        failure: (_ error: Error, _ message: String?) -> Void)
    {
        let response = responseObject as AnyObject
        logInfo(name: "\(#function)", info: response.description)
        success()
    }
    
    fileprivate func postForgotPasswordFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - Hello
    
    func postHello(
        success: @escaping (_ principal: String, _ authenticated: Bool, _ remembered: Bool) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let urlString = "\(basePathString)/hello"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postHelloSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postHelloFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postHelloSuccess(
        responseObject: Any,
        success: (_ principal: String, _ authenticated: Bool, _ remembered: Bool) -> Void,
        failure: (_ error: Error, _ message: String?) -> Void)
    {
        guard let response = responseObject as? Dictionary<String, AnyObject> else {
            postHelloFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let principal = response[BodyKeys.Principal] as? String else {
            postHelloFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let authenticated = response[BodyKeys.Authenticated] as? Bool else {
            postHelloFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        guard let remembered = response[BodyKeys.Remembered] as? Bool else {
            postHelloFailure(error: SignUpRequestManagerError.invalidResponse, failure: failure)
            return
        }
        logInfo(name: "\(#function)", info: response.description)
        success(principal, authenticated, remembered)
    }
    
    fileprivate func postHelloFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - Logout
    
    func postLogout(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let urlString = "\(basePathString)/logout/"
        manager.requestSerializer.clearAuthorizationHeader()
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postLogoutSuccess(success: success)
        }) { (requestOperation, error) in
            self.postLogoutFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postLogoutSuccess(success: () -> Void) {
        logInfo(name: "\(#function)", info: "success")
        success()
    }
    
    fileprivate func postLogoutFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - Helper
    
    fileprivate func stringForError(_ error: Error) -> String? {
        guard let data = ((error as NSError).userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] as? NSData) else { return nil }
        return String(data: data as Data, encoding: String.Encoding.utf8)
    }
    
    fileprivate func jsessionCookie() -> HTTPCookie? {
        return HTTPCookieStorage.shared.cookies?.first(where: { $0.name == "JSESSIONID" })
    }
    
}
