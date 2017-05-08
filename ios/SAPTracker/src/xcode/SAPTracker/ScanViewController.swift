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
    
    weak var homeViewController: HomeViewController?
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var targetImageView: UIImageView!
    
    private var scanSession: AVCaptureSession?
    private var scanOutput: AVCaptureMetadataOutput?
    private var scanPreviewLayer: AVCaptureVideoPreviewLayer?
    private var scanError: NSError?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        startScanning()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layout()
    }
    
    // MARK: - Layout
    
    private func layout() {
        layoutPreviewLayer()
    }
    
    private func layoutPreviewLayer() {
        guard let previewLayer = scanPreviewLayer else { return }
        previewLayer.frame = previewView.bounds
        previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocalization()
        setupScanning()
    }
    
    private func setupLocalization() {
        navigationItem.title = Translation.ScanView.Title.String
    }
    
    private func setupScanning() {
        do {
            let session = AVCaptureSession()
            let output = AVCaptureMetadataOutput()
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
            let previewLayer = AVCaptureVideoPreviewLayer(session: session)
            previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            previewLayer.connection.videoOrientation = videoOrientation(forInterfaceOrientation: UIApplication.sharedApplication().statusBarOrientation)
            previewLayer.frame = previewView.bounds
            previewLayer.position = CGPointMake(CGRectGetMidX(previewView.bounds), CGRectGetMidY(previewView.bounds))
            previewView.layer.addSublayer(previewLayer)
            scanSession = session
            scanOutput = output
            scanPreviewLayer = previewLayer
        } catch let error as NSError {
            scanError = error
        }
    }
    
    // MARK: - Scanning
    
    private func startScanning() {
        guard scanError == nil else {
            let error = scanError!
            let alertController = UIAlertController(
                title: error.localizedDescription,
                message: error.localizedFailureReason,
                preferredStyle: .Alert
            )
            let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .Default) { (action) in
                UIApplication.sharedApplication().openURL(NSURL(string: UIApplicationOpenSettingsURLString) ?? NSURL())
                self.navigationController?.popViewControllerAnimated(true)
            }
            let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel) { (action) in
                self.navigationController?.popViewControllerAnimated(true)
            }
            alertController.addAction(settingsAction)
            alertController.addAction(cancelAction)
            presentViewController(alertController, animated: true, completion: nil)
            return
        }
        targetImageView.image = UIImage(named: "scan_white")
        scanSession?.startRunning()
    }
    
    func stopScanning() {
        targetImageView.image = UIImage(named: "scan_green")
        scanSession?.stopRunning()
    }
    
    // MARK: - UIViewControllerDelegate
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        scanPreviewLayer?.connection.videoOrientation = videoOrientation(forInterfaceOrientation: toInterfaceOrientation)
    }
    
    // MARK: - Properties
    
    private lazy var checkInController: CheckInController = {
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
    
}

// MARK: - AVCaptureMetadataOutputObjectsDelegate

extension ScanViewController: AVCaptureMetadataOutputObjectsDelegate {
    
    func captureOutput(captureOutput: AVCaptureOutput!,
                       didOutputMetadataObjects metadataObjects: [AnyObject]!,
                                                fromConnection connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            performSelectorOnMainThread(#selector(stopScanning), withObject: nil, waitUntilDone: false)
            let metadataObject = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
            if let checkInData = CheckInData(urlString: metadataObject.stringValue) {
                dispatch_async(dispatch_get_main_queue(), {
                    self.captureOutputSuccess(checkInData)
                })
            } else {
                dispatch_async(dispatch_get_main_queue(), {
                    self.captureOutputFailure()
                })
            }
        }
    }
    
    private func captureOutputSuccess(checkInData: CheckInData) {
        checkInController.checkIn(checkInData, completion: { (withSuccess) in
            if withSuccess {
                self.homeViewController?.selectedCheckIn = CoreDataManager.sharedManager.fetchCheckIn(checkInData)
                self.navigationController?.popViewControllerAnimated(true)
            } else {
                self.startScanning()
            }
        })
    }
    
    private func captureOutputFailure() {
        showIncorrectCodeAlert()
    }
    
    // MARK: - Alerts
    
    private func showIncorrectCodeAlert() {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: Translation.ScanView.IncorrectCodeAlert.Message.String,
            preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.startScanning()
        }
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
}