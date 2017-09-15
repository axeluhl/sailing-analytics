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
        super.viewDidLayoutSubviews()
        partnershipTextView.scrollRectToVisible(CGRect(x: 0, y: 0, width: 1, height: 1), animated: false)
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
        setupVersion()
    }
    
    fileprivate func setupButtons() {
        makeBlue(button: licenseInformationButton)
        makeBlue(button: termsButton)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = Translation.AboutView.Title.String
        partnershipTextView.text = Translation.AboutView.PartnershipTextView.Text.String
        licenseInformationButton.setTitle(Translation.LicenseView.Title.String, for: .normal)
        termsButton.setTitle(Translation.AboutView.TermsButton.Title.String, for: .normal)
        versionTitleLabel.text = Translation.AboutView.VersionTitleLabel.Text.String
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    fileprivate func setupVersion() {
        versionLabel.text = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "-"
    }
    
    // MARK: - Actions
    
    @IBAction func doneButtonTapped(_ sender: Any) {
        presentingViewController!.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func termsButtonTapped(_ sender: Any) {
        UIApplication.shared.openURL(URLs.Terms)
    }
    
}
