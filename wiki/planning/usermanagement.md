# User and Account Management

Account, user and role management is required mainly for three reasons:

* Offering services to a restricted audience
Example: special add-on services and advanced analyses available only to sailors and coaches of a particular group

* Remembering user-specific settings
This will include preferences for views such as the leader board, race board and may also help configuring a "portal" with widgets in a personalized way

* Managing data ownership, such as GPS tracks uploaded by a user which (at least initially) are visible only to that user.

We need easy to use and manage sign up, sign on, sign out and manage user account features, ideally including e-mail verification and group or role management functionality.

See also bug 27 (http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=27).

There is a branch `usermanagement` that contains Benjamin Ebling's contributions to the subject, namely a set of bundles that integrate Apache Shiro with the SAP Sailing Analytics, providing the necessary sign-up, sign-on and sign-out functionality and allowing front end and back end to identify the current user. This branch has been forked to `axel-usermanagement` which is the continuation of Benjamin's work, working towards master mergability. [The page behind this link describes the current development status](/wiki/usermanagement).

On top of that we need to implement account management where users can be assigned to specific accounts, such as a club or event of regatta account. Access by users to administrative features of regattas and events and analytics visibility could then be nicely restricted to users belonging to the right accounts. Technically, an account could probably be implemented as a role, and events, regattas or races could use such special roles to control access to their data.