//
//  StartViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 09.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class StartViewController: UIViewController {
    
    fileprivate struct Segue {
        static let Regatta = "Regatta"
        static let Training = "Training"
    }
    
    var userName: String?
    
    @IBOutlet weak var regattaButton: UIButton!
    @IBOutlet weak var trainingButton: UIButton!
    
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
        if (userName == nil) {
            signUpController.loginWithViewController(self)
        } else {
            performSegue(withIdentifier: Segue.Training, sender: self)
        }
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == Segue.Training) {
            guard let trainingTVC = segue.destination as? TrainingTableViewController else { return }
            trainingTVC.userName = userName
            trainingTVC.signUpController = signUpController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var signUpController: SignUpController = {
        let signUpController = SignUpController(baseURLString: "https://ubilabstest.sapsailing.com")
        signUpController.delegate = self
        return signUpController
    }()
    
}

// MARK: - SignUpControllerDelegate

extension StartViewController: SignUpControllerDelegate {
    
    func signUpController(_ controller: SignUpController, didFinishLoginWithUserName userName: String) {
        self.userName = userName
        performSegue(withIdentifier: Segue.Training, sender: self)
    }
    
    func signUpControllerDidCancelLogin(_ controller: SignUpController) {
        userName = nil
    }
    
    func signUpControllerDidLogout(_ controller: SignUpController) {
        userName = nil
    }
    
}
