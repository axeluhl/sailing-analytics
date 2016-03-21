//
//  AboutViewController.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class AboutViewController: UIViewController {
    
    @IBOutlet weak var versionDescriptionLabel: UILabel!
    @IBOutlet weak var versionLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad();
        versionDescriptionLabel.text = NSLocalizedString("Version", comment: "")
        if let text = NSBundle.mainBundle().infoDictionary?["CFBundleVersion"] as? String {
            versionLabel.text = text
        } else {
            versionLabel.text = "-"
        }
		
		// add logo to top left
		let imageView = UIImageView(image: UIImage(named: "sap_logo"))
		let barButtonItem = UIBarButtonItem(customView: imageView)
		navigationItem.leftBarButtonItem = barButtonItem
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func openEULA(sender: AnyObject) {
        UIApplication.sharedApplication().openURL(URLs.EULA)
    }
    
}