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
        versionLabel.text = "1.0"
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func openLicenses(sender: AnyObject) {
        
    }
    
    @IBAction func openEULA(sender: AnyObject) {
        let url = NSURL(string: "http://www.sap.com")!
        UIApplication.sharedApplication().openURL(url)
    }
    
}