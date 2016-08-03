//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController {
    
    var regatta: Regatta!
    var regattaController: RegattaController!
    
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
    
    private func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    private func setupButtons() {
        stopTrackingButton.setBackgroundImage(Images.RedHighlighted, forState: .Highlighted)
    }
    
    private func setupLocalization() {
        stopTrackingButton.setTitle(Translation.TrackingView.StopTrackingButton.Title.String, forState: .Normal)
    }
    
    private func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
        navigationItem.titleView = TitleView(title: regatta.event.name, subtitle: regatta.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Actions
    
    @IBAction func stopTrackingButtonTapped(sender: AnyObject) {
        let alertController = UIAlertController(title: Translation.TrackingView.StopTrackingAlert.Title.String,
                                                message: Translation.TrackingView.StopTrackingAlert.Message.String,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { action in
            LocationManager.sharedManager.stopTracking()
            SVProgressHUD.show()
            self.regattaController.gpsFixController.sendAll({ (withSuccess) in
                SVProgressHUD.popActivity()
                self.dismissViewControllerAnimated(true, completion: nil)
            })
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
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
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return TrackingViewController.Rows.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(TrackingViewController.Rows[indexPath.row]) ?? UITableViewCell()
        if let gpsFixesCell = cell as? TrackingViewGPSFixesCell {
            gpsFixesCell.regatta = regatta
        }
        return cell
    }
    
}

// MARK: - UITableViewDelegate

extension TrackingViewController: UITableViewDelegate {
    
    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 52
    }
    
    func tableView(tableView: UITableView, estimatedHeightForFooterInSection section: Int) -> CGFloat {
        return 0
    }
    
    func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        cell.removeSeparatorInset()
    }
    
}