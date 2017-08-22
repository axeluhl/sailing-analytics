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

@objc protocol SignUpControllerDelegate: class {
    
    func signUpControllerDidFinish(_ controller: SignUpController)
    
    func signUpControllerDidCancel(_ controller: SignUpController)
    
}

class SignUpController: NSObject {

    weak var delegate: SignUpControllerDelegate?
    
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
        requestManager.postAccessToken(userName: userName, password: password, success: { [weak self] userName, accessToken in
            self?.loginViewController(controller, didFinishLoginWithUserName: userName, password: password)
        }) { [weak self] error, message in
            self?.loginViewController(controller, didFailLoginWithError: error, message: message)
        }
    }
    
    func loginViewControllerWillCancel(_ controller: LoginViewController) {
        controller.presentingViewController?.dismiss(animated: true)
        self.delegate?.signUpControllerDidCancel(self)
    }
    
    fileprivate func loginViewController(_ controller: LoginViewController, didFinishLoginWithUserName: String, password: String) {
        controller.presentingViewController?.dismiss(animated: true)
        self.delegate?.signUpControllerDidFinish(self)
    }
    
    fileprivate func loginViewController(_ controller: LoginViewController, didFailLoginWithError error: Error, message: String?) {
        self.showAlert(forError: SignUpController.loginViewControllerError(forError: error), andMessage: message, withViewController: controller)
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
        do {
            try Keychain.userName.savePassword(userName)
            try Keychain.userPassword.savePassword(password)
            try Keychain.userAccessToken.savePassword(accessToken)
        } catch {
            self.showAlert(forError: error, andMessage: nil, withViewController: controller)
        }
        controller.presentingViewController?.dismiss(animated: true)
        self.delegate?.signUpControllerDidFinish(self)
    }
    
    fileprivate func signUpViewController(_ controller: SignUpViewController, didFailSignUpWithError error: Error, message: String?) {
        self.showAlert(forError: error, andMessage: message, withViewController: controller)
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
        self.showAlert(forError: error, andMessage: message, withViewController: controller)
    }
    
}
