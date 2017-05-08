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

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }

    // MARK: - Setup

    private func setup() {
        setupButtons()
        setupNavigationBar()
    }
    
    private func setupButtons() {
        startTrackingButton.setBackgroundImage(Images.GreenHighlighted, forState: .Highlighted)
    }

    private func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: markCheckIn.event.name, subtitle: markCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }

    // MARK: - Update
    
    private func update() {
        SVProgressHUD.show()
        markSessionController.update {
            self.refresh()
            SVProgressHUD.popActivity()
        }
    }
    
    // MARK: - Refresh
    
    private func refresh() {
        markNameLabel.text = markCheckIn.name
    }

    // MARK: - Actions
    
    @IBAction func optionButtonTapped(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .Default) { (action) in
            self.performSegueWithIdentifier(Segue.Settings, sender: self)
        }
        let checkOutAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.CheckOutAction.Title.String, style: .Default) { (action) in
            self.checkOut()
        }
        let updateAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.UpdateAction.Title.String, style: .Default) { (action) -> Void in
            self.update()
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier(Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(checkOutAction)
        alertController.addAction(updateAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }

    // MARK: - Properties
    
    private lazy var markSessionController: MarkSessionController = {
        return MarkSessionController(checkIn: self.markCheckIn)
    }()

}

// MARK: SessionViewControllerDelegate

extension MarkViewController: SessionViewControllerDelegate {

    func performCheckOut() {
        markSessionController.checkOut { (withSuccess) in
            self.performCheckOutCompleted(withSuccess)
        }
    }
    
    private func performCheckOutCompleted(withSuccess: Bool) {
        CoreDataManager.sharedManager.deleteObject(markCheckIn)
        CoreDataManager.sharedManager.saveContext()
        self.navigationController!.popViewControllerAnimated(true)
    }

    func startTracking() throws {
        try markSessionController.startTracking()
    }

}