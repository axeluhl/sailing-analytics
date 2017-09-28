//
//  SignUpViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum SignUpViewError: Error {
    case userNameIsToShort
    case passwordIsToShort
    case passwordsNotEqual
}

extension SignUpViewError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .userNameIsToShort:
            return SignUpTranslation.SignUpViewError.UserNameIsToShort.String
        case .passwordIsToShort:
            return SignUpTranslation.SignUpViewError.PasswordIsToShort.String
        case .passwordsNotEqual:
            return SignUpTranslation.SignUpViewError.PasswordsNotEqual.String
        }
    }
}

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
    
    weak var signUpController: SignUpController?
    
    @IBOutlet weak var infoLabel: UILabel!
    @IBOutlet weak var errorLabel: UILabel!
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
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
    }
    
    fileprivate func setupButtons() {
        makeBlue(button: signUpButton)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = SignUpTranslation.SignUpView.Title.String
        infoLabel.text = SignUpTranslation.SignUpView.InfoLabel.Text.String
        errorLabel.text = ""
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
    
    // MARK: - Actions
    
    @IBAction func signUpButtonTapped(_ sender: Any) {
        errorLabel.text = ""
        do {
            try validateTextFields()
            signUpController?.signUpViewController(
                self,
                willSignUpWithUserName: userNameTextField.text!,
                email: emailTextField.text!,
                fullName: fullNameTextField.text!,
                company: companyTextField.text!,
                password: passwordTextField.text!
            )
        } catch {
            errorLabel.text = error.localizedDescription
            let alertController = UIAlertController.init(title: errorLabel.text, message: nil, preferredStyle: .alert)
            let okAction = UIAlertAction.init(title: Translation.Common.OK.String, style: .cancel)
            alertController.addAction(okAction)
            self.present(alertController, animated: true)
        }
        UIView.animate(withDuration: 0.5) {
            self.view.layoutIfNeeded()
        }
    }
    
    // MARK: - Validation
    
    fileprivate func validateTextFields() throws {
        
        // User name
        if let userName = userNameTextField.text {
            if userName.characters.count < 3 {
                throw SignUpViewError.userNameIsToShort
            }
        } else {
            throw SignUpViewError.userNameIsToShort
        }
        
        // Password
        if let password = passwordTextField.text {
            if password.characters.count < 5 {
                throw SignUpViewError.passwordIsToShort
            }
        } else {
            throw SignUpViewError.passwordIsToShort
        }
        
        // Repeat password
        if passwordTextField.text != repeatPasswordTextField.text {
            throw SignUpViewError.passwordsNotEqual
        }
    }
    
    // MARK: - UITextFieldDelegate
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        errorLabel.text = ""
        do {
            try validateTextFields()
        } catch {
            errorLabel.text = error.localizedDescription
        }
        textField.layoutIfNeeded()
        UIView.animate(withDuration: 0.5) {
            self.view.layoutIfNeeded()
        }
    }
    
}
