//
//  LeaderboardViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class LeaderboardViewController: UIViewController {
    
    var regatta: Regatta!
    
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocalization()
        setupWebView()
    }
    
    private func setupLocalization() {
        navigationItem.title = Translation.LeaderboardView.Title.String
    }
    
    private func setupWebView() {
        guard let url = regatta.leaderboardURL() else { return }
        webView.loadRequest(NSURLRequest(URL: url))
    }
    
    // MARK: - Actions
    
    @IBAction func doneButtonTapped(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

// MARK: - UIWebViewDelegate

extension LeaderboardViewController: UIWebViewDelegate {

    func webViewDidFinishLoad(webView: UIWebView) {
        activityIndicator.stopAnimating()
    }
    
    func webView(webView: UIWebView, didFailLoadWithError error: NSError?) {
        activityIndicator.stopAnimating()
        let alertController = UIAlertController(title: error?.localizedDescription, message: nil, preferredStyle: .Alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
        }
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
    }

}
