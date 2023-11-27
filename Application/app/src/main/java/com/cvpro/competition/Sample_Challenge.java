package com.cvpro.competition;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
public class Sample_Challenge extends AppCompatActivity {
    private int seconds;
    private EditText editText;
    private Button onButton;
    private TextView timerTextView;
    private CountDownTimer countDownTimer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_challenge);

        editText = findViewById(R.id.et_seconds);
        onButton = findViewById(R.id.ON);
        timerTextView = findViewById(R.id.timer);

        onButton.setText("Start"); // Set the initial text for the button

        onButton.setOnClickListener(v -> {
            // Handle the ON button click event
            if (editText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter seconds", Toast.LENGTH_SHORT).show();
            } else {
                // Toggle the button text between "Start" and "Stop"
                if (onButton.getText().equals("Start")) {
                    onButton.setText("Stop");
                    startSendingData();
                } else {
                    stopSendingData();
                }
            }
        });
    }

    private void startSendingData() {
        // Stop the previous countdown timer if it exists
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Start a thread to continuously send data through USB
        UsbService.write("w".getBytes(StandardCharsets.UTF_8));
        Toast.makeText(this, "Starting Bot", Toast.LENGTH_SHORT).show();

        seconds = Integer.parseInt(editText.getText().toString().trim());

        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                // Used for formatting digit to be in 2 digits only
                NumberFormat f = new DecimalFormat("00");
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(f.format(min) + ":" + f.format(sec));
            }

            // When the task is over it will print 00:00 there
            @SuppressLint("SetTextI18n")
            public void onFinish() {
                timerTextView.setText("00:00");
                UsbService.write("o".getBytes(StandardCharsets.UTF_8));
                Toast.makeText(Sample_Challenge.this, "Stop the Bot", Toast.LENGTH_SHORT).show();
                // Change the text of the button to "Start" when the timer is done
                onButton.setText("Start");
                // Clear the EditText
                editText.getText().clear();
            }
        }.start();
    }

    private void stopSendingData() {
        // Stop the countdown timer and reset UI
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerTextView.setText("00:00");
        UsbService.write("o".getBytes(StandardCharsets.UTF_8));
        Toast.makeText(this, "Stop the Bot", Toast.LENGTH_SHORT).show();
        // Clear the EditText
        editText.getText().clear();
        // Change the text of the button to "Start"
        onButton.setText("Start");
    }
}
