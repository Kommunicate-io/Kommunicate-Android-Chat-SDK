package io.kommunicate.ui.kommunicate.activities;

import static io.kommunicate.ui.utils.SentryUtils.configureSentryWithKommunicateUI;

import android.app.ProgressDialog;
import android.os.ResultReceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.adapters.KmPrechatInputAdapter;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.models.KmPrechatInputModel;

import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.kommunicate.users.KMUser;
import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmUtils;

public class LeadCollectionActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EMAIL_VALIDATION_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    public static final String PHONE_NUMBER_VALIDATION_REGEX = "^\\d{10}$";
    public static final String NUMBER_VALIDATION_REGEX = "[0-9]+";
    private ResultReceiver prechatReceiver;
    private KmPrechatInputAdapter prechatInputAdapter;
    private List<KmPrechatInputModel> inputModelList;
    private CustomizationSettings customizationSettings;
    private TextView greetingText;
    private String greetingMessage;
    private boolean returnDataMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_km_lead_collection);
        AppContextService.initWithContext(this);

        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }
        configureSentryWithKommunicateUI(this, customizationSettings.toString());
        KmUtils.setStatusBarColor(this, KmThemeHelper.getInstance(this, customizationSettings).getStatusBarColor());
        if (getIntent() != null) {
            prechatReceiver = getIntent().getParcelableExtra(KmConstants.PRECHAT_RESULT_RECEIVER);
            returnDataMap = getIntent().getBooleanExtra(KmConstants.PRECHAT_RETURN_DATA_MAP, false);
            String preChatModelListJson = getIntent().getStringExtra(KmPrechatInputModel.KM_PRECHAT_MODEL_LIST);
            if (!TextUtils.isEmpty(preChatModelListJson)) {
                inputModelList = Arrays.asList((KmPrechatInputModel[]) GsonUtils.getObjectFromJson(preChatModelListJson, KmPrechatInputModel[].class));
                for (KmPrechatInputModel model : inputModelList) {
                    if(!TextUtils.isEmpty(model.getField())) {
                        if (model.getField().equals(getString(R.string.emailEt))) {
                            model.setValidationRegex(EMAIL_VALIDATION_REGEX);
                        } else if (model.getField().equals(getString(R.string.phoneNumberEt))) {
                            model.setValidationRegex(NUMBER_VALIDATION_REGEX);
                        }
                    }
                }
            }
            greetingMessage = getIntent().getStringExtra(KmAppSettingModel.PRE_CHAT_GREETINGS);
            if (TextUtils.isEmpty(greetingMessage)) {
                greetingMessage = getString(R.string.prechat_screen_text);
            }

        }
        greetingText = (TextView) findViewById(R.id.kmPreChatGreetingText);
        setGreetingsText();
        RecyclerView kmPreChatRecyclerView = findViewById(R.id.kmPreChatRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        kmPreChatRecyclerView.setLayoutManager(layoutManager);
        prechatInputAdapter = new KmPrechatInputAdapter((inputModelList != null && !inputModelList.isEmpty()) ? inputModelList : getDefaultModelList(), this);
        kmPreChatRecyclerView.setAdapter(prechatInputAdapter);

        Button startConversationButton = findViewById(R.id.start_conversation);
        startConversationButton.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (prechatReceiver != null) {
            prechatReceiver.send(KmConstants.PRECHAT_RESULT_FAILURE, null);
        }

    }

    private void setGreetingsText() {
        greetingText.setText(greetingMessage);
    }

    @Override
    public void onClick(View v) {
        if (prechatInputAdapter != null && prechatInputAdapter.areFieldsValid()) {
            if(returnDataMap) {
                sendPrechatData(prechatInputAdapter.getDataMap());
                return;
            }
            sendPrechatUser(prechatInputAdapter.getDataMap());

        }
    }

    public List<KmPrechatInputModel> getDefaultModelList() {
        List<KmPrechatInputModel> inputModelList = new ArrayList<>();

        KmPrechatInputModel emailField = new KmPrechatInputModel();
        emailField.setType(KmPrechatInputModel.KmInputType.EMAIL);
        emailField.setRequired(true);
        emailField.setValidationRegex(EMAIL_VALIDATION_REGEX);
        emailField.setField(getString(R.string.emailEt));
        emailField.setCompositeRequiredField(getString(R.string.phoneNumberEt));

        KmPrechatInputModel nameField = new KmPrechatInputModel();
        nameField.setType(KmPrechatInputModel.KmInputType.TEXT);
        nameField.setField(getString(R.string.nameEt));

        KmPrechatInputModel contactField = new KmPrechatInputModel();
        contactField.setType(KmPrechatInputModel.KmInputType.NUMBER);
        contactField.setValidationRegex(PHONE_NUMBER_VALIDATION_REGEX);
        contactField.setField(getString(R.string.phoneNumberEt));

        inputModelList.add(emailField);
        inputModelList.add(nameField);
        inputModelList.add(contactField);

        return inputModelList;
    }

    public void sendPrechatData(Map<String, String> dataMap) {
        if (dataMap != null) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.km_prechat_processing_wait_info));
            dialog.show();

            ResultReceiver finishActivityReceiver = new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    dialog.dismiss();
                    if (resultCode == KmConstants.PRECHAT_RESULT_CODE) {
                        finish();
                    }
                }
            };

            Bundle bundle = new Bundle();
            bundle.putString(KmConstants.KM_USER_DATA, GsonUtils.getJsonFromObject(dataMap, Map.class));
            bundle.putParcelable(KmConstants.FINISH_ACTIVITY_RECEIVER, finishActivityReceiver);
            if (prechatReceiver != null) {
                prechatReceiver.send(KmConstants.PRECHAT_RESULT_CODE, bundle);
            }
        }
    }

    public void sendPrechatUser(Map<String, String> dataMap) {
        if (dataMap != null) {
            String EMAIL_FIELD = getString(R.string.emailEt);
            String CONTACT_NUMBER_FILED = getString(R.string.phoneNumberEt);
            String NAME_FIELD = getString(R.string.nameEt);

            KMUser user = new KMUser();
            String userName = !TextUtils.isEmpty(dataMap.get(EMAIL_FIELD)) ? dataMap.get(EMAIL_FIELD) : dataMap.get(CONTACT_NUMBER_FILED);

            if (!TextUtils.isEmpty(userName)) {
                user.setUserName(userName);
            }

            if (!TextUtils.isEmpty(dataMap.get(EMAIL_FIELD))) {
                user.setEmail(dataMap.get(EMAIL_FIELD));
            }

            if (!TextUtils.isEmpty(dataMap.get(NAME_FIELD))) {
                user.setDisplayName(dataMap.get(NAME_FIELD));
            }

            if (!TextUtils.isEmpty(dataMap.get(CONTACT_NUMBER_FILED))) {
                user.setContactNumber(dataMap.get(CONTACT_NUMBER_FILED));
            }

            if (TextUtils.isEmpty(user.getUserId())) {
                Kommunicate.getVisitor(new KmCallback() {
                    @Override
                    public void onSuccess(Object message) {
                        KMUser visitorUser = (KMUser) message;
                        handleUserProcessing(visitorUser);
                    }

                    @Override
                    public void onFailure(Object error) {
                        handleUserProcessing(new KMUser());
                    }
                });
            } else {
                handleUserProcessing(user);
            }
        }
    }

    private void handleUserProcessing(KMUser user) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.km_prechat_processing_wait_info));
        dialog.show();

        ResultReceiver finishActivityReceiver = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                dialog.dismiss();
                if (resultCode == KmConstants.PRECHAT_RESULT_CODE) {
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
}
