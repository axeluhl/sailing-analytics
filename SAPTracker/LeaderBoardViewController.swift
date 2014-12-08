//
//  LeaderBoardViewController.swift
//  SAPTracker
//
//  Created by computing on 08/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class LeaderBoardViewController: UIViewController, UIWebViewDelegate, UIAlertViewDelegate {
    
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // TODO: create URL
        webView!.loadRequest(NSURLRequest(URL: NSURL(string: "http://championsleague2014.sapsailing.com/gwt/Home.html#LeaderboardPlace:eventId=eb326f1b-81e8-468b-89cb-10131a8d8459&leaderboardName=Champions%20League%202014&showRaceDetails=true&showSettings=true")!))
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