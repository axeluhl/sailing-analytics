This page descrips how to manage the media content like images for the events at www.sapsailing.com, providing a guidance based on user's best experiences. For locations finding media content itself please refer to the [[media|wiki/events/Sailing-events-media-content]] page.

[[_TOC_]]

# Images

## Image types and their impact
There are four image types which can be added to an event carrying different roles:

* **logo:** the sailing event or league logo which gets displayed e.g. in the upper left corner next to the name of the sailing event.
* **stage:** used as a banner for the sailing event and gets displayed e.g. on the landing page.
* **teaser:** used to give a preview about the sailing event and gets displayed e.g. in the event overview where all events are grouped by years.    
* **galery:** representing the images which are displayed under the _media_ tab of each event.

## Scale guidelines for several purposes
For performance reasons, images should be scaled before uploading them. The following image resolutions showed up as good manner:

* **logo:** about 150 x 150 pixel (file size < 100 kB); take a .png file with transparent background!
* **teaser:** about 640 x 480 pixel (file size < 100 kB)
* **stage:** about 1800 x 500 pixel (file size < 500 kB)
* **gallery:** about 1400 x 1000 pixel (file size < 350 kB)

## Tutorial - GIMP
The following guide is based on the tool [GIMP](https://www.gimp.org/) and its [plugin](http://registry.gimp.org/node/26259) for batch manipulation.  

1. Start up GIMP go to File -> Batch Image Manipulation
2. Add -> Resize -> select the option _set exact size in pixel_ and _preserve aspect ratio_ -> insert the desired width & height -> Ok
3. Add -> Add a watermark -> leave defaults -> insert copyright information -> Ok
4. Add images -> ... -> Apply

## S3 folder structure
Before uploading the images an appropirate folder needs to be created at the S3 storage. For a good contribution please stick to the following naming conventions:





