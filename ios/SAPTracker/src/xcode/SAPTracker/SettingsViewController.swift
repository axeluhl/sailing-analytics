//
//  SettingsViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class SettingsViewController: UITableViewController {
    
    @IBOutlet weak var batterySavingTitleLabel: UILabel!
    @IBOutlet weak var batterySavingDescriptionLabel: UILabel!
    @IBOutlet weak var batterySavingSwitch: UISwitch!
    @IBOutlet weak var deviceIdentifierTitleLabel: UILabel!
    @IBOutlet weak var deviceIdentifierLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupBatterySavingSwitch()
        setupBatterySavingDescriptionLabel()
        setupDeviceIdentifierLabel()
        setupLocalization()
        setupNavigationBar()
        setupTableView()
    }
    
    private func setupBatterySavingSwitch() {
        batterySavingSwitch.setOn(Preferences.batterySaving, animated: true)
    }
    
    private func setupBatterySavingDescriptionLabel() {
        batterySavingDescriptionLabel.text = String(format: Translation.SettingsView.BatterySavingDescriptionLabel.Text.String,
                                                    Preferences.batterySaving ?
                                                        BatteryManager.SendingInterval.BatterySaving :
                                                        BatteryManager.SendingInterval.Normal)
    }
    
    private func setupDeviceIdentifierLabel() {
        deviceIdentifierLabel.text = Preferences.uuid
    }
    
    private func setupLocalization() {
        navigationItem.title = Translation.SettingsView.Title.String
        batterySavingTitleLabel.text = Translation.SettingsView.BatterySavingTitleLabel.Text.String
        deviceIdentifierTitleLabel.text = Translation.SettingsView.DeviceIdentifierTitleLabel.Text.String
    }
    
    private func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    private func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    // MARK: - Actions
    
    @IBAction func batterySavingChanged(sender: UISwitch) {
        Preferences.batterySaving = sender.on
        setupBatterySavingDescriptionLabel()
    }
    
    @IBAction func doneButtonTapped(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // MARK: - UITableViewDataSource
    
    override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        switch section {
        case 0: return Translation.SettingsView.TableView.BatterySavingSection.Title.String
        case 1: return Translation.SettingsView.TableView.OtherSettingsSection.Title.String
        default: return ""
        }
    }
    
    // MARK: - UITableViewDelegate
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
}