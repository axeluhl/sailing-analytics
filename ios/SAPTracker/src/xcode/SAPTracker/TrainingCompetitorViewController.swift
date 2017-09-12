//
//  TrainingCompetitorViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingCompetitorViewController: SessionViewController {
    
    weak var competitorCheckIn: CompetitorCheckIn!
    weak var trainingCoreDataManager: CoreDataManager!
    
    @IBOutlet weak var stopTrainingButton: UIButton!
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
        self.stopActiveTrainingRace(success: {
            self.startNewTrainingRace(success: {
                super.startTrackingButtonTapped(sender)
            }) { [weak self] (error) in
                self?.showAlert(forError: error)
            }
        }) { [weak self] (error) in
            self?.showAlert(forError: error)
        }
    }
    
    fileprivate func stopActiveTrainingRace(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        if let activeTrainingRaceData = Preferences.activeTrainingRaceData {
            trainingController.leaderboardRaceStopTracking(forTrainingRaceData: activeTrainingRaceData, success: {
                self.autoCourseTrainingRace(forTrainingRaceData: activeTrainingRaceData) { (withSuccess) in
                    Preferences.activeTrainingRaceData = nil
                    success()
                }
            }) { (error) in
                failure(error)
            }
        } else {
            success()
        }
    }
    
    fileprivate func startNewTrainingRace(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingController.leaderboardRaceStartTracking(forCheckIn: competitorCheckIn, success: { (trainingRaceData) in
            Preferences.activeTrainingRaceData = trainingRaceData
            success()
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func autoCourseTrainingRace(
        forTrainingRaceData trainingRaceData: TrainingRaceData,
        completion: @escaping (_ withSuccess: Bool) -> Void)
    {
        autoCourseTrainingRace(
            leaderboardName: trainingRaceData.leaderboardName,
            raceColumnName: trainingRaceData.raceColumnName,
            fleetName: trainingRaceData.fleetName,
            completion: completion
        )
    }
    
    fileprivate func autoCourseTrainingRace(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        completion: @escaping (_ withSuccess: Bool) -> Void)
    {
        trainingController.leaderboardRaceAutoCourse(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
            completion(true)
        }) { (error) in
            completion(false)
        }
    }
    
    // MARK: - Alerts
    
    fileprivate func showAlert(forError error: Error) {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: error.localizedDescription,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Properties
    
    fileprivate lazy var competitorSessionController: CompetitorSessionController = {
        return CompetitorSessionController(checkIn: self.competitorCheckIn, coreDataManager: self.trainingCoreDataManager)
    }()
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: self.competitorCheckIn.serverURL)
    }()
    
}

// MARK: - SessionViewControllerDelegate

extension TrainingCompetitorViewController: SessionViewControllerDelegate {
    
    var checkIn: CheckIn { get { return competitorCheckIn } }
    
    var coreDataManager: CoreDataManager { get { return trainingCoreDataManager } }
    
    var sessionController: SessionController { get { return competitorSessionController } }
    
    func makeOptionSheet() -> UIAlertController {
        return makeDefaultOptionSheet()
    }
    
    func refresh() {
        trainingNameLabel.text = competitorCheckIn.event.name
    }
    
}
