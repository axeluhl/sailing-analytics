# Building, Deploying, Stopping and Starting Server Instances

[[_TOC_]]

## Running a Build

Builds are generally executed in either of the following two ways:

 * Check out a branch of your liking from git and run <gitroot>/configuration/buildAndUpdateProduct.sh build (check the various options of this script by invoking it with no arguments). The build results including the p2 product repository are then located in the git workspace.

 * Ensure that [Hudson](http://hudson.sapsailing.com) has a job for your branch; simply push the branch to the central git at sapsailing.com and let Hudson do the job. This is mostly good for knowing whether everything builds and tests ok. Only if you push the special "release" tag, Hudson will build a release and upload it to [releases.sapsailing.com](http://releases.sapsailing.com).

## Deploying Build Results

When the build has been run using the `buildAndUpdateProduct.sh` script, the build results in the git workspace can be deployed to a server environment under `~/servers/<servername>` using the command

    <gitroot>/configuration/buildAndUpdateProduct.sh -s <servername> install

This will copy all necessary files, in particular the p2 product, to the server directory, including the start and stop scripts.

Again, check out the script's options for more and other possibilities including a remote deployment option and a hot deploy of individual bundles into a running server environment.

Deploying build results is generally also possible with a Hudson build, but it is not recommended because a user would need to log in to the Hudson server, know where which build workspace is located and then apply the deployment script there.

##[Hudson](http://hudson.sapsailing.com) Working with Releases

Particularly when starting an EC2 instance, it is helpful to be able to do that using a well-known release of the product. When an EC2 instance starts, it has a version of the product built into the image and its disk snapshots from which the instance got initialized. This, however, is usually not up to date. To refresh it, you could run a build from a specific git commit, or you could install a **release** previously assembled using the `release` option of the `buildAndUpdateProduct.sh` script.