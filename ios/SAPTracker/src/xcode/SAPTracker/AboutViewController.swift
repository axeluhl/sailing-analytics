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
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func openEULA(sender: AnyObject) {
        UIApplication.sharedApplication().openURL(URLs.EULA)
    }
    
}