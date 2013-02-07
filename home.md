# Welcome to the SAP Sailing Wiki

This is the <img src="http://analysis.sapsailing.com/themes/logo.png" height="58" width="200" /> Wiki where useful information regarding this project can be found. This Wiki is being stored directly into the default GIT repository on branch **master**. You can edit information here freely but do not leave any nonsense here.

The syntax is relatively easy and is documented here http://daringfireball.net/projects/markdown/dingus. When you create a new Page then make sure to put some thinking into the right tree structure and then just extend the URL above. 

**ATTENTION**: 

* Make sure to always have _wiki/_ as the base path. So if you want to create `server/SystemBoot` then the correct URL is `wiki/server/SystemBoot`. Make sure to add you Page to the Table of Contents below by editing the document.

* Password can be found in the encrypted page [[Passwords|wiki/passwords]]. You can decrypt it by using `gpg -d < passwords.md` after having entered the password. If you want to edit, first decrypt and then encrypt again using `gpg -a --symmetric passwords.md`. Make sure to never save the unencrypted version!

If you have any questions then either contact Axel Uhl, Jens Rommel or Simon Pamiés.

### Internal services that you can access

* [Bugzilla Issue Tracking System](http://bugzilla.sapsailing.com/bugzilla/)
* [Maven Repository Browser](http://maven.sapsailing.com/maven/)
* [Main Sailing Website](http://www.sapsailing.com)

### Table of Contents Wiki

* [[Architecture and Infrastructure|wiki/architecture-and-infrastructure]]
* [[Configure Races on Server|wiki/configure-races-on-server]]