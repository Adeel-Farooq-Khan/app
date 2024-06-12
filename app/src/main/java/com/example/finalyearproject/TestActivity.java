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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private static final String BASE_URL = "http://adeelfarooq417.pythonanywhere.com/";
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds

    private RadioGroup[] questionGroups = new RadioGroup[10];
    private int[] questionAnswers = new int[10];  // Array to store answers as integers
    private String dateOfBirth;
    private int gender, ethnicity, familyMemberWithASD, ageMonths;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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
                        Toast.makeText(TestActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(TestActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeRadioGroups() {
        try {
            questionGroups[0] = findViewById(R.id.options1);
            questionGroups[1] = findViewById(R.id.options2);
            questionGroups[2] = findViewById(R.id.options3);
            questionGroups[3] = findViewById(R.id.options4);
            questionGroups[4] = findViewById(R.id.options5);
            questionGroups[5] = findViewById(R.id.options6);
            questionGroups[6] = findViewById(R.id.options7);
            questionGroups[7] = findViewById(R.id.options8);
            questionGroups[8] = findViewById(R.id.options9);
            questionGroups[9] = findViewById(R.id.options10);

            for (RadioGroup group : questionGroups) {
                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        try {
                            collectAnswers();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in onCheckedChanged: " + e.getMessage(), e);
                            Toast.makeText(TestActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeRadioGroups: " + e.getMessage(), e);
            Toast.makeText(TestActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(TestActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    // Ensure all values are non-null before encoding
                    dateOfBirth = dateOfBirth != null ? dateOfBirth : "";

                    // Construct the URL with query parameters
                    StringBuilder urlBuilder = new StringBuilder(BASE_URL);
                    urlBuilder.append("?A1=").append(questionAnswers[0])
                            .append("&A2=").append(questionAnswers[1])
                            .append("&A3=").append(questionAnswers[2])
                            .append("&A4=").append(questionAnswers[3])
                            .append("&A5=").append(questionAnswers[4])
                            .append("&A6=").append(questionAnswers[5])
                            .append("&A7=").append(questionAnswers[6])
                            .append("&A8=").append(questionAnswers[7])
                            .append("&A9=").append(questionAnswers[8])
                            .append("&A10=").append(questionAnswers[9])
                            .append("&Age_Mons=").append(ageMonths)
                            .append("&Sex=").append(gender)
                            .append("&Ethnicity=").append(ethnicity)
                            .append("&Family_mem_with_ASD=").append(familyMemberWithASD);

                    Log.d(TAG, "Request URL: " + urlBuilder.toString());

                    URL url = new URL(urlBuilder.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(CONNECTION_TIMEOUT);
                    conn.setReadTimeout(CONNECTION_TIMEOUT);

                    int responseCode = conn.getResponseCode();
                    String responseMessage = conn.getResponseMessage();
                    Log.d(TAG, "Response Code: " + responseCode);
                    Log.d(TAG, "Response Message: " + responseMessage);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        handleApiResponse(conn);
                    } else {
                        handleApiError(conn, responseCode, responseMessage);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error in sendDataToApi: " + e.getMessage(), e);
                    showToast("An error occurred: " + e.getMessage());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    private void handleApiResponse(HttpURLConnection conn) throws Exception {
        InputStream inputStream = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();
        final String response = responseBuilder.toString();
        Log.d(TAG, "Response Body: " + response);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayResult(response);
            }
        });
    }

    private void handleApiError(HttpURLConnection conn, int responseCode, String responseMessage) {
        try {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                StringBuilder errorBuilder = new StringBuilder();
                String line;
                while (true) {
                    try {
                        if (!((line = reader.readLine()) != null)) break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    errorBuilder.append(line);
                }
                reader.close();
                Log.e(TAG, "Error Response: " + errorBuilder.toString());
                showToast("Error in data submission: " + responseCode + " " + responseMessage + "\n" + errorBuilder.toString());
            } else {
                Log.e(TAG, "Error Response: " + responseCode + " " + responseMessage);
                showToast("Error in data submission: " + responseCode + " " + responseMessage);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling API error response: " + e.getMessage(), e);
            showToast("An error occurred while handling API error response: " + e.getMessage());
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
            int score = Integer.parseInt(response);
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
        } catch (NumberFormatException e) {
            showToast("Invalid response from server");
        }
    }
}
