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
        setups()
    }
	
    override func updateViewConstraints() {
        super.updateViewConstraints()
        tableViewHeight.constant = tableView.contentSize.height
    }
    
    // MARK: - Setups
    
    private func setups() {
        setupButtons()
        setupNavigationBar()    
    }
    
    private func setupButtons() {
        stopTrackingButton.setBackgroundImage(Images.RedHighlighted, forState: .Highlighted)
    }
    
    private func setupNavigationBar() {
        navigationItem.title = regatta?.leaderboard.name
    }
    
	// MARK: - Actions
	
	@IBAction func stopTrackingButtonTapped(sender: AnyObject) {
		let alertTitle = NSLocalizedString("Stop tracking?", comment: "")
		let alertController = UIAlertController(title: alertTitle, message: "", preferredStyle: .Alert)
        let stopTitle = NSLocalizedString("Stop", comment: "")
		let stopAction = UIAlertAction(title: stopTitle, style: .Default) { action in
			LocationManager.sharedManager.stopTracking()
            
			// SendGPSFixController.sharedManager.checkIn = nil
            
			self.dismissViewControllerAnimated(true, completion: nil)
		}
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
        alertController.addAction(stopAction)
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
        if (cell.isKindOfClass(CachedFixesTrackingTableViewCell)) {
            let cachedFixesCell = cell as! CachedFixesTrackingTableViewCell
            cachedFixesCell.regatta = regatta
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
    
}