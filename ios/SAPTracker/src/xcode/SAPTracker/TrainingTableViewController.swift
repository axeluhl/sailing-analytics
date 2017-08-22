//
//  TrainingTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingTableViewController: UIViewController {
    
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
    
    // MARK: - Properties
    
    fileprivate lazy var signUpController: SignUpController = {
        let signUpController = SignUpController()
        signUpController.delegate = self
        return signUpController
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
    
}
