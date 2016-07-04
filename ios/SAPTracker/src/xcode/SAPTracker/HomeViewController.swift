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
    
    private var fetchedResultsController: NSFetchedResultsController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupButtons()
        setupLanguage()
        setupNavigationBar()
        setupTableViewDataSource()
        subscribeForNotifications()
        checkEULA(NSNotification.init(name: "", object: nil))
    }

    deinit {
        self.unsubscribeForNotifications()
    }

    // MARK: - Setups
    
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
        fetchedResultsController = CoreDataManager.sharedManager.checkInFetchedResultsController()
        fetchedResultsController!.delegate = self
        do {
            try fetchedResultsController!.performFetch()
        } catch {
            print(error)
        }
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector: #selector(HomeViewController.checkEULA(_:)),
                                                         name: UIApplicationWillEnterForegroundNotification,
                                                         object: nil)
    }
    
    private func unsubscribeForNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func checkEULA(notification: NSNotification) {
        if !Preferences.acceptedTerms {
            let alertTitle = NSLocalizedString("EULA_title", comment: "")
            let alertMessage = NSLocalizedString("EULA_content", comment: "")
            let alertController = UIAlertController(title: alertTitle, message: alertMessage, preferredStyle: .Alert)
            let viewTitle = NSLocalizedString("EULA_view", comment: "")
            let viewAction = UIAlertAction(title: viewTitle, style: .Cancel, handler: { action in
                UIApplication.sharedApplication().openURL(URLs.EULA)
            })
            let confirmTitle = NSLocalizedString("EULA_confirm", comment: "")
            let confirmAction = UIAlertAction(title: confirmTitle, style: .Default, handler: { action in
                Preferences.acceptedTerms = true
            })
            alertController.addAction(viewAction)
            alertController.addAction(confirmAction)
            self.presentViewController(alertController, animated: true, completion: nil)
        }
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTap(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        // FIXME: - what does this code?
        //        if let popoverController = actionSheet.popoverPresentationController,
        //            let barButtonItem = sender as? UIBarButtonItem {
        //            popoverController.barButtonItem = barButtonItem
        //        }
        // FIXME: - should be a simpler solution than dispatch with magic numbers
        let settingsAction = UIAlertAction(title: NSLocalizedString("Settings", comment: ""), style: .Default) { (action) -> Void in
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(0.6 * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) { () -> Void in
                self.performSegueWithIdentifier("SettingsFromHome", sender: alertController)
            }
        }
        let aboutAction = UIAlertAction(title: NSLocalizedString("About", comment: ""), style: .Default) { (action) -> Void in
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(0.6 * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) { () -> Void in
                self.performSegueWithIdentifier("AboutFromHome", sender: alertController)
            }
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
            return
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
            let regattaVC = segue.destinationViewController as! RegattaViewController
            let indexPath = self.tableView.indexPathForSelectedRow
            regattaVC.checkIn = self.fetchedResultsController?.objectAtIndexPath(indexPath!) as? CheckIn
            self.tableView.deselectRowAtIndexPath(indexPath!, animated: true)
        }
    }
    
}

// MARK: - UITableViewDataSource

extension HomeViewController: UITableViewDataSource {

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController!.sections![section].numberOfObjects
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Regatta") as UITableViewCell!
        self.configureCell(cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        let checkIn = fetchedResultsController!.objectAtIndexPath(indexPath) as! CheckIn
        cell.textLabel?.text = checkIn.leaderboardName
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
