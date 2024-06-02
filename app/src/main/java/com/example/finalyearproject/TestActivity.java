package com.example.finalyearproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private String[] questionAnswers = new String[10];
    private String dateOfBirth, gender, ethnicity, familyMemberWithASD;
    private int ageMonths;

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

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 10; i++) {
                    int selectedId = questionGroups[i].getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    if (selectedRadioButton != null) {
                        questionAnswers[i] = selectedRadioButton.getText().toString();
                    } else {
                        questionAnswers[i] = ""; // Default value if no selection
                    }
                }
                sendDataToApi();
            }
        });
    }

    private void sendDataToApi() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    // Construct the URL with query parameters
                    String baseUrl = "http://adeelfarooq417.pythonanywhere.com/";
                    StringBuilder urlBuilder = new StringBuilder(baseUrl);
                    urlBuilder.append("?A1=").append(URLEncoder.encode(safeEncode(questionAnswers[0]), "UTF-8"))
                            .append("&A2=").append(URLEncoder.encode(safeEncode(questionAnswers[1]), "UTF-8"))
                            .append("&A3=").append(URLEncoder.encode(safeEncode(questionAnswers[2]), "UTF-8"))
                            .append("&A4=").append(URLEncoder.encode(safeEncode(questionAnswers[3]), "UTF-8"))
                            .append("&A5=").append(URLEncoder.encode(safeEncode(questionAnswers[4]), "UTF-8"))
                            .append("&A6=").append(URLEncoder.encode(safeEncode(questionAnswers[5]), "UTF-8"))
                            .append("&A7=").append(URLEncoder.encode(safeEncode(questionAnswers[6]), "UTF-8"))
                            .append("&A8=").append(URLEncoder.encode(safeEncode(questionAnswers[7]), "UTF-8"))
                            .append("&A9=").append(URLEncoder.encode(safeEncode(questionAnswers[8]), "UTF-8"))
                            .append("&A10=").append(URLEncoder.encode(safeEncode(questionAnswers[9]), "UTF-8"))
                            .append("&Age_Mons=").append(ageMonths)
                            .append("&Sex=").append(URLEncoder.encode(safeEncode(gender), "UTF-8"))
                            .append("&Ethnicity=").append(URLEncoder.encode(safeEncode(ethnicity), "UTF-8"))
                            .append("&Family_mem_with_ASD=").append(URLEncoder.encode(safeEncode(familyMemberWithASD), "UTF-8"));

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
                        Log.d(TAG, "Response Body: " + responseBuilder.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TestActivity.this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
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

    private String safeEncode(String value) {
        return value == null ? "" : value;
    }
}
