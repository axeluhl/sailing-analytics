//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController {
    
    weak var checkIn: CheckIn!
    weak var sessionController: SessionController!
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var tableViewHeight: NSLayoutConstraint!
    @IBOutlet weak var stopTrackingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func updateViewConstraints() {
        super.updateViewConstraints()
        tableViewHeight.constant = tableView.contentSize.height
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        makeRed(button: stopTrackingButton)
    }
    
    fileprivate func setupLocalization() {
        stopTrackingButton.setTitle(Translation.TrackingView.StopTrackingButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
        navigationItem.titleView = TitleView(title: checkIn.event.name, subtitle: checkIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Actions
    
    @IBAction func stopTrackingButtonTapped(_ sender: Any) {
        let alertController = UIAlertController(
            title: Translation.TrackingView.StopTrackingAlert.Title.String,
            message: Translation.TrackingView.StopTrackingAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { [weak self] (action) in
            self?.stopTracking()
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel)
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func stopTracking() {
        LocationManager.sharedManager.stopTracking()
        SVProgressHUD.show()
        self.sessionController.gpsFixController.sendAll(completion: { (withSuccess) in
            SVProgressHUD.popActivity()
            self.dismiss(animated: true, completion: nil)
        })
    }
    
    @IBAction func optionButtonTapped(_ sender: Any) {
        presentSettingsViewController()
    }
    
}

// MARK: - UITableViewDataSourceDelegate

extension TrackingViewController: UITableViewDataSource {
    
    struct CellIdentifier {
        static let StatusCell = "StatusCell"
        static let ModeCell = "ModeCell"
        static let ChachedFixesCell = "CachedFixesCell"
        static let GPSAccuracyCell = "GPSAccuracyCell"
    }
    
    @nonobjc static let Rows = [
        CellIdentifier.StatusCell,
        CellIdentifier.ModeCell,
        CellIdentifier.ChachedFixesCell,
        CellIdentifier.GPSAccuracyCell
    ]
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return TrackingViewController.Rows.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: TrackingViewController.Rows[indexPath.row]) ?? UITableViewCell()
        if let gpsFixesCell = cell as? TrackingViewGPSFixesCell {
            gpsFixesCell.checkIn = checkIn
        }
        return cell
    }
    
}

// MARK: - UITableViewDelegate

extension TrackingViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 52
    }
    
    func tableView(_ tableView: UITableView, estimatedHeightForFooterInSection section: Int) -> CGFloat {
        return 0
    }
    
    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.removeSeparatorInset()
    }
    
}
