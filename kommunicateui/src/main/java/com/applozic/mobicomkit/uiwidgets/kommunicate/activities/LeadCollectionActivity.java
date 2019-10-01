package com.applozic.mobicomkit.uiwidgets.kommunicate.activities;

import android.app.ProgressDialog;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.regex.Pattern;

import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KmConstants;

public class LeadCollectionActivity extends AppCompatActivity implements View.OnClickListener {
    private ResultReceiver prechatReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_km_lead_collection);

        if (getIntent() != null) {
            prechatReceiver = getIntent().getParcelableExtra(KmConstants.PRECHAT_RESULT_RECEIVER);
        }
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

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.km_prechat_processing_wait_info));
        dialog.show();

        ResultReceiver finishActivityReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == KmConstants.PRECHAT_RESULT_CODE) {
                    dialog.dismiss();
                    finish();
                }
            }
        };

        Bundle bundle = new Bundle();
        bundle.putString(KmConstants.KM_USER_DATA, GsonUtils.getJsonFromObject(user, KMUser.class));
        bundle.putParcelable(KmConstants.FINISH_ACTIVITY_RECEIVER, finishActivityReceiver);
        if (prechatReceiver != null) {
            prechatReceiver.send(KmConstants.PRECHAT_RESULT_CODE, bundle);
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
