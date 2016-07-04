//
//  GPSAccuracyView.swift
//  SAPTracker
//
//  Created by computing on 11/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit

class GPSAccuracyView : UIView {

    struct Color {
        static let Green = UIColor(hex: 0x408000)
        static let Red = UIColor(hex: 0xFF0000)
        static let Orange = UIColor(hex: 0xFF8000)
    }

    let barView = UIView()

    override init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        initialize()
    }
    
    private func initialize() {
        self.setupBarView()
        self.setupBackroundColor()
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupBarView() {
        addSubview(self.barView)
        self.barView.frame = self.bounds    
    }

    private func setupBackroundColor() {
        self.backgroundColor = UIColor.grayColor()
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.LocationManagerUpdated,
                                                         object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerFailed(_:)),
                                                         name:LocationManager.NotificationType.LocationManagerFailed,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.drawBar(notification.userInfo![LocationManager.UserInfo.HorizontalAccuracy] as! Double)
        })
    }
    
    func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.drawBar(-1.0)
        })
    }
    
    // MARK: - Draw
    
    private func drawBar(horizontalAccuracy: Double) {
        var height = 1.0
        var color = Color.Red
        if (horizontalAccuracy < 0) {
            // ...
        } else if (horizontalAccuracy > 48) {
            height = 0.33
        } else if (horizontalAccuracy > 10) {
            height = 0.66
            color = Color.Orange
        } else {
            height = 1.0
            color = Color.Green
        }
        let frame = CGRect(x:0,
                           y:CGFloat(1.0 - height) * bounds.height,
                           width:bounds.width,
                           height:CGFloat(height) * bounds.height)
        barView.frame = frame
        barView.backgroundColor = color
    }
    
}