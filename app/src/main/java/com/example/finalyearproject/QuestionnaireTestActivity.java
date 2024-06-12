package com.example.finalyearproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class QuestionnaireTestActivity extends AppCompatActivity {

    private Button enterDateButton, submitButton;
    private SimpleDateFormat dateFormatter;
    private RadioButton radioButtonYes, radioButtonNo, radioButtonMale, radioButtonFemale;
    private Spinner spinnerEthnicity;
    private TextView selectedDateText;
    private String selectedDate;
    private int gender, ethnicity, autism;

    private HashMap<String, Integer> ethnicityMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire_test);

        enterDateButton = findViewById(R.id.enter_date_button);
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        radioButtonYes = findViewById(R.id.radioButtonYes);
        radioButtonNo = findViewById(R.id.radioButtonNo);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        spinnerEthnicity = findViewById(R.id.spinnerEthnicity);
        selectedDateText = findViewById(R.id.selected_date_text);
        submitButton = findViewById(R.id.submit_button);

        // Define the ethnicity options and map them to integers
        String[] ethnicityOptions = getResources().getStringArray(R.array.ethnicity_options);
        ethnicityMap = new HashMap<>();
        ethnicityMap.put("Middle Eastern", 1);
        ethnicityMap.put("White European", 2);
        ethnicityMap.put("Hispanic", 3);
        ethnicityMap.put("Black", 4);
        ethnicityMap.put("Asian", 5);
        ethnicityMap.put("South Asian", 6);
        ethnicityMap.put("Native Indian", 7);
        ethnicityMap.put("Others", 8);
        ethnicityMap.put("Latino", 9);
        ethnicityMap.put("Mixed", 10);
        ethnicityMap.put("Pacifica", 11);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ethnicityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEthnicity.setAdapter(adapter);

        enterDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCriteriaSelected()) {
                    Intent intent = new Intent(QuestionnaireTestActivity.this, TestActivity.class);
                    intent.putExtra("selectedDate", selectedDate);
                    intent.putExtra("gender", gender);  // Store 1 for Male, 0 for Female
                    intent.putExtra("ethnicity", ethnicity);
                    intent.putExtra("family_member_with_asd", autism);  // Store 1 for Yes, 0 for No
                    try {
                        intent.putExtra("age_months", calculateAgeInMonths(dateFormatter.parse(selectedDate), new Date()));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    startActivity(intent);
                } else {
                    Toast.makeText(QuestionnaireTestActivity.this, "Please select all criteria", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isCriteriaSelected() {
        // Check if a date is selected
        if (selectedDateText.getText().toString().isEmpty()) {
            return false;
        }

        // Check if gender is selected
        if (!radioButtonMale.isChecked() && !radioButtonFemale.isChecked()) {
            return false;
        } else {
            gender = radioButtonMale.isChecked() ? 1 : 0;
        }

        // Check if an option for autism is selected
        if (!radioButtonYes.isChecked() && !radioButtonNo.isChecked()) {
            return false;
        } else {
            autism = radioButtonYes.isChecked() ? 1 : 0;
        }

        // Check if an ethnicity is selected
        if (spinnerEthnicity.getSelectedItemPosition() == 0) {
            return false;
        } else {
            ethnicity = ethnicityMap.get(spinnerEthnicity.getSelectedItem().toString());
        }

        return true;
    }

    private void showDatePickerDialog() {
        final Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, monthOfYear, dayOfMonth);
                Date currentDate = Calendar.getInstance().getTime();

                int childAgeInMonths = calculateAgeInMonths(selectedDate.getTime(), currentDate);
                if (childAgeInMonths < 12 || childAgeInMonths > 72) {
                    Toast.makeText(QuestionnaireTestActivity.this, "Your child's age should be between 12 and 72 months", Toast.LENGTH_SHORT).show();
                } else {
                    displaySelectedDate(selectedDate.getTime());
                }
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void displaySelectedDate(Date selectedDate) {
        this.selectedDate = dateFormatter.format(selectedDate);
        selectedDateText.setText("Selected Date: " + this.selectedDate);
    }

    private int calculateAgeInMonths(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

        return diffMonth;
    }
}
