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
    
    fileprivate func setup() {
        setupBatterySavingSwitch()
        setupBatterySavingDescriptionLabel()
        setupDeviceIdentifierLabel()
        setupLocalization()
        setupNavigationBar()
        setupTableView()
    }
    
    fileprivate func setupBatterySavingSwitch() {
        batterySavingSwitch.setOn(Preferences.batterySaving, animated: true)
    }
    
    fileprivate func setupBatterySavingDescriptionLabel() {
        batterySavingDescriptionLabel.text = String(format: Translation.SettingsView.BatterySavingDescriptionLabel.Text.String,
                                                    Preferences.batterySaving ?
                                                        BatteryManager.SendingInterval.BatterySaving :
                                                        BatteryManager.SendingInterval.Normal)
    }
    
    fileprivate func setupDeviceIdentifierLabel() {
        deviceIdentifierLabel.text = Preferences.uuid
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = Translation.SettingsView.Title.String
        batterySavingTitleLabel.text = Translation.SettingsView.BatterySavingTitleLabel.Text.String
        deviceIdentifierTitleLabel.text = Translation.SettingsView.DeviceIdentifierTitleLabel.Text.String
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    fileprivate func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    // MARK: - Actions
    
    @IBAction func batterySavingChanged(_ sender: UISwitch) {
        Preferences.batterySaving = sender.isOn
        tableView.beginUpdates()
        setupBatterySavingDescriptionLabel()
        tableView.endUpdates()
    }
    
    @IBAction func doneButtonTapped(_ sender: AnyObject) {
        presentingViewController!.dismiss(animated: true, completion: nil)
    }
    
    // MARK: - UITableViewDataSource
    
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        switch section {
        case 0: return Translation.SettingsView.TableView.BatterySavingSection.Title.String
        case 1: return Translation.SettingsView.TableView.OtherSettingsSection.Title.String
        default: return ""
        }
    }
    
    // MARK: - UITableViewDelegate
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
}
