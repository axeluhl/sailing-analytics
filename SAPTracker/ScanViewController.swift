//
//  QRCodeViewController.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

class ScanViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    struct NotificationType {
        static let qrcodeScannedNotificationKey = "qrcode_scanned"
    }
    
    @IBOutlet weak var previewView: UIView!
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
                var alert = UIAlertController(title: "SAP Tracker", message: "Incorrect QR Code", preferredStyle: UIAlertControllerStyle.Alert)
                alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
                self.presentViewController(alert, animated: true, completion: nil)
                return
            }
            // TODO: store qrcodeData somehow
            session.stopRunning()
            APIManager.sharedManager.initManager(qrcodeData.server!)
            APIManager.sharedManager.postDeviceMapping(qrcodeData,
                success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
                    var alert = UIAlertController(title: "SAP Tracker", message: "Connected to Server \(qrcodeData.server!)", preferredStyle: UIAlertControllerStyle.Alert)
                    alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default) { UIAlertAction in
                        self.dismiss()
                        })
                    self.presentViewController(alert, animated: true, completion: nil)
                    NSLog("success")
                }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                    var alert = UIAlertController(title: "SAP Tracker", message: "Couldn't connect to Server \(qrcodeData.server!)", preferredStyle: UIAlertControllerStyle.Alert)
                    alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertActionStyle.Cancel) { UIAlertAction in
                        self.session.startRunning()
                        })
                    self.presentViewController(alert, animated: true, completion: nil)
                    NSLog("failure")
            })
        }
    }
    
    func dismiss() {
        navigationController!.popViewControllerAnimated(true)
    }
}