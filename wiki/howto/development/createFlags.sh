rm /tmp/flags -rf
set -e
git clone https://github.com/gosquared/flags.git /tmp/flags || true
cd /tmp/flags

make || true
echo "Errors before this related to icon creation are ok, the required png's are already generated at this point in time"
cd /tmp/flags/flags/flags-iso/shiny/64/
mkdir convert
echo "cropping images"
for i in `ls *.png`; do convert $i -trim +repage ./convert/`basename $i`; done
echo "done cropping, optimizing images"
cd convert
mkdir /tmp/flags/out
find . -iname '*png' -exec pngcrush -q -brute -rem alla -brute {} /tmp/flags/out/{} \;
echo "results are in the folder /tmp/flags/out/" 