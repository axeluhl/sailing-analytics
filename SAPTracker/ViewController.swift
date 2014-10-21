//
//  ViewController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

class ViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {

    
    @IBOutlet weak var trackingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        addObservers()

        // TODO: move someplace else, e.g. app delegate
        DataManager.sharedManager
    }

    deinit {
        removeObservers()
    }
    
    func addObservers() {
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStarted:", name: LocationManager.NotificationType.trackingStartedNotificationKey, object: nil)
   }
    
    func removeObservers() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }

    func trackingStarted(notification: NSNotification) {
        println("trackingStarted")
        trackingButton.setTitle("Stop Tracking", forState: UIControlState.Normal)
    }
    
    func trackingStopped(notification: NSNotification) {
        println("trackingStarted")
        trackingButton.setTitle("Start Tracking", forState: UIControlState.Normal)
    }

    @IBAction func trackingButtonTap(sender: AnyObject) {
        if LocationManager.sharedManager.isTracking {
            LocationManager.sharedManager.stopTracking()
        } else {
            LocationManager.sharedManager.startTracking()
        }
    }
  
    // MARK - QR Code
    var session: AVCaptureSession!
    var preview: AVCaptureVideoPreviewLayer!

    @IBAction func qrCode(sender: AnyObject) {
        var device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        var writeError : NSError? = nil
        var input = AVCaptureDeviceInput.deviceInputWithDevice(device, error: &writeError) as? AVCaptureDeviceInput
        
        var output = AVCaptureMetadataOutput()
        output.setMetadataObjectsDelegate(self,queue: dispatch_get_main_queue())
        
        session = AVCaptureSession()
        session.canSetSessionPreset(AVCaptureSessionPresetHigh)
        if session.canAddInput(input) {
            session.addInput(input)
        }
        if session.canAddOutput(output){
            session.addOutput(output)
        }
        output.metadataObjectTypes = [AVMetadataObjectTypeEAN13Code,AVMetadataObjectTypeEAN8Code,AVMetadataObjectTypeQRCode]

        preview = AVCaptureVideoPreviewLayer(session: session)
        preview.videoGravity = AVLayerVideoGravityResizeAspectFill
        preview.frame = CGRectMake(0,0,self.view.frame.size.width,self.view.frame.size.height)
        self.view.layer.addSublayer(preview)
        
        session.startRunning()
    }
    
    
    func captureOutput(captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [AnyObject]!, fromConnection connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as AVMetadataMachineReadableCodeObject
            session.stopRunning()
            preview.removeFromSuperlayer()
            println(metadataObject.stringValue)
        }
    }

}

