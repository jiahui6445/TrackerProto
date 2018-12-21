package com.ak.track;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cjh on 2018/12/20.
 */
public class Tracker {
  private static final int HANDLER_WHAT_TRACKER = 1;

  private static Tracker mInstance;
  private WeakReference<Activity> mActivity;
  private boolean mIsDebug = false;

  private String mUrl;
  private boolean isTracker = false;
  private int mDuration;

  @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      if (msg.what == HANDLER_WHAT_TRACKER) {

      }
    }
  };

  private Tracker() {
  }

  public static Tracker getDefault() {
    if (null == mInstance) {
      synchronized (Tracker.class) {
        if (null == mInstance) {
          mInstance = new Tracker();
        }
      }
    }
    return mInstance;
  }

  public void initialize(Activity activity, boolean isDebug) {
    mActivity = new WeakReference<>(activity);
    mIsDebug = isDebug;

    new Thread(new Runnable() {
      @Override public void run() {
        String result = NetUtil.requestGet(new HashMap<String, String>());
        MyLog.i(result);

        try {
          JSONObject rootObject = new JSONObject(result);
          int status = rootObject.getInt("status");
          if (status == 1) {
            JSONObject dataObject = rootObject.getJSONObject("data");
            mUrl = dataObject.getString("url");
            isTracker = dataObject.getInt("istrack") == 1;
            String range = dataObject.getString("range");
            String[] split = range.split(",");
            int minDuration = Integer.parseInt(split[0]);
            int maxDuration = Integer.parseInt(split[1]);
            Random random = new Random();
            mDuration = random.nextInt(maxDuration - minDuration + 1) + minDuration;

            if (isTracker) {
              byte[] bytes = getRequestData().getBytes(Charset.forName("UTF-8"));
              byte[] encode = Base64.encode(bytes, Base64.NO_WRAP);
              String base64Encode = new String(encode, Charset.forName("UTF-8"));

              String urlEncode = URLEncoder.encode(base64Encode, "UTF-8");
              String reuqest_data = "data_list=" + urlEncode;

              NetUtil.sendCPICPostRequest(reuqest_data);

              mHandler.sendEmptyMessage(HANDLER_WHAT_TRACKER);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  boolean isDebug() {
    return mIsDebug;
  }

  private String getUUId() {
    return String.valueOf((Build.BRAND + Build.MODEL).hashCode()) + getAndroidId();
  }

  private String getAndroidId() {
    if (mActivity.get() != null) {
      return android.provider.Settings.Secure.getString(mActivity.get().getContentResolver(),
          android.provider.Settings.Secure.ANDROID_ID);
    }
    return "";
  }

  private String getRequestData() {
    JSONArray dataJsonArr = new JSONArray();
    JSONObject dataObject = new JSONObject();
    JSONObject propertiesObject = new JSONObject();
    JSONObject libObject = new JSONObject();
    try {
      dataObject.put("time", System.currentTimeMillis());
      dataObject.put("event", "inviteUser");
      dataObject.put("mode", "3");
      dataObject.put("distinct_id", getUUId());

      //properties
      propertiesObject.put("app_version", "1.0");
      propertiesObject.put("lib", "Android");
      propertiesObject.put("model", "MI 4W");
      propertiesObject.put("is_first", "0");
      propertiesObject.put("wifi", true);
      propertiesObject.put("screen_name", "");
      propertiesObject.put("referrer", "");
      propertiesObject.put("imeicode", "862949031244192");
      propertiesObject.put("install_id", "-1500555757429400207");
      propertiesObject.put("carrier", "中国联通");
      propertiesObject.put("lib_version", "0.4.0-SNAPSHOT");
      propertiesObject.put("title", "ak");
      propertiesObject.put("device_id", "602b9ae203599414");
      propertiesObject.put("manufacturer", "OPPO");
      propertiesObject.put("channel", "3238");
      if (this.mDuration > 0) {
        propertiesObject.put("duration", mDuration);
      }

      //lib
      libObject.put("app_version", "9.2");
      libObject.put("0.4.0-SNAPSHOT", "0.4.0-SNAPSHOT");
      libObject.put("lib", "Android");

      dataObject.put("lib", libObject);
      dataObject.put("properties", propertiesObject);

      dataObject.put("type", "track");
      dataObject.put("project_name", "xcar");

      dataJsonArr.put(dataObject);

      return dataJsonArr.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    }
  }
}
