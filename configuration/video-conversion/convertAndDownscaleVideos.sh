#!/bin/bash
#Fetches videos from the given ftp directory, downscales them
# and uploads them back into the directory+/out
#
# Notes on ffmpeg: Make sure to install an ffmpeg version with x264 support. 
#  You can either compile it yourself (there are various online tutorials) or
#  just download it from the http://www.deb-multimedia.org/ repository (if you use debian)
HOST='sapevent.upload.akamai.com'
USER='sail'
PASS='S4iL!uPlo4D*'
LOCALDIR='/tmp/sailvids'
SOURCEDIR='/67696/sail/barbados'

#Create temporary folder for videos
#rm -rf $LOCALDIR
mkdir $LOCALDIR

#Download directory content (could be extended to filter for video files)
lftp -f "
	open $HOST
	user $USER $PASS
	mirror --verbose -r $SOURCEDIR $LOCALDIR
	bye
"

#CREATE out dir
mkdir $LOCALDIR/out

#ITERATE FOR VIDEOS
VIDEOFILES=$LOCALDIR/*.MP4
COUNT=$(find $LOCALDIR -name "*.MP4" -print | wc -l)
STEP=0
for f in $VIDEOFILES
do
  FILENAME=$(basename "$f")
  STEP=$(($STEP + 1))
  echo "Processing Video $STEP/$COUNT: $FILENAME"
  #Just to make sure that this is no directory
  test -f "$f" || continue
  #Actual conversion
  ffmpeg -y -i "$f" -vcodec libx264 -profile baseline -preset slow -b 1500k -s 1024*576 -r 25 "$LOCALDIR/out/$FILENAME"
done


#Mirror directory to remote (since only out/ is changed, 
#	only its contents will be uploaded)
lftp -f "
	open $HOST
	user $USER $PASS
	mirror -r --reverse --verbose $LOCALDIR/out $SOURCEDIR/out
	bye
"

rm -rf $LOCALDIR