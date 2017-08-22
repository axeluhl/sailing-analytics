//
//  CreateTrainingViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CreateTrainingViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // Setup
    
    fileprivate func setup() {
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = "TRAININGS TITLE"
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    // Actions
    
    @IBAction func cancelButtonTapped(_ sender: Any) {
        presentingViewController?.dismiss(animated: true)
    }

}

// MARK: - UIPickerViewDataSource

extension CreateTrainingViewController: UIPickerViewDataSource {

    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return BoatClasses.count
    }
    
}

// MARK: - UIPickerViewDelegate

extension CreateTrainingViewController: UIPickerViewDelegate {

    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return BoatClasses[row]
    }

}
