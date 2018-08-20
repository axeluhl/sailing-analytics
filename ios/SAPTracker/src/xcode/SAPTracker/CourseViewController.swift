//
//  HeadingViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class CourseViewController: UIViewController {
    
    fileprivate let defaultCourseText = "- °"
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var courseLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupCourseLabel(course: -1.0)
        setupLocalization()
    }
    
    fileprivate func setupCourseLabel(course: Double) {
        courseLabel.text = course < 0 ? defaultCourseText : String(format: "%.0f °", course)
    }
    
    fileprivate func setupLocalization() {
        titleLabel.text = Translation.CourseView.TitleLabel.Text.String
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(locationManagerUpdated(_:)),
            name: NSNotification.Name(rawValue: LocationManager.NotificationType.Updated),
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(locationManagerFailed(_:)),
            name: NSNotification.Name(rawValue: LocationManager.NotificationType.Failed),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func locationManagerUpdated(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.setupCourseLabel(course: locationData.location.course)
        })
    }
    
    @objc fileprivate func locationManagerFailed(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.setupCourseLabel(course: -1.0)
        })
    }
    
}
