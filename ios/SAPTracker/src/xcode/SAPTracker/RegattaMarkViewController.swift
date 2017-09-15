//
//  RegattaMarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaMarkViewController: MarkSessionViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
        update()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh(false)
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        makeGreen(button: startTrackingButton)
    }
    
    fileprivate func setupLocalization() {
        startTrackingButton.setTitle(Translation.CompetitorView.StartTrackingButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: markCheckIn.event.name, subtitle: markCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
}

// MARK: SessionViewControllerDelegate

extension RegattaMarkViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return markCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return markCoreDataManager } }
    
    var sessionController: SessionController { get { return markSessionController } }
    
    func makeOptionSheet() -> UIAlertController {
        return makeMarkOptionSheet()
    }
    
    func refresh(_ animated: Bool) {
        markViewController?.refresh(animated)
    }
    
}
