# QR-Codes

There are several use cases where a QR-Code will be created in or outsite of SAP Sailing Analytics. The QR-Codes are used to share links between devices and makes it comfortable for the user to open the right app and jump then directly to the specific event or regatta.

Because it's not trivial to get information about the currently used device and the installation status of the app branch.io is used as an external service to support SAP Sailing for this specific task.

## General Process Description

1. A link and corresponding QR-Code is created
2. The user reads the QR-Code on his mobile device by camera
3. URL will call branch.io
4. The device type is identified by branch.io
5. branch.io tries to get the current installation status
6. If not installed already the app store is opening to install the correct app
7. The app is opeining
8. The specific event/regatta/... is opening (based on the URL parameter of the origin QR-Code link)
9. If no supported device is used a default page is opening

## How to Configure branch.io

At branch.io on the top left site is an app selector located. Here it is possible to choose the specific app, like for example 'SAP Sailing Race Manager' or 'Sail Insight 3.0'.
For each app one or more Quick Links can be defined. These are the entry points, branch.io is reacting on.

The link reflected by the created QR-Code has to be starting with one of these Quick Links. E.g.  `https://racemanager-app.sapsailing.com/invite?...` tries to open SAP Sailing Race Manager app. The rest of the URL will be transfered through all the redirections. This data transfer allows us to use the data from the origin URL far later in the process. An app installation for example will not lead in forgetting the data from origin URL.

There is another section under `CONFIGURE -> Configuration` to define the behaviour after calling this URL. 

Here it is possible to set redirects for Android and/or iOS devices. It needs an URL in case the app is already installed (branch.io is doing some magic to get these information) and some info to find the correct app in the respective app store if the app is not been installed.

For all the other cases, e.g. if the URL is called on a Windows device, a default URL has to be defined.

## Different Use Cases

### Public Invite to Sail Insight App

Invitation to regatta (Sail Insight App)

#### Create Link/QR-Code

- goto `Admin Console -> Regattas`
- select a regatta (not closed)
- press `Share` button in regatta details section (below the regatta table) to see QR-Code/link

Regatta name (regatta_name), secret (secret), server (server) and event ID (event_id) is included in the URL.

Example link:

`https://sailinsight30-app.sapsailing.com/publicInvite?regatta_name=Sp%C3%A6khugger+(Tirsdag)&secret=4f3f1a90-cb96-11ea-8fdd-99de47376606&server=https%3A%2F%2Fdev.sapsailing.com&event_id=3a12a728-42f4-48d3-92a6-e93aaf82edd1`

#### Quick link (branch.io)

https://sailinsight30-app.sapsailing.com/publicInvite

##### 

### Competitor or Boat Registration

With this QR-Code the Sail Insight app is (installing and) opening by specified competitor or boat.

#### Quick Link (branch.io)

https://sailinsight30-app.sapsailing.com

#### Create Link/QR-Code

- goto `Admin Console -> Connectors - Smartphone Tracking`
- select a leaderboard
- create a competitor with boat under (person icon) `Register Competitors`
- click `Map Devices to Competitors, Boats and Marks`
- click `Add` button
- select a mark, boat or competitor (for each the QR-Code will look different)
- QR-Code is created, if an event is selected

Server-URL (checkUrl) is later used to identify this link as a competitor or boat request.

Example link:

`https://sailinsight30-app.sapsailing.com/invite?checkinUrl=https%3A%2F%2Fdev.sapsailing.com%2Ftracking%2Fcheckin%3Fevent_id%3Db9777623-3afb-48d6-98fc-dd154129e773%26leaderboard_name%3DAudi%2BSailing%2B25.06%26competitor_id%3D5e72cdca-3644-4c6e-b68b-e80230b0c02d%26secret%3D487cce25-3037-4ebd-898c-74f4b1ef29db`

### Race Management App

With this link the SAP Sailing Race Mangager will be installed or opened and the defined device will be linked.

#### Quick Link (branch.io)

https://racemanager-app.sapsailing.com/invite

#### Create Link/QR-Code

- goto `Admin Console -> Race Manager App - Device Configuration` 
- select a device	
- under `Configuration` click QR-Sync button
- optional check `Include access token for your account?` 

Server URL (server_url), device name (device_config_identifier) and device ID (device_config_uuid) are used to create the link.

Example link:

`https://racemanager-app.sapsailing.com/invite?server_url=https://dev.sapsailing.com&device_config_identifier=Almere&device_config_uuid=1ea38a89-127c-41e5-aaee-b24e36ba0fa7&event_id=30b60b53-63b6-407b-8480-710ff5bf8aa6&priority=1&token=ZtKJq0TdC2EGPYM10jerQq5omp8U6EcoAmArs5J1sZg%3D`


### Boye Pinger

-- to be defined --
