//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 17/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import UIKit

@objc protocol QRCodeManagerDelegate {
    optional var activityIndicatorView: UIActivityIndicatorView { get }
    optional func qrCodeOK()
    optional func qrCodeCancel()
}

class QRCodeManager: NSObject, UIAlertViewDelegate {
    
    enum AlertViewTag: Int {
        case IncorrectQRCode, AcceptMapping, ServerError
    }
    
    private var delegate: QRCodeManagerDelegate
    private var qrcodeData: QRCodeData?
    private var eventDictionary: [String: AnyObject]?
    private var leaderBoardDictionary: [String: AnyObject]?
    private var competitorDictionary: [String: AnyObject]?
    
    init(delegate: QRCodeManagerDelegate) {
        self.delegate = delegate
    }
    
    func parseUrl(url: NSString) {
        self.delegate.activityIndicatorView?.startAnimating()
        
        qrcodeData = QRCodeData()
        let parseSuccess = qrcodeData!.parseString(url)
        if !parseSuccess {
            let alertView = UIAlertView(title: "Incorrect QR Code", message: "", delegate: self, cancelButtonTitle: nil, otherButtonTitles: "OK")
            alertView.tag = AlertViewTag.IncorrectQRCode.rawValue;
            alertView.show()
            return
        }
        
        APIManager.sharedManager.initManager(qrcodeData!.serverUrl!)
        
        // get event
        APIManager.sharedManager.getEvent(qrcodeData!.eventId,
            success: { (AFHTTPRequestOperation operation, AnyObject eventResponseObject) -> Void in
                self.eventDictionary = eventResponseObject as? [String: AnyObject]
                NSThread.sleepForTimeInterval(10);
                APIManager.sharedManager.getLeaderBoard(self.qrcodeData!.leaderBoardName,
                    
                    // get leader board
                    success: { (AFHTTPRequestOperation operation, AnyObject leaderBoardResponseObject) -> Void in
                        self.leaderBoardDictionary = leaderBoardResponseObject as? [String: AnyObject]
                        APIManager.sharedManager.getCompetitor(self.qrcodeData!.competitorId,
                            
                            // get competitor
                            success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                                self.delegate.activityIndicatorView?.stopAnimating()
                                
                                self.competitorDictionary = competitorResponseObject as? [String: AnyObject]
                                let competitorName = (self.competitorDictionary!["displayName"]) as String
                                let leaderBoardName = (self.leaderBoardDictionary!["name"]) as String
                                let sailId = (self.competitorDictionary!["sailID"]) as String
                                var title = "Hello \(competitorName). Welcome to \(leaderBoardName). You are registered as \(sailId)."
                                let alertView = UIAlertView(title: title, message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "OK")
                                alertView.tag = AlertViewTag.AcceptMapping.rawValue;
                                alertView.show()
                            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                                self.delegate.activityIndicatorView?.stopAnimating()
                                let alertView = UIAlertView(title: "Couldn't get competitor \(self.qrcodeData!.competitorId!)", message: "", delegate: self, cancelButtonTitle: "Cancel")
                                alertView.tag = AlertViewTag.ServerError.rawValue;
                                alertView.show()
                                
                        }) }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                            self.delegate.activityIndicatorView?.stopAnimating()
                            let alertView = UIAlertView(title: "Couldn't get leader board \(self.qrcodeData!.leaderBoardName!.stringByReplacingPercentEscapesUsingEncoding(NSUTF8StringEncoding))", message: "", delegate: self, cancelButtonTitle: "Cancel")
                            alertView.tag = AlertViewTag.ServerError.rawValue;
                            alertView.show()
                })
                
            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                self.delegate.activityIndicatorView?.stopAnimating()
                let alertView = UIAlertView(title: "Couldn't get event \(self.qrcodeData!.eventId!)", message: "", delegate: self, cancelButtonTitle: "Cancel")
                alertView.tag = AlertViewTag.ServerError.rawValue;
                alertView.show()
        })
    }
    
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Stop tracking?
        case AlertViewTag.AcceptMapping.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                cancel()
                break
            default:
                // create core data objects
                var event = DataManager.sharedManager.event(qrcodeData!.eventId!)
                event.serverUrl = qrcodeData!.serverUrl!
                event.initWithDictionary(eventDictionary!)
                var leaderBoard = DataManager.sharedManager.leaderBoard(event)
                leaderBoard.initWithDictionary(leaderBoardDictionary!)
                var competitor = DataManager.sharedManager.competitor(leaderBoard)
                competitor.initWithDictionary(competitorDictionary!)
                DataManager.sharedManager.saveContext()
                
                delegate.qrCodeOK?()
                break
            }
            break
        default:
            cancel()
            break
        }
    }
    
    private func cancel() {
        eventDictionary = nil
        leaderBoardDictionary = nil
        competitorDictionary = nil
        delegate.qrCodeCancel?()
    }
    
}