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

// MARK: - UIButton

extension UIButton {
    
    func updateLayer(cornerRadius: CGFloat) {
        layer.cornerRadius = cornerRadius
        layer.masksToBounds = true
    }
    
    func updateLayer(cornerRadius: CGFloat, borderColor: UIColor) {
        updateLayer(cornerRadius: cornerRadius)
        layer.borderWidth = 1
        layer.borderColor = borderColor.cgColor
    }
    
}

// MARK: - UIColor

extension UIColor { // Set color to RGB hex value. See http://stackoverflow.com/a/24263296
    
    convenience init(hex:Int) {
        self.init(hex: hex, alpha: 1)
    }
    
    convenience init(hex:Int, alpha: CGFloat) {
        let red = CGFloat((hex >> 16) & 0xff) / 255
        let green = CGFloat((hex >> 8) & 0xff) / 255
        let blue = CGFloat(hex & 0xff) / 255
        self.init(red: red, green: green, blue: blue, alpha: alpha)
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

// MARK: - UIViewController

extension UIViewController {
    
    func makeViewRoundWithShadow(_ view: UIView) {
        
        // Corner radius
        view.layer.cornerRadius = view.frame.height / 2
        
        // Border
        view.layer.borderWidth = 1.0
        view.layer.borderColor = UIColor.black.cgColor
        
        // Shadow
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOffset = CGSize(width: 3, height: 3)
        view.layer.shadowOpacity = 0.2
        view.layer.shadowRadius = 4.0
    }
    
    func makeBlue(button: UIButton) {
        button.setTitleColor(Colors.BlueButtonTitleColor, for: .normal)
        button.setBackgroundImage(Images.BlueButton, for: .normal)
        button.setBackgroundImage(Images.BlueButtonHighlighted, for: .highlighted)
        button.updateLayer(cornerRadius: 1)
    }
    
    func makeGray(button: UIButton) {
        button.setTitleColor(Colors.GrayButtonTitleColor, for: .normal)
        button.setBackgroundImage(Images.GrayButton, for: .normal)
        button.setBackgroundImage(Images.GrayButtonHighlighted, for: .highlighted)
        button.updateLayer(cornerRadius: 1, borderColor: Colors.GrayButtonBorder)
    }
    
    func makeGreen(button: UIButton) {
        button.setTitleColor(Colors.GreenButtonTitleColor, for: .normal)
        button.setBackgroundImage(Images.GreenButton, for: .normal)
        button.setBackgroundImage(Images.GreenButtonHighlighted, for: .highlighted)
        button.updateLayer(cornerRadius: 1)
    }
    
    func makeRed(button: UIButton) {
        button.setTitleColor(Colors.RedButtonTitleColor, for: .normal)
        button.setBackgroundImage(Images.RedButton, for: .normal)
        button.setBackgroundImage(Images.RedButtonHighlighted, for: .highlighted)
        button.updateLayer(cornerRadius: 1)
    }
    
    func makeTranslucent(button: UIButton) {
        button.setTitleColor(Colors.TranslucentButtonTitleColor, for: .normal)
        button.setBackgroundImage(Images.TranslucentButton, for: .normal)
        button.setBackgroundImage(Images.TranslucentButtonHighlighted, for: .highlighted)
        button.updateLayer(cornerRadius: 1, borderColor: Colors.TranslucentButtonBorder)
    }
    
    func presentAboutViewController() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        guard let aboutNC = storyboard.instantiateViewController(withIdentifier: "AboutNavigationController") as? UINavigationController else { return }
        present(aboutNC, animated: true)
    }
    
    func presentSettingsViewController() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        guard let aboutNC = storyboard.instantiateViewController(withIdentifier: "SettingsNavigationController") as? UINavigationController else { return }
        present(aboutNC, animated: true)

    }
    
    func showAlert(forError error: Error) {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: error.localizedDescription,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
}
