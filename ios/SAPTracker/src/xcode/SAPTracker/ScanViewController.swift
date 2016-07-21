//
//  ScanViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

class ScanViewController: UIViewController {
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var targetImageView: UIImageView!

    private var session: AVCaptureSession!
    private var output: AVCaptureMetadataOutput!
    private var previewLayer: AVCaptureVideoPreviewLayer!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupSession()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        startScanning()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layoutPreviewLayer()
    }
    
    // MARK: - Layouts
    
    private func layoutPreviewLayer() {
        previewLayer.frame = previewView.bounds
        previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
    }
    
    // MARK: - Setups
    
    private func setupSession() {
        session = AVCaptureSession()
        output = AVCaptureMetadataOutput()
        do {
            let device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            let input = try AVCaptureDeviceInput(device: device);
            output.setMetadataObjectsDelegate(self, queue: dispatch_get_main_queue())
            session.canSetSessionPreset(AVCaptureSessionPresetHigh)
            if session.canAddInput(input) {
                session.addInput(input)
            }
            if session.canAddOutput(output) {
                session.addOutput(output)
            }
            output.metadataObjectTypes = [AVMetadataObjectTypeQRCode]
            previewLayer = AVCaptureVideoPreviewLayer(session: self.session)
            previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            previewLayer.connection.videoOrientation = videoOrientation(forInterfaceOrientation: UIApplication.sharedApplication().statusBarOrientation)
            previewLayer.frame = previewView.bounds
            previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
            previewView.layer.addSublayer(previewLayer)
        } catch {
            print(error)
        }
    }
    
    // MARK: - UIViewControllerDelegate
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        previewLayer.connection.videoOrientation = videoOrientation(forInterfaceOrientation: toInterfaceOrientation)
    }
    
    // MARK: - Scanning
    
    private func startScanning() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            self.session.startRunning()
        })
        targetImageView.image = UIImage(named: "scan_white")
    }
    
    private func stopScanning() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            self.session.stopRunning()
        })
        targetImageView.image = UIImage(named: "scan_green")
    }
    
    // MARK: - Properties
    
    lazy var checkInController: CheckInController = {
        let checkInController = CheckInController()
        checkInController.delegate = self
        return checkInController
    }()
    
    // MARK: - Helper
    
    private func videoOrientation(forInterfaceOrientation orientation: UIInterfaceOrientation) -> AVCaptureVideoOrientation {
        switch orientation {
        case .LandscapeLeft: return .LandscapeLeft
        case .LandscapeRight: return .LandscapeRight
        case .Portrait: return .Portrait
        case .PortraitUpsideDown: return .PortraitUpsideDown
        case .Unknown: return .Portrait
        }
    }
    
}

// MARK: - CheckInControllerDelegate

extension ScanViewController: CheckInControllerDelegate {

    func showCheckInAlert(sender: CheckInController, alertController: UIAlertController) {
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    func checkInDidEnd(sender: CheckInController, withSuccess succeed: Bool) {
        if succeed {
            navigationController?.popViewControllerAnimated(true)
        } else {
            startScanning()
        }
    }
    
}

// MARK: - AVCaptureMetadataOutputObjectsDelegate

extension ScanViewController: AVCaptureMetadataOutputObjectsDelegate {

    func captureOutput(captureOutput: AVCaptureOutput!,
                       didOutputMetadataObjects metadataObjects: [AnyObject]!,
                                                fromConnection connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            let metadataObject: AVMetadataMachineReadableCodeObject = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
            if let regattaData = RegattaData(urlString: metadataObject.stringValue) {
                stopScanning()
                checkInController.checkIn(regattaData)
            } else {
                let alertTitle = NSLocalizedString("Incorrect QR Code", comment: "")
                let alertController = UIAlertController(title: alertTitle, message: "", preferredStyle: .Alert)
                let cancelTitle = NSLocalizedString("Cancel", comment: "")
                let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
                alertController.addAction(cancelAction)
                presentViewController(alertController, animated: true, completion: nil)
            }
        }
    }

}