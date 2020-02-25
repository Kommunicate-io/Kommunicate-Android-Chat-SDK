package kommunicate.io.sample;

import android.content.Intent;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.commons.core.utils.Utils;

import io.kommunicate.KmConversationBuilder;
import io.kommunicate.app.BuildConfig;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KMUser;
import io.kommunicate.app.R;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(SplashScreenActivity.this, KMUser.isLoggedIn(SplashScreenActivity.this) ? ConversationActivity.class : MainActivity.class);
                SplashScreenActivity.this.startActivity(intent);
                SplashScreenActivity.this.finish();*/

                new KmConversationBuilder(SplashScreenActivity.this)
                        .setWithPreChat(true)
                        .setAppId(BuildConfig.APP_ID)
                        .launchConversation(new KmCallback() {
                            @Override
                            public void onSuccess(Object message) {
                                Utils.printLog(SplashScreenActivity.this, "TestSome", "Successfully created conversation : " + message);
                            }

                            @Override
                            public void onFailure(Object error) {
                                Utils.printLog(SplashScreenActivity.this, "TestSome", "Failed to create conversation : " + error);
                            }
                        });
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
