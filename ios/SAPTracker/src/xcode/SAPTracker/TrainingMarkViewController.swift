//
//  TrainingMarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 11.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingMarkViewController: MarkSessionViewController {
    
    struct Segue {
        static let EmbedTraining = "EmbedTraining"
    }
    
    weak var trainingViewController: TrainingViewController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh(false)
    }

    // MARK: - Setup

    fileprivate func setup() {
        setupNavigationBar()
    }

    fileprivate func setupNavigationBar() {
        navigationItem.title = markCheckIn.event.name
    }

    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == Segue.EmbedTraining) {
            if let trainingViewController = segue.destination as? TrainingViewController {
                trainingViewController.delegate = self
                trainingViewController.trainingCheckIn = markCheckIn
                trainingViewController.trainingCoreDataManager = markCoreDataManager
                self.trainingViewController = trainingViewController
            }
        }
    }
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingMarkViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return markCheckIn } }

    var checkOutActionTitle: String { get { return Translation.TrainingView.OptionSheet.CheckOutAction.Title.String } }

    var checkOutAlertMessage: String { get { return Translation.TrainingView.CheckOutAlert.Message.String } }

    var coreDataManager: CoreDataManager { get { return markCoreDataManager } }
    
    var sessionController: SessionController { get { return markSessionController } }
    
    func makeOptionSheet() -> UIAlertController {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = self.optionButton
        }
        alertController.addAction(self.makeActionCheckOut())
        alertController.addAction(self.makeActionSettings())
        alertController.addAction(self.makeActionInfo())
        alertController.addAction(self.makeActionCancel())
        return alertController
    }

    func refresh(_ animated: Bool) {
        markViewController?.refresh(animated)
        trainingViewController?.refresh(animated)
    }
    
}

// MARK: - TrainingViewControllerDelegate

extension TrainingMarkViewController: TrainingViewControllerDelegate {

    func trainingViewController(_ controller: TrainingViewController, refreshButtonTapped sender: Any) {
        super.refreshButtonTapped(sender)
    }

    func trainingViewController(_ controller: TrainingViewController, startTrackingButtonTapped sender: Any) {
        super.startTrackingButtonTapped(sender)
    }
    
    func trainingViewController(_ controller: TrainingViewController, leaderboardButtonTapped sender: Any) {
        super.leaderboardButtonTapped(sender)
    }
    
    func trainingViewControllerDidFinishTraining(_ controller: TrainingViewController) {
        updatePessimistic()
    }
    
    func trainingViewControllerDidReactivateTraining(_ controller: TrainingViewController) {
        updatePessimistic()
    }
    
}
