package com.ak.track;

import android.util.Log;

/**
 * Created by cjh on 2018/12/20.
 */
class MyLog {
  static void i(String msg) {
    if (Tracker.getDefault().isDebug()) {
      Log.i("ak", msg);
    }
  }
}
