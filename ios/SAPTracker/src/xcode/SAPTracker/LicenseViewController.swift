//
//  LicenseViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.01.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class LicenseViewController: UIViewController {

    @IBOutlet weak var licenseTextView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.title = Translation.LicenseView.Title.String
        if let file = NSBundle.mainBundle().pathForResource("Acknowledgements", ofType: "markdown") {
            let license = try? NSString(contentsOfFile: file, encoding: NSUTF8StringEncoding)
            if (license != nil) {
                licenseTextView.attributedText = TSMarkdownParser.standardParser().attributedStringFromMarkdown(license! as String)
                licenseTextView.layoutIfNeeded()
                licenseTextView.setContentOffset(CGPoint.zero, animated: false)
            }
        }
    }
    
}
