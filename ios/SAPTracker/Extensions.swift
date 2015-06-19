//
//  Extensions.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

/* Set color to RGB hex value. See http://stackoverflow.com/a/24263296 */
extension UIColor {
    convenience init(red: Int, green: Int, blue: Int) {
        assert(red >= 0 && red <= 255, "Invalid red component")
        assert(green >= 0 && green <= 255, "Invalid green component")
        assert(blue >= 0 && blue <= 255, "Invalid blue component")
        
        self.init(red: CGFloat(red) / 255.0, green: CGFloat(green) / 255.0, blue: CGFloat(blue) / 255.0, alpha: 1.0)
    }

    convenience init(hex:Int) {
        self.init(red:(hex >> 16) & 0xff, green:(hex >> 8) & 0xff, blue:hex & 0xff)
    }
}

/* Needed for setting border color in Interface Builder. */
extension CALayer {
    var borderUIColor: UIColor {
        get {
            return UIColor(CGColor: self.borderColor)!
        }
        set {
            self.borderColor = newValue.CGColor
        }
    }
}

/* Needed for bottom status labels. */
class PaddedLabel : UILabel {
    override func drawTextInRect(rect: CGRect) {
        let insets = UIEdgeInsets(top: 0, left: 5, bottom: 0, right: 5)
        return super.drawTextInRect(UIEdgeInsetsInsetRect(rect, insets))
    }
}

/* Needed for programmatically setting button heights in iOS 7. */
struct ButtonHeight {
    static let bigButtonPortrait = CGFloat(75.0)
    static let bigButtonLandscape = CGFloat(50.0)
    static let smallButtonPortrait = CGFloat(50.0)
    static let smallButtonLandscape = CGFloat(50.0)
}