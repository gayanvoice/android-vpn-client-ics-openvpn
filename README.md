# Android VPN Client using ics-openvpn

The client app is based on OpenVPN protocol, and you can create your own android vpn app like Turbo VPN, Thunder VPN, or Hotspot Shield Free VPN. The app can calcualte the daily usage of data, connected time, check connection speeds from notifications, change the interface into night-mode.

![Light Mode](https://lh3.googleusercontent.com/DYA8r0_U2C0c0N0OMG16m1MQzKe3vpJ0utP-Phwd_3FSnDYTM35tOYArfFqGfobZsLE=w1366-h657-rw)
![Dark Mode](https://lh3.googleusercontent.com/yvUkS8eVus7uFmN29-A-xGWihG_4JyizZ-09X4rpjsoxL7tJH3vKFsvfRsD78dnfKFE=w1366-h657-rw) 



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

1. **Create an *empty* repository on GitHub.** (2 minutes)

    * For this tutorial, I'll create a repository named `react-gh-pages`.
    * By *empty*, I mean *without* a `README.md` file, a `.gitignore` file, a `LICENSE` file, or any other files.

2. **Create a new React app on your computer.** (5 minutes)

    ```sh
    $ create-react-app react-gh-pages
    ```
    
    * This is the app you will deploy to GitHub Pages in step 7.
    * I opted to give the app the same name as my GitHub repository (i.e. `react-gh-pages`). However, you can name them differently from one another (e.g. you can name your app `app-123` and your GitHub Repository `repo-456`).
    * This will create a new folder named `react-gh-pages` (or whatever you named your app) on your computer.

3. **Install the `gh-pages` package as a "dev-dependency" of the app.** (1 minute)

    ```
    $ cd react-gh-pages
    $ npm install gh-pages --save-dev
    ```
    
    * The commands shown in the following steps can all be issued from within the app's folder.

4. **Add some properties to the app's `package.json` file.** (3 minutes)

    * At the top level, add a `homepage` property. Define its value to be the string `http://{username}.github.io/{repo-name}`, where `{username}` is your GitHub username, and `{repo-name}` is the name of the GitHub repository you created in step 1. Since my GitHub username is `gitname` and the name of my GitHub repository is `react-gh-pages`, I added the following property:
    
    ```js
    //...
    "homepage": "http://gitname.github.io/react-gh-pages"
    ```
    
    * In the existing `scripts` property, add a `predeploy` property and a `deploy` property, each having the values shown below:

    ```js
    "scripts": {
      //...
      "predeploy": "npm run build",
      "deploy": "gh-pages -d build"
    }
    ```
    
    * **Shortcut:** Instead of adding those properties using a text editor; if I have `sed` installed on my system, I can add the properties by issuing the following shell commands:
    
    ```sh
    $ sed -i '5i\  "homepage": "http://gitname.github.io/react-gh-pages",' ./package.json
    $ sed -i '15i\    "predeploy": "npm run build",' ./package.json
    $ sed -i '16i\    "deploy": "gh-pages -d build",' ./package.json
    ```
    
5. **Create a git repository in the app's folder.** (1 minute)

    ```
    $ git init
    Initialized empty Git repository in C:/path/to/react-gh-pages/.git/
    ```

6. **Add the GitHub repository as a "remote" in your local git repository.** (1 minute)

    ```
    $ git remote add origin https://github.com/gitname/react-gh-pages.git
    ```
    
    * This will make it so the `gh-pages` package knows where you want it to deploy your app in step 7.
    * It will also make it so git knows where you want it to push your source code (i.e. the commits on your `master` branch) in step 8.

7. **Generate a *production build* of your app, and deploy it to GitHub Pages.** (2 minutes)

    ```
    $ npm run deploy
    ```
    
    * That's it! Your app is now accessible at the URL you specified in step 4.
    * In my case, my app is now accessible at: https://gitname.github.io/react-gh-pages/
    * I recommend exploring the GitHub repository at this point. When I explored it, I noticed that, although a `master` branch did not exist, a `gh-pages` branch did exist. I noticed the latter contained the *built* app code, as opposed to the app's source code.

8. **Optionally, commit your source code to the "master" branch and push your commit to GitHub.** (1 minute)

    ```
    $ git add .
    $ git commit -m "Create a React app and publish it to GitHub Pages"
    $ git push origin master
    ```

    * I recommend exploring the GitHub repository once again at this point. When I did that, I noticed that a `master` branch now existed, and it contained the app's source code.
    * So, the `master` branch held the source code, and the `gh-pages` branch held the *built* app code.

# References

1. [Facebook's tutorial on deploying a React app to GitHub Pages](https://facebook.github.io/create-react-app/docs/deployment#github-pages-https-pagesgithubcom)

# Notes

* I created this React app using [`create-react-app`](https://github.com/facebookincubator/create-react-app). By default, apps created using `create-react-app` have a README.md file that looks like [this](https://github.com/facebookincubator/create-react-app/blob/master/packages/react-scripts/template/README.md). Indeed, the README.md file you're now reading originally looked like that. I have since changed it to look the way it looks today.
* Special thanks to GitHub, Inc., for providing us with the GitHub Pages hosting functionality at no extra charge.
* And now, time to turn the default `create-react-app` app into something unique!
