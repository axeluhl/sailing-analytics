//
//  LeaderBoardViewController.swift
//  SAPTracker
//
//  Created by computing on 08/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class LeaderBoardViewController: UIViewController, UIWebViewDelegate, UIAlertViewDelegate {
    
    weak var checkIn: CheckIn?
    
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let serverUrl = self.checkIn?.serverURL
        let leaderboardName = self.checkIn?.leaderboardName?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())!
        let url = "\(serverUrl)/gwt/Leaderboard.html?name=\(leaderboardName)&showRaceDetails=false&embedded=true&hideToolbar=true"
        webView!.loadRequest(NSURLRequest(URL: NSURL(string: url)!))
    }
    
    @IBAction func done(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func webViewDidFinishLoad(webView: UIWebView) {
        activityIndicator.stopAnimating()
    }
    
    func wwebView(webView: UIWebView, didFailLoadWithError error: NSError) {
        activityIndicator.hidden = true
        let alertView = UIAlertView(title: NSLocalizedString("Couldn't load about view", comment: ""), message: nil, delegate: nil, cancelButtonTitle: NSLocalizedString("Cancel", comment: ""))
        alertView.show()
    }
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
}