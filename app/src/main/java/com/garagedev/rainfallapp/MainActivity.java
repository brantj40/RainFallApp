package com.garagedev.rainfallapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MAX_SIZE = 12;
    // don't make these private so MonthData.java class can use them also
    // not having any modifier (such as private, protected, public) makes them package-private
    static final int COL_1 = 5;
    static final int COL_2 = 9;
    static final int COL_3 = 13;
    static final int COL_4 = 14;

    private TextView errorText;
    private EditText locationText, timeCodeEditText, rainFallEditText;
    private Button saveButton, runReportButton;
    private LinearLayout dataLayout;

    private int loopIndex = 0;
    private String[] monthTimeHintArray;
    private String[] monthRainFallHintArray;
    private MonthData[] dataArray = new MonthData[MAX_SIZE];

    private double more = 0.0;
    private double less = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load array from string from the res/values/strings.xml project file
        monthTimeHintArray = getResources().getStringArray(R.array.timeCodeHints);
        monthRainFallHintArray = getResources().getStringArray(R.array.rainFallHints);

        // associate UI widgets to their xml named values for the app res/layout project file
        timeCodeEditText = findViewById(R.id.editTextMonthTimeCode);
        rainFallEditText = findViewById(R.id.editTextRailFall);
        saveButton = findViewById(R.id.saveButton);
        runReportButton = findViewById(R.id.runReportButton);
        errorText = findViewById(R.id.errorText);
        dataLayout = findViewById(R.id.data_linearLayout);
        setWidgetsInvisible(); // set the initial display view to hide fields on the UI
        runReportButton.setEnabled(false);

        locationText = findViewById(R.id.editTextLocation);
        locationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //don't care
            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                //don't care
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged: called");
                // not validating the location so as long as they typed something
                // allow further progress by enabling the text input fields
                setWidgetsVisible();
                if (loopIndex == 0) {
                    // start text input hints/prompts at array index 0
                    // setNextHint will also increment the loopIndex by 1
                    setNextHint();
                }
                saveButton.setEnabled(true);
            }
        });

        // set UI listener for button clicks
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // this code will run if user clicks the button
                if (validateRainFall()) {

                    MonthData md = new MonthData(String.valueOf(loopIndex),
                            timeCodeEditText.getText().toString(),
                            Double.parseDouble(rainFallEditText.getText().toString()));
                    // loopIndex was already incremented to sub 1 during this use
                    dataArray[loopIndex - 1] = md;

                    if (loopIndex < MAX_SIZE) {
                        // setNextHint will also increment the loopIndex by 1
                        setNextHint();
                    } else {
                        // have run MAX_SIZE times (12). Change UI to stop any changes
                        // and make the run reports button active
                        setWidgetsInvisible();
                        locationText.setEnabled(false);
                        runReportButton.setEnabled(true);
                    }
                }
            }
        });

        runReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // calculateAverage also sets the more and less rainy value
                double ave = calculateAverage();

                // this type of formatting to make rows and columns is a mess
                // this formatting works for System.out.println, but not well
                // to a TextView. So I am printing out and also populating the UI
                // differently

                String fmt1 = "%1$s %2$s %3$s %4$s";
                String fmt2 = "%1$"+COL_1+"s %2$"+COL_2+"s %3$"+COL_3+"s %4$"+COL_4+"s";

                String location = locationText.getText().toString();
                String header = String.format(Locale.ENGLISH, fmt1,
                        getString(R.string.reportColumn1), getString(R.string.reportColumn2),
                        getString(R.string.reportColumn3), getString(R.string.reportColumn4));

                String divider = String.format(Locale.ENGLISH, fmt2,
                        getString(R.string.reportDiv1), getString(R.string.reportDiv2),
                        getString(R.string.reportDiv3), getString(R.string.reportDiv4));


                System.out.println(getString(R.string.reportTitleText, location));
                System.out.println(header);
                System.out.println(divider);

                // below is a shortcut way to "locally" initialize and use a screen UI widget
                // same as like:
                // TextView myTextView = findViewById(R.id.myWidgetId)
                // myTextView.setText("my text to set")
                ((TextView) findViewById(R.id.reportTitle)).setText(getString(R.string.reportTitleText, location));
                ((TextView) findViewById(R.id.reportHeader1)).setText(R.string.reportColumn1);
                ((TextView) findViewById(R.id.reportHeader2)).setText(R.string.reportColumn2);
                ((TextView) findViewById(R.id.reportHeader3)).setText(R.string.reportColumn3);
                ((TextView) findViewById(R.id.reportHeader4)).setText(R.string.reportColumn4);

                setDivider(R.id.reportDivider1, getString(R.string.reportColumn1));
                setDivider(R.id.reportDivider2, getString(R.string.reportColumn2));
                setDivider(R.id.reportDivider3, getString(R.string.reportColumn3));
                setDivider(R.id.reportDivider4, getString(R.string.reportColumn4));

                for (MonthData md : dataArray) {
                    md.setClassification(determineClassification(md.getRainFall()));
                    createNewRow(md);
                    // output to print
                    System.out.println(md.toString());
                }

                // programmatically add the last textview for displaying average
                TextView avgTv = new TextView(MainActivity.this);
                avgTv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                avgTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                avgTv.setText(getString(R.string.reportAveResultText, ave));
                dataLayout.addView(avgTv);


            }
        });

        View mainView = findViewById(R.id.main_container);
        // listen for touches on the screen and use that to close the screen keyboard
        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSoftKeyboard();
            }
        });
    } // End of OnCreate Method

    private void setWidgetsVisible() {
        timeCodeEditText.setVisibility(View.VISIBLE);
        rainFallEditText.setVisibility(View.VISIBLE);
    }

    private void setWidgetsInvisible() {
        timeCodeEditText.setVisibility(View.INVISIBLE);
        rainFallEditText.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);
    }

    private void setNextHint() {
        Log.d(TAG, "setNextHint: called with index " + loopIndex);
        timeCodeEditText.setText("");
        timeCodeEditText.setHint(monthTimeHintArray[loopIndex]);
        rainFallEditText.setText("");
        // note that I am incrementing loopIndex this time after using it
        rainFallEditText.setHint(monthRainFallHintArray[loopIndex++]);
    }

    private boolean validateRainFall() {
        // get the user's input from the UI screen edit text field widget
        try {
            double value = Double.parseDouble(rainFallEditText.getText().toString());
            if (value <= 0.0 || value > 1000.0) {
                errorText.setText(R.string.errorMsg);
                return false;
            } else {
                // else, the value is good. clear any possible previous error display
                errorText.setText("");
                return true;
            }
        } catch (Exception e){
            Log.d(TAG, "validateRainFall: exception "+e.getMessage());
            errorText.setText(e.getLocalizedMessage());
            return false;
        }
    }

    private double calculateAverage() {
        double sum = 0.0;
        for (MonthData md : dataArray) {
            sum += md.getRainFall();
        }
        double ave = sum / MAX_SIZE;
        more += (ave * .20);
        less -= (ave * .25);
        return ave;
    }

    private String determineClassification(double value) {
        if (value > more) return "Rainy";
        if (value < less) return "Dry";
        return "Average";
    }

    private void setDivider(int id, String word){

        StringBuilder sb = new StringBuilder();
        // add some extra dashes
        int length = word.length() + 5;
        for (int i = 0; i < length ; i++) {
            sb.append("-");
        }
        ((TextView) findViewById(id)).setText(sb.toString());
    }

    private void createNewRow(MonthData md){

        // programmatically create widgets dynamically instead of having fixed in layout xml
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv1 = new TextView(this);
        TextView tv2 = new TextView(this);
        TextView tv3 = new TextView(this);
        TextView tv4 = new TextView(this);

        tv1.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        tv2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        tv3.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        tv4.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        tv1.setText(md.getMonth());
        tv2.setText(md.getTimeCode());
        tv3.setText(String.valueOf(md.getRainFall()));
        tv4.setText(md.getClassification());

        ll.addView(tv1);
        ll.addView(tv2);
        ll.addView(tv3);
        ll.addView(tv4);

        dataLayout.addView(ll);
    }

    private void closeSoftKeyboard() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (NullPointerException npe) {
                Log.d(TAG, "closeSoftKeyboard: " + npe.toString());
            }
        }
    }

}