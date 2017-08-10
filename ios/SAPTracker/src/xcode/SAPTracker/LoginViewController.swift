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
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = NSLocalizedString("LoginView.Title", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        userNameLabel.text = NSLocalizedString("LoginView.UserNameLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        passwordLabel.text = NSLocalizedString("LoginView.PasswordLabel.Text", tableName: "SignUp", bundle: Bundle.main, value: "", comment: "")
        forgotPasswordButton.setTitle(NSLocalizedString("LoginView.ForgotPasswordButton.Title", tableName: "SignUp", bundle: Bundle.main, value: "", comment: ""), for: .normal)
        signUpButton.setTitle(NSLocalizedString("SignUpView.Title", tableName: "SignUp", bundle: Bundle.main, value: "", comment: ""), for: .normal)
        loginButton.setTitle(NSLocalizedString("LoginView.LoginButton.Title", tableName: "SignUp", bundle: Bundle.main, value: "", comment: ""), for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }

    // MARK: - Actions
    
    @IBAction func cancelButtonTapped(_ sender: Any) {
        presentingViewController!.dismiss(animated: true, completion: nil)
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
        if let signUpVC = segue.destination as? SignUpViewController {
            signUpVC.signUpController = signUpController
        }
        if let forgotPasswordVC = segue.destination as? ForgotPasswordViewController {
            forgotPasswordVC.signUpController = signUpController
        }
    }

}
