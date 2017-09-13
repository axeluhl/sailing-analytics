//
//  TrainingCompetitorViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingCompetitorViewController: CompetitorSessionViewController {
    
    @IBOutlet weak var trainingNameLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        startTrackingButton.setBackgroundImage(Images.GreenHighlighted, for: .highlighted)
    }
    
    fileprivate func setupLocalization() {
        startTrackingButton.setTitle(Translation.CompetitorView.StartTrackingButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: competitorCheckIn.event.name, subtitle: competitorCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Actions
    
    override func startTrackingButtonTapped(_ sender: AnyObject) {
        SVProgressHUD.show()
        self.trainingController.stopActiveRace(success: { 
            self.trainingController.startNewRace(forCheckIn: self.competitorCheckIn, success: {
                SVProgressHUD.dismiss()
                super.startTrackingButtonTapped(sender)
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
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.competitorCoreDataManager, baseURLString: self.competitorCheckIn.serverURL)
    }()
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingCompetitorViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return competitorCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return competitorCoreDataManager } }
    
    var sessionController: SessionController { get { return competitorSessionController } }
    
    func makeOptionSheet() -> UIAlertController {
        return makeCompetitorOptionSheet()
    }
    
    func refresh() {
        competitorViewController?.refresh()
        trainingNameLabel.text = competitorCheckIn.event.name
    }
    
}
