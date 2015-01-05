//
//  SplashScreenDummy.swift
//  SAPTracker
//
//  Created by computing on 02/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

/* Helper class used for created launch screens. */
class SplashScreenDummy: UIViewController {
 
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // add logo to top left
        let imageView = UIImageView(image: UIImage(named: "sap_logo"))
        let barButtonItem = UIBarButtonItem(customView: imageView)
        navigationItem.leftBarButtonItem = barButtonItem

        //UIApplication.sharedApplication().statusBarHidden = true
     }
}

/* Uncomment this when creating launch screens. */
/*
extension UINavigationBar {
    public override func sizeThatFits(size: CGSize) -> CGSize {
        let newSize = CGSizeMake(UIScreen.mainScreen().bounds.width,  64)
        return newSize
    }
}
*/