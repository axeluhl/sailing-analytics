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

    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var userNameLabel: UILabel!
    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var companyLabel: UILabel!
    @IBOutlet weak var companyTextField: UITextField!
    @IBOutlet weak var passwordLabel: UILabel!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var repeatPasswordLabel: UILabel!
    @IBOutlet weak var repeatPasswordTextField: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
        textFields.append(contentsOf: [
            emailTextField,
            userNameTextField,
            nameTextField,
            companyTextField,
            passwordTextField,
            repeatPasswordTextField]
        )
        setup()
    }

    fileprivate func setup() {
        setupNavigationBar()
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.title = "SIGN UP"
    }

    @IBAction func loginButtonTapped(_ sender: Any) {
        
    }
    
    @IBAction func signUpButtonTapped(_ sender: Any) {
        signUpController?.signUpViewController(
            self,
            willSignUpWithUserName: userNameTextField.text!,
            email: emailTextField.text!,
            fullName: nameTextField.text!,
            company: companyTextField.text!,
            password: passwordTextField.text!
        )
    }

}
