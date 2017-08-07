//
//  SignUpController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

@objc protocol SignUpControllerDelegate {
    
    func signUpController(_ sender: SignUpController, show viewController: UIViewController)

}

class SignUpController: NSObject {

    var delegate: SignUpControllerDelegate?
    
    fileprivate let requestManager = SignUpRequestManager(baseURLString: "https://dev.sapsailing.com")

    func login() {
        let storyboard = UIStoryboard(name: "SignUp", bundle: nil)
        let loginNC = storyboard.instantiateInitialViewController() as! UINavigationController
        let loginVC = loginNC.viewControllers[0] as! LoginViewController
        loginVC.signUpController = self
        delegate?.signUpController(self, show: loginNC)
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
            
        }) { (error) in
            
        }
    }

}

// MARK: - ForgotPasswordViewControllerDelegate

extension SignUpController: ForgotPasswordViewControllerDelegate {

    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePassword password: String, forEmail email: String) {
        
    }

}
