//
//  LoginViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol LoginViewControllerDelegate {
    
    func loginViewController(_ controller: LoginViewController, willLoginWithUserName userName: String, password: String)
    
    func loginViewControllerWillCancel(_ controller: LoginViewController)
    
}

class LoginViewController: FormularViewController {

    var signUpController: SignUpController?

    @IBOutlet var endpointLabel: UILabel!
    @IBOutlet var endpointValueLabel: UILabel!
    @IBOutlet var userNameLabel: UILabel!
    @IBOutlet var userNameTextField: UITextField!
    @IBOutlet var passwordLabel: UILabel!
    @IBOutlet var passwordTextField: UITextField!
    @IBOutlet var forgotPasswordButton: UIButton!
    @IBOutlet var signUpButton: UIButton!
    @IBOutlet var loginButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        textFields.append(contentsOf: [userNameTextField, passwordTextField])
        setup()
    }

    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
        setupTextFields()
    }
    
    fileprivate func setupButtons() {
        makeGray(button: signUpButton)
        makeBlue(button: loginButton)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = SignUpTranslation.LoginView.Title.String
        endpointLabel.text = SignUpTranslation.Common.Server.String
        endpointValueLabel.text = signUpController?.baseURLString
        userNameLabel.text = SignUpTranslation.Common.UserName.String
        passwordLabel.text = SignUpTranslation.Common.Password.String
        forgotPasswordButton.setTitle(SignUpTranslation.ForgotPasswordView.Title.String, for: .normal)
        signUpButton.setTitle(SignUpTranslation.SignUpView.Title.String, for: .normal)
        loginButton.setTitle(SignUpTranslation.LoginView.LoginButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }

    fileprivate func setupTextFields() {
        userNameTextField.text = signUpController?.userName
        passwordTextField.text = signUpController?.userPassword
    }

    // MARK: - Actions
    
    @IBAction func cancelButtonTapped(_ sender: Any) {
        signUpController?.loginViewControllerWillCancel(self)
    }
    
    @IBAction func loginButtonTapped(_ sender: Any) {
        signUpController?.loginViewController(
            self,
            willLoginWithUserName: userNameTextField.text!,
            password: passwordTextField.text!
        )
    }

    // MARK: - Segues

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if let signUpVC = segue.destination as? SignUpViewController {
            signUpVC.signUpController = signUpController
        }
        if let forgotPasswordVC = segue.destination as? ForgotPasswordViewController {
            forgotPasswordVC.signUpController = signUpController
        }
    }

}
