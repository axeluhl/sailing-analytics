//
//  BoatViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.03.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//

import UIKit

class BoatViewController: UIViewController {

    weak var boatCheckIn: BoatCheckIn!
    weak var boatCoreDataManager: CoreDataManager!
    weak var boatSessionController: BoatSessionController!

    @IBOutlet weak var boatNameView: UIView!
    @IBOutlet weak var boatNameLabel: UILabel!

    // MARK: - Refresh

    func refresh(_ animated: Bool) {
        refreshBoatNameLabel(animated)
    }

    fileprivate func refreshBoatNameLabel(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshBoatNameLabel() }
        } else {
            refreshBoatNameLabel()
        }
    }

    fileprivate func refreshBoatNameLabel() {
        boatNameLabel.text = boatCheckIn.displayName()
        if let color = UIColor.init(hexString: boatCheckIn.color) {
            boatNameView.backgroundColor = color
            boatNameLabel.textColor = UIColor.init(contrastColorFor: color)
        }
    }

}
