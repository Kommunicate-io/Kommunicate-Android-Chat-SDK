<img align="center" src="https://raw.githubusercontent.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/master/img/Header.png" width="900" />

### [Kommunicate](https://www.kommunicate.io/?utm_source=github&utm_medium=readme&utm_campaign=android) Android Chat SDK for Customer Support

An Open Source Android Live Chat SDK for Customer Support

## Overview

Kommunicate provides open source live chat SDK in android. The Kommunicate SDK is flexible, lightweight and easily integrable. It lets you easily add real time live chat and in-app messaging in your mobile applications and websites for customer support. The SDK is equipped with advance messaging options such as sending attachments, sharing location and rich messaging. 

<img align="center" src="https://raw.githubusercontent.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/master/img/Support.gif" height="520" />

Kommunicate SDK lets you integrate custom chatbots in your mobile apps for automating tasks. It comes with multiple features to make it a full fledged customer support SDK. 

<img align="center" src="https://raw.githubusercontent.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/master/img/Bot.gif" height="520" />

To get started with Kommunicate Android SDK, head over to Kommunicate website and [Signup](https://dashboard.kommunicate.io/signup?utm_source=github&utm_medium=readme&utm_campaign=android) to get your Application ID.


### Installation 

Add the following in your app build.gradle dependency:

```compile 'io.kommunicate:kommunicate:1.6.2'```

Add the following permissions in your `AndroidManifest.xml` file:

```
<uses-permission android:name="<your package name>.permission.MAPS_RECEIVE" />
<permission
        android:name="<your package name>..permission.MAPS_RECEIVE"
        android:protectionLevel="signature" />
```

Add your geo-API_KEY in `AndroidManifest.xml` file:
```
       <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="<your-geo-API-KEY>" />
```

After the app has successfully build, open your Application Class(If you do not have an application class, create one) and add imlement the ```KmActionCallback``` interface:

```java
      public class KommunicateApplication extends MultiDexApplication implements KmActionCallback {
```
Then override the ```KmActionCallback```'s ```onReceive``` method :            
```java
 @Override
    public void onReceive(Context context, final Object object, String action) {

        switch (action) {
             //This action will be received on click of the default start new chat button
            case Kommunicate.START_NEW_CHAT:
                Kommunicate.setStartNewChat(context, "vipin+testkm01012018@applozic.com", "Hotel-Booking-Assistant"); //pass null if you want to use default bot
                break;
             //This action will be received on click of logout option in menu
            case Kommunicate.LOGOUT_CALL:
                Kommunicate.performLogout(context, object); //object will receive the exit Activity, the one that will be launched when logout is successfull
                break;
        }
    }
```

The above method will receive the callbacks with an object. You can do your custom operations based on the actions received or use Kommunicate's default actions.

### Authorization

You need to initialise the Kommunicate SDK with your application key obtained from dashboard before accessing any method:

```java
Kommunicate.init(context, <your-app-id>);
```

You can authorize a user as below:
        
```java
        KMUser user = new KMUser();
        user.setUserId("reytum_01");  //unique userId
        user.setApplicationId("22823b4a764f9944ad7913ddb3e43cae1");   //your application key
```
        
 Then call the below method:
    
```java
         Kommunicate.login(this, user, new KMLoginHandler() {
             @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                  //do something in on success
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                  //do something in on failure
            }
        });
      }
 ```
 
 If at some point you need to check if the user is logged in, you can use the below code:
 ```java
 KMUser.isLoggedIn(context){
      //user is logged in  
   }
 ```
 
 You can get the logged in user details as below:
 ```java
 KMUser user = KMUser.getLoggedInUser(context);
 ```
 
 ### Launch chat screen:
 
 You can launch the chat screen(Where all the conversations are listed in descending order of communication time) as below:
    
 ```java
    Kommunicate.openConversation(context);
 ```
    
### Create a new Conversation:
 
 You can create a new conversation as below :
            
 ```java
            List<String> agentIds; //add agentIds to this list
            List<String> botIds; //add botids to this list
            Kommunicate.startNewConversation(context,
                                             groupName, 
                                             agentIds, 
                                             botIds<null accepted>,
                                             false,  //Pass this as false if you would like to start new Conversation
                                             new KMStartChatHandler() {
                    @Override
                    public void onSuccess(Channel channel, Context context) {
                        channel.getKey(); //get your group Id 
                    }

                    @Override
                    public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                    }
                });
  ```

### Open a particular conversation:
  
  You can open a particular conversation if you have the group id of the conversation.
  
  ```java
  Kommunicate.openParticularConversation(context, <Group Id (Integer)>);
  ```
