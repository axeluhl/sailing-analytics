//
//  SettingsViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class SettingsViewController: UITableViewController, UITableViewDelegate {
    
    @IBOutlet weak var batterySavingSwitch: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        batterySavingSwitch.setOn(BatteryManager.sharedManager.batterySavingPreference, animated: true)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        updateCheckmarks()
    }

    @IBAction func batterySavingChanged(sender: UISwitch) {
        BatteryManager.sharedManager.batterySavingPreference = sender.on
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, nil)
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let cell = tableView.cellForRowAtIndexPath(indexPath)
        if indexPath.section == 1 && cell!.accessoryType == UITableViewCellAccessoryType.None {
            if cell!.reuseIdentifier == "Magnetic Heading" {
                LocationManager.sharedManager.headingPreference = LocationManager.Heading.Magnetic.rawValue
            } else if cell!.reuseIdentifier == "True Heading" {
                LocationManager.sharedManager.headingPreference = LocationManager.Heading.True.rawValue
            }
            updateCheckmarks()
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    private func updateCheckmarks() {
        let magneticIndexPath = NSIndexPath(forItem: 0, inSection: 1)
        let magneticCell = tableView.cellForRowAtIndexPath(magneticIndexPath)
        let trueIndexPath = NSIndexPath(forItem: 1, inSection: 1)
        let trueCell = tableView.cellForRowAtIndexPath(trueIndexPath)
        if LocationManager.sharedManager.headingPreference == LocationManager.Heading.True.rawValue {
            trueCell!.accessoryType = UITableViewCellAccessoryType.Checkmark
            magneticCell!.accessoryType = UITableViewCellAccessoryType.None
        } else {
            trueCell!.accessoryType = UITableViewCellAccessoryType.None
            magneticCell!.accessoryType = UITableViewCellAccessoryType.Checkmark
        }
    }
}