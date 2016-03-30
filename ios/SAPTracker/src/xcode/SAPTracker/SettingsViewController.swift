//
//  SettingsViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class SettingsViewController: UITableViewController {
    
    @IBOutlet weak var batterySavingSwitch: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        batterySavingSwitch.setOn(BatteryManager.sharedManager.batterySavingPreference, animated: true)
		
		// add logo to top left
		let imageView = UIImageView(image: UIImage(named: "sap_logo"))
		let barButtonItem = UIBarButtonItem(customView: imageView)
		navigationItem.leftBarButtonItem = barButtonItem
    }

    @IBAction func batterySavingChanged(sender: UISwitch) {
        BatteryManager.sharedManager.batterySavingPreference = sender.on
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
}