//
//  ViewController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit

class HomeViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    //private var regatta: Regatta
    
    enum AlertViewTag: Int {
        case NoCameraAvailable
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier! == "Regatta") {
            let regattaViewController = segue.destinationViewController as RegattaViewController;
            // TODO
            // regattaViewController.regatta =  selectedRegatta
        }
    }
    
    // MARK: - UITableViewDataSource
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // TODO
        return 1
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier("Regatta") as UITableViewCell!
        // TODO
        cell.textLabel.text = "Kieler Wocher 49er"
        return cell
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return "Your Regattas"
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
            let alertView = UIAlertView(title: "SAP Tracker", message: "No camera available.", delegate: self, cancelButtonTitle: "Cancel")
            alertView.tag = AlertViewTag.NoCameraAvailable.rawValue;
            alertView.alertViewStyle = .Default
            alertView.show()
            
        }
    }
    
}

