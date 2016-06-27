//
//  GradientView.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

@IBDesignable class GradientView: UIView {
    
    @IBInspectable var firstColor: UIColor = UIColor.whiteColor()
    @IBInspectable var secondColor: UIColor = UIColor.blackColor()
    
    override class func layerClass() -> AnyClass {
        return CAGradientLayer.self
    }
    
    override func layoutSubviews() {
        (layer as! CAGradientLayer).colors = [firstColor.CGColor, secondColor.CGColor]
    }
    
}
