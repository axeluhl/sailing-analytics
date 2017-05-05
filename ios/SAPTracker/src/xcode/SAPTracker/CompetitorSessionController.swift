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
        imageData: NSData,
        competitorID: String,
        success: (teamImageURL: String) -> Void,
        failure: (error: RequestManager.Error) -> Void
    ) {
        requestManager.postTeamImageData(
            imageData,
            competitorID: competitorID,
            success: success,
            failure: failure
        )
    }

}
