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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private RadioGroup[] questionGroups = new RadioGroup[10];
    private int[] questionAnswers = new int[10];  // Array to store answers as integers
    private String dateOfBirth, gender, ethnicity, familyMemberWithASD;
    private int ageMonths;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Retrieve data from Intent
        dateOfBirth = getIntent().getStringExtra("date_of_birth");
        gender = getIntent().getStringExtra("gender");
        ethnicity = getIntent().getStringExtra("ethnicity");
        familyMemberWithASD = getIntent().getStringExtra("family_member_with_asd");
        ageMonths = getIntent().getIntExtra("age_months", 0);

        // Initialize RadioGroups
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

        resultTextView = findViewById(R.id.result_text);

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 10; i++) {
                    int selectedId = questionGroups[i].getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    if (selectedRadioButton != null) {
                        String answerText = selectedRadioButton.getText().toString();
                        questionAnswers[i] = mapAnswerToValue(answerText);
                    } else {
                        questionAnswers[i] = 0; // Default value if no selection
                    }
                }
                sendDataToApi();
            }
        });
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
                    gender = gender != null ? gender : "";
                    ethnicity = ethnicity != null ? ethnicity : "";
                    familyMemberWithASD = familyMemberWithASD != null ? familyMemberWithASD : "";

                    // Construct the URL with query parameters
                    String baseUrl = "http://adeelfarooq417.pythonanywhere.com/";
                    StringBuilder urlBuilder = new StringBuilder(baseUrl);
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
                            .append("&Sex=").append(URLEncoder.encode(gender, "UTF-8"))
                            .append("&Ethnicity=").append(URLEncoder.encode(ethnicity, "UTF-8"))
                            .append("&Family_mem_with_ASD=").append(URLEncoder.encode(familyMemberWithASD, "UTF-8"));

                    Log.d(TAG, "Request URL: " + urlBuilder.toString());

                    URL url = new URL(urlBuilder.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(10000); // 10 seconds
                    conn.setReadTimeout(10000); // 10 seconds

                    int responseCode = conn.getResponseCode();
                    String responseMessage = conn.getResponseMessage();
                    Log.d(TAG, "Response Code: " + responseCode);
                    Log.d(TAG, "Response Message: " + responseMessage);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
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
                    } else {
                        InputStream errorStream = conn.getErrorStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder errorBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorBuilder.append(line);
                        }
                        reader.close();
                        Log.e(TAG, "Error Response: " + errorBuilder.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TestActivity.this, "Error in data submission: " + responseCode + " " + responseMessage + "\n" + errorBuilder.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TestActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
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
            Toast.makeText(this, "Invalid response from server", Toast.LENGTH_LONG).show();
        }
    }
}