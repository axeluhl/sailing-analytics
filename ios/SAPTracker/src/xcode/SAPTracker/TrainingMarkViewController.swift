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
    
    // MARK: - Update
    
    fileprivate func update() {
        markSessionController.update { [weak self] in
            self?.refresh()
        }
    }
    
    // MARK: - Refresh
    
    fileprivate func refresh() {
        trainingNameLabel.text = markCheckIn.event.name
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
            trackingVC.checkIn = markCheckIn
            trackingVC.sessionController = markSessionController
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var markSessionController: MarkSessionController = {
        return MarkSessionController(checkIn: self.markCheckIn, coreDataManager: self.trainingCoreDataManager)
    }()
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingMarkViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return markCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return trainingCoreDataManager } }
    
    var sessionController: SessionController { get { return markSessionController } }
    
}
