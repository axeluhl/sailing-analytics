//
//  CameraController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import AVFoundation
import UIKit

class CameraController: NSObject {

    class func deviceCanReadQRCodes() -> Bool {
        var session: AVCaptureSession!
        var output: AVCaptureMetadataOutput!
        (session, output) = CameraController.setUpCaptureSession(nil)
        let types = output.availableMetadataObjectTypes as! [String]
        var deviceCanReadQRCodes = false
        for type in types {
            if type == AVMetadataObjectTypeQRCode {
                deviceCanReadQRCodes = true
                break
            }
        }
        return deviceCanReadQRCodes
    }
    
    class func setUpCaptureSession(delegate: AVCaptureMetadataOutputObjectsDelegate?) -> (session: AVCaptureSession!, output: AVCaptureMetadataOutput!) {
        let device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        let output = AVCaptureMetadataOutput()
        let session = AVCaptureSession()
        do {
            let input = try AVCaptureDeviceInput(device: device);
            output.setMetadataObjectsDelegate(delegate, queue: dispatch_get_main_queue())
            session.canSetSessionPreset(AVCaptureSessionPresetHigh)
            if session.canAddInput(input) {
                session.addInput(input)
            }
            if session.canAddOutput(output) {
                session.addOutput(output)
            }
        } catch {
            print(error)
        }
        return (session, output)
    }
    
}
