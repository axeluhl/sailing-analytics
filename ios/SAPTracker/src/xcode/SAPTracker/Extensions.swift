//
//  Extensions.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

// MARK: - Logging

func logInfo(name: String, info: String) {
    #if DEBUG
        NSLog("[INFO] \(name): %@", info)
    #endif
}

func logError(name: String, error: Error) {
    logError(name: name, error: error as? String ?? "")
}

func logError(name: String, error: String) {
    #if DEBUG
        NSLog("[ERROR] \(name): %@", error)
    #endif
}

// MARK: - UIColor

extension UIColor { // Set color to RGB hex value. See http://stackoverflow.com/a/24263296
    
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

// MARK: - UIImage

extension UIImage {
    
    public convenience init?(color: UIColor, size: CGSize = CGSize(width: 1, height: 1)) {
        let rect = CGRect(origin: .zero, size: size)
        UIGraphicsBeginImageContextWithOptions(rect.size, false, 0.0)
        color.setFill()
        UIRectFill(rect)
        guard let image = UIGraphicsGetImageFromCurrentImageContext() else { return nil }
        UIGraphicsEndImageContext()
        guard let cgImage = image.cgImage else { return nil }
        self.init(cgImage: cgImage)
    }

}

// MARK: - UITableViewCell

extension UITableViewCell {

    func removeSeparatorInset() {
        preservesSuperviewLayoutMargins = false
        layoutMargins = UIEdgeInsets.zero
        separatorInset = UIEdgeInsets.zero
    }
    
}
