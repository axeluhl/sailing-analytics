//
//  StartViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 09.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class StartViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }

    // MARK: - Setup

    fileprivate func setup() {
        setupNavigationBar()
    }

    fileprivate func setupNavigationBar() {
        navigationItem.title = Application.Title
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    // MARK: - Actions

    @IBAction func trainingButtonTapped(_ sender: Any) {
        signUpController.login(self)
    }

    // MARK: - Properties
    
    fileprivate lazy var signUpController: SignUpController = {
        let signUpController = SignUpController()
        signUpController.delegate = self
        return signUpController
    }()

}

// MARK: - SignUpControllerDelegate

extension StartViewController: SignUpControllerDelegate {
    
    func signUpControllerDidFinish(_ controller: SignUpController) {
        
    }
    
    func signUpControllerDidCancel(_ controller: SignUpController) {
        
    }
    
}
