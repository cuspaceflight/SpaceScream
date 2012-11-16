Scream in Space app
===================

(Code hosted on [CUSF Github] (https://github.com/cuspaceflight/SpaceScream/)  and [STRaND-1 SVN] (http://code.google.com/p/s-android/source/browse/trunk/apps/SpaceScream))

Requirements
------------

The SD card should contrain the following files/directories within the main app directory (/sdcard/strand/scream):

* **soundtrack.mp3**
* **intro** folder
  * **01-cusf.png**
* **videos** folder, containing all of the [scream videos] (http://www.screaminspace.com/screams) in .mp4 format
* **images** folder, containing still frames of each scream video
* **dynamicearth** folder
  * **earth.mp3**

The app expects all of these files to exist. The full contents of the scream directory is available to [download] (https://dl.dropbox.com/u/29027386/scream.tar.gz).

Operation
---------

Here we describe the standard operation of the app when it is started by the [MCA] (http://code.google.com/p/s-android/source/browse/trunk/apps/MCAv2).

[ScreamService] (/cuspaceflight/SpaceScream/src/com/strand/scream/ScreamService.java) is the main Service for the app, and manages the Activity scheduling, as well as handling the parameters passed to the app. When run without any parameters, it will start the following activities in turn, looping until a stop command is received.

### Intro ###

Plays soundtrack.mp3, generates and plays some radioteletype tones, and then displays logos on screen with camera preview in background (screenshots are requested of each logo).

### PlayVideos ###

Plays each video found in the videos directory, and records audio from microphone into audio directory (which will be created if it doesn't exist) for each. On completion of recording, a file transfer will be requested. A recording will only be made if one doesn't exist already for that video.

### DisplayImages ###

Displays still frame from each video in full screen (camera preview in background for area of screen not filled by image), and requests a screenshot if one hasn't been taken before.

### DisplayWindowImages ###

Displays still frame from each video in corner of screen, in front of camera preview. Requests screenshot for each (as before, only if one hasn't been taken before), and also takes a photo from phone camera, saving both full resolution version, and a thumbnail. The thumbnail is scheduled for file transfer.

### Outro ###

Plays earth.mp3 (composition by school children), and shows CUSF logo on screen on top of camera preview.

Parameters
----------

The app accepts a `PARAM_LIST` string in query string form. The app schedule will operate as described above without the need for any particular parameters.

The `run` parameter allows you to explicitly specify which of the activities above should be included in the schedule, and takes the value of a bitmask:

    (1) Intro = 1
    (2) PlayVideos = 2
    (3) DisplayImages = 4
    (4) DisplayWindowImages = 8
    (5) Outro = 16
    (6) Repeat = 32

For example, `?run=14` corresponds to just the three main activities being run (2 + 4 + 8 = 14). The last step corresponding to 32 simply allows the activity schedule to loop. `?run=0` could be of practical use in combination with an `action` parameter (below). By default, all activities will run, and loop indefinitely.

The following values of the `action` parameter are implemented:

### reset ###

Example usage: `?action=reset`

This deletes the audio and screenshots directories, meaning that new recording and screenshots will be taken when the app runs (essentially a fresh run).

### delete ###

Example usage: `?action=delete`

This deletes the photos and recorded directories (the latter containing video recordings from the camera, see below), mainly for the purpose of freeing up storage space.

### video ###

Example usage: `?action=video&size=2048576&hq=true`

This command starts an Activity which isn't part of the main app schedule, which will record a video from the camera, and then request a file transfer. The `size` parameter specifies the maximum file size (in bytes) that the video file will be - it will continue recording until that size is reached. The `hq` parameter, if equal to true, will set the recording profile to `QUALITY_HIGH` (by default it is `QUALITY_LOW`).

### file ###

Example usage: `?action=file&path=/sdcard/strand/scream/photos/1353005904.jpg`

This is used to request the transfer of the file matched by the `path` parameter. An example use case would be to downlink the full resolution version of a thumbnail that was previously transferred.