//
//  TitleView.swift
//  SAPTracker
//
//  Created by Raimund Wege on 03.08.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TitleView: UIView {
    
    private struct View {
        static let TitleLabel = "titleLabel"
        static let SubtitleLabel = "subtitleLabel"
    }
    
    private var titleLabel: UILabel
    private var subtitleLabel: UILabel
    
    // MARK: - Life cycle
    
    override init(frame: CGRect) {
        titleLabel = UILabel(frame: CGRectZero)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.backgroundColor = .clearColor()
        titleLabel.textAlignment = .Center
        titleLabel.textColor = Colors.NavigationBarTitleColor
        titleLabel.font = Fonts.OpenSansBold13
        subtitleLabel = UILabel(frame: CGRectZero)
        subtitleLabel.translatesAutoresizingMaskIntoConstraints = false
        subtitleLabel.backgroundColor = .clearColor()
        subtitleLabel.textAlignment = .Center
        subtitleLabel.textColor = Colors.NavigationBarTitleColor
        subtitleLabel.font = Fonts.OpenSans10
        super.init(frame: frame)
        translatesAutoresizingMaskIntoConstraints = true
        addSubviews()
    }
    
    convenience init() {
        self.init(frame: CGRectZero)
    }
    
    convenience init(title: String, subtitle: String) {
        self.init(frame: CGRectZero)
        setTitle(title)
        setSubtitle(subtitle)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("TitleView does not support NSCoding")
    }
    
    private func addSubviews() {
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
        addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[\(View.TitleLabel)]|",
            options: [],
            metrics: nil,
            views: views))
        addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[\(View.SubtitleLabel)]|",
            options: [],
            metrics: nil,
            views: views))
        addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[\(View.TitleLabel)][\(View.SubtitleLabel)]|",
            options: [],
            metrics: nil,
            views: views))
        super.updateConstraints()
    }
    
    override func sizeThatFits(size: CGSize) -> CGSize {
        let width = max(CGRectGetWidth(titleLabel.bounds), CGRectGetWidth(subtitleLabel.bounds))
        let height = CGRectGetHeight(titleLabel.bounds) + CGRectGetHeight(subtitleLabel.bounds)
        return CGSizeMake(width, height)
    }
    
}
