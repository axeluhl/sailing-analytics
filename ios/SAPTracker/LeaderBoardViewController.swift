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
        let serverUrl = DataManager.sharedManager.selectedEvent!.serverUrl
        let eventId = DataManager.sharedManager.selectedEvent!.eventId
        let leaderBoardName = DataManager.sharedManager.selectedEvent!.leaderBoard!.name.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
        let url = "\(serverUrl)/gwt/Home.html#LeaderboardPlace:eventId=\(eventId)&leaderboardName=\(leaderBoardName)&showRaceDetails=true&showSettings=true"
        webView!.loadRequest(NSURLRequest(URL: NSURL(string: url)!))
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