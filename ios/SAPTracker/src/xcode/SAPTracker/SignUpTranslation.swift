//
//  SignUpTranslation.swift
//  SAPTracker
//
//  Created by Raimund Wege on 10.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class SignUpTranslation: NSObject {
    
    fileprivate static let TableName = "SignUp"
    
    // MARK: - Common
    
    struct Common {
        struct Email {
            static let String = NSLocalizedString("Common.Email", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct Password {
            static let String = NSLocalizedString("Common.Password", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct Server {
            static let String = NSLocalizedString("Common.Server", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct UserName {
            static let String = NSLocalizedString("Common.UserName", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
    }
    
    // MARK: - LoginView
    
    struct LoginView {
        struct Title {
            static let String = NSLocalizedString("LoginView.Title", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct LoginButton {
            struct Title {
                static let String = NSLocalizedString("LoginView.LoginButton.Title", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
    }
    
    // MARK: - ForgotPasswordView
    
    struct ForgotPasswordView {
        struct Title {
            static let String = NSLocalizedString("ForgotPasswordView.Title", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct InfoLabel {
            struct Text {
                static let String = NSLocalizedString("ForgotPasswordView.InfoLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct UserNameLabel {
            struct Text {
                static let String = NSLocalizedString("ForgotPasswordView.UserNameLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct EmailLabel {
            struct Text {
                static let String = NSLocalizedString("ForgotPasswordView.EmailLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct ResetPasswordButton {
            struct Title {
                static let String = NSLocalizedString("ForgotPasswordView.ResetPasswordButton.Title", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
    }
    
    // MARK: - SignUpControllerError
    
    struct SignUpControllerError {
        struct LoginFailed {
            static let String = NSLocalizedString("SignUpControllerError.LoginFailed", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct Unauthenticated {
            static let String = NSLocalizedString("SignUpControllerError.Unauthenticated", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
    }
    
    // MARK: - SignUpView
    
    struct SignUpView {
        struct Title {
            static let String = NSLocalizedString("SignUpView.Title", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct InfoLabel {
            struct Text {
                static let String = NSLocalizedString("SignUpView.InfoLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct FullNameLabel {
            struct Text {
                static let String = NSLocalizedString("SignUpView.FullNameLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct CompanyLabel {
            struct Text {
                static let String = NSLocalizedString("SignUpView.CompanyLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct PasswordTextField {
            struct Placeholder {
                static let String = NSLocalizedString("SignUpView.PasswordTextField.Placeholder", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct RepeatPasswordLabel {
            struct Text {
                static let String = NSLocalizedString("SignUpView.RepeatPasswordLabel.Text", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct RepeatPasswordTextField {
            struct Placeholder {
                static let String = NSLocalizedString("SignUpView.RepeatPasswordTextField.Placeholder", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
        struct SignUpButton {
            struct Title {
                static let String = NSLocalizedString("SignUpView.SignUpButton.Title", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
            }
        }
    }
    
    // MARK: SignUpViewError
    
    struct SignUpViewError {
        struct UserNameIsToShort {
            static let String = NSLocalizedString("SignUpViewError.UserNameIsToShort", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct PasswordIsToShort {
            static let String = NSLocalizedString("SignUpViewError.PasswordIsToShort", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
        struct PasswordsNotEqual {
            static let String = NSLocalizedString("SignUpViewError.PasswordsNotEqual", tableName: TableName, bundle: Bundle.main, value: "", comment: "")
        }
    }
    
}

                
