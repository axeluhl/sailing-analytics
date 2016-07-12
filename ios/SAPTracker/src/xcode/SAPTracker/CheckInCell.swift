//
//  CheckInCell.swift
//  SAPTracker
//
//  Created by P.G.Taboada on 12.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class CheckInCell: UITableViewCell {

    
    @IBOutlet weak var regattaUi: UILabel!
    @IBOutlet weak var eventUi: UILabel!
    @IBOutlet weak var competitorUi: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    
}
