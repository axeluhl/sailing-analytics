//
//  ScanViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import AVFoundation

protocol ScanViewControllerDelegate: class {
    
    func scanViewController(_ controller: ScanViewController, didCheckIn checkIn: CheckIn)
    
}

class ScanViewController: UIViewController {
    
    weak var coreDataManager: CoreDataManager!
    weak var delegate: ScanViewControllerDelegate?
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var targetImageView: UIImageView!
    
    fileprivate var scanSession: AVCaptureSession?
    fileprivate var scanOutput: AVCaptureMetadataOutput?
    fileprivate var scanPreviewLayer: AVCaptureVideoPreviewLayer?
    fileprivate var scanError: NSError?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        startScanning()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layout()
    }
    
    // MARK: - Layout
    
    fileprivate func layout() {
        layoutPreviewLayer()
    }
    
    fileprivate func layoutPreviewLayer() {
        guard let previewLayer = scanPreviewLayer else { return }
        previewLayer.frame = previewView.bounds
        previewLayer.position = CGPoint(
            x: previewView.bounds.midX,
            y: previewView.bounds.midY
        )
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupLocalization()
        setupScanning()
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = Translation.ScanView.Title.String
    }
    
    fileprivate func setupScanning() {
        do {
            let session = AVCaptureSession()
            let output = AVCaptureMetadataOutput()
            let device = AVCaptureDevice.defaultDevice(withMediaType: AVMediaTypeVideo)
            let input = try AVCaptureDeviceInput(device: device);
            output.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            session.canSetSessionPreset(AVCaptureSessionPresetHigh)
            if session.canAddInput(input) {
                session.addInput(input)
            }
            if session.canAddOutput(output) {
                session.addOutput(output)
            }
            output.metadataObjectTypes = [AVMetadataObjectTypeQRCode]
            if let previewLayer = AVCaptureVideoPreviewLayer(session: session) {
                previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                previewLayer.connection.videoOrientation = videoOrientation(forInterfaceOrientation: UIApplication.shared.statusBarOrientation)
                previewLayer.frame = previewView.bounds
                previewLayer.position = CGPoint(
                    x: previewView.bounds.midX,
                    y: previewView.bounds.midY
                )
                previewView.layer.addSublayer(previewLayer)
                scanPreviewLayer = previewLayer
            }
            scanSession = session
            scanOutput = output
        } catch let error as NSError {
            scanError = error
        }
    }
    
    // MARK: - Scanning
    
    fileprivate func startScanning() {
        guard scanError == nil else {
            let error = scanError!
            let alertController = UIAlertController(
                title: error.localizedDescription,
                message: error.localizedFailureReason,
                preferredStyle: .alert
            )
            let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .default) { [weak self] action in
                if let settingsURL = URL(string: UIApplicationOpenSettingsURLString) {
                    UIApplication.shared.openURL(settingsURL)
                }
                _ = self?.navigationController?.popViewController(animated: true)
            }
            let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel) { [weak self] action in
                _ = self?.navigationController?.popViewController(animated: true)
            }
            alertController.addAction(settingsAction)
            alertController.addAction(cancelAction)
            present(alertController, animated: true, completion: nil)
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
    
    override func willRotate(to toInterfaceOrientation: UIInterfaceOrientation, duration: TimeInterval) {
        scanPreviewLayer?.connection.videoOrientation = videoOrientation(forInterfaceOrientation: toInterfaceOrientation)
    }
    
    // MARK: - Properties
    
    fileprivate lazy var regattaCheckInController: RegattaCheckInController = {
        return RegattaCheckInController(coreDataManager: self.coreDataManager)
    }()
    
    // MARK: - Helper
    
    fileprivate func videoOrientation(forInterfaceOrientation orientation: UIInterfaceOrientation) -> AVCaptureVideoOrientation {
        switch orientation {
        case .landscapeLeft: return .landscapeLeft
        case .landscapeRight: return .landscapeRight
        case .portrait: return .portrait
        case .portraitUpsideDown: return .portraitUpsideDown
        case .unknown: return .portrait
        }
    }
    
}

// MARK: - AVCaptureMetadataOutputObjectsDelegate

extension ScanViewController: AVCaptureMetadataOutputObjectsDelegate {
    
    func captureOutput(
        _ captureOutput: AVCaptureOutput!,
        didOutputMetadataObjects metadataObjects: [Any]!,
        from connection: AVCaptureConnection!)
    {
        if metadataObjects.count > 0 {
            performSelector(onMainThread: #selector(stopScanning), with: nil, waitUntilDone: false)
            let metadataObject = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
            if let checkInData = CheckInData(urlString: metadataObject.stringValue) {
                DispatchQueue.main.async(execute: {
                    self.captureOutputSuccess(checkInData: checkInData)
                })
            } else {
                DispatchQueue.main.async(execute: {
                    self.captureOutputFailure()
                })
            }
        }
    }
    
    fileprivate func captureOutputSuccess(checkInData: CheckInData) {
        checkIn(withCheckInData: checkInData)
    }
    
    fileprivate func captureOutputFailure() {
        let title = Translation.Common.Error.String
        let message = Translation.ScanView.IncorrectCodeAlert.Message.String
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { [weak self] action in
            self?.startScanning()
        }
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - CheckIn
    
    fileprivate func checkIn(withCheckInData checkInData: CheckInData) {
        regattaCheckInController.checkInWithViewController(self, checkInData: checkInData, success: { [weak self] (checkIn) in
            self?.checkInSuccess(checkIn: checkIn)
        }) { [weak self] (error) in
            self?.checkInFailure(error: error)
        }
    }
    
    fileprivate func checkInSuccess(checkIn: CheckIn) {
        _ = navigationController?.popViewController(animated: true)
        delegate?.scanViewController(self, didCheckIn: checkIn)
    }
    
    fileprivate func checkInFailure(error: Error) {
        startScanning()
    }
    
}
