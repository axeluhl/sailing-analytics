//
//  ModeTrackingTableViewCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class ModeTrackingTableViewCell: UITableViewCell {

    @IBOutlet weak var modeLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.setupModeLabel(RegattaController.Mode.Offline)
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupModeLabel(mode: RegattaController.Mode) {
        modeLabel.text = mode.description
    }
    
    // MARK: - Notifications

    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(regattaControllerModeChanged),
                                                         name:RegattaController.NotificationType.ModeChanged,
                                                         object: nil
        )
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func regattaControllerModeChanged(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let mode = notification.userInfo?[RegattaController.UserInfo.Mode] as? RegattaController.Mode else { return }
            self.setupModeLabel(mode)
        })
    }
    
}
