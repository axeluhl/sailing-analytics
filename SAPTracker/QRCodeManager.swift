//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 17/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import UIKit
import AVFoundation

@objc protocol QRCodeManagerDelegate {
    optional var activityIndicatorView: UIActivityIndicatorView { get }
    optional func qrCodeOK()
    optional func qrCodeCancel()
}

class QRCodeManager: NSObject, UIAlertViewDelegate {
    
    enum AlertView: Int {
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
    
    class func deviceCanReadQRCodes() -> Bool {
        var session: AVCaptureSession!
        var output: AVCaptureMetadataOutput!
        (session, output) = QRCodeManager.setUpCaptureSession(nil)
        let types = output.availableMetadataObjectTypes as [String]
        var deviceCanReadQRCodes = false
        for type in types {
            if type == AVMetadataObjectTypeQRCode {
                deviceCanReadQRCodes = true
                break
            }
        }
        return deviceCanReadQRCodes
    }

    class func setUpCaptureSession(delegate: AVCaptureMetadataOutputObjectsDelegate?) -> (session: AVCaptureSession!, output: AVCaptureMetadataOutput!) {
        var device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        var writeError : NSError? = nil
        var input = AVCaptureDeviceInput.deviceInputWithDevice(device, error: &writeError) as? AVCaptureDeviceInput
        
        var output = AVCaptureMetadataOutput()
        output.setMetadataObjectsDelegate(delegate, queue: dispatch_get_main_queue())
        
        let session = AVCaptureSession()
        session.canSetSessionPreset(AVCaptureSessionPresetHigh)
        if session.canAddInput(input) {
            session.addInput(input)
        }
        if session.canAddOutput(output) {
            session.addOutput(output)
        }
        return (session, output)
    }

    func parseUrl(url: NSString) {
        
        qrcodeData = QRCodeData()
        let parseSuccess = qrcodeData!.parseString(url)
        if !parseSuccess {
            let alertView = UIAlertView(title: "Incorrect QR Code", message: "", delegate: self, cancelButtonTitle: nil, otherButtonTitles: "OK")
            alertView.tag = AlertView.IncorrectQRCode.rawValue;
            alertView.show()
            return
        }
        
        self.delegate.activityIndicatorView?.startAnimating()
        APIManager.sharedManager.initManager(qrcodeData!.serverUrl!)
        
        // get event
        APIManager.sharedManager.getEvent(qrcodeData!.eventId,
            success: { (AFHTTPRequestOperation operation, AnyObject eventResponseObject) -> Void in
                self.eventDictionary = eventResponseObject as? [String: AnyObject]
                APIManager.sharedManager.getLeaderBoard(self.qrcodeData!.leaderBoardName,
                    
                    // get leader board
                    success: { (AFHTTPRequestOperation operation, AnyObject leaderBoardResponseObject) -> Void in
                        self.leaderBoardDictionary = leaderBoardResponseObject as? [String: AnyObject]
                        APIManager.sharedManager.getCompetitor(self.qrcodeData!.competitorId,
                            
                            // get competitor
                            success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                                self.delegate.activityIndicatorView?.stopAnimating()
                                
                                self.competitorDictionary = competitorResponseObject as? [String: AnyObject]
                                let competitorName = (self.competitorDictionary!["name"]) as String
                                let leaderBoardName = (self.leaderBoardDictionary!["name"]) as String
                                let sailId = (self.competitorDictionary!["sailID"]) as String
                                var title = "Hello \(competitorName). Welcome to \(leaderBoardName). You are registered as \(sailId)."
                                let alertView = UIAlertView(title: title, message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "OK")
                                alertView.tag = AlertView.AcceptMapping.rawValue;
                                alertView.show()
                            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                                self.delegate.activityIndicatorView?.stopAnimating()
                                let alertView = UIAlertView(title: "Couldn't get competitor \(self.qrcodeData!.competitorId!)", message: error.localizedDescription, delegate: self, cancelButtonTitle: "Cancel")
                                alertView.tag = AlertView.ServerError.rawValue;
                                alertView.show()
                                
                        }) }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                            self.delegate.activityIndicatorView?.stopAnimating()
                            let alertView = UIAlertView(title: "Couldn't get leader board \(self.qrcodeData!.leaderBoardName!.stringByReplacingPercentEscapesUsingEncoding(NSUTF8StringEncoding))", message: error.localizedDescription, delegate: self, cancelButtonTitle: "Cancel")
                            alertView.tag = AlertView.ServerError.rawValue;
                            alertView.show()
                })
                
            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                self.delegate.activityIndicatorView?.stopAnimating()
                let alertView = UIAlertView(title: "Couldn't get event \(self.qrcodeData!.eventId!)", message: error.localizedDescription, delegate: self, cancelButtonTitle: "Cancel")
                alertView.tag = AlertView.ServerError.rawValue;
                alertView.show()
        })
    }
    
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Stop tracking?
        case AlertView.AcceptMapping.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                cancel()
                break
            default:
                checkIn()
                break
            }
            break
        default:
            cancel()
            break
        }
    }
    
    private func checkIn() {
        let leaderBoardName = leaderBoardDictionary!["name"] as String
        let competitorId = competitorDictionary!["id"] as String
        let now = NSDate()
        let fromMillis = Int64(now.timeIntervalSince1970 * 1000)
        let checkIn = DataManager.sharedManager.newCheckIn()
        checkIn.serverUrl = self.qrcodeData!.serverUrl!
        checkIn.eventId = self.qrcodeData!.eventId!
        checkIn.leaderBoardName = self.qrcodeData!.leaderBoardName!.stringByReplacingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
        checkIn.competitorId = self.qrcodeData!.competitorId!
        checkIn.lastSyncDate = NSDate()
        
        // TODO: add push token
        APIManager.sharedManager.checkIn(leaderBoardName, competitorId: competitorId, deviceUuid: DeviceUDIDManager.UDID, pushDeviceId: "", fromMillis: fromMillis,
            success: { (AFHTTPRequestOperation operation, AnyObject eventResponseObject) -> Void in
                
                // create core data objects
                var event = DataManager.sharedManager.newEvent(checkIn)
                event.initWithDictionary(self.eventDictionary!)
                var leaderBoard = DataManager.sharedManager.newLeaderBoard(checkIn)
                leaderBoard.initWithDictionary(self.leaderBoardDictionary!)
                var competitor = DataManager.sharedManager.newCompetitor(checkIn)
                competitor.initWithDictionary(self.competitorDictionary!)
                DataManager.sharedManager.saveContext()

                self.delegate.qrCodeOK?()
            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                self.delegate.activityIndicatorView?.stopAnimating()
                let alertView = UIAlertView(title: "Couldn't check-in to " + leaderBoardName, message: error.localizedDescription, delegate: self, cancelButtonTitle: "Cancel")
                alertView.tag = AlertView.ServerError.rawValue;
                alertView.show()
        })
    }

    private func cancel() {
        eventDictionary = nil
        leaderBoardDictionary = nil
        competitorDictionary = nil
        delegate.qrCodeCancel?()
    }
    
}