//
//  ViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class HomeViewController: UIViewController {
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var scanCodeButton: UIButton!
    @IBOutlet weak var noCodeButton: UIButton!
    @IBOutlet weak var infoCodeLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setups()
    }

    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        reviews()
        subscribeForNewCheckInURLNotifications()
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        unsubscribeFromNewCheckInURLNotifications()
    }
    
    // MARK: - Setups
    
    private func setups() {
        setupButtons()
        setupLanguage()
        setupNavigationBar()
        setupTableViewDataSource()
    }
    
    private func setupButtons() {
        scanCodeButton.setBackgroundImage(Images.BlueHighlighted, forState: .Highlighted)
        noCodeButton.setBackgroundImage(Images.GrayHighlighted, forState: .Highlighted)
    }
    
    private func setupLanguage() {
        navigationItem.title = NSLocalizedString("Header", comment: "")
        titleLabel.text = NSLocalizedString("Your Regattas", comment: "")
        scanCodeButton.setTitle(NSLocalizedString("Scan Code", comment: ""), forState: .Normal)
        noCodeButton.setTitle(NSLocalizedString("No Code", comment: ""), forState: .Normal)
        infoCodeLabel.text = NSLocalizedString("QR found", comment: "")
    }
    
    private func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    private func setupTableViewDataSource() {
        do {
            try fetchedResultsController.performFetch()
        } catch {
            print(error)
        }
    }
    
    // MARK: - Reviews
    
    func reviews() {
        if !Preferences.termsAccepted {
            reviewEULA()
        } else {
            reviewNewCheckIn()
        }
    }
    
    func reviewEULA() {
        let alertTitle = NSLocalizedString("EULA_title", comment: "")
        let alertMessage = NSLocalizedString("EULA_content", comment: "")
        let alertController = UIAlertController(title: alertTitle, message: alertMessage, preferredStyle: .Alert)
        let viewTitle = NSLocalizedString("EULA_view", comment: "")
        let viewAction = UIAlertAction(title: viewTitle, style: .Cancel, handler: { action in
            UIApplication.sharedApplication().openURL(URLs.EULA)
            self.reviewEULA() // Reopen alert again until user confirms
        })
        let confirmTitle = NSLocalizedString("EULA_confirm", comment: "")
        let confirmAction = UIAlertAction(title: confirmTitle, style: .Default, handler: { action in
            Preferences.termsAccepted = true
            self.reviewNewCheckIn()
        })
        alertController.addAction(viewAction)
        alertController.addAction(confirmAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    func reviewNewCheckIn() {
        guard Preferences.termsAccepted else { return }
        guard let urlString = Preferences.newCheckInURL else { return }
        guard let regattaData = RegattaData(urlString: urlString) else { return }
        checkInController.checkIn(regattaData)
    }
    
    // MARK: - Notifications
    
    private func subscribeForNewCheckInURLNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector: #selector(HomeViewController.newCheckInURLNotification(_:)),
                                                         name: Preferences.NotificationType.NewCheckInURLChanged,
                                                         object: nil)
    }
    
    private func unsubscribeFromNewCheckInURLNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func newCheckInURLNotification(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.reviewNewCheckIn()
        })
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTap(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: NSLocalizedString("Settings", comment: ""), style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier("SettingsFromHome", sender: alertController)
        }
        let aboutAction = UIAlertAction(title: NSLocalizedString("About", comment: ""), style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier("AboutFromHome", sender: alertController)
        }
        let cancelAction = UIAlertAction(title: NSLocalizedString("Cancel", comment: ""), style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func scanButtonTap(sender: AnyObject) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            performSegueWithIdentifier("Scan", sender: sender)
        } else {
            let alertTitle = NSLocalizedString("No camera available.", comment: "")
            let alertController = UIAlertController(title: alertTitle, message: nil, preferredStyle: .Alert)
            let cancelTitle = NSLocalizedString("Cancel", comment: "")
            let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
            alertController.addAction(cancelAction)
            presentViewController(alertController, animated: true, completion: nil)
        }
    }
    
    @IBAction func noCodeButtonTap(sender: AnyObject) {
        let alertTitle = NSLocalizedString("In order to use this app you need to check-in via QR code or email link. Please contact the racing committee if you need either.", comment: "")
        let alertController = UIAlertController(title: alertTitle, message: nil, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Segues
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.destinationViewController.isKindOfClass(RegattaViewController)) {
            guard let regattaVC = segue.destinationViewController as? RegattaViewController else { return }
            guard let indexPath = tableView.indexPathForSelectedRow else { return }
            guard let regatta = fetchedResultsController.objectAtIndexPath(indexPath) as? Regatta else { return }
            regattaVC.regatta = regatta
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    // MARK: - Properties
    
    lazy var checkInController: CheckInController = {
        let checkInController = CheckInController()
        checkInController.delegate = self
        return checkInController
    }()
    
    lazy var fetchedResultsController: NSFetchedResultsController = {
        let fetchedResultsController = CoreDataManager.sharedManager.regattaFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()
    
}

// MARK: - UITableViewDataSource

extension HomeViewController: UITableViewDataSource {

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController.sections?[section].numberOfObjects ?? 0
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Regatta") ?? UITableViewCell()
        self.configureCell(cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        guard let regatta = fetchedResultsController.objectAtIndexPath(indexPath) as? Regatta else { return }
        cell.textLabel?.text = regatta.leaderboard.name
    }

}

// MARK: - UITableViewDelegate

extension HomeViewController: UITableViewDelegate {

    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 74
    }

}

// MARK: - NSFetchedResultsControllerDelegate

extension HomeViewController: NSFetchedResultsControllerDelegate {
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController,
                    didChangeObject object: AnyObject,
                                    atIndexPath indexPath: NSIndexPath?,
                                                forChangeType type: NSFetchedResultsChangeType,
                                                              newIndexPath: NSIndexPath?)
    {
        switch type {
        case .Insert:
            tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
        case .Update:
            let cell = tableView.cellForRowAtIndexPath(indexPath!)
            if cell != nil {
                configureCell(cell!, atIndexPath: indexPath!)
                tableView.reloadRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
            }
        case .Move:
            tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
            tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: .Automatic)
        case .Delete:
            tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
        }
    }
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        tableView.endUpdates()
    }
    
}

// MARK: - CheckInControllerDelegate

extension HomeViewController: CheckInControllerDelegate {

    func showCheckInAlert(sender: CheckInController, alertController: UIAlertController) {
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    func checkInDidEnd(sender: CheckInController, withSuccess succeed: Bool) {
        Preferences.newCheckInURL = nil
    }

}
