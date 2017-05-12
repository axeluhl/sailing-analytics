//
//  MarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class MarkViewController: SessionViewController {

    @IBOutlet weak var markNameLabel: UILabel!

    var markCheckIn: MarkCheckIn!

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

    // MARK: - Update
    
    fileprivate func update() {
        SVProgressHUD.show()
        markSessionController.update {
            self.refresh()
            SVProgressHUD.popActivity()
        }
    }
    
    // MARK: - Refresh
    
    fileprivate func refresh() {
        markNameLabel.text = markCheckIn.name
    }

    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { (action) in
            self.performSegue(withIdentifier: Segue.Settings, sender: self)
        }
        let checkOutAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.CheckOutAction.Title.String, style: .default) { (action) in
            self.checkOut()
        }
        let updateAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.UpdateAction.Title.String, style: .default) { (action) -> Void in
            self.update()
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { (action) -> Void in
            self.performSegue(withIdentifier: Segue.About, sender: alertController)
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
        return MarkSessionController(checkIn: self.markCheckIn)
    }()

}

// MARK: SessionViewControllerDelegate

extension MarkViewController: SessionViewControllerDelegate {

    func performCheckOut() {
        markSessionController.checkOut { (withSuccess) in
            self.performCheckOutCompleted(withSuccess: withSuccess)
        }
    }
    
    fileprivate func performCheckOutCompleted(withSuccess: Bool) {
        CoreDataManager.sharedManager.deleteObject(object: markCheckIn)
        CoreDataManager.sharedManager.saveContext()
        self.navigationController!.popViewController(animated: true)
    }

    func startTracking() throws {
        try markSessionController.startTracking()
    }

}
