package com.xcar.track;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by cjh on 2018/12/20.
 */
class NetUtil {
  static String requestGet(HashMap<String, String> paramsMap) {
    String result = "";
    try {
      String baseUrl = "http://m-api.xcar.com.cn/appinterface/debris/getDebrisInfo";
      StringBuilder tempParams = new StringBuilder();
      int pos = 0;
      for (String key : paramsMap.keySet()) {
        if (pos > 0) {
          tempParams.append("&");
        }
        tempParams.append(
            String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
        pos++;
      }
      String requestUrl = baseUrl + tempParams.toString();
      // 新建一个URL对象
      URL url = new URL(requestUrl);
      // 打开一个HttpURLConnection连接
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      // 设置连接主机超时时间
      urlConn.setConnectTimeout(5 * 1000);
      //设置从主机读取数据超时
      urlConn.setReadTimeout(5 * 1000);
      // 设置是否使用缓存  默认是true
      urlConn.setUseCaches(true);
      // 设置为Post请求
      urlConn.setRequestMethod("GET");
      //urlConn设置请求头信息
      //设置请求中的媒体类型信息。
      urlConn.setRequestProperty("Content-Type", "application/json");
      //设置客户端与服务连接类型
      urlConn.addRequestProperty("Connection", "Keep-Alive");
      // 开始连接
      urlConn.connect();
      // 判断请求是否成功
      if (urlConn.getResponseCode() == 200) {
        // 获取返回的数据
        result = streamToString(urlConn.getInputStream());
      }
      // 关闭连接
      urlConn.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  private static void requestPost(HashMap<String, String> paramsMap) {
    try {
      String baseUrl = "http://dw.xcar.com.cndwapp/dwapp.gif";
      //合成参数
      StringBuilder tempParams = new StringBuilder();
      int pos = 0;
      for (String key : paramsMap.keySet()) {
        if (pos > 0) {
          tempParams.append("&");
        }
        tempParams.append(
            String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
        pos++;
      }
      String params = tempParams.toString();
      // 请求的参数转换为byte数组
      byte[] postData = params.getBytes();
      // 新建一个URL对象
      URL url = new URL(baseUrl);
      // 打开一个HttpURLConnection连接
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      // 设置连接超时时间
      urlConn.setConnectTimeout(5 * 1000);
      //设置从主机读取数据超时
      urlConn.setReadTimeout(5 * 1000);
      // Post请求必须设置允许输出 默认false
      urlConn.setDoOutput(true);
      //设置请求允许输入 默认是true
      urlConn.setDoInput(true);
      // Post请求不能使用缓存
      urlConn.setUseCaches(false);
      // 设置为Post请求
      urlConn.setRequestMethod("POST");
      //设置本次连接是否自动处理重定向
      urlConn.setInstanceFollowRedirects(true);
      // 配置请求Content-Type
      urlConn.setRequestProperty("Content-Type", "application/json");
      // accept配置为text/plain
      urlConn.setRequestProperty("Accept", "text/plain");
      // 开始连接
      urlConn.connect();
      // 发送请求参数
      DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
      dos.write(postData);
      dos.flush();
      dos.close();
      // 判断请求是否成功
      if (urlConn.getResponseCode() == 200) {
        // 获取返回的数据
        String result = streamToString(urlConn.getInputStream());
      }
      // 关闭连接
      urlConn.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static String sendCPICPostRequest(String payLoad) {
    StringBuffer jsonString = new StringBuffer();
    try {
      URL url = new URL("http://dw.xcar.com.cn/dwapp/dwapp.gif");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Accept-Encoding", "gzip");
      connection.setRequestProperty("Connection", "Keep-Alive");
      connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
      connection.setRequestProperty("Host", "dw.xcar.com.cn");

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
      writer.write(payLoad);
      writer.close();
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      jsonString = new StringBuffer();
      String line;
      while ((line = br.readLine()) != null) {
        jsonString.append(line);
      }
      br.close();
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonString.toString();
  }

  /**
   * 将输入流转换成字符串
   *
   * @param is 从网络获取的输入流
   */
  private static String streamToString(InputStream is) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int len = 0;
      while ((len = is.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }
      baos.close();
      is.close();
      byte[] byteArray = baos.toByteArray();
      return new String(byteArray);
    } catch (Exception e) {
      return "";
    }
  }
}
