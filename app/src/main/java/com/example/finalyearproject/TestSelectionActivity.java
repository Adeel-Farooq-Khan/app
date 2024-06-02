package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.finalyearproject.QuestionnaireTestActivity;
import com.example.finalyearproject.EEGTestActivity;


import androidx.appcompat.app.AppCompatActivity;


public class TestSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_selection);

        Button buttonQuestionnaireTest = findViewById(R.id.buttonQuestionnaireTest);
//        Button buttonEEGTest = findViewById(R.id.buttonEEGTest);
        Button buttonImageTest = findViewById(R.id.buttonImageTest);

        buttonQuestionnaireTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for questionnaire-based test
                startActivity(new Intent(TestSelectionActivity.this, QuestionnaireTestActivity.class));
            }
        });

//        buttonEEGTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Handle click for EEG test
//                startActivity(new Intent(TestSelectionActivity.this, EEGTestActivity.class));
//            }
//        });
        buttonImageTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for EEG test
                startActivity(new Intent(TestSelectionActivity.this, ImageTestActivity.class));
            }
        });
    }
}
