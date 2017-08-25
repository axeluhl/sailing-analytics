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
}

struct URLs {
    static let CodeConvention = URL(string: "https://github.com/raywenderlich/swift-style-guide/blob/master/README.markdown")!
    static let Terms = URL(string: "http://www.sapsailing.com/EULA_iOS_SailingBoatTracker.html")!
}

struct Colors {
    static let Blue = UIColor(hex:0x009de0)
    static let BlueHighlighted = UIColor(hex:0x007aad)
    static let Gray = UIColor(hex:0xf3f3f3)
    static let GrayHighlighted = UIColor(hex:0xf3f3f3)
    static let Green = UIColor(hex:0x0e7733)
    static let GreenHighlighted = UIColor(hex:0x08451e)
    static let Red = UIColor(hex:0xcd201b)
    static let RedHighlighted = UIColor(hex:0x991814)
    static let NavigationBarTitleColor = UIColor.black
    static let NavigationBarTintColor = UIColor(hex: 0x009de0)
}

struct FontNames {
    static let OpenSans = "OpenSans"
    static let OpenSansBold = "OpenSans-Bold"
}

struct Fonts {
    static let OpenSansBold17 = UIFont(name: FontNames.OpenSansBold, size: CGFloat(17.0)) ?? UIFont.systemFont(ofSize: 17.0)
    static let OpenSansBold13 = UIFont(name: FontNames.OpenSansBold, size: CGFloat(13.0)) ?? UIFont.systemFont(ofSize: 13.0)
    static let OpenSans10 = UIFont(name: FontNames.OpenSans, size: CGFloat(10.0)) ?? UIFont.systemFont(ofSize: 10.0)
}

struct Images {
    static let Blue = UIImage(color: Colors.Blue)
    static let BlueHighlighted = UIImage(color: Colors.BlueHighlighted)
    static let Gray = UIImage(color: Colors.Gray)
    static let GrayHighlighted = UIImage(color: Colors.GrayHighlighted)
    static let Green = UIImage(color: Colors.Green)
    static let GreenHighlighted = UIImage(color: Colors.GreenHighlighted)
    static let Red = UIImage(color: Colors.Red)
    static let RedHighlighted = UIImage(color: Colors.RedHighlighted)
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
