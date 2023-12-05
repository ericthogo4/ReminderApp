package com.tasks.medicine.study.reminderapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class OnboardingFragment extends Fragment {

    protected int imageResourceLctn;
    protected String titleText;
    protected String descriptionText;

    protected ImageView onbrdngImge;
    protected TextView titleTxtView;
    protected TextView descriptionTextView;
    protected Button getStarted;
    protected ReminderDatabaseAdapter remindersDatabaseAdapter;


    public static OnboardingFragment newInstance(int imageResourceLctn, String titleText, String descriptionText) {
        OnboardingFragment onboardingFragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt("imageResourceLctn", imageResourceLctn);
        args.putString("titleText", titleText);
        args.putString("descriptionText", descriptionText);
        onboardingFragment.setArguments(args);
        return onboardingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageResourceLctn = getArguments().getInt("imageResourceLctn");
        titleText = getArguments().getString("titleText");
        descriptionText = getArguments().getString("descriptionText");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);

        remindersDatabaseAdapter = new ReminderDatabaseAdapter(getActivity());
        remindersDatabaseAdapter.open();

        onbrdngImge = rootView.findViewById(R.id.o_img_vw);
        titleTxtView = rootView.findViewById(R.id.o_ttle_tvw);
        descriptionTextView = rootView.findViewById(R.id.o_descpn_tvw);
        getStarted = rootView.findViewById(R.id.get_started);

        onbrdngImge.setBackgroundResource(imageResourceLctn);
        titleTxtView.setText(titleText);
        descriptionTextView.setText(descriptionText);

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
                ((OnboardingActivity)getActivity()).showRemindersActivity();
            }
        });


        return rootView;
    }

    private void createUser(){
        Runnable cUserRunnable = new Runnable() {
            @Override
            public void run() {
                remindersDatabaseAdapter.createUser(1);
            }
        };
        Thread cUserThread = new Thread(cUserRunnable);
        cUserThread.start();
        try {
            cUserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}