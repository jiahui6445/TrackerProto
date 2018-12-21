package com.ak.track;

import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Created by cjh on 2018/12/21.
 */
public class EncodeUtils {
  public static String encodeBASE64(String input) {
    byte[] bytes = input.getBytes(Charset.forName("UTF-8"));
    byte[] encode = Base64.encode(bytes, Base64.NO_WRAP);
    return new String(encode, Charset.forName("UTF-8"));
  }

  public static String urlEncode(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
}
