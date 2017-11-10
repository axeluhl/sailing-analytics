#pragma once

// All directions in magnetic

// multiboat channels

enum ExChannels
{
	ExUtc,					// Microsoft DATE type,utc system time
	ExBsp,
	ExAwa,
	ExAws,
	ExTwa, // #4
	ExTws, // #5
	ExTwd, // #6

	ExRudderFwd,
	ExDeltaTargBsp,
	ExCourse,
	
	ExLwy, // #10
	ExSet,
	ExDrift,
	ExHdg, // #13
	ExAirTemp,
	ExSeaTemp,
	ExBaro,
	ExDepth,				// metres
	ExRoll,
	ExPitch,
	ExRudder, // #20
	ExTab,
	ExForestayLoad,
	ExDownhaulLoad,
	ExMastAngle,
	ExForestayLen,
	ExMast,
	ExStbdLoadCell,
	ExPortLoadCell,
	ExRake,
	ExVolts,  // #30
	ExVmg,
	ExROT,
	ExLayDistOnStrb,
	ExLayTimeOnStrb,
	ExLayPortBear,
	ExLayDistOnPort,
	ExLayTimeOnPort,
	ExLayStrbBear,
	ExGpsQuality,			// 0 Bad, 1 Autonomous, 2 Differential, 3 p-code, 4,5 Rtk, 6 dr, if change this, need to change CPort::NmeaAPB()
	ExGpsHDOP, // #40
	ExGpsPDOP,
	ExGpsVDOP,
	ExGpsNumber,			// Number of satellites in active constellation
	ExGpsAge,				// Age of differential data
	ExGpsAltitude,			// antenna height
	ExGpsGeoidSeparation,
	ExGpsMode,				// 0 = 1D, 1 = 2D, 2 = 3D, 3 = Auto, 6 = error
	
	ExLat,	   // #48		// if add GPS vars, extend CCore::IsGPSvar()
	ExLon,     // #49
	ExCog,     // #50
	ExSog,     // #51
	
	ExDiffRefStn,
	
	ExTargTwaN,
	ExTargBspN,
	ExTargVmg,
	ExTargRoll,
	ExPolarBsp,
	ExPolarBspPercent,
	ExPolarRoll,
	
	ExErrorCode, // #60
	
	ExStrbRunner,
	ExPortRunner,

	ExPolarBspN,
	ExPolarBspPercentN,

	ExTargTwaLwy,			// target twa without leeway

	ExVmgPercent,
	
	ExVang,
	ExTraveller,
	ExMainSheet,
	
	ExPolVmcToMark,	// #70		// vmc if headed at mark
		
	ExKeelAngle,
	ExKeelHeight,
	ExBoard,
	ExOilPressure,
	ExRPM1,
	ExRPM2,
	
	ExBoardP,
	ExBoardS,
	
	ExOppTrack,
	ExDistFinish,  // #80
	
	ExStartTimeToPort,
	ExStartTimeToStrb,
	ExLineSquareWind,
	ExStartDistToLine,
	ExStartRchTimeToLine,	// time to reach into line
	ExStartRchDistToLine,
	ExStartRchBspToLine,
	
	ExMarkTime,
	ExNextMarkTimeOnPort,
	ExNextMarkTimeOnStrb, // #90
	
	ExXte,
	ExVmc,
	
	ExMagVar,
	
	ExGwd,   // #94
	ExGws,   // #95
	
	ExLayDist,				// distance to layline we are heading to
	ExLayTime,				// time to layline
	ExLayBear,				// bearing of that layline
	
	ExVmcPercent,
	ExPolVmc,    // #100
	ExOptVmc,
	ExOptVmcHdg,
	ExOptVmcTwa,
	ExDeltaTargTwa,
	
	ExMarkRng,
	ExMarkBrg,
	ExMarkGpsTime,
	ExMarkTwa,
	
	ExPredSet,
	ExPredDrift,      // #110
	
	ExNextMarkRng,
	ExNextMarkBrg,
	ExNextMarkTwa,
	
	ExRadarRng,
	ExRadarBrg,
	ExStartDistBelowLineStern,
	
	ExAlt0,					// alternating number channels must be consecutive
	ExAlt1,
	ExAlt2,
	ExAlt3,        // #120
	ExAlt4,
	ExAlt5,
	ExAlt6,
	ExAlt7,
	ExAlt8,
	ExAlt9,
//	ExAltNum = ExAlt9 - ExAlt0 + 1	// defined in CoreMem.h
	ExAltMaxId = ExAlt9,			// alternating channels must be consecutive

	ExNextMarkPolTime,
	
	ExStartLineBiasDeg,
	ExStartLineBiasLen,
	
	ExStartLayPortBear,	 // #130	// laylines for start line
	ExStartLayStrbBear,
	
	ExNextMarkAwa,
	ExNextMarkAws,
	
	ExStartRSTime,			// turning to right, ending up on starboard
	ExStartRPTime,			// turning to right, ending up on port
	ExStartLSTime,			// turning to left, ending up on starboard
	ExStartLPTime,			// turning to left, ending up on port
	
	ExGpsDistToRaceNote,
	ExGpsTimeToRaceNote,
	
	ExLogBsp,         // #140
	ExLogSog,
	ExStartGpsTimeToLine,
	ExStartGpsTimeToBurn,
	ExTargTwaS,				// Start
	ExTargBspS,				// Start
	
	ExGpsTime,          // #146
	
	ExTwdPlus90,			// Twd + 90
	ExTwdLess90,			// Twd - 90
	
	ExShadow,
	ExShadowOppTack,    // #150
	
	ExDownhaulLoad2,
	
	ExTackAngle,
	ExTackAnglePolar,
	
	ExTargAwa,
	
	ExStartTimeBurnStrbX,	// time to burn when tack onto starboard end starboard layline and sail to 20s from line
	ExStartTimeBurnPortX,	// offset for the start stbd layline to the pin
	ExStartLayTimeP,
	ExStartLayTimeS,

	ExMarkSet,
	ExMarkDrift,      // #160
	
	ExMarkLat,
	ExMarkLon,
	ExStartPortEndLat,		// ends of line
	ExStartPortEndLon,
	ExStartStrbEndLat,
	ExStartStrbEndLon,
	
	ExGpsHPE,
	ExHumidity,
	ExLeadPort,
	ExLeadStbd,       // #170
	ExBackstay,
	
	ExUser0,		// #172: RakeDeg (Rake in degrees)		// user channels must be consecutive
	ExUser1,		// #173: DflectrPP (Deflector percentage)
	ExUser2,		// #174: TG Heel (Target Heel)
	ExUser3,		// #175: ForestayPres (Forestay Pressure)
	ExUser4,		// #176: DflectrMM (Deflector in mm)
	ExUser5,
	ExUser6,
	ExUser7,
	ExUser8,       // #180
	ExUser9,
	ExUser10,
	ExUser11,
	ExUser12,
	ExUser13,
	ExUser14,
	ExUser15,
	ExUser16,
	ExUser17,
	ExUser18,      // #190
	ExUser19,
	ExUser20,
	ExUser21,
	ExUser22,
	ExUser23,
	ExUser24,
	ExUser25,
	ExUser26,
	ExUser27,
	ExUser28,      // #200
	ExUser29,
	ExUser30,
	ExUser31,
	ExUserMax = ExUser31,		// user channels must be consecutive
	
	ExStartTimeToGun,
	ExStartTimeToLine,
	ExStartTimeToBurn,
	ExStartDistBelowLine,
	ExStartDistBelowLineGun,
	
	ExGateTimeOnPort,			// this is to the gate mark
	ExGateDistOnStrb,  // #210
	ExGateTimeOnStrb,
	ExGateDistOnPort,
	
	ExGateSpotTimeOnStrb,
	ExGateSpotTimeOnPort,
	
	ExLayPortBearUp,
	ExLayStrbBearUp,
	ExLayPortBearDn,
	ExLayStrbBearDn,
	
	ExTideLayPortTimeOnPort,
	ExTideLayPortTimeOnStbd,  // #220
	ExTideLayStbdTimeOnPort,
	ExTideLayStbdTimeOnStbd,
	ExTideLayPortTime,
	ExTideLayStbdTime,
	
	ExMaxLayPortBear,
	ExMinLayPortBear,
	ExMaxLayStrbBear,
	ExMinLayStrbBear,
	
	ExTwdLayMark,
	ExTwdLayMarkOpp,	// #230		// lay on other board
	
	ExDeltaBspSog,
	ExDeltaHdgCog,
	
	ExLayPortRatio,
	ExLayStrbRatio,
	
	ExFourierTwd,
	ExFourierTws,
	
	ExTargTwaP,
	ExTargBspP,
	
	ExTwdTarg,
	
	ExPolCustom1,		// #240		// these need to be in this order : see CCore::DerivedPolarNumbers()
	ExPolCustom2,
	ExPolCustom3,
	ExPolCustom4,
	ExPolCustom1PC,
	ExPolCustom2PC,
	ExPolCustom3PC,
	ExPolCustom4PC,
	ExPolCustom1Targ,
	ExPolCustom2Targ,
	ExPolCustom3Targ,   // #250
	ExPolCustom4Targ,
	
	ExWaveSigHeight,			// XDR from Volvo wave sensor
	ExWaveSigPeriod,
	ExWaveMaxHeight,
	ExWaveMaxPeriod,
	
	ExSlam,
	ExMotion,
	
	ExMwa,
	ExMws,
	ExBoom,             // #260
	
	ExTargBspPercent,
	ExHeadingToSteer,
	ExHeadingToSteerPol,
	
	ExStartBspToPort,
	ExStartBspToStrb,
	ExStartBspOnPort,
	ExStartBspOnStrb,

	ExTwdTwist,

	ExSailNow,
	ExSailMark,         // #270
	ExSailNext,

	ExTackLossVMGSec,
	ExTackLossVMGMetres,
		
	ExNearestTide,

	ExTripLog,
	ExTurnToMark,		// delta of cog and bearing to mark

	ExPitchRate,
	ExRollRate,

	ExDeltaPolBsp,
	ExDeltaTargRoll,    // #280

	ExDeflectorP,
	ExRudderP,
	ExRudderS,
	ExRudderToe,
	ExBspTransverse,
	ExForestayInner,
	ExGateTime,					// this is to the gate mark

	ExZeroAhead,
	ExBrgFromBoat0,
	ExRngFromBoat0,     // #290

	ExDeflectorS,
	ExBobstay,
	ExOuthaul,

	ExD0port,
	ExD0starboard,
	ExD1port,
	ExD1starbboard,
	ExV0port,
	ExV0starbboard,
	ExV1port,           // #300
	ExV1starbboard,

	ExStartTimeToPortSimple,
	ExStartTimeToStrbSimple,

	ExNumChannels
};
