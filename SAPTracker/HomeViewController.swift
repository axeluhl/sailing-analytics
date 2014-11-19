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
    
    //private var regatta: Regatta
    
    enum AlertViewTag: Int {
        case NoCameraAvailable
    }
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var activityIndicatorView: UIActivityIndicatorView?
    var fetchedResultsController: NSFetchedResultsController?
    private var qrCodeManager: QRCodeManager?
    
    override func viewDidLoad() {
        qrCodeManager = QRCodeManager(delegate: self)
        
        fetchedResultsController = DataManager.sharedManager.eventsFetchedResultsController()
        fetchedResultsController!.delegate = self
        fetchedResultsController!.performFetch(nil)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        DataManager.sharedManager.selectedEvent = nil;
    }
    
    // MARK: - QR code

    func openUrl(url: NSURL) {
        println(url)
        qrCodeManager!.parseUrl(url.absoluteString!)
    }

    // MARK: - UIActionSheetDelegate
    @IBAction func showActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Settings", "About", "Cancel")
        actionSheet.cancelButtonIndex = 2
        actionSheet.showInView(self.view)
    }
    
    func actionSheet(actionSheet: UIActionSheet!, clickedButtonAtIndex buttonIndex: Int) {
        switch buttonIndex{
        case 0:
            performSegueWithIdentifier("Settings", sender: actionSheet)
            break
        case 1:
            performSegueWithIdentifier("About", sender: actionSheet)
            break
        default:
            break
        }
    }
    
    // MARK: - UITableViewDataSource
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let info = fetchedResultsController!.sections![section] as NSFetchedResultsSectionInfo
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
        cell.textLabel.text = event.leaderBoard?.name
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
                self.tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Automatic)
            case .Update:
                let cell = self.tableView.cellForRowAtIndexPath(indexPath)
                self.configureCell(cell!, atIndexPath: indexPath)
                self.tableView.reloadRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            case .Move:
                self.tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
                self.tableView.insertRowsAtIndexPaths([newIndexPath], withRowAnimation: .Automatic)
            case .Delete:
                self.tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            default:
                return
            }
    }
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        self.tableView.endUpdates()
    }
    
    
    // MARK: - UITableViewDelegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        DataManager.sharedManager.selectedEvent = (fetchedResultsController!.objectAtIndexPath(indexPath) as Event)
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        performSegueWithIdentifier("Regatta", sender: tableView)
    }
    
    // MARK: - Button actions
    
    @IBAction func scanButtonTap(sender: AnyObject) {
        if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)) {
            performSegueWithIdentifier("Scan", sender: sender)
        } else {
            let alertView = UIAlertView(title: "No camera available.", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
            alertView.tag = AlertViewTag.NoCameraAvailable.rawValue;
            alertView.show()
        }
    }
    
    // Für die Verwendung des Regatta-Trackers ist ein Checkin mit QR-Code oder ein Checkin-Link aus der E-Mail notwendig. Sollten Sie diesen nicht erhalten haben, wenden Sie sich bitte an die Wettfahrtleitung.
    
    @IBAction func noQrCodeButtonTap(sender: AnyObject) {
        let alertView = UIAlertView(title:  "In order to use this app you need to check-in via QR code or email link. Please contact the racing committee if you need either.", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
        alertView.tag = AlertViewTag.NoCameraAvailable.rawValue;
        alertView.show()
    }
}

