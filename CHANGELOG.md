# CHANGELOG

The changelog for [Kommunicate-Android-Chat-SDK](https://github.com/Kommunicate-io/Kommunicate-Android-Chat-SDK). Also see the
[releases](https://github.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/releases) on Github.

## Kommunicate Android SDK 2.2.2
1) Notification changes for Agent app
2) Show feedback for Agent app
3) Customization settings: Message Hint text can be changed using: EditTextHintText
4) Some Crash fix

## Kommunicate Android SDK 2.2.1
1) Added Plugin Listeners
2) Added support for adding FAQ name at runtime
3) Fix for initial conversations showing up in Agent app
4) API Endpoints and url change
5) Double callback bug fix and minor fix

## Kommunicate Android SDK 2.1.7
1) Added support for submitting feedback multiple times.
2) Added STT and TTS setting in json file for Hybrid platforms

## Kommunicate Android SDK 2.1.6
1) Moved Kommunicate dependency to JFrog. From this version onwards, add the below repository url to your project level build.grade file inside repositories:
   ```
   maven {
            url 'https://kommunicate.jfrog.io/artifactory/kommunicate-android-sdk'
         }
   ```
2) Added away messages changes in real time
3) Added support for autosuggestion type rich messages
4) Added support to update conversationAssignee and conversationMetadata from KmConversationBuilder
5) Added support to sync messages on conversation screen launch. Push notification is not a mandatory step now.
6) Some crash fixes and optimisations

## Kommunicate Android SDK 2.1.0
1) Security improvement for secret keys in shared preferences. The secret keys are removed from the shared preferences and moved to secured storage.
2) Security improvement for API calls using the user password. Removed user password and other secret keys and replaced them by a JWT token.
3) Fixed issue where the conversation creation and load were taking a lot of time over some networks.

## Kommunicate Android SDK 2.0.5
1) Added date and time picker support in the form template
2) Added support for text field validation in form template
3) Added support for single conversation setting from the dashboard

## Kommunicate Android SDK 2.0.4.1
1) Fixed attachment download issue with AWS storage service setting

## Kommunicate Android SDK 2.0.4
1) Fixed issue where form data was being cleared on submit in Form type rich message
2) Fixed record and send button visibility issue
3) Fixed issue where send button was not visible after caption was removed from image attachment

## Kommunicate Android SDK 2.0.3
1) Added support for language chnage bot
2) MQTT security enhancement: Added support for JWT based authentication in MQTT connection
3) Added support for away status
4) Added support for postBackToBotPlatform in form type rich message
5) Added support for character limit check in message for dialog flow bot
6) Added custom toasts in the SDK. You can use the KmToast.success and KmToast.error methods to display custom Toast in your app.
7) Added support for message blocking via custom regex
8) Fixed issue with message status in MessageInfoFragment
9) Fixed issue where form data was getting cleared after submit

## Kommunicate Android SDK 2.0.1
1) Setting to override message status icon color
2) SDK optimisation: reduced size of SDK by ~1MB
3) Fixed singe conversation opening issue when agentList or botList are passed
4) Fixed message scrolling issue

## Kommunicate Android SDK 2.0.0
1) Option to set custom prechat fields and validations
2) Form type rich messge support with new rich message models
3) Option to set some themes/colors dynamically from the dashboard
4) Speech to text and textToSpeech feature
6) Removed Contact and Phone releated permissions/code
7) Several bug fixes and optimisations

## Kommunicate Android SDK 1.9.6
* Changed the CSAT rating scale to 1,5,10
* Show toast in case of APP_ID change
* Fixed MQTT not working for users registered in app version 112
* Fixed file access issue for apps targetting SDK version 29
* Fixed issue where file upload was not working after the first install

## Kommunicate Android SDK 1.9.5
Removed all contact related permissions and code

## Kommunicate Android SDK 1.9.4

Features:-
1) Support for templateId 11 in Rich messages
2) Settings to add conversation parent activity dynamically. Use the below code to set the parent activity dynamically:
   ```java
    ApplozicSetting.getInstance(getContext()).setParentActivity("<COMPLETE-RESOLVED-PATH-OF-THE-ACTIVITY>");
    //resolved path e.g: kommunicate.io.sample.MainActivity
   ```
3) Optimised login flow and push notification registration. The functions will internally check for already logged in user.
4) Added method to update bot language
5) Added deepLink support in web-links rich messages
6) Added new file provider authority for Kommunicate SDK to avoid conflicts with other file providers.

Fixes:-
1) Fixed FAQ button click on the toolbar
2) Fixed rich message rendering on sender side
3) Optimised rich messages
4) Fixed the clicks for old rich message buttons
5) Fixed document view click on the sender side

## Kommunicate Android SDK 1.9.3

Features:-
1) Added launch and create method in conversation builder. It works as below:
    a) If there are no conversations, create a new conversation and open it. 
    b) If there is only one conversation, open it.
    c) If there are multiple conversations, open the conversation list screen.

    Below is the code to launch the conversation:
    ```
       new KmConversationBuilder(context).setAppId("<Your-App-Id>")
                            .launchAndCreateIfEmpty(new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            
                        }

                        @Override
                        public void onFailure(Object error) {

                        }
                    });
    ```
2)  SDK is now present under kommunicate organization in bintray. The SDK needs to be imported using below path:
    ```
       implementation 'io.kommunicate.sdk:kommunicateui:1.9.3'
    ```

## Kommunicate Android SDK 1.9.1

**Features**
1. Added feedback option in conversation once the conversation is closed.
2. Migrated complete kommunicate SDK to AndroidX.
3. Added chat context update method.

**Fixes**

1. Fix channel name not updating in real time.
2. Fix logout issue.

**Breaking changes** 

We have reversed the gradle name to kommunicateui  from kommunicate.

If your updaing the gradle version to new version i.e to 1.9.1 and above the please use the correct gradle plugin name 

### Before 
The gradle plugin you where using like ``implementation 'io.kommunicate:kommunicate:1.9.*`` the artifact name is changed from **kommunicate**  to **kommunicateui**.  

### Now 
The gradle plugin now you need to use  it as ``implementation 'io.kommunicate:kommunicateui:1.9.*`` the artifact name is changed from **kommunicate**  to **kommunicateui**.

## Kommunicate Android SDK 1.9.0
**Features**
1. KMConversationBuilder to create conversation seamlessly. All the methods or classes with chat text have been deprecated along with KmChatBuilder.
2. Option to set conversation metadata, conversation assignee and skip conversation routing rules when creating a new conversation.
3. Complete RTL support.
4. Created a new custom dialog builder for kommunicate.
5. New method to get details of login users.
6. Method to get conversation metadata by conversation id.

**Fixes**

1. Typing indicator is not unsubscribing when switching the conversation from a notification.
2. Conversation title is not changing when switching the conversation from a notification.
3. Fixed minor UI issues with one-to-one chats and group chats.
4. Fixed issue where prechat screen was dismissing for brief time before launching conversation

## Kommunicate Android SDK 1.8.8
* Option to pass clientConversationId when creating/launching a conversation.
* Updated FCM and GMS libraries to latest versions:
     ```groovy
        api 'com.google.firebase:firebase-messaging:18.0.0'
        api 'com.google.android.gms:play-services-maps:16.1.0'
        api 'com.google.android.gms:play-services-location:16.0.0'
     ```
* The minimum SDK version is increased to 16 due to the above google library updates
* Disabled away messages for normal groups
* Fixed quick reply issue in carousels
* Fixed logout issue for some cases
* Fixed attachment screen opening multiple times incase of multiple clicks
* Fixed Keyboard not dismissing in landscape mode on click of done button in message sender view.

## Kommunicate Android SDK 1.8.7
* New message received callback from the SDK.
* Fixed conversation list layout issue for tablets
* Other bug fixes and optimizations

## Kommunicate Android SDK 1.8.5
* FAQ option on both screens. Enable it using the below setting in `applozic-settings.json` file:
```    
"enableFaqOption": [
    false,
    false
  ]
```

* The default message metadata will be set to the metadata of new conversation created
* Message metadata merge. The replyMetadata will have priority over default metadata
* Deleted groups won't display in the app
* Fixed issue where public group and broadcast group names were not displaying in conversation list
* Other bug fixes and optimisation

## Kommunicate Android SDK 1.8.4

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
