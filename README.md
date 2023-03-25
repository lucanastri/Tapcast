# Tapcast
Android Application developed with Kotlin

Tapcast is a demo application for listening to online podcast on your phone!
This application works with Firebase Realtime Database, Firebase Cloud Firestore and Firebase Authentication for providing data and to keep a collection of podcasts.
Its main functions are: One Tap Google Sign-In, Profile Creation and Synchronization between multiple devices, Fetching podcast data through Internet (also with offline capabilities), Explore Tapcast Podcasts Collection and Media Playback through the newest API androidx.media3.
Tapcast follows clean architecture principles and uses kotlin coroutines and flows to handle the ui state, it also follows one activity architecture (almost) with navigation component.
It consists of 7 different pages: Login, Registration, Dashboard, Search, Podcast, Episodes, Settings

#### Login
Allows the user to login into the Tapcast Application with an easy One Tap Sign-In provided by Google.

#### Registration
The page where the user register itself with the Firebase Authentication API.
The user can specify an username and set a profile picture that will be stored in the Firebase Storage.

#### Dashboard
The main page of the app with a Nested RecyclerView that shows the entire collection of podcasts available in the app.
The user can click on every podcast to visit the detail page or input a text on the searchview to look for a specific podcast.

#### Search
The search page allows the user to perform a search by name of a podcast.
From this page you can also access to the podcast's detail page

#### Podcast
This is the podcast detail page showing everything about the podcast selected from the user.
You can go to the episodes' page or play/pause the podcast audio.
Accessing this page will show a notification for the media playback with the main controls

#### Episodes
Shows the list of episodes the podcast has ordered by date.
In this page the mediacontroller is linked through the RecyclerView to provide a correct play/pause behaviour

#### Settings
Finally, the settings page is where you can find all the info about the app and some important settings (not every setting is really implemented but it serves as a placeholder).

# Design

### Light and Dark Theme
![TapcastDesign](https://user-images.githubusercontent.com/32841796/226195772-0ca824f4-d026-4b0b-a294-fdf5288909b2.png)

<sub>*This application must not be considered a final product but a way to showcase a small project functionalities</sub>
