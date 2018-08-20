//
//  SettingsViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class SettingsViewController: UITableViewController {
    
    @IBOutlet var batterySavingTitleLabel: UILabel!
    @IBOutlet var batterySavingDescriptionLabel: UILabel!
    @IBOutlet var batterySavingSwitch: UISwitch!
    @IBOutlet var deviceIdentifierTitleLabel: UILabel!
    @IBOutlet var deviceIdentifierLabel: UILabel!
    @IBOutlet var trainingServerTitleLabel: UILabel!
    @IBOutlet var trainingEndpointTextField: UITextField!

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
        setupTrainingEndpointTextField()
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
        trainingServerTitleLabel.text = Translation.SettingsView.TrainingServerTitleLabel.Text.String
        trainingEndpointTextField.placeholder = Preferences.trainingEndpoint
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    fileprivate func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }

    fileprivate func setupTrainingEndpointTextField() {
        trainingEndpointTextField.text = Preferences.trainingEndpoint
        trainingEndpointTextField.delegate = self
    }

    // MARK: - Actions
    
    @IBAction func batterySavingChanged(_ sender: UISwitch) {
        Preferences.batterySaving = sender.isOn
        tableView.beginUpdates()
        setupBatterySavingDescriptionLabel()
        tableView.endUpdates()
    }

    @IBAction func trainingEndpointEditingDidEnd(_ sender: Any) {
        guard let trainingEndpoint = trainingEndpointTextField.text else { return }
        Preferences.trainingEndpoint = trainingEndpoint
    }

    @IBAction func doneButtonTapped(_ sender: Any) {
        presentingViewController!.dismiss(animated: true, completion: nil)
    }
    
    // MARK: - UITableViewDataSource
    
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        switch section {
        case 0: return Translation.SettingsView.TableView.BatterySavingSection.Title.String
        case 1: return Translation.SettingsView.TableView.DeviceSection.Title.String
        case 2: return Translation.SettingsView.TableView.TrainingSection.Title.String
        default: return ""
        }
    }
    
    // MARK: - UITableViewDelegate
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }

    // TODO: Activate Training Feature -> Delete this method
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }

}

// MARK: - <UITextFieldDelegate>

extension SettingsViewController: UITextFieldDelegate {

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }

}
