# Android VPN Client using ics-openvpn
[![Build Status](https://travis-ci.org/gayanvoice/android-vpn-client-ics-openvpn.svg?branch=master)](https://travis-ci.org/gayanvoice/android-vpn-client-ics-openvpn) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/gayanvoice/android-vpn-client-ics-openvpn) 
[![HitCount](http://hits.dwyl.io/gayanvoice/android-vpn-client-ics-openvpn.svg)](http://hits.dwyl.io/gayanvoice/android-vpn-client-ics-openvpn)
[![GitHub Stats](http://www.githubstats.com/get/gayanvoice/android-vpn-client-ics-openvpn)](http://www.githubstats.com/stats)

<img width="300" alt="Android VPN Client using ics-openvpn" src="https://gayanvoice.github.io/android-vpn-client-ics-openvpn/static/media/0-android-vpn-client-app.b8e63ecd.gif">

Go to the Medium article to see how to configure the project [https://medium.com/@kuruppu.gayan/develop-a-vpn-app-in-java-using-android-studio-6f1f2d66031e?sk=57ebd1c9175d5f56bd8e328731b5ac74](https://medium.com/@kuruppu.gayan/develop-a-vpn-app-in-java-using-android-studio-6f1f2d66031e?sk=57ebd1c9175d5f56bd8e328731b5ac74)

The client app is based on OpenVPN protocol, and you can create your own android VPN apps like Turbo VPN, Thunder VPN, or Hotspot Shield Free VPN.

The app can calculate the daily usage of data, connected time, check connection speed from notifications, change the interface into night-mode, multiple servers, remote-config and visual-effects.

# The servers are now offline! So the servers are waiting for a connection. Get an OpenVPN file and paste the file in [filedetails.json](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/filedetails.json). Create your own JSON file and host in a server.

## Links

Get the APK files from the following app-stores
1. [https://play.google.com/store/apps/details?id=com.buzz.vpn](https://play.google.com/store/apps/details?id=com.buzz.vpn)
2. [https://buzz-vpn-fast-free-unlimited-secure-vpn-proxy.en.uptodown.com/android](https://buzz-vpn-fast-free-unlimited-secure-vpn-proxy.en.uptodown.com/android)
3. [https://www.amazon.com/Buzz-VPN-Free-Pop-up-Ads/dp/B07T3X677T](https://www.amazon.com/Buzz-VPN-Free-Pop-up-Ads/dp/B07T3X677T)

## Images
![Android VPN Client using ics-openvpn](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/android-vpn-client-ics-openvpn-image.png "Android VPN Client using ics-openvpn")

## Watch
<a href="https://www.youtube.com/watch?v=1ms4mxEw378">
<img src="https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/android-vpn-client-ics-openvpn-yt.png" height="600px"/>
</a>

# Introduction

In this android app, you can develop your own android vpn client. The project is based up on [https://github.com/schwabe/ics-openvpn](https://github.com/schwabe/ics-openvpn).

This repository contains the files related to the app. The `master` branch contains the app's source code (the code the app's developers edit).

The remainder of this document contains how to deploy the app on production and configuring the source code.

# Develop

## # Follow [https://github.com/gayanvoice](https://github.com/gayanvoice) on GitHub

## #1 Select checkout project from version control
![https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-1-checkout-project-from-version-control.png](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-1-checkout-project-from-version-control.png "step-1-checkout-project-from-version-control")

## #2 Enter the link of the repository and select project folder
![https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-2-enter-the-link-of-the--git-repository.png](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-2-enter-the-link-of-the--git-repository.png "step-2-enter-the-link-of-the--git-repository")

## #3 Click Yes to open the repositoy on the project
![https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-3-click-yes-to-open-the-repository.png](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-3-click-yes-to-open-the-repository.png "step-3-click-yes-to-open-the-repository")

## #4 Build started
![https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-4-build-started.png](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-4-build-started.png "step-4-build-started")

## #5 Build finished
![https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-5-build-finished.png](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/step-5-build-finished.png "step-5-build-finished")

## Deploy

* Before deploying your app, please remove the google-services.json file from your app. [google-services.json](https://github.com/gayanvoice/android-vpn-client-ics-openvpn/blob/cfd8f922f145d404618cfe1522fb76d9a9b8b698/app/google-services.json#L4)

# Install OpenVPN server for multiple users by few steps. Go to this repository and clone. Run the script [OpenVPN Install for Multiple Users](https://github.com/gayanvoice/openvpn-install-for-multiple-users)


### Remote config

**App details**

Load the details of the app from the following link [app details](https://github.com/gayanvoice/android-vpn-client-ics-openvpn/blob/c35b88b40a8ba6aa382ca7324981511f4c6e886d/app/src/main/java/com/buzz/vpn/WelcomeActivity.java#L59).
Go to [appdetails.json](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/appdetails.json).

**File details**

Load the OpenVPN files from the following link [file details](https://github.com/gayanvoice/android-vpn-client-ics-openvpn/blob/c35b88b40a8ba6aa382ca7324981511f4c6e886d/app/src/main/java/com/buzz/vpn/WelcomeActivity.java#L60).
Go to [filedetails.json](https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/filedetails.json).

# Configure appdetails.json
```json
{
  "ads":"true",
  "update":[{
    "version":"2.8.1600",
    "title":"This app is now Open Source",
    "description":"The App is available at github.com/gayankuruppu/android-vpn-client-ics-openvpn",
    "size":"https://git.io/JeY69"
  }],
  "blocked":[
    {"id":0, "app":"com.android.game"},
    {"id":1, "app":"com.utorrent.client"},
    {"id":2, "app":"com.torrent.client"},
    {"id":3, "app":"com.tor.client"},
    {"id":4, "app":"com.insta.client"},
    {"id":5, "app":"com.facebook.client"},
    {"id":6, "app":"com.get.client"}
  ],
  "free":[
    {"id":0, "file":0, "city":"Essen","country":"Germany","image":"germany","ip":"51.68.191.75","active":"true","signal":"a"},
    {"id":1, "file":0, "city":"Hamburg","country":"Germany","image":"germany","ip":"51.68.191.75","active":"true","signal":"b"},
    {"id":2, "file":1, "city":"Los Angeles CA","country":"United States","image":"unitedstates","ip":"205.185.119.100","active":"true","signal":"c"}
  ]
}
```

## The appdetails.json file has four main parts.
* The value `ads` is a `boolean value`, you can choose `true` or `false`. If the value is `true`, the app will show ads when the session starts. Otherwise, ads will not show if the value is `false`.

* The array `update` has three values. version is a `String value` which is the latest version of the app. When the session starts the app check if the version value is equal to the version of the app. If the values do not match with each other the Update View will show. The values `title`, `description`, and `size` are the values display in the Update View.

* The array `blocked` has JSON objects with values `id` and `package name`. You can add apps such as Torrent to avoid the peer to peer file sharing which often misuse by downloading digital media.

* The array `free` has values of the server names. The value `city` displays the `server name` and the value `image` is the name of the flag. The value `signal` is the value of the `strength of the signal`. The value `file` is the `index` value of the `source OVPN file` in the `filedetails.json`.

* Change the JSON values and upload into your server or host it in the forked repository (https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/appdetails.json) and add the link of the JSON file in the WelcomeActivity.java file https://github.com/gayanvoice/android-vpn-client-ics-openvpn/blob/c35b88b40a8ba6aa382ca7324981511f4c6e886d/app/src/main/java/com/buzz/vpn/WelcomeActivity.java#L59

# Configure filedetails.json
```json
{
  "ovpn_file":[
    {"id":0,"file":"client
    dev tun
    proto udp
    ...
    d4ec4105a39c814bd980c9c0e0b8efb4
    -----END OpenVPN Static key V1-----
    </tls-auth>"},
    {"id":1,"file":""}]
}
```
* The filedetails.json file stores the `OpenVPN source file` which is a `String` value to set up teh internet connection.
* Copy the text in the `OVPN file` and paste it in the JSON String (https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/filedetails.json)
* Add the link address in the WelcomeActivity.java https://github.com/gayanvoice/android-vpn-client-ics-openvpn/blob/c35b88b40a8ba6aa382ca7324981511f4c6e886d/app/src/main/java/com/buzz/vpn/WelcomeActivity.java#L60

# Run the app
* That is all. Now to can change the app UI and deploy the app.
* Remove the Google Services JSON file before deploying (https://github.com/gayankuruppu/android-vpn-client-ics-openvpn/blob/master/app/google-services.json)
Github https://github.com/gayanvoice/android-vpn-client-ics-openvpn

# References
1. [OpenVPN for Android - GitHub](https://github.com/schwabe/ics-openvpn)
2. [Develop a VPN App in Java using Android Studio - Medium](https://medium.com/@kuruppu.gayan/develop-a-vpn-app-in-java-using-android-studio-6f1f2d66031e)
