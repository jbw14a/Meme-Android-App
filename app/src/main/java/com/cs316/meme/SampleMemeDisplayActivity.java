package com.cs316.meme;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SampleMemeDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_meme_display);

        TextView tx = (TextView)findViewById(R.id.myImageViewText);

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/impact.ttf");

        tx.setTypeface(custom_font);
    }
}
