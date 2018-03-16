//
//  Constants.swift
//  SAPTracker
//
//  Created by Konstantin Gonikman on 15/02/16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

struct Application {
    static let Title = "SAP Sail InSight"
    static let RequestTimeout: TimeInterval = 7
}

struct AutoCourse {
    static let MinGPSFixesNeeded = 10
}

struct URLs {
    static let CodeConvention = URL(string: "https://github.com/raywenderlich/swift-style-guide/blob/master/README.markdown")!
    static let Terms = URL(string: "http://www.sapsailing.com/EULA_iOS_SailingBoatTracker.html")!
}

struct Colors {
    static let BlueButton = UIColor(hex:0x009de0)
    static let BlueButtonHighlighted = UIColor(hex:0x007aad)
    static let BlueButtonTitleColor = UIColor.white
    static let GrayButton = UIColor(hex:0xf3f3f3)
    static let GrayButtonBorder = UIColor(hex:0x999999)
    static let GrayButtonHighlighted = UIColor(hex:0xbfbfbf)
    static let GrayButtonTitleColor = UIColor.black
    static let GreenButton = UIColor(hex:0x0e7733)
    static let GreenButtonHighlighted = UIColor(hex:0x08451e)
    static let GreenButtonTitleColor = UIColor.white
    static let RedButton = UIColor(hex:0xcd201b)
    static let RedButtonHighlighted = UIColor(hex:0x991814)
    static let RedButtonTitleColor = UIColor.white
    static let TranslucentButton = UIColor(hex:0xffffff, alpha: 0.5)
    static let TranslucentButtonBorder = UIColor(hex:0xffffff)
    static let TranslucentButtonHighlighted = UIColor(hex:0xffffff, alpha: 0.5)
    static let TranslucentButtonTitleColor = UIColor.white
    static let NavigationBarTitleColor = UIColor.black
    static let NavigationBarTintColor = UIColor(hex: 0x009de0)
}

struct Images {
    static let BlueButton = UIImage(color: Colors.BlueButton)
    static let BlueButtonHighlighted = UIImage(color: Colors.BlueButtonHighlighted)
    static let GrayButton = UIImage(color: Colors.GrayButton)
    static let GrayButtonHighlighted = UIImage(color: Colors.GrayButtonHighlighted)
    static let GreenButton = UIImage(color: Colors.GreenButton)
    static let GreenButtonHighlighted = UIImage(color: Colors.GreenButtonHighlighted)
    static let RedButton = UIImage(color: Colors.RedButton)
    static let RedButtonHighlighted = UIImage(color: Colors.RedButtonHighlighted)
    static let TranslucentButton = UIImage(color: Colors.TranslucentButton)
    static let TranslucentButtonHighlighted = UIImage(color: Colors.TranslucentButtonHighlighted)
}

let BoatClassNames = [
    "18Footer",
    "2.4 Meter",
    "12 Meter",
    "29er",
    "49er",
    "49erFX",
    "420",
    "470",
    "5O5",
    "5.5mR",
    "8mR",
    "A-Catamaran",
    "Albin Express",
    "Albin Ballad",
    "B/ONE",
    "Cadet",
    "International Canoe",
    "Canoe Taifun",
    "Contender",
    "C&C 30",
    "D-One",
    "Dragon Int.",
    "Delphia 24",
    "Dyas",
    "Extreme 40",
    "D35",
    "Elliott 6m",
    "Europe Int.",
    "Formula 18",
    "Farr 30",
    "Farr 280",
    "Finn",
    "Flying Dutchman",
    "Flying Phantom",
    "Folkboat",
    "FUN",
    "Formula 16",
    "GC 32",
    "GP 26",
    "Hobie 16",
    "H-Boat",
    "Hansa 303",
    "Hobie Tiger",
    "Hobie Wild Cat",
    "International 14",
    "J/22",
    "J/24",
    "J/70",
    "J/80",
    "J/88",
    "J/105",
    "J/111",
    "Kielzugvogel",
    "Kite",
    "Laser 2",
    "Laser 4.7",
    "Laser Radial",
    "Laser Int.",
    "Laser SB3",
    "Lago 26",
    "Longtze",
    "M32",
    "Melges 20",
    "Melges 24",
    "Mini Transat 6.50",
    "Musto Skiff",
    "Nacra 15",
    "Nacra 17",
    "Nacra 17 Foiling",
    "O-Jolle",
    "OK Dinghy",
    "O'pen BIC",
    "Optimist",
    "Pirate",
    "Platu 25",
    "PWA",
    "RC44",
    "RS 100",
    "RS 200",
    "RS 400",
    "RS 500",
    "RS 800",
    "RS Aero",
    "RS:X",
    "RS Feva",
    "SKUD 18",
    "Sonar",
    "Soling",
    "Splash Blue",
    "Splash Red",
    "Splash Green",
    "Star",
    "Streamline",
    "Sunbeam 22",
    "Swan 45",
    "Tartan 10",
    "Techno 293",
    "Techno 293 Plus",
    "Teeny",
    "Tornado Catamaran",
    "Tom 28 MAX",
    "Trias",
    "Viper 640",
    "VO60",
    "VX ONE",
    "Weta",
    "X-99",
    "ORC",
    "ORC Club",
    "ORC International",
    "PHRF"
].sorted { $0 < $1 }
