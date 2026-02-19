package kommunicate.io.sample;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.ui.conversation.activity.ConversationActivity;
import io.kommunicate.users.KMUser;
import io.kommunicate.app.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new LoadSettingsAsyncTask().execute();
    }

    private class LoadSettingsAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return FileUtils.loadSettingsJsonFile(getApplicationContext());
        }

        @Override
        protected void onPostExecute(String jsonString) {
            Intent intent = new Intent(SplashScreenActivity.this, KMUser.isLoggedIn(SplashScreenActivity.this) ? ConversationActivity.class : MainActivity.class);
            if (jsonString != null) {
                intent.putExtra("customizationSettings", jsonString);
            }
            startActivity(intent);
            finish();
        }
    }
}
