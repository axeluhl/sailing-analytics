//
//  QRCodeViewController.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

class ScanViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate, UIAlertViewDelegate {
    
    enum AlertViewTag: Int {
        case IncorrectQRCode, AcceptMapping, ServerError
    }
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var activityView: UIActivityIndicatorView!
    
    private var session: AVCaptureSession!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    
    private var qrcodeData: QRCodeData?
    private var eventDictionary: [String: AnyObject]?
    private var leaderBoardDictionary: [String: AnyObject]?
    private var competitorDictionary: [String: AnyObject]?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        startScanning()
    }
    
    /* Set up camera and QR code scanner */
    @IBAction func startScanning() {
        var device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        var writeError : NSError? = nil
        var input = AVCaptureDeviceInput.deviceInputWithDevice(device, error: &writeError) as? AVCaptureDeviceInput
        
        var output = AVCaptureMetadataOutput()
        output.setMetadataObjectsDelegate(self, queue: dispatch_get_main_queue())
        
        session = AVCaptureSession()
        session.canSetSessionPreset(AVCaptureSessionPresetHigh)
        if session.canAddInput(input) {
            session.addInput(input)
        }
        if session.canAddOutput(output) {
            session.addOutput(output)
        }
        output.metadataObjectTypes = [AVMetadataObjectTypeEAN13Code,AVMetadataObjectTypeEAN8Code,AVMetadataObjectTypeQRCode]
        
        previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        previewLayer.frame = previewView.bounds
        previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds));
        previewView.layer.addSublayer(previewLayer)
        
        session.startRunning()
    }
    
    /* AVCaptureMetadataOutputObjectsDelegate. Parse scanned QR code */
    func captureOutput(captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [AnyObject]!, fromConnection connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            session.stopRunning()
            activityView.startAnimating()
            
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as AVMetadataMachineReadableCodeObject
            println(metadataObject.stringValue)
            
            qrcodeData = QRCodeData()
            let parseSuccess = qrcodeData!.parseString(metadataObject.stringValue)
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
                    APIManager.sharedManager.getLeaderBoard(self.qrcodeData!.leaderBoardName,
                        
                        // get leader board
                        success: { (AFHTTPRequestOperation operation, AnyObject leaderBoardResponseObject) -> Void in
                            self.leaderBoardDictionary = leaderBoardResponseObject as? [String: AnyObject]
                            APIManager.sharedManager.getCompetitor(self.qrcodeData!.competitorId,
                                
                                // get competitor
                                success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                                    self.activityView.stopAnimating()
                                    
                                    self.competitorDictionary = competitorResponseObject as? [String: AnyObject]
                                    var title = "Hello "
                                    title += (self.competitorDictionary!["displayName"]) as String
                                    title += ". Welcome to "
                                    title += (self.leaderBoardDictionary!["name"]) as String
                                    title += ". You are registered as "
                                    title += (self.competitorDictionary!["sailID"]) as String
                                    title += "."
                                    let alertView = UIAlertView(title: title, message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "OK")
                                    alertView.tag = AlertViewTag.AcceptMapping.rawValue;
                                    alertView.show()
                                }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                                    self.activityView.stopAnimating()
                                    let alertView = UIAlertView(title: "Couldn't get competitor \(self.qrcodeData!.competitorId!)", message: "", delegate: self, cancelButtonTitle: "Cancel")
                                    alertView.tag = AlertViewTag.ServerError.rawValue;
                                    alertView.show()
                                    
                            }) }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                                self.activityView.stopAnimating()
                                let alertView = UIAlertView(title: "Couldn't get leader board \(self.qrcodeData!.leaderBoardName!.stringByReplacingPercentEscapesUsingEncoding(NSUTF8StringEncoding))", message: "", delegate: self, cancelButtonTitle: "Cancel")
                                alertView.tag = AlertViewTag.ServerError.rawValue;
                                alertView.show()
                    })
                    
                }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                    self.activityView.stopAnimating()
                    let alertView = UIAlertView(title: "Couldn't get event \(self.qrcodeData!.eventId!)", message: "", delegate: self, cancelButtonTitle: "Cancel")
                    alertView.tag = AlertViewTag.ServerError.rawValue;
                    alertView.show()
            })
        }
    }
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Stop tracking?
        case AlertViewTag.AcceptMapping.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
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
                
                // pop back to home view
                navigationController!.popViewControllerAnimated(true)
                break
            }
            break
        default:
            self.session.startRunning()
            eventDictionary = nil
            leaderBoardDictionary = nil
            competitorDictionary = nil
            break
        }
    }
    
    
}