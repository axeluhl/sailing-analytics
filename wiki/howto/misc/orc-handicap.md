# ORC Handicap/Scoring System

To understand how a race is scored by the ORC system, read the following articles:
https://bugzilla.sapsailing.com/bugzilla/attachment.cgi?id=146
https://orc.org/index.asp?id=31

---

## ORC Certificate Information

The link to obtain the certificates for a country is

  http://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER
  
(in this case for three-letter IOC code "GER" for germany).

### Example Line

```
NATCERTN.FILE_ID SAILNUMB    NAME                    TYPE              BUILDER           DESIGNER          YEAR CLUB                                OWNER                               ADRS1                               ADRS2                               C_Type   D CREW DD_MM_yyYY HH:MM:SS  LOA   IMSL   DRAFT  BMAX   DSPL  INDEX    DA   GPH    TMF    ILCGA  PLT-O PLD-O   WL6    WL8    WL10   WL12   WL14   WL16   WL20   OL6    OL8    OL10   OL12   OL14   OL16   OL20   CR6    CR8    CR10   CR12   CR14   CR16   CR20   NSP6   NSP8   NSP10  NSP12  NSP14  NSP16  NSP20   OC6    OC8    OC10   OC12   OC14   OC16   OC20  UA6   UA8   UA10  UA12  UA14  UA16  UA20  DA6   DA8  DA10  DA12  DA14  DA16  DA20   UP6    UP8    UP10   UP12   UP14   UP16   UP20   R526   R528   R5210  R5212  R5214  R5216  R5220  R606   R608   R6010  R6012  R6014  R6016  R6020  R756   R758   R7510  R7512  R7514  R7516  R7520  R906   R908   R9010  R9012  R9014  R9016  R9020  R1106  R1108  R11010 R11012 R11014 R11016 R11020 R1206  R1208  R12010 R12012 R12014 R12016 R12020 R1356  R1358 R13510 R13512 R13514 R13516 R13520  R1506  R1508 R15010 R15012 R15014 R15016 R15020    D6     D8     D10    D12    D14    D16    D20 OTNLOW  OTNMED  OTNHIG  ITNLOW  ITNMED  ITNHIG DH_TOD DH_TOT  PLT-I PLD-I TMF-OF  PLT2H PLD2H    OSN ReferenceNo    CDL    DSPS     WSS    MAIN   GENOA     SYM    ASYM
GER140772GER5549 GER 5549    MOANA                   MARTEN 49 Mod     MARTEN MARINE     REICHEL/PUGH      2004 KYC                                 DR. HANNO ZIEHM                                                                                             INTL     R  900 21 04 2016 16:11:00 15.044 13.944 3.727  4.15   9704. 145.5   0.00  494.8 1.2435  542.8  0.862  46.1   808.5  651.3  568.7  527.0  505.2  483.4  445.9  769.3  622.2  550.8  513.9  489.7  468.2  443.0  677.6  548.7  480.5  440.9  415.6  397.2  369.2  739.4  592.8  513.1  465.4  434.6  413.2  384.9  834.5  645.6  540.0  475.7  433.1  401.8  354.6  42.9  41.8  39.8  38.0  37.3  36.4  36.1 141.9 143.3 147.4 152.4 151.7 148.1 142.1  832.5  678.5  609.2  579.2  562.1  546.1  536.7  542.7  449.4  420.0  409.5  403.5  397.8  389.9  508.1  429.9  406.8  397.2  391.0  386.5  374.1  478.1  416.9  393.7  377.9  368.4  361.9  350.3  478.8  418.0  393.2  370.5  351.3  339.6  328.2  488.5  414.1  392.5  376.2  359.5  344.0  304.5  507.0  420.3  387.7  367.2  350.6  335.9  302.4  567.4  453.3  407.7  379.9  349.5  318.8  282.1  679.3  540.5  457.6  410.6  388.3  364.3  307.5  784.4  624.1  528.3  474.9  448.4  420.6  355.1 1.2035  1.5321  1.7318  0.9248  1.2366  1.4217  487.4 1.2310  1.199 284.8 1.2439  0.873  56.8  482.4 GER20014417 13.620   11215    42.5    87.8    59.4     0.0   240.7
```

### Overview of Codes

| Code | Explanation |
| ----------------- | --------------------------------------- |
| NATCERTN.FILE_ID  | National Certificate Number |
| SAILNUMB          | Sailnumber                  |
| TYPE              | Class                       |
| BUILDER           | |
| DESIGNER          | |
| YEAR              | |
| CLUB              | |
| OWNER             | |
| ADRS1             | |
| ADRS2             | |
|||
| C_Type            | Certificate Type (Club or International) |
| D                 | IMS Regulation Division (R = Performance, S, U, C = Racer, Sailboat, ? Cruiser) |
| CREW              | Minimum Declared Crew Weight (in kg) |
| DD_MM_yyYY        | Date of Issue |
| HH:MM:SS          | Time of Issue |
| LOA               | Length Overall
| IMSL              | Freeboard
| DRAFT             | Draft
| BMAX              | Maximum Beam
| DSPL              | Displacement
| INDEX             | Stability Index
| DA                | Dynamic Allowance (in %)
|||
| GPH               | General Purpose Handicap (in s/nm Allowance)
| TMF-OF            | Offshore Time-On-Time Value (in s/s)
| OSN               | Offshore Time-On-Distance Value (in s/nm)
| TMF               | Inshore Time-On-Time Value (in s/s)
| ILCGA             | Inshore Time-On-Distance Value (in s/nm)
| PLT-O             | Offshore Performance Line (PLT)
| PLD-O             | Offshore Performance Line (PLD)
| PLT-I             | Inshore Performance Line (PLT)
| PLD-I             | Inshore Performance Line (PLD)
|||
| WL[6,8,10,12,14,16,20] | *Selected Courses:* **Windward/Leeward** Time Allowances for 6..20kt of Wind (in s/nm)
| OL[6,8,10,12,14,16,20] | *Selected Courses:* ??? Time Allowances for 6..20kt of Wind (in s/nm)
| CR[6,8,10,12,14,16,20] | *Selected Courses:* **Circular Random** Time Allowances for 6..20kt of Wind (in s/nm)
| NS[6,8,10,12,14,16,20] | *Selected Courses:* **Non Spinnaker** Time Allowances for 6..20kt of Wind (in s/nm)
| OC[6,8,10,12,14,16,20] | *Selected Courses:* **Ocean for PCS** Time Allowances for 6..20kt of Wind (in s/nm)
|||
| UA[6,8,10,12,14,16,20] | **Beat Angles** for 6..20kt of Wind
| DA[6,8,10,12,14,16,20] | **Gybe Angles** for 6..20kt of Wind (in s/nm)
|||
| UP[6,8,10,12,14,16,20] | **Upwind** Time Allowances for 6..20kt of Wind (in s/nm)
| R[52,60,75,90,110,120,135][6,8,10,12,14,16,20] | Time Allowances for 52..135° TWA with 6..20kt of Wind(in s/nm)  
| D[6,8,10,12,14,16,20] | **Downwind** Time Allowances for 6..20kt of Wind (in s/nm)
||| 
| OTNLOW/OTNMED/OTNHIG  | Triple Number Offshore Low/Medium/High
| ITNLOW/ITNMED/ITNHIG  | Triple Number Inshore Low/Medium/High
| DH_TOD    | Double-Hand Time-On-Distance
| DH_TOT    | Double-Hand Time-On-Time
| PLT2H | *???*
| PLD2H | *???*   
|||
| ReferenceNo | ORC Reference Number   
| CDL    | Class Division Length
| DSPS   |  *???*
| WSS    | Area of smallest jib (in m^2)
| MAIN   | Area of Mainsail (in m^2)
| GENOA  | Area of Genoa (in m^2)
| SYM    | Area of symmetrical Spinnaker (in m^2)
| ASYM   | Area of asymmetrical Spinnaker (in m^2)

---