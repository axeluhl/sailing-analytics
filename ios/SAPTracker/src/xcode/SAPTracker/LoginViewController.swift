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

    @IBOutlet weak var loginLabel: UILabel!
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
        setupNavigationBar()
    }

    fileprivate func setupNavigationBar() {
        navigationItem.title = "LOGIN"
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }

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
    }

}
