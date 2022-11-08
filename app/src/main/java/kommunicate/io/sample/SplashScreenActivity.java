package kommunicate.io.sample;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;

import java.util.HashMap;
import java.util.Map;

import io.kommunicate.KmSettings;
import io.kommunicate.Kommunicate;
import io.kommunicate.users.KMUser;
import io.kommunicate.app.R;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Map<String, String> metadata = new HashMap<>();
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("name", "RandomName"); // Pass key value string
        metadata2.put("id", "bot-e8xil"); // Pass key value string

        metadata.put("bot_customization", String.valueOf(metadata2)); // Pass key value string

        if (Kommunicate.isLoggedIn(this)) { // Pass application context
            KmSettings.updateChatContext(this, metadata);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, KMUser.isLoggedIn(SplashScreenActivity.this) ? ConversationActivity.class : MainActivity.class);
                SplashScreenActivity.this.startActivity(intent);
                SplashScreenActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}