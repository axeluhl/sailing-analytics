//
//  HeadingViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class CourseViewController: UIViewController {
    
    private let defaultCourseText = "- °"
    
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
    
    private func setup() {
        setupCourseLabel(-1.0)
        setupLocalization()
    }
    
    private func setupCourseLabel(course: Double) {
        courseLabel.text = course < 0 ? defaultCourseText : String(format: "%.0f °", course)
    }
    
    private func setupLocalization() {
        titleLabel.text = Translation.CourseView.TitleLabel.Text.String
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.Updated,
                                                         object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerFailed(_:)),
                                                         name:LocationManager.NotificationType.Failed,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    @objc private func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.setupCourseLabel(locationData.location.course)
        })
    }
    
    @objc private func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupCourseLabel(-1.0)
        })
    }
    
}