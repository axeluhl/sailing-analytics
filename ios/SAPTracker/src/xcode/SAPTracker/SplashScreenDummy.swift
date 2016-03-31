//
//  SplashScreenDummy.swift
//  SAPTracker
//
//  Created by computing on 02/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

final class SplashScreenDummy: UIViewController {

	override func viewDidAppear(animated: Bool) {
		super.viewDidAppear(animated)
		
		dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(2 * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) { () -> Void in
			let storyboard = UIStoryboard(name: "Main", bundle: nil)
			let vc = storyboard.instantiateViewControllerWithIdentifier("rootViewController") 
			vc.modalTransitionStyle = .CrossDissolve
			self.presentViewController(vc, animated: true, completion: nil)
		}
	}
}