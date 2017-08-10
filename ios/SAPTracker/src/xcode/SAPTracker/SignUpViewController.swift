//
//  SignUpViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol SignUpViewControllerDelegate {
    
    func signUpViewController(
        _ controller: SignUpViewController,
        willSignUpWithUserName userName: String,
        email: String,
        fullName: String,
        company: String,
        password: String
    )
    
}

class SignUpViewController: FormularViewController {

    var signUpController: SignUpController?

    @IBOutlet weak var infoLabel: UILabel!
    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var userNameLabel: UILabel!
    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var fullNameLabel: UILabel!
    @IBOutlet weak var fullNameTextField: UITextField!
    @IBOutlet weak var companyLabel: UILabel!
    @IBOutlet weak var companyTextField: UITextField!
    @IBOutlet weak var passwordLabel: UILabel!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var repeatPasswordLabel: UILabel!
    @IBOutlet weak var repeatPasswordTextField: UITextField!
    @IBOutlet weak var signUpButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()
        textFields.append(contentsOf: [
            emailTextField,
            userNameTextField,
            fullNameTextField,
            companyTextField,
            passwordTextField,
            repeatPasswordTextField]
        )
        setup()
    }

    fileprivate func setup() {
        setupLocalization()
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = SignUpTranslation.SignUpView.Title.String
        infoLabel.text = SignUpTranslation.SignUpView.InfoLabel.Text.String
        emailLabel.text = SignUpTranslation.Common.Email.String
        userNameLabel.text = SignUpTranslation.Common.UserName.String
        fullNameLabel.text = SignUpTranslation.SignUpView.FullNameLabel.Text.String
        companyLabel.text = SignUpTranslation.SignUpView.CompanyLabel.Text.String
        passwordLabel.text = SignUpTranslation.Common.Password.String
        passwordTextField.placeholder = SignUpTranslation.SignUpView.PasswordTextField.Placeholder.String
        repeatPasswordLabel.text = SignUpTranslation.SignUpView.RepeatPasswordLabel.Text.String
        repeatPasswordTextField.placeholder = SignUpTranslation.SignUpView.RepeatPasswordTextField.Placeholder.String
        signUpButton.setTitle(SignUpTranslation.SignUpView.SignUpButton.Title.String, for: .normal)
    }
    
    @IBAction func signUpButtonTapped(_ sender: Any) {
        signUpController?.signUpViewController(
            self,
            willSignUpWithUserName: userNameTextField.text!,
            email: emailTextField.text!,
            fullName: fullNameTextField.text!,
            company: companyTextField.text!,
            password: passwordTextField.text!
        )
    }

}
