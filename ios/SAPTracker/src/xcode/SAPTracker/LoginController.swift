//
//  LoginController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

@objc protocol LoginControllerDelegate {
    
    func loginController(_ sender: LoginController, show viewController: UIViewController)
    
}

class LoginController: NSObject {

    var delegate: LoginControllerDelegate?
    
    fileprivate var requestManager = RequestManager()

    func login() {
        let storyboard = UIStoryboard(name: "SignUp", bundle: nil)
        let controller = storyboard.instantiateInitialViewController()
        delegate?.loginController(self, show: controller!)
    }

}
