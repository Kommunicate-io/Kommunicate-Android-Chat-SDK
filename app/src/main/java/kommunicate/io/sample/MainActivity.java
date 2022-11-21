package kommunicate.io.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.zendesk.logger.Logger;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
import com.zopim.android.sdk.api.ZopimChatApi;

import java.util.HashMap;
import java.util.Map;

import io.kommunicate.KmConversationHelper;
import io.kommunicate.KmException;
import io.kommunicate.app.BuildConfig;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KMUser;
import io.kommunicate.Kommunicate;
import io.kommunicate.app.R;
import io.kommunicate.callbacks.KMLoginHandler;

import com.zopim.android.sdk.model.VisitorInfo;


public class MainActivity extends AppCompatActivity {

    EditText mUserId, mPassword;
    AppCompatButton loginButton, visitorButton;
    LinearLayout layout;
    boolean exit = false;
    public static final String APP_ID = BuildConfig.APP_ID;
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID";
    public void callbackzendesk() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Chat.INSTANCE.init(MainActivity.this,"izhjCMjBEdx8uqV4OSaP24fr9b5zrnAX", "io.kommunicate.app");
        ZopimChatApi.init("izhjCMjBEdx8uqV4OSaP24fr9b5zrnAX");

        Logger.setLoggable(true);
        //ProfileProvider profileProvider = Chat.INSTANCE.providers().profileProvider();
        //ChatProvider chatProvider = Chat.INSTANCE.providers().chatProvider();


//        profileProvider.setVisitorInfo(visitorInfo, new ZendeskCallback<Void>() {
//            @Override
//            public void onSuccess(Void unused) {
//                Log.e("zendesklogin", "success");
//
//            }
//
//            @Override
//            public void onError(ErrorResponse errorResponse) {
//                Log.e("zendesklogin", "fail");
//
//            }
//        });
        //profileProvider.


        layout = (LinearLayout) findViewById(R.id.footerSnack);
        mUserId = (EditText) findViewById(R.id.userId_editText);
        mPassword = (EditText) findViewById(R.id.password_editText);
        loginButton = (AppCompatButton) findViewById(R.id.btn_signup);
        visitorButton = findViewById(R.id.btn_login_as_visitor);

        TextView txtViewPrivacyPolicy = (TextView) findViewById(R.id.txtPrivacyPolicy);
        txtViewPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VisitorInfo visitorInfo = new VisitorInfo.Builder().
                        name("sathyan").
                        phoneNumber("6383362545")
                        .email("sathya@gmail.com")
                        .build();
//                com.zopim.android.sdk.model.VisitorInfo visitorInfo =                 com.zopim.android.sdk.model.VisitorInfo
//                        withName("sathyan").
//                        withPhoneNumber("6383362545")
//                        .withEmail("sathya@gmail.com")
//                        .build();
                ZopimChatApi.setVisitorInfo(visitorInfo);

//
////                ChatProvidersConfiguration chatProvidersConfiguration = ChatProvidersConfiguration.builder()
////                        .withVisitorInfo(visitorInfo)
////                        .build();
////
////                Chat.INSTANCE.setChatProvidersConfiguration(chatProvidersConfiguration);
//                //Chat.INSTANCE.providers().profileProvider().
//                Chat.INSTANCE.providers().profileProvider().setVisitorInfo(visitorInfo, new ZendeskCallback<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Log.e("zendesklogin", "success");
//                    }
//
//                    @Override
//                    public void onError(ErrorResponse errorResponse) {
//                        Log.e("zendesklogin", "success");
//
//                    }
//                });
                ZopimChatApi.start(MainActivity.this).send("Your message");

//                VisitorInfo visitor = Chat.INSTANCE.providers().profileProvider().getVisitorInfo();
//                Log.e("zendeskvisitor", visitor.getEmail());
//                if(Chat.INSTANCE.providers().chatProvider() == null) {
//                    Log.e("zendesknull", String.valueOf(true));
//                }
//                ChatLog.Message message = Chat.INSTANCE.providers().chatProvider().sendMessage("testmessage2");
//                ChatLog.Message message1 = Chat.INSTANCE.providers().chatProvider().resendFailedMessage(message.getId());
//                Log.e("zendesknull", String.valueOf(message.getDeliveryStatus()));

                //Log.e("zendeskmessage", String.valueOf(message.getDeliveryStatus()));
//                MessagingActivity.builder()
//                        .withEngines(ChatEngine.engine())
//                        .show(MainActivity.this);
            }
        });

        visitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public boolean isPlaceHolderAppId() {
        if (Kommunicate.PLACEHOLDER_APP_ID.equals(APP_ID)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setMessage(Utils.getString(this, R.string.invalid_app_id_error));
            dialogBuilder.show();
            return true;
        }
        return false;
    }

    public String getInvalidAppIdError(RegistrationResponse registrationResponse) {
        if (registrationResponse != null) {
            if (registrationResponse.getMessage() != null && INVALID_APP_ID.equals(registrationResponse.getMessage())) {
                return getString(R.string.invalid_app_id_error);
            } else {
                return registrationResponse.getMessage();
            }
        }
        return "";
    }

    public void createLoginErrorDialog(RegistrationResponse registrationResponse, Exception exception) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(true);
        StringBuilder message = new StringBuilder(getString(R.string.some_error_occured));
        if (registrationResponse != null) {
            if (!TextUtils.isEmpty(getInvalidAppIdError(registrationResponse))) {
                message.append(" : ");
                message.append(getInvalidAppIdError(registrationResponse));
            }
        } else if (exception != null) {
            message.append(" : ");
            message.append(exception.getMessage());
        }

        dialogBuilder.setMessage(message.toString());
        dialogBuilder.show();
    }

    public void showSnackBar(int resId) {
        Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onBackPressed() {

        if (exit) {
            finish();
        } else {
            KmToast.success(this, R.string.press_back_exit, Toast.LENGTH_SHORT).show();
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }

    }

    public void initLoginData(String userId, String password, final ProgressDialog progressDialog) {

        final KMUser user = new KMUser();
        user.setUserId(userId);
        user.setApplicationId(APP_ID);

        if (!TextUtils.isEmpty(password)) {
            user.setPassword(password);
        }

        Kommunicate.login(MainActivity.this, user, new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                if (KMUser.RoleType.USER_ROLE.getValue().equals(registrationResponse.getRoleType())) {
                    ApplozicClient.getInstance(context).hideActionMessages(true).setMessageMetaData(null);
                } else {
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("skipBot", "true");
                    ApplozicClient.getInstance(context).hideActionMessages(false).setMessageMetaData(metadata);
                }

                try {
                    KmConversationHelper.openConversation(context, true, null, new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            finish();
                        }

                        @Override
                        public void onFailure(Object error) {

                        }
                    });
                } catch (KmException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                createLoginErrorDialog(registrationResponse, exception);
            }
        });
    }
}
