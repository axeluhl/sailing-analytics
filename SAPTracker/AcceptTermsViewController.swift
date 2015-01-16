//
//  AcceptTermsViewController.swift
//  SAPTracker
//
//  Created by computing on 16/01/15.
//  Copyright (c) 2015 com.sap.sailing. All rights reserved.
//

import Foundation

class AcceptTermsViewController: UIViewController {
   
    struct Keys {
        static let acceptedTerms = "acceptedTerms"
    }
    class func acceptedTerms()->Bool {
        
        return NSUserDefaults.standardUserDefaults().boolForKey(Keys.acceptedTerms)
    }
   
    override func viewDidLoad() {
        super.viewDidLoad()
        let urlpath = NSBundle.mainBundle().pathForResource("eula-ios", ofType: "html");
        let requesturl = NSURL(string: urlpath!)
        let request = NSURLRequest(URL: requesturl!)
        (self.view as UIWebView).loadRequest(request)
    }
    
    @IBAction func declineButtonTap(sender: AnyObject) {
        let alertView = UIAlertView(title: "You must accept the terms to use this app.", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
        alertView.show()
        return
    }
    
    @IBAction func acceptButtonTap(sender: AnyObject) {
        NSUserDefaults.standardUserDefaults().setBool(true, forKey:Keys.acceptedTerms)
        dismissViewControllerAnimated(true, completion: nil)
    }
}