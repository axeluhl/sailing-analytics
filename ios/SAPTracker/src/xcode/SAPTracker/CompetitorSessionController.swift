//
//  CompetitorSessionController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CompetitorSessionController: SessionController {

    // MARK: - TeamImage
    
    func postTeamImageData(
        imageData: Data,
        competitorID: String,
        success: @escaping (_ teamImageURL: String) -> Void,
        failure: @escaping (_ error: Error) -> Void
    ) {
        checkInRequestManager.postTeamImageData(
            imageData: imageData,
            competitorID: competitorID,
            success: success,
            failure: failure
        )
    }

}
