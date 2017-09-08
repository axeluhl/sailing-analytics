//
//  CreateTrainingViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CreateTrainingViewController: UIViewController {
    
    weak var trainingController: TrainingController!
    weak var trainingCoreDataManager: TrainingCoreDataManager!
    
    @IBOutlet weak var boatClassPickerView: UIPickerView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
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
    
    // MARK: - Actions
    
    @IBAction func cancelButtonTapped(_ sender: Any) {
        presentingViewController?.dismiss(animated: true)
    }
    
    @IBAction func createTrainingButtonTapped(_ sender: Any) {
        let boatClassName = BoatClassNames[boatClassPickerView.selectedRow(inComponent: 0)]
        createTraining(boatClassName: boatClassName)
    }
    
    // MARK: - CreateTraining
    
    fileprivate func createTraining(boatClassName: String) {
        trainingController.createTraining(forBoatClassName: boatClassName, success: { checkInData in
            self.createTrainingSuccess(checkInData: checkInData)
        }) { (error) in
            self.createTrainingFailure(error: error)
        }
    }
    
    fileprivate func createTrainingSuccess(checkInData: CheckInData) {
        trainingCheckInController.checkInWithViewController(self, checkInData: checkInData, success: { checkIn in
            // TODO
        }) { error in
            self.createTrainingFailure(error: error)
        }
    }
    
    fileprivate func createTrainingFailure(error: Error) {
        // TODO
    }
    
    // MARK: - Propeties
    
    fileprivate lazy var trainingCheckInController: TrainingCheckInController = {
        return TrainingCheckInController(coreDataManager: self.trainingCoreDataManager)
    }()
    
}

// MARK: - UIPickerViewDataSource

extension CreateTrainingViewController: UIPickerViewDataSource {

    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return BoatClassNames.count
    }
    
}

// MARK: - UIPickerViewDelegate

extension CreateTrainingViewController: UIPickerViewDelegate {

    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return BoatClassNames[row]
    }

}
