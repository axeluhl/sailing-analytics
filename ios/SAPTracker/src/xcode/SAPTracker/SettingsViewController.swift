//
//  SettingsViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class SettingsViewController: UITableViewController {
    
    @IBOutlet weak var batterySavingSwitch: UISwitch!
    @IBOutlet weak var deviceIdentifierLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupBatterySavingSwitch()
        setupDeviceIdentifierLabel()
        setupNavigationBar()
    }
    
    // MARK: - Setups
    
    private func setupBatterySavingSwitch() {
        batterySavingSwitch.setOn(Preferences.batterySaving, animated: true)
    }
    
    private func setupDeviceIdentifierLabel() {
        deviceIdentifierLabel.text = Preferences.uuid
    }
    
    private func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    // MARK: - Actions

    @IBAction func batterySavingChanged(sender: UISwitch) {
        Preferences.batterySaving = sender.on
    }
    
    @IBAction func doneButtonTapped(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
}