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
    @IBOutlet weak var boatClassNameLabel: UILabel!
    @IBOutlet weak var createTrainingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        makeBlue(button: createTrainingButton)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = Translation.CreateTrainingView.Title.String
        boatClassNameLabel.text = Translation.CreateTrainingView.BoatClassNameLabel.Text.String
        createTrainingButton.setTitle(Translation.CreateTrainingView.CreateTrainingButton.Title.String, for: .normal)
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
        SVProgressHUD.show()
        trainingController.createTraining(forBoatClassName: boatClassName, sailID: "", nationality: "GER", success: { checkInData in
            SVProgressHUD.dismiss()
            self.checkIn(withCheckInData: checkInData)
        }) { (error) in
            SVProgressHUD.dismiss()
            self.showAlert(forError: error)
        }
    }
    
    fileprivate func checkIn(withCheckInData checkInData: CheckInData) {
        trainingCheckInController.checkInWithViewController(self, checkInData: checkInData, success: { (checkIn) in
            self.createTrainingSuccess(checkIn: checkIn)
        }) { (error) in
            self.showAlert(forError: error)
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
