package com.example.lexo_dotslash;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;


public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.read_text).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_text) {
            Intent intent = new Intent(this, OcrReader.class);
            startActivity(intent);
        }
    }
}


