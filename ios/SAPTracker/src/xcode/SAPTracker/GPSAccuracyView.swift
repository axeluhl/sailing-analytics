//
//  GPSAccuracyView.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
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
        setups()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setups() {
        setupBarView()
        setupBackroundColor()
    }
    
    private func setupBarView() {
        addSubview(barView)
        barView.frame = bounds
    }

    private func setupBackroundColor() {
        backgroundColor = UIColor.grayColor()
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.Updated,
                                                         object: nil
        )
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerFailed(_:)),
                                                         name:LocationManager.NotificationType.Failed,
                                                         object: nil
        )
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.drawBar(locationData.location.horizontalAccuracy)
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