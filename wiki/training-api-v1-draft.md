# Training API V1 – draft

## Introduction

This document describes the a API for tracking training sessions. 

Use cases include:

* User Management
    * Register as new user
    * Login as user
    * Logout
    * Edit profile
* Training
    * Set up
        * define course
        * set wind
        * invite others
    * Track
    * Analyze sessions
    * View history

Please note that this interface is independant of the race tracking API described here: http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft

## Endpoint

All interfaces described here are accessed via one public endpoint, e.g.  
```
http://training.sapsailing.com/api/v1
```
This is unlike the race tracking API where the server is part of the configuration stored in the QR code. 

All relative URLs given below are relative to this base URL.

## Authentication

Clients authenticate users with E-Mail and password. The password should be hashed with salt.  On authentication, the server returns an **access token**.

### Custom Header
For all following requests, the client passes the access token in a custom header field, ``X-Access-Token``.

Any request containing an invalid access token produces a response with a ``401`` HTTP status code.

### Login

This should be the only API call that doesn't need the ``X-Access-Token`` header field.

**Path:** ``login``

**Verb:** ``POST``

**Request:**
```
{
  "email" : "anna@web.de",
  "password" : "8e578ab3de1f7aa66bfeb6bf7528e49d"
}
```

**Response:**

```
{
  "access_token" : "57f1e17d64d93597956d56b362ae52ec"
}
```
On incorrect login, HTTP status code ``401`` is returned.

### Logout

Logging out invalidates the access token.

**Path:** ``logout``

**Verb:** ``POST``

## Users

Users are not competitors as definied in the race tracking API. A user is a person that uses the tracking app for training purposes. Should the app be used for tracking a race, the previously defined procedures (E-Mail link or QR-code scan) are used.

### Properties

The ``users`` object has the following properties:

* ``user_id`` server generated key for user
* ``email`` email address is used for logging in, therefore it is unique
* ``name`` full name of user, displayed on user's device and on others' invite list
* ``password`` password of user, preferably hashed, see login further below
* ``sailing_club`` optional, name of sailing club
* ``city`` optional, city of user
* ``countryCode`` optional, country code of user
* ``profile_image_url`` optional, URL pointing to user's profile image

### Get Details of Logged In User

Called for getting details of logged in user, typically after login.

**Path:** ``me``

**Verb:** ``GET``

**Response:**
```
{
  "user_id" : "9871d3a2c554b27151cacf1422eec048",
  "email" : "anna@web.de",
  "name" : "Anna",
  "city" : "Waldorf",
  "countryCode" : "DE",
  ...
}
```

The password is omitted from the response.

### Get User Details

Called for getting details of other users, e.g. on invite to training.

**Path:** ``users/{user_id}``

**Verb:** ``GET``

**Response:**
```
{
  "user_id" : "d34974c6fca82061398f88bd1ab993c7",
  "email" : "berta@web.de",
  "name" : "Berta",
  "city" : "Waldorf",
  "countryCode" : "DE",
  ...
}
```

The password is omitted from the response.

### Create New User

Called on registration.

**Path:** ``users``

**Verb:** ``POST``

**Request:**
```
{
  "email" : "anna@web.de",
  "name" : "Anna",
  "password" : "8e578ab3de1f7aa66bfeb6bf7528e49d"
}
```

**Response:**

On registration, user details as well as an ``access_token`` are returned (latter to short-cut a call to ``login``).

```
{
  "access_token" : "0e0530c1430da76495955eb06eb99d95",
  "user_id" : "9871d3a2c554b27151cacf1422eec048",
  "email" : "anna@web.de",
  "name" : "Anna"
}
```
The password is omitted from the response.

### Update User

Update a user. Only sent properties are updated, all others remain untouched. To delete a value, it must be set to ``null``.

**Path:** ``users/{user_id}``

**Verb:** ``PUT``

**Request:**
```
{
  "name" : "Anna",
  "city" : "New York",
  "countryCode" : "US"
}
```

**Response:**
```
{
  "user_id" : "9871d3a2c554b27151cacf1422eec048",
  "email" : "anna@web.de",
  "name" : "Anna",
  "city" : "New York",
  "countryCode" : "US",
  ...
}
```
The password is omitted from the response.

### Upload Profile Image

A multipart message is used to upload a profile image.

**Path:** ``me/{user_id}/profile_image``

**Verb:** ``POST``

**Request:**

Multipart message with ``image`` part containing encoded JPEG or PNG image.

**Response:**
```
{
  "profile_image_url" : "http://training.sapsailing.com/profile_images/9871d3a2c554b27151cacf1422eec048.jpeg"
}
```
The response contains the URL of the uploaded image.

## Training

A training is defined by a course, participants and is split into sessions.

### Properties

The ``training`` object has the following properties:

* ``training_id`` server generated key for training session
* ``create_date`` timestamp, date training was created
* ``finish_date`` timestamp, date training was finished
* ``create_user_id`` ID of user that created training session
* ``user_ids`` array of user IDs participating
* ``course`` object describing course, e.g. type, GPS coordinates of marks
* ``wind`` object describing wind, i.e. direction and speed
* ``markers`` array containing timestamps of markers set during training
* ``sessions`` array of training sessions

### Get Training

Get complete details about a training. These include:

* course
* wind
* list of join requests
* list of markers
* list of sesions

**Path:** ``training/{training_id}``

**Verb:** ``GET``

**Response:**
```
{
  "training_id" : "c185ddac8b5a8f5aa23c5b80bc12d214",
  "create_user_id" : "9871d3a2c554b27151cacf1422eec048",
  "create_user_name" : "John Doe",
  "create_date" : 14144160080000,
  "finish_date" : 14144180080000,
  "course" : {
    "course_type" : "triangle",
    "marks" : [
      {
        "mark_id" : "ac673f4dbac79922838901b5974a419a",
        "mark_type" : "pin_end",
        "latitude" : 55.12456,
        "longitude" : 8.03456
      }  
    ]
  },
  "wind" : {
    "direction" : 14.2,
    "speed" : 5.1
  },
  "join_requests" : [
    {
      "join_request_id": "7f06f4e629a40de268ceaa22bba16f2b"
      "status": "accepted",
      "user_id" : "9871d3a2c554b27151cacf1422eec048",
      "name" : "Anna"
    } 
  ],
  "markers" : [
    {
      "marker_id" : "111dda7aaed6fb03a543e8adb272393c",
      "timestamp" : 14144168490000,
      "latitude" : 55.12456,
      "longitude" : 8.03456
    }
  ],
  "sessions" : [
    {
      "session_id" : "21d6f40cfb511982e4424e0e250a9557",
      "start" : 14144168490000,
      "finish" : 14144190000000
    }
  ]
}
```

### Create New Training

Start a new training.

**Path:** ``training``

**Verb:** ``POST``

**Request:**
```
{
  "create_user_id" : "9871d3a2c554b27151cacf1422eec048",
  "create_date" : 14144160080000
}
```
**Response:**
```
{
  "training_id" : "c185ddac8b5a8f5aa23c5b80bc12d214",
  "create_user_id" : "9871d3a2c554b27151cacf1422eec048",
  "create_date" : 14144160080000
}
```

### Finish Training

Finish a training.

**Path:** ``training/{training_id}/finish``

**Verb:** ``POST``

**Request:**
```
{
  "finish_date" : 14144180080000
}
```

### Users

When a user sets up a training, the device GPS location is sent to the server. Nearby users can see the training and request to join in. The author of the training can than accept join requests.

#### Get List of Nearby Trainings 

This call gets all training possiblilities nearby.

**Path:** ``training/nearby``

**Verb:** ``GET``

**Response:**
```
{
  "nearby" : [
    {
      "training_id" : "c185ddac8b5a8f5aa23c5b80bc12d214",
      "create_user_id" : "9871d3a2c554b27151cacf1422eec048",
      "create_user_name" : "John Doe",
      "create_date" : 14144160080000
    }
  ]
}
```

#### Send a Join Request

This call is used by the users who wants to join a training.

This call gets all training possiblilities nearby.

**Path:** ``training/{training_id}/join_request``

**Verb:** ``POST``

**Request:**
```
{
  "user_id" : "9871d3a2c554b27151cacf1422eec048",
  "name" : "Anna"
}
```
**Response:**
```
{
  "join_request_id": "7f06f4e629a40de268ceaa22bba16f2b"
  "status": "open",
  "user_id" : "9871d3a2c554b27151cacf1422eec048",
  "name" : "Anna"
}
```

#### Get List of Join Requests

This call is used by the initiator to see who wants to join in.

**Path:** ``training/{training_id}/join_requests``

**Verb:** ``GET``

**Response:**
```
{
  "join_requests": [
    {
      "join_request_id": "7f06f4e629a40de268ceaa22bba16f2b"
      "status": "open",
      "user_id" : "9871d3a2c554b27151cacf1422eec048",
      "name" : "Anna"
    }
  ]
}
```
``status`` can have the values ``open``, ``denied``, ``accepted``

#### Accept or Deny a Join Request

This call is used by the initiator accept or deny a join request.

**Path:** ``training/{training_id}/join_requests/{join_request_id}``

**Verb:** ``PUT``

**Request:**
```
{
  "status" : "accepted"
}
```
``status`` can have the values ``open``, ``denied``, ``accepted``

**Response:**
 ```
{
  "join_request_id": "7f06f4e629a40de268ceaa22bba16f2b"
  "status": "accepted",
  "user_id" : "9871d3a2c554b27151cacf1422eec048",
  "name" : "Anna"
}
```

### Course

#### Get Course

Get details about a training's course.

**Path:** ``training/{training_id}/course``

**Verb:** ``GET``

**Response:**
```
{
  "course_type" : "triangle",
  "marks" : [
    {
      "mark_id" : "ac673f4dbac79922838901b5974a419a",
      "mark_type" : "pin_end",
      "latitude" : 55.12456,
      "longitude" : 8.03456
    }  
  ]
}
```

#### Update Course

Currently only the course type can be set.

**Path:** ``training/{training_id}/course``

**Verb:** ``PUT``

**Request:**
```
{
  "course_type" : "triangle"
}
```
TODO Possible values for ``course_type`` must be defined.

**Response:**
```
{
  "course_type" : "triangle"
}
```

#### Create Course

Creates the race course for training.

**Path:** ``training/{training_id}/course``

**Verb:** ``POST``

**Request:**
```
{
  "course_type" : "triangle"
}
```
TODO Possible values for ``course_type`` must be defined.

**Response:**
```
{
  "course_type" : "triangle"
}
```

#### Set Course Mark

Sets a course mark, e.g. pin end, leeward mark, windward park, or reach mark.

**Path:** ``training/{training_id}/course/marks/{mark_id}``

**Verb:** ``POST``

**Request:**
```
{
  "mark_type" : "pin_end",
  "latitude" : 55.12456,
  "longitude" : 8.03456
}
```
TODO Possible values for ``mark_type`` must be defined.

**Response:**
```
{
  "mark_id" : "ac673f4dbac79922838901b5974a419a",
  "mark_type" : "pin_end",
  "latitude" : 55.12456,
  "longitude" : 8.03456
}
```

#### Delete Course Mark

Deletes a previously defined course mark.

**Path:** ``training/{training_id}/course/marks/{mark_id}``

**Verb:** ``DELETE``

### Set Wind

Sets wind speed and direction.

**Path:** ``training/{training_id}/wind``

**Verb:** ``POST``

**Request:**
```
{
  "direction" : 14.2,
  "speed" : 5.1
}
```
``speed`` is in meters per second.

### Sessions

During a training, markers are set, these are timestamps. Afterwards, the training is split into sessions which are time spans. Analytics are applied to a session.

#### Set Marker

Sets wind speed and direction.

**Path:** ``training/{training_id}/markers``

**Verb:** ``POST``

**Request:**
```
{
  "timestamp" : 14144168490000,
  "latitude" : 55.12456,
  "longitude" : 8.03456
}
```

**Response:**
```
{
  "marker_id" : "111dda7aaed6fb03a543e8adb272393c",
  "timestamp" : 14144168490000,
  "latitude" : 55.12456,
  "longitude" : 8.03456
}
```

#### Create a Session

**Path:** ``training/{training_id}/sessions``

**Verb:** ``POST``

**Request:**
```
{
  "start" : 14144168490000,
  "finish" : 14144190000000
}
```

**Response:**
```
{
  "session_id" : "21d6f40cfb511982e4424e0e250a9557",
  "start" : 14144168490000,
  "finish" : 14144190000000
}
```

#### Update a Session

**Path:** ``training/{training_id}/sessions/{session_id}``

**Verb:** ``PUT``

**Request:**
```
{
  "start" : 14144168490000,
  "finish" : 14144190000000
}
```

**Response:**
```
{
  "session_id" : "21d6f40cfb511982e4424e0e250a9557",
  "start" : 14144168490000,
  "finish" : 14144190000000
}
```

#### Delete a Session

**Path:** ``training/{training_id}/sessions/{session_id}``

**Verb:** ``DELETE``

## Tracking

The tracking interface is identical to that of the race tracking API http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft#Send-Measurements-%28to-the-Fix-Store%29

**Path:** ``gps_fixes``

**Verb:** ``POST``

**Request:**
```
{
  "deviceUuid" : "af855a56-9726-4a9c-a77e-da955bd289bf",
  "fixes" : [
    {
      "timestamp" : 14144160080000,
      "latitude" : 54.325246,
      "longitude" : 10.148556,
      "speed" : 3.61,
      "course" : 258.11,
    },
    {
      "timestamp" : 14144168490000,
      "latitude" : 55.12456,
      "longitude" : 8.03456,
      "speed" : 5.1,
      "course" : 14.2,
    }
  ]
}
```
* JSON array may contain one or several fixes
* ``speed`` Speed over ground in meters per second.
* ``course`` Bearing in degrees.