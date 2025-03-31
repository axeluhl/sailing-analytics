//
//  LeaderboardViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class LeaderboardViewController: UIViewController {
    
    weak var checkIn: CheckIn!
    
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupLocalization()
        setupNavigationBar()
        setupWebView()
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = Translation.LeaderboardView.Title.String
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    fileprivate func setupWebView() {
        guard let url = checkIn.leaderboardURL() else { return }
        webView.loadRequest(URLRequest(url: url))
    }
    
    // MARK: - Actions
    
    @IBAction func doneButtonTapped(_ sender: Any) {
        presentingViewController!.dismiss(animated: true, completion: nil)
    }
    
}

// MARK: - UIWebViewDelegate

extension LeaderboardViewController: UIWebViewDelegate {

    func webViewDidFinishLoad(_ webView: UIWebView) {
        activityIndicator.stopAnimating()
    }
    
    func webView(_ webView: UIWebView, didFailLoadWithError error: Error) {
        activityIndicator.stopAnimating()
        let alertController = UIAlertController(title: error.localizedDescription, message: nil, preferredStyle: .alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { [weak self] action in
            self?.presentingViewController!.dismiss(animated: true, completion: nil)
        }
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }

}
