# Importing official results

This page describes how to import official regatta results from different external regatta management systems.

## Sailwave

For this description the version 2.16.7 of Sailwave has been used.
Sailwave is a offline Windows software which can export regatta results in different formats.
Although a XRR-Format-export does exist (which would be the best solution) we use the .csv export as the XRR-Export does not export race results (only regatta results).

How to use the .csv export:
Before we can start the .csv export we must change some settings to export the data in the right format:

## 1. Select data columns to export

<img src="/wiki/images/sailwave/columns.jpg" width="100%" height="100%" alt="" />

## 2. Set points/discard format

Make sure to press Tools > Rescore so that the penalty fields are correctly filled in the format `p,c`. If you see just the penalty and not the points it will _not_ work. The CSV needs to containt points and penalty codes.

<img src="/wiki/images/sailwave/discard_format.jpg" width="100%" height="100%" alt="" />
 
## 3. Set ranking format

Make sure the highlighted checkbox is **UNCHECKED**

<img src="/wiki/images/sailwave/general_options.jpg" width="100%" height="100%" alt="" />

## 4. Set column separator and export the data...
In the file menu use the 'Export series summary to windows clipboard...' menu item.

<img src="/wiki/images/sailwave/clipboard_options.jpg" width="100%" height="100%" alt="" />

## Manage2Sail

tbd.

## WinRegatta

tbd.