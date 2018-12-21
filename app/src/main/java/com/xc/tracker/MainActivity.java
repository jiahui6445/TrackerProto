package com.xc.tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.ak.track.Tracker;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Tracker.getDefault().initialize(MainActivity.this, true);
      }
    });
  }
}
