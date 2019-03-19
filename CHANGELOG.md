# CHANGELOG

The changelog for [Kommunicate-Android-Chat-SDK](https://github.com/Kommunicate-io/Kommunicate-Android-Chat-SDK). Also see the
[releases](https://github.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/releases) on Github.

## Kommunicate Android SDK 1.8.3

### Bug fixes and improvement

* Update glide to version 4.9.0.
* Fixed issue for group name was not showing  in chat list for  public,private and broadcast groups.

## Kommunicate Android SDK 1.8.3

### Features

* FAQ option added in the toolbar. Enable it by setting the below property true in applozic-settings.json file:
"isFaqOptionEnabled": true
* Login as visitor option in sample app
* Option to set APP_ID in build.gradle file for the sample app. In App level build.gradle file add the below property inside defaultConfig:
buildConfigField "String", "APP_ID", '"<Your-APP_ID>"'
* Added APP_ID missing error dialog incase user forgets to set the APP_ID in build.gradle file in the sample app.

### Bug fixes and optimisation

* Fixed UI issues for pre lollipop devices
* Fixed crash for rendering broadcast type groups(For agent app)
* Fixed duplicate message issue when creating a new conversation
* Fixed 'email/html type message not getting displayed to the sender'
* Fixed 'carousels displaying even incase of invalid JSON'

## Kommunicate Android SDK 1.8.2

### New Features

* Setting to change the corner radius of message bubbles.
Add the below properties in applozic-settings.json file:
```
"sentMessageCornerRadii": [
10,
10,
10,
10
],
"receivedMessageCornerRadii": [
10,
10,
10,
10
]
```
The order is topLeft, topRight, bottomRight, bottomLeft

* Setting to change the fonts for some TextViews. You can set some external fonts using ttf files or select one from the default android fonts.
Below are the TextViews which support changing the fonts. Add the below property in applozic-settings.json file:
```
"fontModel": {
    "messageTextFont": "",
    "messageDisplayNameFont": "",
    "createdAtTimeFont": "",
    "toolbarTitleFont": "",
    "toolbarSubtitleFont": "",
    "messageEditTextFont": ""
  }
 ```
* Support for Rich message carousels has been added(Template ID - 10).

### Bug Fixes

* Fixed crash when updating to version 1.8.1 and audio option is missing in applozic-settings.json file
* Fixed crash on pre lollipop devices
* Fixed the conversation name not being displayed if CONVERSATION_ASIGNEE is missing in the conversation(In case of older conversations)
* Fixed issue related to external audio is being stopped when user navigates back from the chat thread.

## Kommunicate Android SDK 1.8.0

###  Features

* Audio recording functionality added.
* Full HTML support for ContentType 3 messages.
* Image type rich messages added.

### Bug fixes

* Fixed issue related to unread count not resetting after opening the conversation.
* Fixed NPE in createSingleChat and ContactActivity for users that are not logged in
* Fixed few other crashes.


## Kommunicate Android SDK 1.7.2

### New Features

* In app web link support
* Custom click listener for rich message action clicks
* Conversation assignee name and image in conversation list
* Reply metadata feature in Rich messages

### Bug Fixes

* Html formatting issue in rich messages
* Rich message Custom Click fixed
* Group title was being displayed for a brief time after opening a conversation - fixed


## Kommunicate Android SDK 1.7.1

### Bug Fixes
* Fixed issue related to creating new conversation
* Fixed issue related to online/offline status not visible to users in some cases


### New Features

* Online/Offline status for agents
* Current conversation Assignee image will be displayed on toolbar
* Typing indicator for agents/bots
* Chat builder for building conversation
* Now you can set attachment type in the settings. Only those attachment types would be supported for the user.
* Full HTML/CSS email type support in the chat
* Now you can pass agentList as null/empty when starting a conversation - In this case the conversation would be created using the default agent
* New Staggered layout for quick replies/web links

### Fixes/Improvements

* Html message not displaying in notification - Fixed
* Fixed crash related to invalid payload being sent for Rich Lists
* Fixed static map not loading issue
* The Web links/Quick replies click was not being received outside the SDK - Fixed


## Kommunicate Android SDK 1.6.3

### Fixes/Improvements

* Method to launch chat directly without calling login -> createChat -> launchChat
* Fixed image loading issues in conversation screen
* Fixed back button issue in Message info screen
* Fixed issue in which some attachments were not getting loaded from storage


## Kommunicate Android SDK Version 1.6.1

### Features

* Templates for FAQ now available
* Improvements in list templates, addition of header and item images
* Push notification fix for Oreo devices
* Custom sound can be set for notifications


## Kommunicate Android SDK 1.5

### Features

* Added setting to hide Attachment options
* Added setting to hide conversation subtitle
* Added visitor login option
* Added Message status icon and theme customisation capability

### Bug Fixes

* Away message crash fixed
* Localisation issues fixed
* Sent messages not visible without adding settings file fixed
