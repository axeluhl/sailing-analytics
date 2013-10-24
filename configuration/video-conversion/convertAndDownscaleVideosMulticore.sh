#!/bin/bash
#Fetches videos from the given ftp directory, downscales them
# and uploads them back into the directory+/out
# Uses GNU parallel for multi-cpu usage 
#
# Notes on ffmpeg: Make sure to install an ffmpeg version with x264 support. 
#  You can either compile it yourself (there are various online tutorials) or
#  just download it from the http://www.deb-multimedia.org/ repository (if you use debian)
#
# Notes on GNU parallel: Make sure to install the GNU version of parallel (there are other
#  programs with the same command. In debian: sudo apt-get install parallel
HOST='sapevent.upload.akamai.com'
USER='sail'
PASS='S4iL!uPlo4D*'
LOCALDIR='/tmp/sailvids'
SOURCEDIR='/67696/sail/barbados'

#Create temporary folder for videos
#rm -rf $LOCALDIR
mkdir $LOCALDIR

#Download directory content (could be extended to filter for video files)
time lftp -f "
	open $HOST
	user $USER $PASS
	mirror --verbose -r $SOURCEDIR $LOCALDIR
	bye
"

#CREATE out dir
mkdir $LOCALDIR/out
#Reference all video files
echo "Starting to process videos"  
#Starts gnu parallel with the conversion jobs
time ls $LOCALDIR/*.{MP4,mp4,AVI,avi,3GP,3gp,MPEG,mpeg,MPG,mpg} | parallel -u --gnu 'echo "Starting conversion for: {/}";ffmpeg -y -i {} -vcodec libx264 -profile baseline -preset slow -b 1500k -s 1024*576 -r 25 {//}/out/{/.}.MP4;echo "Finished conversion for: {/}"'

#Mirror directory to remote
time lftp -f "
	open $HOST
	user $USER $PASS
	mirror -r --reverse --verbose $LOCALDIR/out $SOURCEDIR/out
	bye
"

rm -rf $LOCALDIR