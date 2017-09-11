//
//  TrainingCompetitorViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingCompetitorViewController: SessionViewController {
    
    weak var competitorCheckIn: CompetitorCheckIn!
    weak var trainingCoreDataManager: CoreDataManager!
    
    @IBOutlet weak var trainingNameLabel: UILabel!
    
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
        navigationItem.titleView = TitleView(title: competitorCheckIn.event.name, subtitle: competitorCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == Segue.Tracking) {
            let trackingNC = segue.destination as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.checkIn = competitorCheckIn
            trackingVC.sessionController = competitorSessionController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var competitorSessionController: CompetitorSessionController = {
        return CompetitorSessionController(checkIn: self.competitorCheckIn, coreDataManager: self.trainingCoreDataManager)
    }()
    
    fileprivate lazy var trainingCompetitorOptionSheet: UIAlertController = {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
//        if let popoverController = alertController.popoverPresentationController {
//            popoverController.barButtonItem = sender as? UIBarButtonItem
//        }
        alertController.addAction(self.actionSettings)
        alertController.addAction(self.actionCheckOut)
        alertController.addAction(self.actionUpdate)
        alertController.addAction(self.actionInfo)
        alertController.addAction(self.actionCancel)
        return alertController
    }()
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingCompetitorViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return competitorCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return trainingCoreDataManager } }
    
    var optionSheet: UIAlertController { get { return trainingCompetitorOptionSheet } }
    
    var sessionController: SessionController { get { return competitorSessionController } }
    
    // MARK: - Refresh
    
    func refresh() {
        trainingNameLabel.text = competitorCheckIn.event.name
    }
    
}
