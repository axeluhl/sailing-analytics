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

    @IBOutlet weak var userNameLabel: UILabel!
    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var passwordLabel: UILabel!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var forgotPasswordButton: UIButton!
    @IBOutlet weak var signUpButton: UIButton!
    @IBOutlet weak var loginButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        textFields.append(contentsOf: [userNameTextField, passwordTextField])
        setup()
    }

    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        // TODO
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = SignUpTranslation.LoginView.Title.String
        userNameLabel.text = SignUpTranslation.Common.UserName.String
        passwordLabel.text = SignUpTranslation.Common.Password.String
        forgotPasswordButton.setTitle(SignUpTranslation.ForgotPasswordView.Title.String, for: .normal)
        signUpButton.setTitle(SignUpTranslation.SignUpView.Title.String, for: .normal)
        loginButton.setTitle(SignUpTranslation.LoginView.LoginButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
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
