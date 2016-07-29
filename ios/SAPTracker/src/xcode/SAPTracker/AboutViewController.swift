//
//  AboutViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class AboutViewController: UIViewController {
    
    @IBOutlet weak var partnershipTextView: UITextView!
    @IBOutlet weak var licenseInformationButton: UIButton!
    @IBOutlet weak var termsButton: UIButton!
    @IBOutlet weak var versionTitleLabel: UILabel!
    @IBOutlet weak var versionLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidLayoutSubviews() {
        partnershipTextView.scrollRectToVisible(CGRect(x: 0, y: 0, width: 1, height: 1), animated: false)
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocalization()
        setupNavigationBar()
        setupVersion()
    }
    
    private func setupLocalization() {
        navigationItem.title = Translation.AboutView.Title.String
        partnershipTextView.text = Translation.AboutView.PartnershipTextView.Text.String
        licenseInformationButton.setTitle(Translation.LicenseView.Title.String, forState: .Normal)
        termsButton.setTitle(Translation.AboutView.TermsButton.Title.String, forState: .Normal)
        versionTitleLabel.text = Translation.AboutView.VersionTitleLabel.Text.String
    }
    
    private func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    private func setupVersion() {
        versionLabel.text = NSBundle.mainBundle().infoDictionary?["CFBundleVersion"] as? String ?? "-"
    }
    
    // MARK: - Actions
    
    @IBAction func doneButtonTapped(sender: AnyObject) {
        presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func eulaButtonTapped(sender: AnyObject) {
        UIApplication.sharedApplication().openURL(URLs.EULA)
    }
    
}