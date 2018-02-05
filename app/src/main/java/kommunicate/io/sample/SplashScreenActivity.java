package kommunicate.io.sample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.kommunicate.users.KMUser;
import io.kommunicate.activities.KMConversationActivity;
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
                Intent intent = new Intent(SplashScreenActivity.this, KMUser.isLoggedIn(SplashScreenActivity.this) ? KMConversationActivity.class : MainActivity.class);
                SplashScreenActivity.this.startActivity(intent);
                SplashScreenActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
