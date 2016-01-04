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
    @IBOutlet weak var targetImageView: UIImageView!
    
    var activityIndicatorView: UIActivityIndicatorView!
    private var session: AVCaptureSession!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    private var qrCodeManager: QRCodeManager?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        activityIndicatorView = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.White)
        activityIndicatorView.hidesWhenStopped = true
        let barButton = UIBarButtonItem(customView: activityIndicatorView)
        self.navigationItem.rightBarButtonItem = barButton
        
        qrCodeManager = QRCodeManager(delegate: self)
        startScanning()
    }
    
    /* Set up camera and QR code scanner */
    @IBAction func startScanning() {
        var output: AVCaptureMetadataOutput!
        (session, output) = QRCodeManager.setUpCaptureSession(self)
        output.metadataObjectTypes = [AVMetadataObjectTypeQRCode]
        
        previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        previewLayer.frame = previewView.bounds
        previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
        previewView.layer.addSublayer(previewLayer)
        
        session.startRunning()
        
        targetImageView.image = UIImage(named: "scan_white")
    }
    
    /* AVCaptureMetadataOutputObjectsDelegate. Parse scanned QR code */
    func captureOutput(captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [AnyObject]!, fromConnection connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            targetImageView.image = UIImage(named: "scan_green")
            session.stopRunning()
            activityIndicatorView.startAnimating()
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
            qrCodeManager!.parseUrl(metadataObject.stringValue)
        }
    }
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        if UIDevice.currentDevice().orientation == UIDeviceOrientation.LandscapeLeft {
            previewLayer!.connection.videoOrientation = AVCaptureVideoOrientation.LandscapeRight
        } else if UIDevice.currentDevice().orientation == UIDeviceOrientation.LandscapeRight {
            previewLayer!.connection.videoOrientation = AVCaptureVideoOrientation.LandscapeLeft
        } else {
            previewLayer!.connection.videoOrientation = AVCaptureVideoOrientation.Portrait
        }
    }
    
    // MARK: - QRCodeManagerDelegate
    
    func qrCodeOK() {
        // pop back to home view
        navigationController!.popViewControllerAnimated(true)
    }
    
    func qrCodeCancel() {
        self.session.startRunning()
        activityIndicatorView.stopAnimating()
        targetImageView.image = UIImage(named: "scan_white")
    }
    
}