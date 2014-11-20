//
//  QRCodeViewController.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

class ScanViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate, QRCodeManagerDelegate {

    @IBOutlet weak var previewView: UIView!
    
    var activityIndicatorView: UIActivityIndicatorView!
    private var session: AVCaptureSession!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    private var qrCodeManager: QRCodeManager?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        activityIndicatorView = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.Gray)
        activityIndicatorView.hidesWhenStopped = true
        let barButton = UIBarButtonItem(customView: activityIndicatorView)
        self.navigationItem.rightBarButtonItem = barButton
 
        qrCodeManager = QRCodeManager(delegate: self)
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
        previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
        previewView.layer.addSublayer(previewLayer)
        
        session.startRunning()
    }
    
    /* AVCaptureMetadataOutputObjectsDelegate. Parse scanned QR code */
    func captureOutput(captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [AnyObject]!, fromConnection connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            session.stopRunning()
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as AVMetadataMachineReadableCodeObject
            qrCodeManager!.parseUrl(metadataObject.stringValue)
        }
    }
    
    // MARK: - QRCodeManagerDelegate
    
    func qrCodeOK() {
        // pop back to home view
        navigationController!.popViewControllerAnimated(true)
    }
    
    func qrCodeCancel() {
        self.session.startRunning()        
    }
    
}