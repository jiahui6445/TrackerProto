package com.xc.tracker;

import android.app.Application;
import com.facebook.stetho.Stetho;

/**
 * Created by cjh on 2018/12/21.
 */
public class MyApp extends Application {
  @Override public void onCreate() {
    super.onCreate();
    Stetho.initializeWithDefaults(this);
  }
}
