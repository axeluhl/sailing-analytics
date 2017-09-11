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
    weak var coreDataManager: CoreDataManager!
    
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
    
    // MARK: - Update
    
    fileprivate func update() {
        competitorSessionController.update { [weak self] in
            self?.refresh()
        }
    }
    
    // MARK: - Refresh
    
    fileprivate func refresh() {
        trainingNameLabel.text = competitorCheckIn.event.name
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: Segue.Settings, sender: self)
        }
        let checkOutAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.CheckOutAction.Title.String, style: .default) { [weak self] action in
            self?.checkOut()
        }
        let updateAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.UpdateAction.Title.String, style: .default) { [weak self] action in
            self?.update()
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(checkOutAction)
        alertController.addAction(updateAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
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
        return CompetitorSessionController(checkIn: self.competitorCheckIn, coreDataManager: self.coreDataManager)
    }()
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingCompetitorViewController: SessionViewControllerDelegate {
    
    func performCheckOut() {
        competitorSessionController.checkOut { (withSuccess) in
            self.performCheckOutCompleted(withSuccess: withSuccess)
        }
    }
    
    fileprivate func performCheckOutCompleted(withSuccess: Bool) {
        coreDataManager.deleteObject(object: competitorCheckIn)
        coreDataManager.saveContext()
        navigationController?.popViewController(animated: true)
    }
    
    func startTracking() throws {
        try competitorSessionController.startTracking()
    }
    
}
