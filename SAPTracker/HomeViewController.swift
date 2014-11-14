//
//  ViewController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class HomeViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, UIActionSheetDelegate, NSFetchedResultsControllerDelegate {
    
    //private var regatta: Regatta
    
    enum AlertViewTag: Int {
        case NoCameraAvailable
    }
    @IBOutlet weak var tableView: UITableView!
    var fetchedResultsController: NSFetchedResultsController?
    
    override func viewDidLoad() {
        fetchedResultsController = DataManager.sharedManager.eventsFetchedResultsController()
        fetchedResultsController!.delegate = self
        fetchedResultsController!.performFetch(nil)
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier! == "Regatta") {
            let regattaViewController = segue.destinationViewController as RegattaViewController;
            // TODO
            // regattaViewController.regatta =  selectedRegatta
        }
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
        println("\(info.numberOfObjects)")
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
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        performSegueWithIdentifier("Regatta", sender: tableView)
    }
    
    // MARK: - Button actions
    
    @IBAction func scanButtonTap(sender: AnyObject) {
        if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera)) {
            performSegueWithIdentifier("Scan", sender: sender)
        } else {
            let alertView = UIAlertView(title: "No camera available.", message: nil, delegate: self, cancelButtonTitle: "Cancel")
            alertView.tag = AlertViewTag.NoCameraAvailable.rawValue;
            alertView.show()
        }
    }
    
}

