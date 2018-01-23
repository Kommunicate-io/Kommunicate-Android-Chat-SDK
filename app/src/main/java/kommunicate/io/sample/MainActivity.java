package kommunicate.io.sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;

import io.kommunicate.KMUser;
import io.kommunicate.Kommunicate;
import io.kommunicate.app.R;
import io.kommunicate.callbacks.KMCreateChatCallback;
import io.kommunicate.callbacks.KMLoginHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Kommunicate.init(this, "22823b4a764f9944ad7913ddb3e43cae1");
    }

    public void initLoginData(){
        KMUser user = new KMUser();
        user.setUserId("reytum_01");
        user.setDisplayName("Kommunicate User");
        user.setEmail("reytum@live.com");
        user.setApplicationId("22823b4a764f9944ad7913ddb3e43cae1");

        Kommunicate.login(this, user, new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                Kommunicate.openConversation(MainActivity.this);

                Kommunicate.startNewConversation(context, "reytum@live.com", null, new KMCreateChatCallback() {
                    @Override
                    public void onSuccess(Channel channel, Context context) {
                        Kommunicate.openParticularConversation(context, channel.getKey());
                    }

                    @Override
                    public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                    }
                });
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
            }
        });
    }
}
