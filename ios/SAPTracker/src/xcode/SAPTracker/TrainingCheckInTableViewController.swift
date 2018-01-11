//
//  TrainingCheckInTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class TrainingCheckInTableViewController: CheckInTableViewController {
    
    fileprivate struct Segue {
        static let CreateTraining = "CreateTraining"
    }

    @IBOutlet weak var addButton: UIButton!
    
    override func viewDidLoad() {
        delegate = self
        super.viewDidLoad()
        setup()
        subscribeForNotifications()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        titleView.setSubtitle(subtitle: signUpController.userName ?? "")
    }

    deinit {
        unsubscribeFromNotifications()
    }

    // MARK: - Setup
    
    fileprivate func setup() {
        setupAddButton()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupAddButton() {
        makeRoundWithShadow(view: addButton)
    }
    
    fileprivate func setupLocalization() {
        headerTitleLabel.text = Translation.TrainingCheckInListView.HeaderTitleLabel.Text.String
        footerTextView.text = Translation.TrainingCheckInListView.FooterTextView.Text.String
    }

    fileprivate func setupNavigationBar() {
        navigationItem.titleView = titleView
        navigationController?.navigationBar.setNeedsLayout()
    }

    // MARK: - Notifications

    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(trainingEndpointChanged(_:)),
            name: NSNotification.Name(rawValue: Preferences.NotificationType.TrainingEndpointChanged),
            object: nil
        )
    }

    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }

    @objc fileprivate func trainingEndpointChanged(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let trainingEndpoint = notification.userInfo?[Preferences.UserInfo.TrainingEndpoint] as? String else { return }
            self.signUpController = SignUpController(baseURLString: trainingEndpoint)
            self.trainingController = TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: trainingEndpoint)
        })
    }

    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: Any) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let logoutAction = UIAlertAction(title: Translation.Common.Logout.String, style: .default) { [weak self] action in
            self?.logout()
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.presentSettingsViewController()
        }
        let infoAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.presentAboutViewController()
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(logoutAction)
        alertController.addAction(settingsAction)
        alertController.addAction(infoAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }

    fileprivate func logout() {
        signUpController.logout {
            self.navigationController?.popViewController(animated: true)
        }
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

    fileprivate lazy var titleView: TitleView = {
        return TitleView(
            title: Translation.TrainingCheckInListView.Title.String,
            subtitle: self.signUpController.userName ?? ""
        )
    }()

    fileprivate lazy var signUpController: SignUpController = {
        return SignUpController(baseURLString: Preferences.trainingEndpoint)
    }()

    fileprivate lazy var trainingCheckInController: TrainingCheckInController = {
        return TrainingCheckInController(coreDataManager: self.trainingCoreDataManager)
    }()
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: Preferences.trainingEndpoint)
    }()
    
    fileprivate lazy var trainingCoreDataManager: TrainingCoreDataManager = {
        return TrainingCoreDataManager(name: "TrainingCoreData")
    }()
    
}

// MARK: - CheckInTableViewControllerDelegate

extension TrainingCheckInTableViewController: CheckInTableViewControllerDelegate {
    
    var checkInController: CheckInController { get { return trainingCheckInController } }
    
    var coreDataManager: CoreDataManager { get { return trainingCoreDataManager } }

    var isFooterViewHidden: Bool { get { return fetchedResultsController?.sections?[0].numberOfObjects ?? 0 > 0 } }

    func checkInTableViewController(_ controller: CheckInTableViewController, configureCell cell: UITableViewCell, forCheckIn checkIn: CheckIn) {
        guard let trainingCheckInTableViewCell = cell as? TrainingCheckInTableViewCell else { return }
        trainingCheckInTableViewCell.eventLabel.text = checkIn.event.name
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andCompetitorCheckIn competitorCheckIn: CompetitorCheckIn) {
        guard let trainingCompetitorVC = segue.destination as? TrainingCompetitorViewController else { return }
        trainingCompetitorVC.competitorCheckIn = competitorCheckIn
        trainingCompetitorVC.competitorCoreDataManager = trainingCoreDataManager
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andMarkCheckIn markCheckIn: MarkCheckIn) {
        guard let trainingMarkVC = segue.destination as? TrainingMarkViewController else { return }
        trainingMarkVC.markCheckIn = markCheckIn
        trainingMarkVC.markCoreDataManager = trainingCoreDataManager
    }
    
}

// MARK: - CreateTrainingViewControllerDelegate

extension TrainingCheckInTableViewController: CreateTrainingViewControllerDelegate {
    
    func createTrainingViewController(_ controller: CreateTrainingViewController, didCheckIn checkIn: CheckIn) {
        performSegue(forCheckIn: checkIn)
    }
    
}
