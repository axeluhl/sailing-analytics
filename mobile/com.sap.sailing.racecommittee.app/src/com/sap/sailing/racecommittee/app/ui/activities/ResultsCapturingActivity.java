package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.PhotoAdapter;
import com.sap.sailing.racecommittee.app.utils.MailHelper;

public class ResultsCapturingActivity extends BaseActivity {
    private static int FINISHER_IMAGE_REQUEST_CODE = 1337;

    private static String ARGUMENTS_KEY_SUBJECT = "subject";
    private static String ARGUMENTS_KEY_TEXT = "text";

    private int currentImageIndex;
    private File currentImageFile;
    private PhotoAdapter listAdapter;

    private EditText subjectEditText;
    private EditText bodyEditText;

    public static Intent createIntent(Context context, String mailSubject, String mailBody) {
        Intent intent = new Intent();
        intent.setClass(context, ResultsCapturingActivity.class);
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENTS_KEY_SUBJECT, mailSubject);
        arguments.putString(ARGUMENTS_KEY_TEXT, mailBody);
        intent.putExtras(arguments);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.results_capturing_view);

        currentImageIndex = 0;
        createAndAdvanceImageFile();

        setupListView();

        Button footer = (Button) findViewById(R.id.results_capturing_view_button_add_photo);
        footer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
                startActivityForResult(intent, FINISHER_IMAGE_REQUEST_CODE);
            }
        });

        String subjectValue = "Scores";
        String bodyValue = "No text.";
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            if (arguments.containsKey(ARGUMENTS_KEY_SUBJECT)) {
                subjectValue = arguments.getString(ARGUMENTS_KEY_SUBJECT);
            }
            if (arguments.containsKey(ARGUMENTS_KEY_TEXT)) {
                bodyValue = arguments.getString(ARGUMENTS_KEY_TEXT);
            }
        }

        subjectEditText = (EditText) findViewById(R.id.results_capturing_view_text_subject);
        subjectEditText.setText(subjectValue);

        bodyEditText = (EditText) findViewById(R.id.results_capturing_view_text_body);
        bodyEditText.setText(bodyValue);

        Button sendButton = (Button) findViewById(R.id.results_capturing_view_button_send);
        sendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(ResultsCapturingActivity.this);
                String recipient = preferences.getString("mailRecipientPreference",
                        getString(R.string.settings_advanced_mail_default));
                MailHelper.send(new String[] { recipient }, getSubjectText(), getBodyText(), listAdapter.getItems(),
                        ResultsCapturingActivity.this);
                finish();
            }
        });
    }

    private String getSubjectText() {
        return subjectEditText.getText().toString();
    }

    private String getBodyText() {
        return bodyEditText.getText().toString();
    }

    private void createAndAdvanceImageFile() {
        currentImageFile = createFinisherImageFile(currentImageIndex++);
    }

    @Override
    protected boolean onHomeClicked() {
        fadeActivity(LoginActivity.class);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FINISHER_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                listAdapter.addPhoto(Uri.fromFile(currentImageFile));
                createAndAdvanceImageFile();
            }
        }
    }

    private void setupListView() {
        TextView header = new TextView(this);
        header.setText(getString(R.string.results_capturing_view_list_header));
        header.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10.0f);

        ListView list = (ListView) findViewById(R.id.results_capturing_view_list);
        list.setEmptyView(findViewById(R.id.results_capturing_view_list_empty));
        list.addHeaderView(header);
        listAdapter = new PhotoAdapter(this);
        list.setAdapter(listAdapter);
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.results_capturing_title));
    }

    private static File createFinisherImageFile(int index) {
        File imageDirectory = new File(Environment.getExternalStorageDirectory() + AppConstants.ApplicationFolder);
        imageDirectory.mkdirs();
        return new File(imageDirectory, String.format("image_%d.jpg", index));
    }
}
