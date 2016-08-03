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
    static let CodeConvention = NSURL(string: "https://github.com/raywenderlich/swift-style-guide/blob/master/README.markdown") ?? NSURL()
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
    static let Tint = UIColor(hex: 0x009de0)
}

struct FontNames {
    static let OpenSans = "OpenSans"
    static let OpenSansBold = "OpenSans-Bold"
}

struct Fonts {
    static let OpenSansBold17 = UIFont(name: FontNames.OpenSansBold, size: CGFloat(17.0)) ?? UIFont.systemFontOfSize(17.0)
    static let OpenSansBold13 = UIFont(name: FontNames.OpenSansBold, size: CGFloat(13.0)) ?? UIFont.systemFontOfSize(13.0)
    static let OpenSans10 = UIFont(name: FontNames.OpenSans, size: CGFloat(10.0)) ?? UIFont.systemFontOfSize(10.0)
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
