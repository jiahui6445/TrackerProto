package com.ak.track;

import android.util.Log;

/**
 * Created by cjh on 2018/12/20.
 */
public class MyLog {
  public static void i(String msg) {
    if (Tracker.getDefault().isDebug()) {
      Log.i("ak", msg);
    }
  }
}
