//
//  TrainingMarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 11.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingMarkViewController: SessionViewController {
    
    weak var markCheckIn: MarkCheckIn!
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
        navigationItem.titleView = TitleView(title: markCheckIn.event.name, subtitle: markCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == Segue.Tracking) {
            let trackingNC = segue.destination as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.checkIn = markCheckIn
            trackingVC.sessionController = markSessionController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var markSessionController: MarkSessionController = {
        return MarkSessionController(checkIn: self.markCheckIn, coreDataManager: self.trainingCoreDataManager)
    }()
    
    fileprivate lazy var trainingMarkOptionSheet: UIAlertController = {
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

extension TrainingMarkViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return markCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return trainingCoreDataManager } }
    
    var optionSheet: UIAlertController { get { return trainingMarkOptionSheet } }
    
    var sessionController: SessionController { get { return markSessionController } }
    
    // MARK: - Refresh
    
    func refresh() {
        trainingNameLabel.text = markCheckIn.event.name
    }
    
}
