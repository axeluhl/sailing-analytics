//
//  ViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class HomeViewController: CheckInViewController, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate {
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var scanCodeButton: UIButton!
    @IBOutlet weak var noCodeButton: UIButton!
    @IBOutlet weak var bottomLabel: PaddedLabel!
    
    private var fetchedResultsController: NSFetchedResultsController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
		
        self.setupLanguage()
        self.setupNavigationbar()
        self.setupTableViewDataSource()
        
        self.subscribeForNotifications()
        
        checkEULA(NSNotification.init(name: "", object: nil))
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // FIXME: - this is a bad implementation of core data
        DataManager.sharedManager.selectedCheckIn = nil;
    }

    deinit {
        self.unsubscribeForNotifications()
    }

    // MARK: - Setups
    
    private func setupTableViewDataSource() {
        fetchedResultsController = DataManager.sharedManager.checkInFetchedResultsController()
        fetchedResultsController!.delegate = self
        do {
            try fetchedResultsController!.performFetch()
        } catch {
            print(error)
        }
    }
    
    private func setupLanguage() {
        self.navigationItem.title = NSLocalizedString("Header", comment: "")
        self.titleLabel.text = NSLocalizedString("Your Regattas", comment: "")
        self.scanCodeButton.setTitle(NSLocalizedString("Scan Code", comment: ""), forState: .Normal)
        self.noCodeButton.setTitle(NSLocalizedString("No Code", comment: ""), forState: .Normal)
        self.bottomLabel.text = NSLocalizedString("QR found", comment: "")
    }
    
    private func setupNavigationbar() {
        let imageView = UIImageView(image: UIImage(named: "sap_logo"))
        let barButtonItem = UIBarButtonItem(customView: imageView)
        self.navigationItem.leftBarButtonItem = barButtonItem
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector: #selector(openUrl(_:)),
                                                         name: AppDelegate.NotificationType.openUrl,
                                                         object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector: #selector(HomeViewController.checkEULA(_:)),
                                                         name: UIApplicationWillEnterForegroundNotification,
                                                         object: nil)
    }
    
    private func unsubscribeForNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func checkEULA(notification: NSNotification) {
        if !Preferences.acceptedTerms() {
            let alertTitle = NSLocalizedString("EULA_title", comment: "")
            let alertMessage = NSLocalizedString("EULA_content", comment: "")
            let alertController = UIAlertController(title: alertTitle,
                                                    message: alertMessage,
                                                    preferredStyle: .Alert)
            let viewTitle = NSLocalizedString("EULA_view", comment: "")
            let viewAction = UIAlertAction(title: viewTitle,
                                           style: .Cancel,
                                           handler: { action in UIApplication.sharedApplication().openURL(URLs.EULA)
            })
            let confirmTitle = NSLocalizedString("EULA_confirm", comment: "")
            let confirmAction = UIAlertAction(title: confirmTitle,
                                              style: .Default,
                                              handler: { action in Preferences.setAcceptedTerms(true)
            })
            alertController.addAction(viewAction)
            alertController.addAction(confirmAction)
            self.presentViewController(alertController, animated: true, completion: nil)
        }
    }

    func openUrl(notification: NSNotification) {
        // TODO: implement CheckInController call
        //let url = notification.userInfo!["url"] as! String
        //checkInController!.startCheckIn(url)
    }
	
//	func _DEBUG_OPEN_URL() {
//		let url = "http://ec2-54-171-89-140.eu-west-1.compute.amazonaws.com:8888/tracking/checkin?event_id=71c7b531-fb1b-441c-b2fa-f4e9ff672d60&leaderboard_name=Ubigatta&competitor_id=9df7b4f6-611b-4be0-b028-c7b1bdd434c2"
//		let url2 = "http://ec2-54-171-89-140.eu-west-1.compute.amazonaws.com:8888/tracking/checkin?event_id=71c7b531-fb1b-441c-b2fa-f4e9ff672d60&leaderboard_name=Ubigatta&competitor_id=a5a00800-daf8-0131-89e6-60a44ce903c3"
//		checkInController!.startCheckIn(url2)
//	}
    
    // MARK: - UITableViewDataSource
    
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
        cell.textLabel?.text = checkIn.leaderBoardName
    }
    
    // MARK: - NSFetchedResultsControllerDelegate
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController, didChangeObject object: AnyObject,  atIndexPath indexPath: NSIndexPath?,
        forChangeType type: NSFetchedResultsChangeType,
        newIndexPath: NSIndexPath?) {
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
    
    // MARK: - UITableViewDelegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        DataManager.sharedManager.selectedCheckIn = (fetchedResultsController!.objectAtIndexPath(indexPath) as! CheckIn)
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        performSegueWithIdentifier("Regatta", sender: tableView)
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
		
        // Check if a camera is available
        if !UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            let alertTitle = NSLocalizedString("No camera available.", comment: "")
            let alertController = UIAlertController(title: alertTitle,
                                                    message: nil,
                                                    preferredStyle: .Alert)
            let cancelTitle = NSLocalizedString("Cancel", comment: "")
            let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
            alertController.addAction(cancelAction)
            self.presentViewController(alertController, animated: true, completion: nil)
            return
        }
        
// FIXME: - is this necessary for iOS >= 8.0
//        if (!CameraController.deviceCanReadQRCodes()) {
//            let alertView = UIAlertView(title: NSLocalizedString("Cannot read QR codes with this device.", comment: ""), message: nil, delegate: nil, cancelButtonTitle: NSLocalizedString("Cancel", comment: ""))
//            alertView.tag = AlertView.NoCameraAvailable.rawValue;
//            alertView.show()
//            return
//        }
        
        performSegueWithIdentifier("Scan", sender: sender)
    }
    
    @IBAction func noCodeButtonTap(sender: AnyObject) {
        let alertTitle = NSLocalizedString("In order to use this app you need to check-in via QR code or email link. Please contact the racing committee if you need either.", comment: "")
        let alertController = UIAlertController(title: alertTitle,
                                                message: nil,
                                                preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
        alertController.addAction(cancelAction)
        self.presentViewController(alertController, animated: true, completion: nil)
    }
    
    // MARK: - CheckInControllerDelegate
    
    func displayCheckInAlert(alertController: UIAlertController, checkInController: CheckInController) {
        self.presentViewController(alertController, animated: true, completion: nil)
    }

    func checkInSucceed(checkInController: CheckInController) {
        
    }
    
    func checkInFailed(checkInController: CheckInController) {
        
    }
    
}

