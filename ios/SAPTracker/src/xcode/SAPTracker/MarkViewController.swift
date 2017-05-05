//
//  MarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class MarkViewController: UIViewController {

    @IBOutlet weak var markNameLabel: UILabel!
    @IBOutlet weak var startTrackingButton: UIButton!

    var markCheckIn: MarkCheckIn!

    override func viewDidLoad() {
        super.viewDidLoad()
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

    // MARK: - Actions

    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
    }

    // MARK: - Properties
    
    private lazy var markSessionController: MarkSessionController = {
        return MarkSessionController(checkIn: self.markCheckIn)
    }()

}
