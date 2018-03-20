//
//  RegattaCheckInController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaCheckInController: CheckInController {

    override init(coreDataManager: CoreDataManager) {
        super.init(coreDataManager: coreDataManager)
        delegate = self
    }
    
}

extension RegattaCheckInController: CheckInControllerDelegate {
    
    func checkInController(
        _ controller: CheckInController,
        postCompetitorCheckIn checkInData: CheckInData,
        success: @escaping (CheckIn) -> Void,
        failure: @escaping (Error) -> Void)
    {
        let title = String(format: Translation.RegattaCheckInController.WelcomeAlert.Title.String, checkInData.competitorData.name)
        let message = String(format: Translation.RegattaCheckInController.WelcomeAlert.Message.String, checkInData.competitorData.sailID)
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { (action) in
            controller.postCheckIn(checkInData: checkInData, success: success, failure: failure)
        }
        let cancelAction = UIAlertAction(title: Translation.RegattaCheckInController.WelcomeAlert.CancelAction.Title.String, style: .cancel) { (action) in
            controller.didCancelCheckIn(failure: failure)
        }
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        viewController?.present(alertController, animated: true)
    }
    
}
