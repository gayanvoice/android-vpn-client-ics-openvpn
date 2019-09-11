# Android VPN Client using ics-openvpn

The client app is based on OpenVPN protocol, and you can create your own android vpn app like Turbo VPN, Thunder VPN, or Hotspot Shield Free VPN. The app can calcualte the daily usage of data, connected time, check connection speeds from notifications, change the interface into night-mode.

## Watch
[![Android VPN Client using ics-openvpn](http://img.youtube.com/vi/1ms4mxEw378/0.jpg)](https://www.youtube.com/watch?v=1ms4mxEw378)

# Introduction

In this android app, you can develop your own android vpn client. The project is based up on [https://github.com/schwabe/ics-openvpn](https://github.com/schwabe/ics-openvpn).

This repository contains the files related to the app. The `master` branch contains the app's source code (the code the app's developers edit).

The remainder of this document contains how to deploy the app on production and configuring the source code.

# Develop

## Prerequisites

1. The latest version of [`Android Studio`](https://developer.android.com/studio) and the latest versin of [`Gradle`](https://gradle.org/). Here's the adequate version I use:

    ```
    Android Studio
    3.4.0.0
    
    ```
1. The latest versin of [`Gradle`](https://gradle.org/). Here's the adequate version I use:
    
    ```
    gradle version
    5.5.1
    ```

## Procedure

 **Click Clone or download button and download the project**

    * Unzip the github project to a folder. Open Android Studio.
    * Go to File -> New -> Import Project.
    * Then choose the specific project you want to import and then click Next->Finish.
      (It will build the Gradle automatically and'll be ready for you to use.)
 
### Or
 **Git clone in command line.** (5 minutes)

    ```sh
    $ git clone --bare https://github.com/gayankuruppu/android-vpn-client-ics-openvpn
    ```


# References

1. [OpenVPN for Android](https://github.com/schwabe/ics-openvpn)
