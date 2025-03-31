//
//  ForgotPasswordViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol ForgotPasswordViewControllerDelegate {

    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForEmail email: String)

    func forgotPasswordViewController(_ controller: ForgotPasswordViewController, willChangePasswordForUserName userName: String)

}

class ForgotPasswordViewController: FormularViewController {

    weak var signUpController: SignUpController?

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
        setupButtons()
        setupLocalization()
    }
    
    fileprivate func setupButtons() {
        makeBlue(button: resetPasswordButton)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = SignUpTranslation.ForgotPasswordView.Title.String
        infoLabel.text = SignUpTranslation.ForgotPasswordView.InfoLabel.Text.String
        emailLabel.text = SignUpTranslation.Common.Email.String
        userNameLabel.text = SignUpTranslation.Common.UserName.String
        resetPasswordButton.setTitle(SignUpTranslation.ForgotPasswordView.ResetPasswordButton.Title.String, for: .normal)
    }
    
    @IBAction func resetPasswordButtonTapped(_ sender: Any) {
        if let email = emailTextField.text {
            if !email.isEmpty {
                signUpController?.forgotPasswordViewController(self, willChangePasswordForEmail: email)
                return
            }
        }
        if let userName = userNameTextField.text {
            if !userName.isEmpty {
                signUpController?.forgotPasswordViewController(self, willChangePasswordForUserName: userName)
                return
            }
        }
    }

}
