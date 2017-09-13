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
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        startTrackingButton.setBackgroundImage(Images.GreenHighlighted, for: .highlighted)
    }
    
    fileprivate func setupLocalization() {
        startTrackingButton.setTitle(Translation.CompetitorView.StartTrackingButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: markCheckIn.event.name, subtitle: markCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
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
    
    var coreDataManager: CoreDataManager { get { return markCoreDataManager } }
    
    var sessionController: SessionController { get { return markSessionController } }
    
    func makeOptionSheet() -> UIAlertController {
        return makeMarkOptionSheet()
    }
    
    func refresh() {
        markViewController?.refresh()
        trainingViewController?.refresh()
    }
    
}

// MARK: - TrainingViewControllerDelegate

extension TrainingMarkViewController: TrainingViewControllerDelegate {
    
    func trainingViewController(_ controller: TrainingViewController, startTrackingButtonTapped sender: Any) {
        super.startTrackingButtonTapped(sender)
    }
    
}
