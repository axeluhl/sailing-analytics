# Igtimi WindBot Riot Connector and Server

[[_TOC_]]

## Architecture Overview

Igtimi used to be a company based in Dunedin, New Zealand, makers of the YachtBot and WindBot devices for boat tracking and wind and current sensing. The devices come equipped with a "Brain" that holds a battery, a SIM card-based Internet connection, an SDCard for configuration and log file storage, and an IMU (Inertial Motion Unit) with a satellite positioning sensor and accelerometers. The WindBot comes with a Gill ultrasonic wind sensor at the top of a carbon fibre pole, connected to the brain through a cable running inside the hollow pole. When switched on, the devices will try to establish a TCP connection to a server (the "Riot" server), performing a handshake during which the device authenticates to the server using a "device group token," then sending sensor and status data.

The transmission protocol is based on Protocol Buffers (protobuf), a serialization / de-serialization standard developed by Google. It works both ways, so the server can also send protobuf messages to the devices through the socket connection, once established. This can be used for acknowledgement messages but also for sending commands to the device, e.g., in order to power it down. The protocol used by the WindBot devices has been documented [here](https://support.yacht-bot.com/YachtBot%20Products/Riot%20Protocol/). The technical specification of the protobuf message formats can be found in [this Github project](https://github.com/igtimi/go-igtimi/tree/main/proto).

The default address to which the devices connect is ``www.igtimi.com:6000``. Since [firmware version 2.2.540](https://support.yacht-bot.com/YachtBot%20Products/Firmware%20Updates/#firmware-update-process) it is possible to [configure](https://github.com/igtimi/go-igtimi?tab=readme-ov-file#device-configuration) the address to which the devices connect in their ``config.ini`` configuration file on their SDCard. Using a line like

```
    riot server_address sapsailing.com 6000
```

the connection will be made to ``sapsailing.com:6000`` instead of the ``igtimi.com`` address.

Igtimi called the back-end to which the devices connect the "Riot" system. It has a REST API through which authenticated clients can discover devices, resources created from data sent from those devices, and data access windows which grant access to data recorded by a specific device for a specified time range to a specific set of users. The API then also allows clients to request the actual data that has been recorded so far, as well as connect with a web socket connection through which the server will stream data from requested devices live if the client authenticated with the web socket connection is permitted to read this data on the basis of one or more data access windows granting permission.

The device-to-Riot connections as well as the web socket connections from API clients to the Riot server implement a heart beat mechanism to notice broken connections and issue a re-connect.

## History of the Riot System at Igtimi and Riedel

Around 2020, the German company *Riedel Communications GmbH* acquired Igtimi and used some of their technology for the America's Cup at the time. The Riot system continued to operate under the Igtimi brand, though owned by Riedel now. In the years that followed, Riedel decided to move out of the sailing domain again. In 2024 they announced they would end the Riot service by the end of the year. Effective January 6, 2025, the Riot system running at ``https://www.yacht-bot.com`` and ``https://www.igtimi.com`` was shut down.

With the new firmware version 2.2.540, the remaining devices can now be made send to a new address that needs to receive the protobuf messages, handle the connection handshake and implement the heart beat mechanism accordingly. An example implementation was provided by Riedel as an [open-source project](https://github.com/igtimi/go-igtimi). With this, existing customers are now on their own when it comes to providing a new back-end for the devices still in use.

We decided to implement a Riot server replacement that comes as a few OSGi bundles which can be embedded in our existing Sailing Analytics architecture.

## Embedded Riot Server in the Sailing Analytics

We captured the requirement to build our own Riot server replacement in [bug 6059](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=6059) and dependent tasks.

### OSGi Bundles

The solution is built around the existing Igtimi connector, mainly implemented by bundle ``com.sap.sailing.domain.igtimiadapter`` and web bundle ``com.sap.sailing.domain.igtimiadapter.gateway`` serving an API under the ``/igtimi`` URL path. The former had a client for the Riot REST API to authenticate with a client ID and a client secret, then query devices and data access windows, load stored data and connect to live data using web socket connections. The gateway bundle was used primarily as a callback interface to receive an OAuth token during the account authentication phase which was then used to authenticate API requests. A third bundle, ``com.sap.sailing.domain.igtimiadapter.persistence``, was used to store the account and token information persistently across server restarts, and a test fragment ``com.sap.sailing.domain.igtimiadapter.test`` ran a few tests for the API client.

A new bundle ``com.sap.sailing.domain.igtimiadapter.server`` was added that now implements the core Riot functionality, in particular the socket server that listens for incoming device connections. The persistence bundle was changed and now stores what the Riot server implementation needs: ``Device`` and ``DataAccessWindow`` entities as well as the messages received form the devices. This makes for three MongoDB collections managed by the bundle:

- ``IGTIMI_DATA_ACCESS_WINDOWS``
- ``IGTIMI_DEVICES``
- ``IGTIMI_MESSAGES``

The server bundle's activator loads the existing devices and data access windows from the database and keeps them in memory during its life span. It launches the socket server, using ``java.nio`` selectors for good resource utilization. Devices connecting and sending an authentication request will be sent an acknowledgement. If a device yet unknown connects for the first time, a new ``Device`` entity will be created automatically, although without any data access window. Messages received from any device will be stored in the database and will be forwarded to any live web socket client that registered for messages from that device and can ``READ`` a data access window for that device spanning the event's time point.

The gateway bundle offers API methods to create, query and update the device and data access window entities, as well as for downloading sensor data and connecting with web sockets for live sensor data. These API methods are not 100% compatible with the original Riot API but have been designed sufficiently similar, achieving the goal of reducing the need for change in the existing client connector implemented by the ``com.sap.sailing.domain.igtimiadapter`` bundle.

The ``com.sap.sailing.gwt.ui`` bundle, in particular its AdminConsole entry point and in it the ``IgtimiDevicesPanel`` as well as the corresponding ``SailingService`` GWT RPC methods have been adjusted. Instead of managing the old accounts required for accessing the Igtimi-hosted Riot API it now provides a view onto the Riot server running embedded in the Sailing Analytics server that the AdminConsole holding the Igtimi panel configures. Consequently, it shows the devices and their associated data access windows and offers basic support for sending commands to the devices as well as seeing their last known heart beat time point and last known position.

### Security Aspects

Devices and data access windows are secured entities that support the usual group and user ownerships as well as access control lists and the default actions ``CREATE``, ``READ``, ``UPDATE``, and ``DELETE``. As usual, the GWT UI's server side, here in particular ``SailingService[Write]Impl``, and the REST API methods in the ``com.sap.sailing.domain.igtimiadapter.gateway`` bundle implement the necessary permission checks. Furthermore, 

TODO Device Auto-Creation/ownerships

## New Riot REST API

### Changes Compared to Original Riot REST API

## Riot/Igtimi Client Connector

### Changes Compared to Original Riot/Igtimi Client Connector