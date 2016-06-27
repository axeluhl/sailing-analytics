//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    struct CellIdentifier {
        static let StatusCell = "StatusCell"
        static let ModeCell = "ModeCell"
        static let ChachedFixesCell = "CachedFixesCell"
        static let GPSAccuracyCell = "GPSAccuracyCell"
    }
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var tableViewHeight: NSLayoutConstraint!
    
    let rows = [
        CellIdentifier.StatusCell,
        CellIdentifier.ModeCell,
        CellIdentifier.ChachedFixesCell,
        CellIdentifier.GPSAccuracyCell
    ]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.setupNavigationBarTitle()
    }
	
    override func updateViewConstraints() {
        super.updateViewConstraints()
        self.tableViewHeight.constant = tableView.contentSize.height
    }
    
    // MARK: - Setups
    
    private func setupNavigationBarTitle() {
        navigationItem.title = DataManager.sharedManager.selectedCheckIn!.leaderBoardName
    }
    
	// MARK: - Actions
	
	/* Stop tracking, go back to regattas view */
	@IBAction func stopTrackingButtonTapped(sender: AnyObject) {
		let alertTitle = NSLocalizedString("Stop tracking?", comment: "")
		let alertController = UIAlertController(title: alertTitle,
		                                        message: "",
		                                        preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
		let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
        let stopTitle = NSLocalizedString("Stop", comment: "")
		let stopAction = UIAlertAction(title: stopTitle, style: .Default) { action in
			LocationManager.sharedManager.stopTracking()
			SendGPSFixController.sharedManager.checkIn = nil
			self.dismissViewControllerAnimated(true, completion: nil)
		}
        alertController.addAction(cancelAction)
		alertController.addAction(stopAction)
		presentViewController(alertController, animated: true, completion: nil)
	}

    // MARK: - UITableViewDataSourceDelegate
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return rows.count
    }

    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return tableView.dequeueReusableCellWithIdentifier(rows[indexPath.row]) ?? UITableViewCell()
    }
    
    // MARK: - UITableViewDelegate
    
    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 52
    }
    
    func tableView(tableView: UITableView, estimatedHeightForFooterInSection section: Int) -> CGFloat {
        return 0
    }
    
}