//
//  AboutViewController.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class AboutViewController: UIViewController, UIWebViewDelegate, UIAlertViewDelegate {
    
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        webView!.loadRequest(NSURLRequest(URL: NSURL(string: "http://sapsailing.com")!))
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, nil)
    }
    
    func webViewDidFinishLoad(webView: UIWebView) {
        activityIndicator.stopAnimating()
    }
    
    func wwebView(webView: UIWebView, didFailLoadWithError error: NSError) {
        activityIndicator.hidden = true
        let alertView = UIAlertView(title: "Couldn't load about view", message: nil, delegate: nil, cancelButtonTitle: "Cancel")
        alertView.show()
    }
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
}