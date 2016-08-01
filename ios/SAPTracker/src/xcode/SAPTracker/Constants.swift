//
//  Constants.swift
//  SAPTracker
//
//  Created by Konstantin Gonikman on 15/02/16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

struct Application {
    static let Title = "SAP Sail InSight"
}

struct URLs {
    static let Terms = NSURL(string: "http://www.sapsailing.com/EULA_iOS_SailingBoatTracker.html") ?? NSURL()
}

struct Colors {
    static let Blue = UIColor(hex:0x009de0)
    static let BlueHighlighted = UIColor(hex:0x007aad)
    static let Gray = UIColor(hex:0xf3f3f3)
    static let GrayHighlighted = UIColor(hex:0xf3f3f3)
    static let Green = UIColor(hex:0x0e7733)
    static let GreenHighlighted = UIColor(hex:0x08451e)
    static let Red = UIColor(hex:0xcd201b)
    static let RedHighlighted = UIColor(hex:0x991814)
}

struct Images {
    static let Blue = UIImage(color: Colors.Blue)
    static let BlueHighlighted = UIImage(color: Colors.BlueHighlighted)
    static let Gray = UIImage(color: Colors.Gray)
    static let GrayHighlighted = UIImage(color: Colors.GrayHighlighted)
    static let Green = UIImage(color: Colors.Green)
    static let GreenHighlighted = UIImage(color: Colors.GreenHighlighted)
    static let Red = UIImage(color: Colors.Red)
    static let RedHighlighted = UIImage(color: Colors.RedHighlighted)
}
