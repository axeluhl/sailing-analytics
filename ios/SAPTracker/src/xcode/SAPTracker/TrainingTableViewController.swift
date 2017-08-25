//
//  TrainingTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingTableViewController: UIViewController {
    
    fileprivate struct Segue {
        static let About = "About"
        static let CreateTraining = "CreateTraining"
        static let Settings = "Settings"
    }
    
    var login = true
    
    @IBOutlet weak var headerTitleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var footerTextView: UITextView!
    @IBOutlet weak var addButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if login {
            signUpController.loginWithViewController(self)
            login = false
        }
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupAddButton()
    }
    
    fileprivate func setupAddButton() {
        makeViewRoundWithShadow(addButton)
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: Any) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let logoutAction = UIAlertAction(title: "LOGOUT ACTION", style: .default) { [weak self] action in
            self?.logout()
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: Segue.Settings, sender: alertController)
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(logoutAction)
        alertController.addAction(settingsAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func logout() {
        signUpController.logoutWithViewController(self)
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == Segue.CreateTraining) {
            let createTrainingNC = segue.destination as! UINavigationController
            let createTrainingVC = createTrainingNC.viewControllers[0] as! CreateTrainingViewController
            createTrainingVC.trainingController = trainingController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var signUpController: SignUpController = {
        let signUpController = SignUpController(baseURLString: "https://ubilabstest.sapsailing.com")
        signUpController.delegate = self
        return signUpController
    }()
    
    fileprivate lazy var trainingController: TrainingController = {
        let trainingController = TrainingController(baseURLString: "https://ubilabstest.sapsailing.com")
        return trainingController
    }()
    
}

// MARK: - UITableViewDataSource

extension TrainingTableViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell()
        self.configureCell(cell: cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: IndexPath) {
//        guard let homeViewCell = cell as? HomeViewCell else { return }
//        let checkIn = fetchedResultsController.object(at: indexPath)
//        homeViewCell.eventLabel.text = checkIn.event.name
//        homeViewCell.leaderboardLabel.text = checkIn.leaderboard.name
//        homeViewCell.competitorLabel.text = checkIn.name
    }
    
}

// MARK: - UITableViewDelegate

extension TrainingTableViewController: UITableViewDelegate {
    
}

// MARK: - SignUpControllerDelegate

extension TrainingTableViewController: SignUpControllerDelegate {
    
    func signUpControllerDidFinish(_ controller: SignUpController) {
        
    }
    
    func signUpControllerDidCancel(_ controller: SignUpController) {
        navigationController?.popViewController(animated: true)
    }
    
    func signUpControllerDidLogout(_ controller: SignUpController) {
        navigationController?.popToRootViewController(animated: true)
    }
    
}
