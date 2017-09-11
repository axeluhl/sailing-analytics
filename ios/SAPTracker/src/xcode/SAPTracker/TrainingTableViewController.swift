//
//  TrainingTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class TrainingTableViewController: CheckInTableViewController {
    
    fileprivate struct Segue {
        static let About = "About"
        static let CreateTraining = "CreateTraining"
        static let Settings = "Settings"
    }
    
    var signUpController: SignUpController!
    var userName: String!
    
    @IBOutlet weak var addButton: UIButton!
    
    override func viewDidLoad() {
        delegate = self
        super.viewDidLoad()
        setup()
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
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == Segue.CreateTraining) {
            guard let createTrainingNC = segue.destination as? UINavigationController else { return }
            guard let createTrainingVC = createTrainingNC.viewControllers[0] as? CreateTrainingViewController else { return }
            createTrainingVC.delegate = self
            createTrainingVC.trainingCoreDataManager = trainingCoreDataManager;
            createTrainingVC.trainingController = trainingController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var trainingCheckInController: TrainingCheckInController = {
        return TrainingCheckInController(coreDataManager: self.trainingCoreDataManager)
    }()
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: "https://ubilabstest.sapsailing.com")
    }()
    
    fileprivate lazy var trainingCoreDataManager: TrainingCoreDataManager = {
        return TrainingCoreDataManager(name: self.userName)
    }()
    
}

// MARK: - CheckInTableViewControllerDelegate

extension TrainingTableViewController: CheckInTableViewControllerDelegate {
    
    var checkInController: CheckInController {
        get {
            return trainingCheckInController
        }
    }
    
    var coreDataManager: CoreDataManager {
        get {
            return trainingCoreDataManager
        }
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, configureCell cell: UITableViewCell, forCheckIn checkIn: CheckIn) {
        guard let trainingTableViewCell = cell as? TrainingTableViewCell else { return }
        trainingTableViewCell.eventLabel.text = checkIn.event.name
        trainingTableViewCell.leaderboardLabel.text = checkIn.leaderboard.name
        trainingTableViewCell.competitorLabel.text = checkIn.name
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andCompetitorCheckIn checkIn: CompetitorCheckIn) {
        
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andMarkCheckIn checkIn: MarkCheckIn) {
        
    }
    
}

// MARK: - CreateTrainingViewControllerDelegate

extension TrainingTableViewController: CreateTrainingViewControllerDelegate {
    
    func createTrainingViewController(_ controller: CreateTrainingViewController, didCheckIn checkIn: CheckIn) {
        performSegue(forCheckIn: checkIn)
    }
    
}
