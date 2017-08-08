//
//  SignUpController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

@objc protocol SignUpControllerDelegate {

    func signUpControllerDidFinish(_ controller: SignUpController)

    func signUpControllerDidCancel(_ controller: SignUpController)

}

class SignUpController: NSObject {

    var delegate: SignUpControllerDelegate?
    
    fileprivate let requestManager = SignUpRequestManager(baseURLString: "https://dev.sapsailing.com")

    func login(_ sender: UIViewController) {
        let storyboard = UIStoryboard(name: "SignUp", bundle: nil)
        let loginNC = storyboard.instantiateInitialViewController() as! UINavigationController
        let loginVC = loginNC.viewControllers[0] as! LoginViewController
        loginVC.signUpController = self
        sender.present(loginNC, animated: true, completion: nil)
    }

}

// MARK: - LoginViewControllerDelegate

extension SignUpController: LoginViewControllerDelegate {

    func loginViewController(_ controller: LoginViewController, willLoginWithUserName userName: String, password: String) {
        
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
        requestManager.postUser(userName: userName, email: email, fullName: fullName, company: company, password: password, success: {
            self.delegate?.signUpControllerDidFinish(self)
        }) { (error, message) in
            let alertController = UIAlertController.init(title: error.localizedDescription, message: message, preferredStyle: .alert)
            let okAction = UIAlertAction.init(title: Translation.Common.OK.String, style: .cancel, handler: nil)
            alertController.addAction(okAction)
            controller.present(alertController, animated: true, completion: nil)
        }
    }

}

// MARK: - ForgotPasswordViewControllerDelegate

extension SignUpController: ForgotPasswordViewControllerDelegate {

    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForEmail email: String) {
        
    }

    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForUserName userName: String) {
        
    }
    
}
