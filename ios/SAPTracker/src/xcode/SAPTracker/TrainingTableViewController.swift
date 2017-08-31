//
//  TrainingTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class TrainingTableViewController: UIViewController {
    
    fileprivate struct Segue {
        static let About = "About"
        static let CreateTraining = "CreateTraining"
        static let Settings = "Settings"
    }
    
    var signUpController: SignUpController!
    var userName: String!
    
    @IBOutlet var headerView: UIView!
    
    @IBOutlet weak var headerTitleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var footerTextView: UITextView!
    @IBOutlet weak var addButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupAddButton()
        setupTableView()
        setupTableViewDataSource()
        setupTableViewHeader()
    }
    
    fileprivate func setupAddButton() {
        makeViewRoundWithShadow(addButton)
    }
    
    fileprivate func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    fileprivate func setupTableViewDataSource() {
        do {
            try fetchedResultsController.performFetch()
        } catch {
            logError(name: "\(#function)", error: error)
        }
    }
    
    fileprivate func setupTableViewHeader() {
        if fetchedResultsController.sections?[0].numberOfObjects ?? 0 == 0 {
            tableView.tableHeaderView = nil
        } else {
            tableView.tableHeaderView = headerView
        }
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
            createTrainingVC.trainingCoreDataManager = trainingCoreDataManager;
            createTrainingVC.trainingController = trainingController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var trainingController: TrainingController = {
        let trainingController = TrainingController(baseURLString: "https://ubilabstest.sapsailing.com")
        return trainingController
    }()
    
    fileprivate lazy var trainingCoreDataManager: TrainingCoreDataManager = {
        let trainingCoreDataManager = TrainingCoreDataManager(name: self.userName)
        return trainingCoreDataManager
    }()
    
    fileprivate lazy var fetchedResultsController: NSFetchedResultsController<CheckIn> = {
        let fetchedResultsController = self.trainingCoreDataManager.checkInFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()
    
}

// MARK: - UITableViewDataSource

extension TrainingTableViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController.sections?[section].numberOfObjects ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell()
        self.configureCell(cell: cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: IndexPath) {
        guard let trainingTableViewCell = cell as? TrainingTableViewCell else { return }
        let checkIn = fetchedResultsController.object(at: indexPath)
        trainingTableViewCell.eventLabel.text = checkIn.event.name
        trainingTableViewCell.leaderboardLabel.text = checkIn.leaderboard.name
        trainingTableViewCell.competitorLabel.text = checkIn.name
    }
    
}

// MARK: - UITableViewDelegate

extension TrainingTableViewController: UITableViewDelegate {
    
}


// MARK: - NSFetchedResultsControllerDelegate

extension TrainingTableViewController: NSFetchedResultsControllerDelegate {
    
    func controllerWillChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        tableView.beginUpdates()
    }
    
    func controller(
        _ controller: NSFetchedResultsController<NSFetchRequestResult>,
        didChange object: Any,
        at indexPath: IndexPath?,
        for type: NSFetchedResultsChangeType,
        newIndexPath: IndexPath?)
    {
        switch type {
        case .insert:
            tableView.insertRows(at: [newIndexPath!], with: UITableViewRowAnimation.automatic)
        case .update:
            let cell = tableView.cellForRow(at: indexPath!)
            if cell != nil {
                configureCell(cell: cell!, atIndexPath: indexPath!)
                tableView.reloadRows(at: [indexPath!], with: UITableViewRowAnimation.automatic)
            }
        case .move:
            tableView.deleteRows(at: [indexPath!], with: UITableViewRowAnimation.automatic)
            tableView.insertRows(at: [newIndexPath!], with: .automatic)
        case .delete:
            tableView.deleteRows(at: [indexPath!], with: UITableViewRowAnimation.automatic)
        }
    }
    
    func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        tableView.endUpdates()
        setupTableViewHeader()
    }
    
}
