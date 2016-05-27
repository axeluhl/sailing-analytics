//
//  ScanViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

class ScanViewController: CheckInViewController, AVCaptureMetadataOutputObjectsDelegate {
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var targetImageView: UIImageView!
    
    private var session: AVCaptureSession!
    private var output: AVCaptureMetadataOutput!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Setups
        self.setupSession()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // Start scanning
        self.startScanning()
    }
    
    // MARK: - Setups
    
    private func setupSession() {
        self.session = AVCaptureSession()
        self.output = AVCaptureMetadataOutput()
        do {
            let device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            let input = try AVCaptureDeviceInput(device: device);
            self.output.setMetadataObjectsDelegate(self, queue: dispatch_get_main_queue())
            self.session.canSetSessionPreset(AVCaptureSessionPresetHigh)
            if session.canAddInput(input) {
                session.addInput(input)
            }
            if session.canAddOutput(output) {
                session.addOutput(output)
            }
            self.output.metadataObjectTypes = [AVMetadataObjectTypeQRCode]
            self.previewLayer = AVCaptureVideoPreviewLayer(session: self.session)
            self.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            self.previewLayer.frame = previewView.bounds
            self.previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
            self.previewView.layer.addSublayer(previewLayer)
        } catch {
            print(error)
        }
    }
    
    // MARK: - Scanning
    
    private func startScanning() {
        self.session.startRunning()
        self.targetImageView.image = UIImage(named: "scan_white")
    }
    
    private func stopScanning() {
        self.session.stopRunning()
        self.targetImageView.image = UIImage(named: "scan_green")
    }
    
    // MARK: - AVCaptureMetadataOutputObjectsDelegate
    
    func captureOutput(captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [AnyObject]!, fromConnection connection: AVCaptureConnection!) {
        if metadataObjects.count > 0 {
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
            if let checkInData = CheckInData(urlString: metadataObject.stringValue) {
                self.session.stopRunning()
                self.checkIn(checkInData)
            } else {
                let alertTitle = NSLocalizedString("Incorrect QR Code", comment: "")
                let alertController = UIAlertController(title: alertTitle,
                                                        message: "",
                                                        preferredStyle: .Alert)
                let cancelTitle = NSLocalizedString("Cancel", comment: "")
                let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
                alertController.addAction(cancelAction)
                self.presentViewController(alertController, animated: true, completion: nil)
            }
        }
    }
    
    // MARK: - CheckInControllerDelegate
    
    override func checkInDidEnd(checkInController: CheckInController, withSuccess succeed: Bool) {
        super.checkInDidEnd(checkInController, withSuccess: succeed)
        
        // Handle success and failure
        if succeed {
            self.navigationController?.popViewControllerAnimated(true)
        } else {
            self.startScanning()
        }
    }
    
    // MARK: - UIViewControllerDelegate
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        if UIDevice.currentDevice().orientation == UIDeviceOrientation.LandscapeLeft {
            previewLayer!.connection.videoOrientation = AVCaptureVideoOrientation.LandscapeRight
        } else if UIDevice.currentDevice().orientation == UIDeviceOrientation.LandscapeRight {
            previewLayer!.connection.videoOrientation = AVCaptureVideoOrientation.LandscapeLeft
        } else {
            previewLayer!.connection.videoOrientation = AVCaptureVideoOrientation.Portrait
        }
    }
    
}