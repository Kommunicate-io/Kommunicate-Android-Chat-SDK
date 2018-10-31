package io.kommunicate.activities;

import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.applozic.mobicommons.json.GsonUtils;

import java.util.regex.Pattern;

import io.kommunicate.users.KMUser;

public class LeadCollectionActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PRECHAT_RESULT_RECEIVER = "kmPrechatReceiver";
    public static final int PRECHAT_RESULT_CODE = 100;
    public static final String KM_USER_DATA = "kmUserData";
    private ResultReceiver prechatReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.applozic.mobicomkit.uiwidgets.R.layout.activity_lead_collection);

        if (getIntent() != null) {
            prechatReceiver = getIntent().getParcelableExtra(PRECHAT_RESULT_RECEIVER);
        }
        Button startConversationButton = findViewById(com.applozic.mobicomkit.uiwidgets.R.id.start_conversation);
        startConversationButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText emailEt = findViewById(com.applozic.mobicomkit.uiwidgets.R.id.emailIdEt);
        EditText phoneEt = findViewById(com.applozic.mobicomkit.uiwidgets.R.id.phoneNumberEt);
        EditText nameEt = findViewById(com.applozic.mobicomkit.uiwidgets.R.id.nameEt);

        String emailId = emailEt.getText().toString();
        String name = nameEt.getText().toString();
        String phoneNumber = phoneEt.getText().toString();

        if (TextUtils.isEmpty(emailId) && TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, getString(com.applozic.mobicomkit.uiwidgets.R.string.prechat_screen_toast_error_message), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(emailId) && !isValidPhone(phoneNumber)) {
                Toast.makeText(this, getString(com.applozic.mobicomkit.uiwidgets.R.string.invalid_contact_number), Toast.LENGTH_SHORT).show();
            } else if (!isValidPhone(phoneNumber) && !isValidEmail(emailId)) {
                Toast.makeText(this, getString(com.applozic.mobicomkit.uiwidgets.R.string.invalid_email_id), Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(emailId) && isValidEmail(emailId)) {
                sendPrechatUser(emailId, emailId, name, phoneNumber);
            } else if (TextUtils.isEmpty(emailId) && isValidPhone(phoneNumber)) {
                sendPrechatUser(phoneNumber, emailId, name, phoneNumber);
            }
        }
    }

    public void sendPrechatUser(String userId, String emailId, String name, String phoneNumber) {
        KMUser user = new KMUser();

        if (TextUtils.isEmpty(userId)) {
            return;
        } else {
            user.setUserName(userId);
        }

        if (!TextUtils.isEmpty(emailId)) {
            user.setEmail(emailId);
        }

        if (!TextUtils.isEmpty(name)) {
            user.setDisplayName(name);
        }

        if (!TextUtils.isEmpty(phoneNumber)) {
            user.setContactNumber(phoneNumber);
        }

        Bundle bundle = new Bundle();
        bundle.putString(KM_USER_DATA, GsonUtils.getJsonFromObject(user, KMUser.class));
        if (prechatReceiver != null) {
            prechatReceiver.send(PRECHAT_RESULT_CODE, bundle);
            finish();
        }
    }

    public boolean isValidEmail(String emailId) {
        if (TextUtils.isEmpty(emailId)) {
            return false;
        }

        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(emailId).matches();
    }

    public boolean isValidPhone(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        if (phoneNumber.length() < 8) {
            return false;
        }
        return true;
    }
}
