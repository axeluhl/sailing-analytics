//
//  ViewController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class HomeViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, UIActionSheetDelegate, NSFetchedResultsControllerDelegate, QRCodeManagerDelegate {
    
    enum AlertView: Int {
        case NoCameraAvailable
    }
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var tableViewHeight: NSLayoutConstraint!
    @IBOutlet weak var activityIndicatorView: UIActivityIndicatorView?
    var fetchedResultsController: NSFetchedResultsController?
    private var qrCodeManager: QRCodeManager?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // set QR manager, needed in case app is being open by custom URL
        qrCodeManager = QRCodeManager(delegate: self)
        
        // set up data source for list
        fetchedResultsController = DataManager.sharedManager.eventsFetchedResultsController()
        fetchedResultsController!.delegate = self
        fetchedResultsController!.performFetch(nil)
        
        // register for open custom URL events
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "openUrl:", name: AppDelegate.NotificationType.openUrl, object: nil)
        
        // add logo to top left
        let imageView = UIImageView(image: UIImage(named: "sap_logo"))
        let barButtonItem = UIBarButtonItem(customView: imageView)
        navigationItem.leftBarButtonItem = barButtonItem
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        DataManager.sharedManager.selectedEvent = nil;
    }

    func openUrl(notification: NSNotification) {
        let url = notification.userInfo!["url"] as String
        qrCodeManager!.parseUrl(url)
    }
    
    // MARK: - UIActionSheetDelegate
    
    @IBAction func showActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Feedback", "Settings", "About", "Cancel")
        actionSheet.cancelButtonIndex = 3
        actionSheet.showInView(view)
    }
    
    func actionSheet(actionSheet: UIActionSheet!, clickedButtonAtIndex buttonIndex: Int) {
        switch buttonIndex{
        case 0:
            let feedbackComposeViewController = BITHockeyManager.sharedHockeyManager().feedbackManager.feedbackComposeViewController()
            let navController = UINavigationController(rootViewController: feedbackComposeViewController)
            navController.modalPresentationStyle = UIModalPresentationStyle.FormSheet;
            presentViewController(navController, animated: true, completion: nil)
        case 1:
            performSegueWithIdentifier("Settings", sender: actionSheet)
            break
        case 2:
            performSegueWithIdentifier("About", sender: actionSheet)
            break
        default:
            break
        }
    }
    
    // MARK: - UITableViewDataSource
    func resizeTable() {
        let info = fetchedResultsController!.sections![0] as NSFetchedResultsSectionInfo
        let rows = info.numberOfObjects
        if rows < 3 {
            tableView.removeConstraint(tableViewHeight)
            let layoutConstraint = NSLayoutConstraint(item: tableView, attribute: NSLayoutAttribute.Height, relatedBy: NSLayoutRelation.Equal, toItem: nil, attribute: NSLayoutAttribute.Height, multiplier: 1.0, constant: CGFloat(22 + 44 * rows))
            tableView.addConstraint(layoutConstraint)
        }
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let info = fetchedResultsController!.sections![section] as NSFetchedResultsSectionInfo
        resizeTable()
        return info.numberOfObjects
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return "Your Regattas"
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier("Regatta") as UITableViewCell!
        configureCell(cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        let event = fetchedResultsController!.objectAtIndexPath(indexPath) as Event
        cell.textLabel?.text = event.leaderBoard?.name
    }
    
    // MARK: - NSFetchedResultsControllerDelegate
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController, didChangeObject object: AnyObject,  atIndexPath indexPath: NSIndexPath,
        forChangeType type: NSFetchedResultsChangeType,
        newIndexPath: NSIndexPath) {
            switch type {
            case .Insert:
                tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Automatic)
            case .Update:
                let cell = tableView.cellForRowAtIndexPath(indexPath)
                configureCell(cell!, atIndexPath: indexPath)
                tableView.reloadRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            case .Move:
                tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
                tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Automatic)
            case .Delete:
                tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            default:
                return
            }
    }
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        tableView.endUpdates()
    }
    
    
    // MARK: - UITableViewDelegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        DataManager.sharedManager.selectedEvent = (fetchedResultsController!.objectAtIndexPath(indexPath) as Event)
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        performSegueWithIdentifier("Regatta", sender: tableView)
    }
    
    // MARK: - Button actions
    
    @IBAction func scanButtonTap(sender: AnyObject) {
        if !UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            let alertView = UIAlertView(title: "No camera available.", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
            alertView.tag = AlertView.NoCameraAvailable.rawValue;
            alertView.show()
            return
        }
        
        if (!QRCodeManager.deviceCanReadQRCodes()) {
            let alertView = UIAlertView(title: "Cannot read QR codes with this device.", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
            alertView.tag = AlertView.NoCameraAvailable.rawValue;
            alertView.show()
            return
        }
        performSegueWithIdentifier("Scan", sender: sender)
    }
    
    // Für die Verwendung des Regatta-Trackers ist ein Checkin mit QR-Code oder ein Checkin-Link aus der E-Mail notwendig. Sollten Sie diesen nicht erhalten haben, wenden Sie sich bitte an die Wettfahrtleitung.
    
    @IBAction func noQrCodeButtonTap(sender: AnyObject) {
        let alertView = UIAlertView(title:  "In order to use this app you need to check-in via QR code or email link. Please contact the racing committee if you need either.", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
        alertView.tag = AlertView.NoCameraAvailable.rawValue;
        alertView.show()
    }
    
}

