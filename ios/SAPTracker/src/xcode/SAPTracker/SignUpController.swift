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
}

extension SignUpControllerError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .loginFailed:
            return SignUpTranslation.SignUpControllerError.LoginFailed.String
        }
    }
}

@objc protocol SignUpControllerDelegate {
    
    func signUpControllerDidFinish(_ controller: SignUpController)
    
    func signUpControllerDidCancel(_ controller: SignUpController)
    
}

class SignUpController: NSObject {

    var delegate: SignUpControllerDelegate?
    
    fileprivate let requestManager = SignUpRequestManager(baseURLString: "https://dev.sapsailing.com")
    
    func loginWithViewController(_ controller: UIViewController) {
        let storyboard = UIStoryboard(name: "SignUp", bundle: nil)
        let loginNC = storyboard.instantiateInitialViewController() as! UINavigationController
        let loginVC = loginNC.viewControllers[0] as! LoginViewController
        loginVC.signUpController = self
        controller.present(loginNC, animated: true, completion: nil)
    }
    
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
        requestManager.postAccessToken(userName: userName, password: password, success: { (userName, accessToken) in
            
        }) { (error, message) in
            self.showAlert(forError: self.loginViewControllerError(forError: error), andMessage: message, withViewController: controller)
        }
    }
    
    private func loginViewControllerError(forError error: Error) -> Error {
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
        requestManager.postCreateUser(userName: userName, email: email, fullName: fullName, company: company, password: password, success: { userName, accessToken in
            do {
                try Keychain.userName.savePassword(userName)
                try Keychain.userPassword.savePassword(password)
                try Keychain.userAccessToken.savePassword(accessToken)
                self.delegate?.signUpControllerDidFinish(self)
            } catch {
                self.showAlert(forError: error, andMessage: nil, withViewController: controller)
            }
        }) { (error, message) in
            self.showAlert(forError: error, andMessage: message, withViewController: controller)
        }
    }
    
}

// MARK: - ForgotPasswordViewControllerDelegate

extension SignUpController: ForgotPasswordViewControllerDelegate {
    
    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForEmail email: String) {
        requestManager.postForgotPassword(email: email, success: { 
            
        }) { (error, message) in
            self.showAlert(forError: error, andMessage: message, withViewController: controller)
        }
    }
    
    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForUserName userName: String) {
        requestManager.postForgotPassword(userName: userName, success: {
            
        }) { (error, message) in
            self.showAlert(forError: error, andMessage: message, withViewController: controller)
        }
    }
    
}
