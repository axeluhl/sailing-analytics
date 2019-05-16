//
//  SessionViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol SessionViewControllerDelegate: class {
    
    var checkIn: CheckIn { get }

    var checkOutActionTitle: String { get }

    var checkOutAlertMessage: String { get }

    var coreDataManager: CoreDataManager { get }
    
    var sessionController: SessionController { get }
    
    func makeOptionSheet() -> UIAlertController
    
    func refresh(_ animated: Bool)
    
}

class SessionViewController: UIViewController {
    
    struct SessionSegue {
        static let Tracking = "Tracking"
    }
    
    @IBOutlet weak var optionButton: UIBarButtonItem!
    @IBOutlet weak var startTrackingButton: UIButton!
    
    weak var delegate: SessionViewControllerDelegate!
    
    // MARK: - Tracking
    
    fileprivate func startTracking() {
        do {
            try delegate.sessionController.startTracking()
            performSegue(withIdentifier: SessionSegue.Tracking, sender: self)
        } catch let error as LocationManager.LocationManagerError {
            showStartTrackingFailureAlert(message: error.description)
        } catch {
            logError(name: "\(#function)", error: error)
        }
    }
    
    // MARK: - Update
    
    func updateOptimistic() {
        update(success: { 
            // ...
        }) { (error) in
            // ...
        }
    }
    
    func updatePessimistic() {
        SVProgressHUD.show()
        update(success: { 
            SVProgressHUD.dismiss()
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.showAlert(forError: error)
        }
    }
    
    fileprivate func update(
        success: @escaping (() -> Void),
        failure: @escaping ((_ error: Error) -> Void))
    {
        do {
            try delegate.sessionController.update(success: { [weak self] in
                success()
                self?.delegate.refresh(true)
            }) { (error) in
                failure(error)
            }
        } catch {
            failure(error)
        }
    }
    
    // MARK: - CheckOut
    
    func checkOut() {
        showCheckOutAlert()
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: Any) {
        present(delegate.makeOptionSheet(), animated: true, completion: nil)
    }
    
    func makeActionCancel() -> UIAlertAction {
        return UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
    }
    
    func makeActionCheckOut() -> UIAlertAction {
        return UIAlertAction(title: delegate?.checkOutActionTitle, style: .default) { [weak self] action in
            self?.checkOut()
        }
    }
    
    func makeActionInfo() -> UIAlertAction {
        return UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.presentAboutViewController()
        }
    }
    
    func makeActionSettings() -> UIAlertAction {
        return UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.presentSettingsViewController()
        }
    }

    @IBAction func refreshButtonTapped(_ sender: Any) {
        updatePessimistic()
    }

    @IBAction func startTrackingButtonTapped(_ sender: Any) {
        // TODO: Add or add not WiFi Alert?
        //if SMTWiFiStatus.wifiStatus() == WiFiStatus.On && !AFNetworkReachabilityManager.sharedManager().reachableViaWiFi {
        //    showStartTrackingWiFiAlert()
        //} else {
        startTracking()
        //}
    }
    
    @IBAction func eventButtonTapped(_ sender: UIButton) {
        if let eventURL = delegate.checkIn.eventURL() {
            UIApplication.shared.openURL(eventURL)
        }
    }
    
    @IBAction func leaderboardButtonTapped(_ sender: Any) {
        presentLeaderboardViewController()
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == SessionSegue.Tracking) {
            let trackingNC = segue.destination as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.checkIn = delegate.checkIn
            trackingVC.sessionController = delegate.sessionController
        }
    }
    
    fileprivate func presentLeaderboardViewController() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        guard let leaderboardNC = storyboard.instantiateViewController(withIdentifier: "LeaderboardNavigationController") as? UINavigationController else { return }
        guard let leaderboardVC = leaderboardNC.childViewControllers.first as? LeaderboardViewController else { return }
        leaderboardVC.checkIn = delegate.checkIn
        present(leaderboardNC, animated: true)
    }
    
    // MARK: - Alerts

    fileprivate func showCheckOutAlert() {
        let alertController = UIAlertController(
            title: Translation.Common.Warning.String,
            message: delegate?.checkOutAlertMessage,
            preferredStyle: .alert
        )
        let yesAction = UIAlertAction(title: Translation.Common.Yes.String, style: .default) { [weak self] action in
            self?.performCheckOut()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func performCheckOut() {
        delegate.sessionController.checkOut { [weak self] (withSuccess) in
            self?.performCheckOutCompleted(withSuccess: withSuccess)
        }
    }
    
    fileprivate func performCheckOutCompleted(withSuccess: Bool) {
        delegate.coreDataManager.deleteObject(object: delegate.checkIn)
        delegate.coreDataManager.saveContext()
        navigationController?.popViewController(animated: true)
    }
    
    fileprivate func showStartTrackingFailureAlert(message: String) {
        let alertController = UIAlertController(
            title: Translation.Common.Warning.String,
            message: message,
            preferredStyle: .alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .default) { action in
            if let url = URL(string: UIApplicationOpenSettingsURLString) {
                UIApplication.shared.openURL(url)
            }
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .default, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
}
