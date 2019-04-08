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
    
    fileprivate func initialize() {
        setup()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupBarView()
        setupBackroundColor()
    }
    
    fileprivate func setupBarView() {
        addSubview(barView)
        barView.frame = bounds
    }

    fileprivate func setupBackroundColor() {
        backgroundColor = UIColor.gray
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
            self.drawBar(horizontalAccuracy: locationData.location.horizontalAccuracy)
        })
    }
    
    @objc fileprivate func locationManagerFailed(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.drawBar(horizontalAccuracy: -1.0)
        })
    }
    
    // MARK: - Draw
    
    fileprivate func drawBar(horizontalAccuracy: Double) {
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
        let frame = CGRect(
            x:0,
            y:CGFloat(1.0 - height) * bounds.height,
            width:bounds.width,
            height:CGFloat(height) * bounds.height
        )
        barView.frame = frame
        barView.backgroundColor = color
    }
    
}
