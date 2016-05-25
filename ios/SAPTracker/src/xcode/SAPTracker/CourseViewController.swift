//
//  HeadingViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class CourseViewController: UIViewController {
    
    @IBOutlet weak var courseLabel: UILabel!
    
    let defaultCourseText = "- °"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        courseLabel.text = defaultCourseText
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector:#selector(CourseViewController.newLocation(_:)), name:LocationManager.NotificationType.newLocation, object: nil)
    }

    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func newLocation(notification: NSNotification) {
        let course = notification.userInfo!["course"] as! Double
        courseLabel.text = course < 0 ? defaultCourseText : String(format: "%.0f °", course)
    }
    
}