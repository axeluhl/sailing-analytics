//
//  TrainingCompetitorViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingCompetitorViewController: CompetitorSessionViewController {
    
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
        navigationItem.title = competitorCheckIn.event.name
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == Segue.EmbedTraining) {
            if let trainingViewController = segue.destination as? TrainingViewController {
                trainingViewController.delegate = self
                trainingViewController.trainingCheckIn = competitorCheckIn
                trainingViewController.trainingCoreDataManager = competitorCoreDataManager
                self.trainingViewController = trainingViewController
            }
        }
    }
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingCompetitorViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return competitorCheckIn } }

    var checkOutActionTitle: String { get { return Translation.TrainingView.OptionSheet.CheckOutAction.Title.String } }

    var checkOutAlertMessage: String { get { return Translation.TrainingView.CheckOutAlert.Message.String } }

    var coreDataManager: CoreDataManager { get { return competitorCoreDataManager } }
    
    var sessionController: SessionController { get { return competitorSessionController } }
    
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
        competitorViewController?.refresh(animated)
        trainingViewController?.refresh(animated)
    }
    
}

// MARK: - TrainingViewControllerDelegate

extension TrainingCompetitorViewController: TrainingViewControllerDelegate {

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
