package com.tasks.medicine.study.reminderapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class LaunchScreenActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

}