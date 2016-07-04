//
//  AboutViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class AboutViewController: UIViewController {
    
	@IBOutlet weak var textView: UITextView!
    @IBOutlet weak var versionDescriptionLabel: UILabel!
    @IBOutlet weak var versionLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad();
        setupNavigationBar()
        setupVersion()
    }

	override func viewDidLayoutSubviews() {		
		textView.scrollRectToVisible(CGRect(x: 0, y: 0, width: 1, height: 1), animated: false)
	}
    
    // MARK: - Setups
    
    private func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    private func setupVersion() {
        versionDescriptionLabel.text = NSLocalizedString("Version", comment: "")
        versionLabel.text = NSBundle.mainBundle().infoDictionary?["CFBundleVersion"] as? String ?? "-"
    }
    
    // MARK: - Actions
    
    @IBAction func doneButtonTapped(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func eulaButtonTapped(sender: AnyObject) {
        UIApplication.sharedApplication().openURL(URLs.EULA)
    }
    
}