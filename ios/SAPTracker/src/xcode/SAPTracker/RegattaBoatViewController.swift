//
//  RegattaBoatViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.03.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaBoatViewController: BoatSessionViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
        updateOptimistic()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh(false)
    }

    // MARK: - Setup

    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }

    fileprivate func setupButtons() {
        makeGreen(button: startTrackingButton)
    }

    fileprivate func setupLocalization() {
        startTrackingButton.setTitle(Translation.RegattaView.StartTrackingButton.Title.String, for: .normal)
    }

    fileprivate func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: boatCheckIn.event.name, subtitle: boatCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }

}

// MARK: SessionViewControllerDelegate

extension RegattaBoatViewController: SessionViewControllerDelegate {

    var checkIn: CheckIn { get { return boatCheckIn } }

    var checkOutActionTitle: String { get { return Translation.RegattaView.OptionSheet.CheckOutAction.Title.String } }

    var checkOutAlertMessage: String { get { return Translation.RegattaView.CheckOutAlert.Message.String } }

    var coreDataManager: CoreDataManager { get { return boatCoreDataManager } }

    var sessionController: SessionController { get { return boatSessionController } }

    func makeOptionSheet() -> UIAlertController {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = self.optionButton
        }
        alertController.addAction(self.makeActionCheckOut())
        alertController.addAction(self.makeActionSettings())
        alertController.addAction(self.makeActionInfo())
        alertController.addAction(self.makeActionCancel())
        return alertController
    }

    func refresh(_ animated: Bool) {
        boatViewController?.refresh(animated)
    }

}
