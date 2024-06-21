package com.example.finalyearproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private static final String BASE_URL = "http://adeelfarooq417.pythonanywhere.com/";

    private RadioGroup[] questionGroups = new RadioGroup[10];
    private int[] questionAnswers = new int[10];  // Array to store answers as integers
    private String dateOfBirth;
    private int gender, ethnicity, familyMemberWithASD, ageMonths;
    private TextView resultTextView;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Initialize OkHttpClient
        client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        try {
            // Retrieve data from Intent
            dateOfBirth = getIntent().getStringExtra("selectedDate");
            gender = getIntent().getIntExtra("gender", -1);
            ethnicity = getIntent().getIntExtra("ethnicity", -1);
            familyMemberWithASD = getIntent().getIntExtra("family_member_with_asd", -1);
            ageMonths = getIntent().getIntExtra("age_months", 0);

            // Initialize RadioGroups
            initializeRadioGroups();

            resultTextView = findViewById(R.id.result_text);

            Button submitButton = findViewById(R.id.submit_button);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        collectAnswers();
                        sendDataToApi();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onClick: " + e.getMessage(), e);
                        showToast("An error occurred: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private void initializeRadioGroups() {
        try {
            for (int i = 0; i < questionGroups.length; i++) {
                int resID = getResources().getIdentifier("options" + (i + 1), "id", getPackageName());
                questionGroups[i] = findViewById(resID);
                questionGroups[i].setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        try {
                            collectAnswers();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in onCheckedChanged: " + e.getMessage(), e);
                            showToast("An error occurred: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeRadioGroups: " + e.getMessage(), e);
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private void collectAnswers() {
        try {
            for (int i = 0; i < questionGroups.length; i++) {
                int selectedId = questionGroups[i].getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedId);
                if (selectedRadioButton != null) {
                    questionAnswers[i] = mapAnswerToValue(selectedRadioButton.getText().toString());
                } else {
                    questionAnswers[i] = 0; // Default value if no answer is selected
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in collectAnswers: " + e.getMessage(), e);
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private int mapAnswerToValue(String answerText) {
        switch (answerText) {
            case "None":
                return 0;
            case "Mild":
                return 1;
            case "Moderate":
                return 2;
            case "Severe":
                return 3;
            default:
                return 0;
        }
    }

    private void sendDataToApi() {
        try {
            // Ensure all values are non-null before encoding
            dateOfBirth = dateOfBirth != null ? dateOfBirth : "";

            // Construct the URL with query parameters
            StringBuilder urlBuilder = new StringBuilder(BASE_URL);
            for (int i = 0; i < questionAnswers.length; i++) {
                urlBuilder.append("&A").append(i + 1).append("=").append(questionAnswers[i]);
            }
            urlBuilder.append("&Age_Mons=").append(ageMonths)
                    .append("&Sex=").append(gender)
                    .append("&Ethnicity=").append(ethnicity)
                    .append("&Family_mem_with_ASD=").append(familyMemberWithASD);

            String url = urlBuilder.toString().replaceFirst("&", "?");

            Log.d(TAG, "Request URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error in sendDataToApi: " + e.getMessage(), e);
                    showToast("An error occurred: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        Log.d(TAG, "Response Body: " + responseBody);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayResult(responseBody);
                            }
                        });
                    } else {
                        final int responseCode = response.code();
                        final String responseMessage = response.message();
                        Log.e(TAG, "Error in sendDataToApi: " + responseCode + " " + responseMessage);
                        showToast("Error in data submission: " + responseCode + " " + responseMessage);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in sendDataToApi: " + e.getMessage(), e);
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayResult(String response) {
        try {
            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(response);
            JSONArray predictionArray = jsonObject.getJSONArray("prediction");
            double score = predictionArray.getDouble(0);  // Extract the first value from the prediction array

            String result;
            if (score >= 0 && score <= 6) {
                result = "The child is non-autistic.";
            } else if (score >= 7 && score <= 14) {
                result = "The child is mildly autistic.";
            } else if (score >= 15 && score <= 24) {
                result = "The child is moderately autistic.";
            } else {
                result = "The child is severely autistic.";
            }
            resultTextView.setText(result);
        } catch (JSONException e) {
            showToast("Invalid response from server: " + e.getMessage());
        }
    }
}
