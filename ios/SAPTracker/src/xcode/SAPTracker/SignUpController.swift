//
//  SignUpController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum SignUpControllerError: Error {
    case loginFailed
    case unauthenticated
}

extension SignUpControllerError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .loginFailed:
            return SignUpTranslation.SignUpControllerError.LoginFailed.String
        case .unauthenticated:
            return SignUpTranslation.SignUpControllerError.Unauthenticated.String
        }
    }
}

@objc protocol SignUpControllerDelegate: class {
    
    func signUpController(_ controller: SignUpController, didFinishLoginWithUserName userName: String)
    
    func signUpControllerDidCancelLogin(_ controller: SignUpController)
    
    func signUpControllerDidLogout(_ controller: SignUpController)
    
}

class SignUpController: NSObject {

    weak var delegate: SignUpControllerDelegate?
    
    fileprivate let requestManager: SignUpRequestManager
    
    init(baseURLString: String) {
        requestManager = SignUpRequestManager(baseURLString: baseURLString)
        super.init()
    }

    // MARK: - Check Authentication

    func checkAuthentication(
        success: @escaping (_ userName: String) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        requestManager.postHello(success: { (principal, authenticated, remembered) in
            if authenticated {
                success(principal)
            } else {
                failure(SignUpControllerError.unauthenticated, nil)
            }
        }) { (error, message) in
            failure(error, message)
        }
    }

    // MARK: - Refresh Access Token

    func refreshAccessToken(
        success: @escaping (_ userName: String) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        requestManager.postAccessToken(userName: userName ?? "", password: userPassword ?? "", success: { (userName, accessToken) in
            self.userAccessToken = accessToken
            success(userName)
        }, failure: failure)
    }

    // MARK: - Login

    func login(
        success: @escaping (_ userName: String) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        self.checkAuthentication(success: success) { (error, message) in
            self.refreshAccessToken(success: success, failure: failure)
        }
    }

    func loginWithViewController(_ controller: UIViewController) {
        let storyboard = UIStoryboard(name: "SignUp", bundle: nil)
        let loginNC = storyboard.instantiateInitialViewController() as! UINavigationController
        let loginVC = loginNC.viewControllers[0] as! LoginViewController
        loginVC.signUpController = self
        controller.present(loginNC, animated: true, completion: nil)
    }

    // MARK: - Logout

    func logout(completion: @escaping () -> Void) {
        userName = nil
        userPassword = nil
        userAccessToken = nil
        requestManager.postLogout(success: { [weak self] in
            self?.didLogout()
            completion()
        }) { [weak self] (error, message) in
            self?.didLogout()
            completion()
        }
    }

    // MARK: SignUpControllerDelegate
    
    fileprivate func didFinishLogin() {
        delegate?.signUpController(self, didFinishLoginWithUserName: userName ?? "")
    }
    
    fileprivate func didCancelLogin() {
        delegate?.signUpControllerDidCancelLogin(self)
    }
    
    fileprivate func didLogout() {
        delegate?.signUpControllerDidLogout(self)
    }

    // MARK: - Properties

    var baseURLString: String {
        get {
            return requestManager.baseURLString
        }
    }

    var userName: String? {
        get {
            return password(forItem: Keychain.userName(forService: baseURLString))
        }
        set (value) {
            setPassword(value, forItem: Keychain.userName(forService: baseURLString))
        }
    }

    var userPassword: String? {
        get {
            return password(forItem: Keychain.userPassword(forService: baseURLString))
        }
        set (value) {
            setPassword(value, forItem: Keychain.userPassword(forService: baseURLString))
        }
    }

    var userAccessToken: String? {
        get {
            return password(forItem: Keychain.userAccessToken(forService: baseURLString))
        }
        set (value) {
            setPassword(value, forItem: Keychain.userAccessToken(forService: baseURLString))
        }
    }

    fileprivate func password(forItem item: KeychainPasswordItem) -> String? {
        do {
            return try item.readPassword()
        } catch {
            print(error)
            return nil
        }
    }

    fileprivate func setPassword(_ password: String?, forItem item: KeychainPasswordItem) {
        do {
            if let password = password {
                try item.savePassword(password)
            } else {
                try item.deleteItem()
            }
        } catch {
            print(error)
        }
    }

    // MARK: - Helper
    
    fileprivate func showAlert(forError error: Error, andMessage message: String?, withViewController controller: UIViewController) {
        let alertController = UIAlertController.init(title: error.localizedDescription, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction.init(title: Translation.Common.OK.String, style: .cancel, handler: nil)
        alertController.addAction(okAction)
        controller.present(alertController, animated: true, completion: nil)
    }

}

// MARK: - LoginViewControllerDelegate

extension SignUpController: LoginViewControllerDelegate {
    
    func loginViewController(_ controller: LoginViewController, willLoginWithUserName userName: String, password: String) {
        requestManager.postAccessToken(userName: userName, password: password, success: { [weak self] userName, accessToken in
            self?.loginViewController(controller, didFinishLoginWithUserName: userName, password: password, accessToken: accessToken)
        }) { [weak self] error, message in
            self?.loginViewController(controller, didFailLoginWithError: error, message: message)
        }
    }
    
    func loginViewControllerWillCancel(_ controller: LoginViewController) {
        controller.presentingViewController?.dismiss(animated: true)
        didCancelLogin()
    }
    
    fileprivate func loginViewController(_ controller: LoginViewController, didFinishLoginWithUserName userName: String, password: String, accessToken: String) {
        self.userName = userName
        self.userPassword = password
        self.userAccessToken = accessToken
        controller.presentingViewController?.dismiss(animated: true)
        didFinishLogin()
    }
    
    fileprivate func loginViewController(_ controller: LoginViewController, didFailLoginWithError error: Error, message: String?) {
        showAlert(forError: SignUpController.loginViewControllerError(forError: error), andMessage: message, withViewController: controller)
    }
    
    fileprivate static func loginViewControllerError(forError error: Error) -> Error {
        if (error as NSError).domain == NSURLErrorDomain {
            return error
        }
        return SignUpControllerError.loginFailed
    }
    
}

// MARK: - SignUpViewControllerDelegate

extension SignUpController: SignUpViewControllerDelegate {
    
    func signUpViewController(
        _ controller: SignUpViewController,
        willSignUpWithUserName userName: String,
        email: String,
        fullName: String,
        company: String,
        password: String)
    {
        requestManager.postCreateUser(userName: userName, email: email, fullName: fullName, company: company, password: password, success: { [weak self] userName, accessToken in
            self?.signUpViewController(controller, didFinishSignUpWithUserName: userName, password: password, accessToken: accessToken)
        }) { [weak self] error, message in
            self?.signUpViewController(controller, didFailSignUpWithError: error, message: message)
        }
    }
    
    fileprivate func signUpViewController(
        _ controller: SignUpViewController,
        didFinishSignUpWithUserName userName: String,
        password: String,
        accessToken: String)
    {
        self.userName = userName
        self.userPassword = password
        self.userAccessToken = accessToken
        controller.presentingViewController?.dismiss(animated: true)
        didFinishLogin()
    }
    
    fileprivate func signUpViewController(_ controller: SignUpViewController, didFailSignUpWithError error: Error, message: String?) {
        showAlert(forError: error, andMessage: message, withViewController: controller)
    }
    
}

// MARK: - ForgotPasswordViewControllerDelegate

extension SignUpController: ForgotPasswordViewControllerDelegate {
    
    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForEmail email: String) {
        requestManager.postForgotPassword(email: email, success: { [weak self] in
            self?.forgotPasswordViewControllerDidFinishChangePassword(controller)
        }) { [weak self] error, message in
            self?.forgotPasswordViewController(controller, didFailChangePasswordWithError: error, message: message)
        }
    }
    
    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForUserName userName: String) {
        requestManager.postForgotPassword(userName: userName, success: { [weak self] in
            self?.forgotPasswordViewControllerDidFinishChangePassword(controller)
        }) { [weak self] error, message in
            self?.forgotPasswordViewController(controller, didFailChangePasswordWithError: error, message: message)
        }
    }
    
    fileprivate func forgotPasswordViewControllerDidFinishChangePassword(_ controller: ForgotPasswordViewController) {
        // TODO
    }
    
    fileprivate func forgotPasswordViewController(_ controller: ForgotPasswordViewController, didFailChangePasswordWithError error: Error, message: String?) {
        showAlert(forError: error, andMessage: message, withViewController: controller)
    }
    
}
