
### Kommunicate-Android-Chat-SDK-Customer-Support

Kommunicate.io Android Chat SDK for Customer Support

### Installation 

Clone this repo and then from Android Studio select File ->New -> Import Module  -> Select 'kommunicate' from cloned path.
Check in your app level gradle file, if the dependency for kommunicate does'nt exists then add it as below 

```compile 'io.kommunicate:kommunicate:1.2.4'```

After the app has successfully build, open your Application Class(If you do not have an application class, create one) and add imlement the ```KmActionCallback``` interface:

```
      public class KommunicateApplication extends MultiDexApplication implements KmActionCallback {
```
Then override the ```KmActionCallback```'s ```onReceive``` method :

```
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

You can authorize a user as below:
        
```
        KMUser user = new KMUser();
        user.setUserId("reytum_01");  //unique userId
        user.setApplicationId("22823b4a764f9944ad7913ddb3e43cae1");   //your application key
```
        
 Then call the below method:
    
```
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
 ```
 KMUser.isLoggedIn(context){
      //user is logged in  
   }
 ```
 
 You can get the logged in user details as below:
 ```
 KMUser user = KMUser.getLoggedInUser(context);
 ```
 
 ### Launch chat screen:
 
 You can launch the chat screen(Where all the conversations are listed in descending order of communication time) as below:
    
 ```
    Kommunicate.openConversation(context);
 ```
    
### Create a new Conversation:
 
 You can create a new conversation as below :
            
 ```
            Kommunicate.startNewConversation(context, <pass agent id here>, <pass bot id here, null accepted>, new KMCreateChatCallback() {
                    @Override
                    public void onSuccess(Channel channel, Context context) {
                        
                    }

                    @Override
                    public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                    }
                });
  ```

### Open a particular conversation:
  
  You can open a particular conversation if you have the group id of the conversation.
  
  `Kommunicate.openParticularConversation(context, <Group Id (Integer)>);`
