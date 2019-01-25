package com.gbmaniac.smartdashboard;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;

/**
 * Class untuk menampilkan dialog pengaturan ulang Tripmeter
 */
public class DialogResetTripmeter extends Dialog {
    private Context context;

    public DialogResetTripmeter(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public DialogResetTripmeter(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_tripmeter);
        Objects.requireNonNull(getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView positiveButton = findViewById(R.id.positive_button);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DockingActivity.start_trip = false;
                //Toast.makeText(context,"Reset Tripmeter",Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        TextView negativeButton = findViewById(R.id.negative_button);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
