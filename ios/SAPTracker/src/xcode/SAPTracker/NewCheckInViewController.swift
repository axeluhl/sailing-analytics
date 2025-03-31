//
//  NewCheckInViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 29.01.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//

import UIKit

protocol NewCheckInViewControllerDelegate: class {

    func newCheckInViewController(_ controller: NewCheckInViewController, didCheckIn checkIn: CheckIn)

}

class NewCheckInViewController: UIViewController {

    fileprivate struct Segue {
        static let CreateTraining = "CreateTraining"
        static let Scan = "Scan"
    }

    weak var coreDataManager: CoreDataManager!
    weak var delegate: NewCheckInViewControllerDelegate?

    @IBOutlet var scanCodeButton: UIButton!
    @IBOutlet var noCodeButton: UIButton!
    @IBOutlet var infoCodeLabel: UILabel!
    @IBOutlet var createTrainingButton: UIButton!

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
        makeBlue(button: scanCodeButton)
        makeGray(button: noCodeButton)
        makeBlue(button: createTrainingButton)
    }

    fileprivate func setupLocalization() {
        // navigationItem.title = Translation.RegattaCheckInListView.Title.String
        scanCodeButton.setTitle(Translation.ScanView.Title.String, for: .normal)
        noCodeButton.setTitle(Translation.RegattaCheckInListView.NoCodeAlert.Title.String, for: .normal)
        infoCodeLabel.text = Translation.RegattaCheckInListView.InfoCodeLabel.Text.String
    }

    fileprivate func setupNavigationBar() {
        navigationItem.title = Application.Title
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }

    // MARK: - Actions

    @IBAction func cancelButtonTapped(_ sender: Any) {
        presentingViewController?.dismiss(animated: true)
    }

    @IBAction func scanButtonTapped(_ sender: Any) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.camera) {
            performSegue(withIdentifier: Segue.Scan, sender: sender)
        } else {
            showNoCameraAlert()
        }
    }

    @IBAction func noCodeButtonTapped(_ sender: Any) {
        showNoCodeAlert()
    }

    // MARK: - Alerts

    fileprivate func showNoCameraAlert() {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: Translation.RegattaCheckInListView.NoCameraAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }

    fileprivate func showNoCodeAlert() {
        let alertController = UIAlertController(
            title: Translation.RegattaCheckInListView.NoCodeAlert.Title.String,
            message: Translation.RegattaCheckInListView.NoCodeAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }

    // MARK: - Segues

    // TODO: Activate Training Feature -> Show training view in IB
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if segue.identifier == Segue.Scan {
            guard let scanVC = segue.destination as? ScanViewController else { return }
            scanVC.coreDataManager = coreDataManager
            scanVC.delegate = self
        } else if segue.identifier == Segue.CreateTraining {
            guard let createTrainingVC = segue.destination as? CreateTrainingViewController else { return }
            createTrainingVC.coreDataManager = coreDataManager
            createTrainingVC.delegate = self
        }
    }

}

// MARK: - ScanViewControllerDelegate

extension NewCheckInViewController: ScanViewControllerDelegate {

    func scanViewController(_ controller: ScanViewController, didCheckIn checkIn: CheckIn) {
        delegate?.newCheckInViewController(self, didCheckIn: checkIn)
        dismiss(animated: true)
    }

}

// MARK: - CreateTrainingViewControllerDelegate

extension NewCheckInViewController: CreateTrainingViewControllerDelegate {

    func createTrainingViewController(_ controller: CreateTrainingViewController, didCheckIn checkIn: CheckIn) {
        delegate?.newCheckInViewController(self, didCheckIn: checkIn)
        dismiss(animated: true)
    }

}
