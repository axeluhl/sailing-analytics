//
//  MarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class MarkViewController: SessionViewController {

    @IBOutlet weak var markNameLabel: UILabel!

    var markCheckIn: MarkCheckIn!

    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
        update()
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }

    // MARK: - Setup

    private func setup() {
        setupButtons()
        setupNavigationBar()
    }
    
    private func setupButtons() {
        startTrackingButton.setBackgroundImage(Images.GreenHighlighted, forState: .Highlighted)
    }

    private func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: markCheckIn.event.name, subtitle: markCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }

    // MARK: - Update
    
    private func update() {
        SVProgressHUD.show()
        markSessionController.update {
            self.refresh()
            SVProgressHUD.popActivity()
        }
    }
    
    // MARK: - Refresh
    
    private func refresh() {
        markNameLabel.text = markCheckIn.name
    }

    // MARK: - Properties
    
    private lazy var markSessionController: MarkSessionController = {
        return MarkSessionController(checkIn: self.markCheckIn)
    }()

}

// MARK: SessionViewControllerDelegate

extension MarkViewController: SessionViewControllerDelegate {

    func startTracking() throws {
        try markSessionController.startTracking()
    }

}