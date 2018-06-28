package com.applozic.mobicomkit.uiwidgets;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmActionCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeadCollectionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_collection);

        Button startConversationButton = findViewById(R.id.start_conversation);
        startConversationButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText emailEt = findViewById(R.id.emailIdEt);
        EditText phoneEt = findViewById(R.id.phoneNumberEt);
        EditText nameEt = findViewById(R.id.nameEt);

        String emailId = emailEt.getText().toString();
        String name = nameEt.getText().toString();
        String phoneNumber = phoneEt.getText().toString();

        if (TextUtils.isEmpty(emailId) && TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, getString(R.string.prechat_screen_toast_error_message), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(emailId) && !isValidPhone(phoneNumber)) {
                Toast.makeText(this, getString(R.string.invalid_contact_number), Toast.LENGTH_SHORT).show();
            } else if (!isValidPhone(phoneNumber) && !isValidEmail(emailId)) {
                Toast.makeText(this, getString(R.string.invalid_email_id), Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(emailId) && isValidEmail(emailId)) {
                performLogin(emailId, emailId, name, phoneNumber);
            } else if (TextUtils.isEmpty(emailId) && isValidPhone(phoneNumber)) {
                performLogin(phoneNumber, emailId, name, phoneNumber);
            }
        }
    }

    public void performLogin(String userId, String emailId, String name, String phoneNumber) {
        User user = new User();

        if (TextUtils.isEmpty(userId)) {
            return;
        } else {
            user.setUserId(userId);
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

        if (getApplicationContext() instanceof KmActionCallback) {
            ((KmActionCallback) getApplicationContext()).onReceive(this, user, "prechatLogin");
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
