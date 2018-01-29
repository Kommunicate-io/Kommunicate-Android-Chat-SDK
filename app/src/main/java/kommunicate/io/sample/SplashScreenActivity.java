package kommunicate.io.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.kommunicate.KMUser;
import io.kommunicate.Kommunicate;
import io.kommunicate.activities.KMConversationActivity;
import io.kommunicate.app.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent intent = new Intent(this, KMUser.isLoggedIn(this) ? KMConversationActivity.class : MainActivity.class);
        startActivity(intent);
        finish();
    }
}
