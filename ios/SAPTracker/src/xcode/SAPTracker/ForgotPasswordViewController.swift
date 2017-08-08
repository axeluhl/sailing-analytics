//
//  ForgotPasswordViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol ForgotPasswordViewControllerDelegate {

    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePassword password: String, forEmail email: String)

}

class ForgotPasswordViewController: FormularViewController {

    var signUpController: SignUpController?

    @IBOutlet weak var infoLabel: UILabel!
    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var userNameLabel: UILabel!
    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var resetPasswordButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()
        textFields.append(contentsOf: [emailTextField, userNameTextField])
        setup()
    }

    fileprivate func setup() {
        setupNavigationBar()
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.title = "FORGOT PASSWORD"
    }

    @IBAction func resetPasswordButtonTapped(_ sender: Any) {
    }

}
