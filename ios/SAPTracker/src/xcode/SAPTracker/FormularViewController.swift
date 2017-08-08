//
//  FormularViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 08.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class FormularViewController: UIViewController {

    var textFields: [UITextField] = []

    @IBOutlet weak var scrollView: UIScrollView!

    override func viewDidLoad() {
        super.viewDidLoad()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillShowNotification(_:)),
            name: .UIKeyboardWillShow,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillHideNotification(_:)),
            name: .UIKeyboardWillHide,
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func keyboardWillShowNotification(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            if let keyboardSize = notification.userInfo?[UIKeyboardFrameBeginUserInfoKey] as? CGRect {
                let contentInsets = UIEdgeInsets(top: 0, left: 0, bottom: keyboardSize.height, right: 0)
                self.scrollView.contentInset = contentInsets
                self.scrollView.scrollIndicatorInsets = contentInsets
            }
        })
    }
    
    @objc fileprivate func keyboardWillHideNotification(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            let contentInsets = UIEdgeInsets.zero
            self.scrollView.contentInset = contentInsets
            self.scrollView.scrollIndicatorInsets = contentInsets
        })
    }

    // MARK: - Actions

    @IBAction func viewTapped(_ sender: Any) {
        textFields.forEach { (textField) in
            textField.resignFirstResponder()
        }
    }

}

// MARK: - UITextFieldDelegate

extension FormularViewController: UITextFieldDelegate {
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if let nextResponder = textField.superview?.viewWithTag(textField.tag + 1) {
            nextResponder.becomeFirstResponder()
        } else {
            textField.resignFirstResponder()
        }
        return false
    }
    
}
