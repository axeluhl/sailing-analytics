//
//  TitleView.swift
//  SAPTracker
//
//  Created by Raimund Wege on 03.08.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TitleView: UIView {
    
    fileprivate struct View {
        static let TitleLabel = "titleLabel"
        static let SubtitleLabel = "subtitleLabel"
    }
    
    fileprivate var titleLabel: UILabel
    fileprivate var subtitleLabel: UILabel
    
    // MARK: - Life cycle
    
    override init(frame: CGRect) {
        titleLabel = UILabel(frame: CGRect.zero)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.backgroundColor = .clear
        titleLabel.textAlignment = .center
        titleLabel.textColor = Colors.NavigationBarTitleColor
        titleLabel.font = UIFont.boldSystemFont(ofSize: 13)
        subtitleLabel = UILabel(frame: CGRect.zero)
        subtitleLabel.translatesAutoresizingMaskIntoConstraints = false
        subtitleLabel.backgroundColor = .clear
        subtitleLabel.textAlignment = .center
        subtitleLabel.textColor = Colors.NavigationBarTitleColor
        subtitleLabel.font = UIFont.systemFont(ofSize: 10)
        super.init(frame: frame)
        translatesAutoresizingMaskIntoConstraints = true
        addSubviews()
    }
    
    convenience init() {
        self.init(frame: CGRect.zero)
    }
    
    convenience init(title: String, subtitle: String) {
        self.init(frame: CGRect.zero)
        setTitle(title: title)
        setSubtitle(subtitle: subtitle)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("TitleView does not support NSCoding")
    }
    
    fileprivate func addSubviews() {
        addSubview(titleLabel)
        addSubview(subtitleLabel)
    }
    
    // MARK: - Methods
    
    func setTitle(title: String) {
        titleLabel.text = title
        titleLabel.sizeToFit()
        sizeToFit()
        setNeedsUpdateConstraints()
    }
    
    func setSubtitle(subtitle: String) {
        subtitleLabel.text = subtitle
        subtitleLabel.sizeToFit()
        sizeToFit()
        setNeedsUpdateConstraints()
    }
    
    // MARK: - Layout
    
    override func updateConstraints() {
        removeConstraints(self.constraints)
        let views = [View.TitleLabel: titleLabel, View.SubtitleLabel: subtitleLabel]
        addConstraints(NSLayoutConstraint.constraints(
            withVisualFormat: "H:|[\(View.TitleLabel)]|",
            options: [],
            metrics: nil,
            views: views
        ))
        addConstraints(NSLayoutConstraint.constraints(
            withVisualFormat: "H:|[\(View.SubtitleLabel)]|",
            options: [],
            metrics: nil,
            views: views
        ))
        addConstraints(NSLayoutConstraint.constraints(
            withVisualFormat: "V:|[\(View.TitleLabel)][\(View.SubtitleLabel)]|",
            options: [],
            metrics: nil,
            views: views
        ))
        super.updateConstraints()
    }
    
    override func sizeThatFits(_ size: CGSize) -> CGSize {
        let width = max(titleLabel.bounds.width, subtitleLabel.bounds.width)
        let height = titleLabel.bounds.height + subtitleLabel.bounds.height
        return CGSize(width: width, height: height)
    }
    
}
