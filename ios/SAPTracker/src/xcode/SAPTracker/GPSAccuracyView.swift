//
//  GPSAccuracyView.swift
//  SAPTracker
//
//  Created by computing on 11/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit

class GPSAccuracyView: UIView {

    let barView = UIView()

    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setup()
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    private func setup() {
        backgroundColor = UIColor.grayColor()
        
        addSubview(barView)
        barView.frame = bounds
        
        // add listeners
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"newLocation:", name:LocationManager.NotificationType.newLocation, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"locationManagerFailed:", name:LocationManager.NotificationType.locationManagerFailed, object: nil)

    }

    private func drawBar(horizontalAccuracy: Double) {
        var height = 1.0
        var color = TrackingViewController.Color.Red
        if (horizontalAccuracy < 0) {
        } else if (horizontalAccuracy > 48) {
            var height = 0.3
        } else if (horizontalAccuracy > 10) {
            height = 0.66
            color = TrackingViewController.Color.Orange
        } else {
            height = 1.0
            color = TrackingViewController.Color.Green
        }
        let frame = CGRect(x: 0, y: CGFloat(1.0-height)*bounds.height, width: bounds.width, height: CGFloat(height)*bounds.height)
        barView.frame = frame
        barView.backgroundColor = color

    }
    
    func newLocation(notification: NSNotification) {
        let horizontalAccuracy = notification.userInfo!["horizontalAccuracy"] as! Double
        drawBar(horizontalAccuracy)
    }
    
    
    func locationManagerFailed(notification: NSNotification) {
        drawBar(-1.0)
    }
}