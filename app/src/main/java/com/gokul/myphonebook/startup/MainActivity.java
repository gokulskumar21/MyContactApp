package com.gokul.myphonebook.startup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import com.gokul.myphonebook.R;
import com.gokul.myphonebook.activities.AddNewContactActivity;
import com.gokul.myphonebook.activities.DialerActivity;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fabAddNew , fabDialer , fabEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindID();

    }
    private void bindID() {
        fabAddNew = findViewById(R.id.fabAddNew);
        fabDialer = findViewById(R.id.fabDialer);
        fabEdit = findViewById(R.id.fabEdit);
        fabAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNewContactActivity.class);
                startActivity(intent);
            }
        });
        fabDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DialerActivity.class);
                startActivity(intent);
            }
        });

    }
}