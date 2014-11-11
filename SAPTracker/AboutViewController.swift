//
//  AboutViewController.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class AboutViewController: UIViewController {
    
    @IBOutlet weak var webView: UIWebView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        webView!.loadRequest(NSURLRequest(URL: NSURL(string: "http://sapsailing.com")!))
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, nil)
    }
}