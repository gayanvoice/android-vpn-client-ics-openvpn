# Android VPN Client using ics-openvpn

The client app is based on OpenVPN protocol, and you can create your own android VPN apps like Turbo VPN, Thunder VPN, or Hotspot Shield Free VPN.

The app can calculate the daily usage of data, connected time, check connection speed from notifications, change the interface into night-mode, multiple servers, remote-config and visual-effects.

# The servers are now offline! So the servers are waiting for a connection. Get an OpenVPN file and paste the file in [filedetails.json](https://gayankuruppu.github.io/oml/buzz/filedetails.json). Create your own JSON file and host in a server.

## Links

Get the APK files from the following app-stores
1. [https://play.google.com/store/apps/details?id=com.buzz.vpn](https://play.google.com/store/apps/details?id=com.buzz.vpn)
2. [https://buzz-vpn-fast-free-unlimited-secure-vpn-proxy.en.uptodown.com/android](https://buzz-vpn-fast-free-unlimited-secure-vpn-proxy.en.uptodown.com/android)
3. [https://www.amazon.com/Buzz-VPN-Free-Pop-up-Ads/dp/B07T3X677T](https://www.amazon.com/Buzz-VPN-Free-Pop-up-Ads/dp/B07T3X677T)

## Images
![Android VPN Client using ics-openvpn](https://lh3.googleusercontent.com/yvUkS8eVus7uFmN29-A-xGWihG_4JyizZ-09X4rpjsoxL7tJH3vKFsvfRsD78dnfKFE=w1366-h657-rw "Android VPN Client using ics-openvpn")

## Watch
[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/1ms4mxEw378/0.jpg)](https://www.youtube.com/watch?v=1ms4mxEw378)

# Introduction

In this android app, you can develop your own android vpn client. The project is based up on [https://github.com/schwabe/ics-openvpn](https://github.com/schwabe/ics-openvpn).

This repository contains the files related to the app. The `master` branch contains the app's source code (the code the app's developers edit).

The remainder of this document contains how to deploy the app on production and configuring the source code.

# Develop

## #1 Select checkout project from version control
![https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-1-checkout-project-from-version-control.png](https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-1-checkout-project-from-version-control.png "step-1-checkout-project-from-version-control")

## #2 Enter the link of the repository and select project folder
![https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-2-enter-the-link-of-the--git-repository.PNG](https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-2-enter-the-link-of-the--git-repository.PNG "step-2-enter-the-link-of-the--git-repository")

## #3 Click Yes to open the repositoy on the project
![https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-3-click-yes-to-open-the-repository.PNG](https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-3-click-yes-to-open-the-repository.PNG "step-3-click-yes-to-open-the-repository")

## #4 Build started
![https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-4-build-started.PNG](https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-4-build-started.PNG "step-4-build-started")

## #5 Build finished
![https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-5-build-finished.PNG](https://raw.githubusercontent.com/gayankuruppu/gayankuruppu.github.io/master/images/step-5-build-finished.PNG "step-5-build-finished")

## Deploy

* Before deploying your app, please remove the google-services.json file from your app. [google-services.json](https://github.com/gayankuruppu/android-vpn-client-ics-openvpn/blob/cfd8f922f145d404618cfe1522fb76d9a9b8b698/app/google-services.json#L4)

# Install OpenVPN server for multiple users by few steps. Go to this repository and clone. Run the script [OpenVPN Install for Multiple Users](https://github.com/gayankuruppu/openvpn-install-for-multiple-users)


### Remote config

**App details**

Load the details of the app from the following link [app details](https://github.com/gayankuruppu/android-vpn-client-ics-openvpn/blob/c35b88b40a8ba6aa382ca7324981511f4c6e886d/app/src/main/java/com/buzz/vpn/WelcomeActivity.java#L59).
Go to [appdetails.json](https://gayankuruppu.github.io/oml/buzz/appdetails.json).

**File details**

Load the OpenVPN files from the following link [file details](https://github.com/gayankuruppu/android-vpn-client-ics-openvpn/blob/c35b88b40a8ba6aa382ca7324981511f4c6e886d/app/src/main/java/com/buzz/vpn/WelcomeActivity.java#L60).
Go to [filedetails.json](https://gayankuruppu.github.io/oml/buzz/filedetails.json).

# References

1. [OpenVPN for Android](https://github.com/schwabe/ics-openvpn)
