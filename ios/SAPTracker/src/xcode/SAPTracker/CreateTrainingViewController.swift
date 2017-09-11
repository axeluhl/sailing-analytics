//
//  CreateTrainingViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol CreateTrainingViewControllerDelegate: class {
    
    func createTrainingViewController(_ controller: CreateTrainingViewController, didCheckIn checkIn: CheckIn)
    
}

class CreateTrainingViewController: UIViewController {
    
    weak var delegate: CreateTrainingViewControllerDelegate?
    
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
        createTraining(forBoatClassName: boatClassName)
    }
    
    // MARK: - CreateTraining
    
    fileprivate func createTraining(forBoatClassName boatClassName: String) {
        trainingController.createTraining(forBoatClassName: boatClassName, success: { checkInData in
            self.checkIn(withCheckInData: checkInData)
        }) { (error) in
            logError(name: "\(#function)", error: error)
        }
    }
    
    fileprivate func checkIn(withCheckInData checkInData: CheckInData) {
        trainingCheckInController.checkInWithViewController(self, checkInData: checkInData, success: { (checkIn) in
            self.leaderboardSetup(forCheckIn: checkIn)
        }) { (error) in
            logError(name: "\(#function)", error: error)
        }
    }
    
    fileprivate func leaderboardSetup(forCheckIn checkIn: CheckIn) {
        trainingController.leaderboardSetup(checkIn: checkIn, success: { 
            self.createTrainingSuccess(checkIn: checkIn)
        }) { (error) in
            logError(name: "\(#function)", error: error)
        }
    }
    
    fileprivate func createTrainingSuccess(checkIn: CheckIn) {
        dismiss(animated: true) {
            self.delegate?.createTrainingViewController(self, didCheckIn: checkIn)
        }
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
