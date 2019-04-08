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

    weak var coreDataManager: CoreDataManager!

    @IBOutlet var boatClassPickerView: UIPickerView!
    @IBOutlet var boatClassNameLabel: UILabel!
    @IBOutlet var createTrainingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        login()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupBoatClassPickerView()
        setupButtons()
        setupLocalization()
    }

    fileprivate func setupBoatClassPickerView() {
        if let row = BoatClassNames.index(of: Preferences.boatClassName) {
            boatClassPickerView.selectRow(row, inComponent: 0, animated: true)
        }
    }

    fileprivate func setupButtons() {
        makeBlue(button: createTrainingButton)
    }

    fileprivate func setupLocalization() {
        navigationItem.title = Translation.CreateTrainingView.Title.String
        boatClassNameLabel.text = Translation.CreateTrainingView.BoatClassNameLabel.Text.String
        createTrainingButton.setTitle(Translation.CreateTrainingView.CreateTrainingButton.Title.String, for: .normal)
    }

    // MARK: - Login

    fileprivate func login() {
        SVProgressHUD.show()
        signUpController.login(success: { (userName) in
            SVProgressHUD.dismiss()
        }) { (error, message) in
            SVProgressHUD.dismiss()
        }
    }

    // MARK: - Actions
    
    @IBAction func cancelButtonTapped(_ sender: Any) {
        presentingViewController?.dismiss(animated: true)
    }
    
    @IBAction func createTrainingButtonTapped(_ sender: Any) {
        // TODO: sailID and nationality shouldn't be static
        createTrainingAndPerformCheckIn(
            forBoatClassName: BoatClassNames[boatClassPickerView.selectedRow(inComponent: 0)],
            sailID: "",
            nationality: "GER"
        )
    }
    
    // MARK: - CreateTraining

    fileprivate func createTrainingAndPerformCheckIn(
        forBoatClassName boatClassName: String,
        sailID: String,
        nationality: String)
    {
        SVProgressHUD.show()
        self.trainingController.createTraining(forBoatClassName: boatClassName, sailID: sailID, nationality: nationality, success: { checkInData in
            Preferences.boatClassName = boatClassName
            self.trainingCheckInController.checkInWithViewController(self, checkInData: checkInData, success: { [weak self] (checkIn) in
                SVProgressHUD.dismiss()
                if let strongSelf = self {
                    strongSelf.dismiss(animated: true) {
                        strongSelf.delegate?.createTrainingViewController(strongSelf, didCheckIn: checkIn)
                    }
                }
            }) { [weak self] (error) in
                SVProgressHUD.dismiss()
                self?.handle(error: error)
            }
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.handle(error: error)
        }
    }

    // MARK: - Propeties

    fileprivate lazy var signUpController: SignUpController = {
        return SignUpController(baseURLString: self.trainingController.baseURLString)
    }()

    fileprivate lazy var trainingCheckInController: TrainingCheckInController = {
        return TrainingCheckInController(coreDataManager: self.coreDataManager)
    }()

    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.coreDataManager, baseURLString: Preferences.trainingEndpoint)
    }()

    // MARK: - Helper

    fileprivate func handle(error: Error) {
        if ErrorHelper.isResponseUnauthorized(error: error as NSError) {
            self.signUpController.loginWithViewController(self)
        } else {
            self.showAlert(forError: error)
        }
    }

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
