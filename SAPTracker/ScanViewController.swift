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
        case IncorrectQRCode, ConnectedToServer, ServerError
    }
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var activityView: UIActivityIndicatorView!
    
    var session: AVCaptureSession!
    var previewLayer: AVCaptureVideoPreviewLayer!
    
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
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as AVMetadataMachineReadableCodeObject
            println(metadataObject.stringValue)
            
            var qrcodeData = QRCodeData()
            if !qrcodeData.parseString(metadataObject.stringValue) {
                let alertView = UIAlertView(title: "Incorrect QR Code", message: "", delegate: self, cancelButtonTitle: nil, otherButtonTitles: "OK")
                alertView.tag = AlertViewTag.IncorrectQRCode.rawValue;
                alertView.show()
            }
            // TODO: store qrcodeData somehow
            session.stopRunning()
            activityView.startAnimating()
            APIManager.sharedManager.initManager(qrcodeData.server!)
            APIManager.sharedManager.getEvent(qrcodeData.eventId,
                success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
                    var event = DataManager.sharedManager.event(qrcodeData.eventId!)
                    event.initWithDictionary(responseObject as Dictionary)
                    APIManager.sharedManager.getLeaderboard(qrcodeData.leaderboardName,
                        success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
                            var leaderboard = DataManager.sharedManager.leaderboard(event)
                            leaderboard.initWithDictionary(responseObject as Dictionary)
                            APIManager.sharedManager.getCompetitor(qrcodeData.competitorId,
                                success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
                                    self.activityView.stopAnimating()
                                    var competitor = DataManager.sharedManager.competitor(leaderboard)
                                    competitor.initWithDictionary(responseObject as Dictionary)
                                    navigationController!.popViewControllerAnimated(true)
                                }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                                    self.activityView.stopAnimating()
                                    let alertView = UIAlertView(title: "Couldn't get competitor \(qrcodeData.competitorId)", message: nil, delegate: self, cancelButtonTitle: "Cancel")
                                    alertView.tag = AlertViewTag.ServerError.rawValue;
                                    alertView.show()
                            }) }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                                self.activityView.stopAnimating()
                                let alertView = UIAlertView(title: "Couldn't get leaderboard \(qrcodeData.leaderboardName)", message: nil, delegate: self, cancelButtonTitle: "Cancel")
                                alertView.tag = AlertViewTag.ServerError.rawValue;
                                alertView.show()
                    })
                    
                }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                    self.activityView.stopAnimating()
                    let alertView = UIAlertView(title: "Couldn't get event \(qrcodeData.eventId)", message: nil, delegate: self, cancelButtonTitle: "Cancel")
                    alertView.tag = AlertViewTag.ServerError.rawValue;
                    alertView.show()
            })
        }
    }
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Stop tracking?
        case AlertViewTag.ConnectedToServer.rawValue:
            navigationController!.popViewControllerAnimated(true)
            break
        case AlertViewTag.ServerError.rawValue:
            self.session.startRunning()
            break
        default:
            
            break
        }
    }
    
    
}