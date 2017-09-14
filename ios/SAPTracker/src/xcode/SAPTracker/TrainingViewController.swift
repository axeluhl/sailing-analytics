//
//  TrainingViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol TrainingViewControllerDelegate: class {
    
    func trainingViewController(_ controller: TrainingViewController, startTrackingButtonTapped sender: Any)
    
    func trainingViewController(_ controller: TrainingViewController, leaderboardButtonTapped sender: Any)
    
}

class TrainingViewController: UIViewController {
    
    weak var delegate: TrainingViewControllerDelegate?
    
    weak var trainingCheckIn: CheckIn!
    weak var trainingCoreDataManager: CoreDataManager!
    
    @IBOutlet weak var stopTrainingButton: UIButton!
    @IBOutlet weak var stopTrainingButtonZeroHeight: NSLayoutConstraint!
    @IBOutlet weak var trainingNameLabel: UILabel!
    @IBOutlet weak var leaderboardButton: UIButton!
    @IBOutlet weak var startTrackingButton: UIButton!
    
    // MARK: - Refresh
    
    func refresh() {
        refreshStopTrainingButton()
        refreshTrainingNameLabel()
        UIView.animate(withDuration: 0.5) {
            self.view.layoutIfNeeded()
        }
    }
    
    fileprivate func refreshStopTrainingButton() {
        stopTrainingButtonZeroHeight.isActive = isTrainingActive
    }
    
    fileprivate func refreshTrainingNameLabel() {
        trainingNameLabel.text = trainingCheckIn.event.name
    }
    
    // MARK: - Actions
    
    @IBAction func stopTrainingButtonTapped(_ sender: Any) {
        
    }
    
    @IBAction func leaderboardButtonTapped(_ sender: Any) {
        delegate?.trainingViewController(self, leaderboardButtonTapped: sender)
    }
    
    @IBAction func startTrackingButtonTapped(_ sender: Any) {
        SVProgressHUD.show()
        self.trainingController.stopActiveRace(success: {
            self.trainingController.startNewRace(forCheckIn: self.trainingCheckIn, success: {
                SVProgressHUD.dismiss()
                self.delegate?.trainingViewController(self, startTrackingButtonTapped: sender)
            }) { [weak self] (error) in
                SVProgressHUD.dismiss()
                self?.showAlert(forError: error)
            }
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.showAlert(forError: error)
        }
    }
    
    // MARK: - Properties
    
    fileprivate var isTrainingActive: Bool {
        get {
             return trainingCheckIn.event.endDate - Date().timeIntervalSince1970 < 0
        }
    }
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: self.trainingCheckIn.serverURL)
    }()
    
}
