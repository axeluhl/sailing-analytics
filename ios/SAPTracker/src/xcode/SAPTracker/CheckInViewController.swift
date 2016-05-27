//
//  CheckInViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 26.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class CheckInViewController: UIViewController, CheckInControllerDelegate {
    
    func checkIn(checkInData: CheckInData) {
        let checkInController = CheckInController(checkInData: checkInData, delegate: self)
        checkInController.startCheckIn()
    }
    
    func checkInDidStart(checkInController: CheckInController) {

    }
    
    func checkInDidEnd(checkInController: CheckInController, withSuccess succeed: Bool) {

    }
    
    func showCheckInAlert(checkInController: CheckInController, alertController: UIAlertController) {
        self.presentViewController(alertController, animated: true, completion: nil)
    }
    
}
