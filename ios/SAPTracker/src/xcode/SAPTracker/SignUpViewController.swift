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
        navigationItem.title = NSLocalizedString("SignUpView.Title", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        infoLabel.text = NSLocalizedString("SignUpView.InfoLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        emailLabel.text = NSLocalizedString("SignUpView.EmailLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        userNameLabel.text = NSLocalizedString("SignUpView.UserNameLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        fullNameLabel.text = NSLocalizedString("SignUpView.FullNameLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        companyLabel.text = NSLocalizedString("SignUpView.CompanyLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        passwordLabel.text = NSLocalizedString("SignUpView.PasswordLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        passwordTextField.placeholder = NSLocalizedString("SignUpView.PasswordTextField.Placeholder", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        repeatPasswordLabel.text = NSLocalizedString("SignUpView.RepeatPasswordLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        repeatPasswordTextField.placeholder = NSLocalizedString("SignUpView.RepeatPasswordTextField.Placeholder", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        signUpButton.setTitle(NSLocalizedString("SignUpView.SignUpButton.Title", tableName: "SignUp", bundle: Bundle.main, value: "", comment: ""), for: .normal)
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
