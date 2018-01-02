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
    
    // MARK: - Login
    
    func loginWithViewController(_ controller: UIViewController) {
        SVProgressHUD.show()
        requestManager.postHello(success: { (principal, authenticated, remembered) in
            self.loginWithViewControllerSuccess(controller)
        }) { (error, message) in
            do {
                let userName = try Keychain.userName.readPassword()
                let password = try Keychain.userPassword.readPassword()
                self.requestManager.postAccessToken(userName: userName, password: password, success: { (userName, accessToken) in
                    do {
                        try Keychain.userName.savePassword(userName)
                        try Keychain.userAccessToken.savePassword(accessToken)
                    } catch {
                        self.showAlert(forError: error, andMessage: message, withViewController: controller)
                    }
                    self.loginWithViewControllerSuccess(controller)
                }, failure: { (error, message) in
                    self.loginWithViewControllerFailure(controller)
                })
            } catch {
                self.loginWithViewControllerFailure(controller)
            }
        }
    }
    
    fileprivate func loginWithViewControllerSuccess(_ controller: UIViewController) {
        SVProgressHUD.dismiss()
        self.didFinishLoginWithViewController(controller)
    }
    
    fileprivate func loginWithViewControllerFailure(_ controller: UIViewController) {
        SVProgressHUD.dismiss()
        let storyboard = UIStoryboard(name: "SignUp", bundle: nil)
        let loginNC = storyboard.instantiateInitialViewController() as! UINavigationController
        let loginVC = loginNC.viewControllers[0] as! LoginViewController
        loginVC.signUpController = self
        controller.present(loginNC, animated: true, completion: nil)
    }
    
    // MARK: - Logout
    
    func logoutWithViewController(_ controller: UIViewController) {
        do {
            try Keychain.userName.deleteItem()
            try Keychain.userPassword.deleteItem()
            try Keychain.userAccessToken.deleteItem()
        } catch {
            showAlert(forError: error, andMessage: nil, withViewController: controller)
        }
        requestManager.postLogout(success: { [weak self] in
            self?.didLogoutWithViewController(controller)
        }) { [weak self] (error, message) in
            self?.didLogoutWithViewController(controller)
        }
    }
    
    // MARK: SignUpControllerDelegate
    
    fileprivate func didFinishLoginWithViewController(_ controller: UIViewController) {
        do {
            let userName = try Keychain.userName.readPassword()
            delegate?.signUpController(self, didFinishLoginWithUserName: userName)
        } catch {
            showAlert(forError: error, andMessage: nil, withViewController: controller)
            didCancelLoginWithViewController(controller)
        }
    }
    
    fileprivate func didCancelLoginWithViewController(_ controller: UIViewController) {
        delegate?.signUpControllerDidCancelLogin(self)
    }
    
    fileprivate func didLogoutWithViewController(_ controller: UIViewController) {
        delegate?.signUpControllerDidLogout(self)
    }

    // MARK: - Properties

    var baseURLString: String {
        get {
            return requestManager.baseURLString
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
        didCancelLoginWithViewController(controller)
    }
    
    fileprivate func loginViewController(_ controller: LoginViewController, didFinishLoginWithUserName userName: String, password: String, accessToken: String) {
        do {
            try Keychain.userName.savePassword(userName)
            try Keychain.userPassword.savePassword(password)
            try Keychain.userAccessToken.savePassword(accessToken)
        } catch {
            showAlert(forError: error, andMessage: nil, withViewController: controller)
        }
        controller.presentingViewController?.dismiss(animated: true)
        didFinishLoginWithViewController(controller)
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
        do {
            try Keychain.userName.savePassword(userName)
            try Keychain.userPassword.savePassword(password)
            try Keychain.userAccessToken.savePassword(accessToken)
        } catch {
            showAlert(forError: error, andMessage: nil, withViewController: controller)
        }
        controller.presentingViewController?.dismiss(animated: true)
        didFinishLoginWithViewController(controller)
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
