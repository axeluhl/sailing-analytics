//
//  RegattaMarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaMarkViewController: SessionViewController {
    
    @IBOutlet weak var markNameLabel: UILabel!
    
    weak var markCheckIn: MarkCheckIn!
    weak var regattaCoreDataManager: CoreDataManager!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
        update()
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
        return MarkSessionController(checkIn: self.markCheckIn, coreDataManager: self.regattaCoreDataManager)
    }()
    
    fileprivate lazy var regattaMarkOptionSheet: UIAlertController = {
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

// MARK: SessionViewControllerDelegate

extension RegattaMarkViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return markCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return regattaCoreDataManager } }
    
    var optionSheet: UIAlertController { get { return regattaMarkOptionSheet } }
    
    var sessionController: SessionController { get { return markSessionController } }
    
    // MARK: - Refresh
    
    func refresh() {
        markNameLabel.text = markCheckIn.name
    }
    
}
