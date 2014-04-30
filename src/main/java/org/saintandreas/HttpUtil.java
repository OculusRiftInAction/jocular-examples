package org.saintandreas;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.common.io.ByteStreams;

public class HttpUtil {

  public static String getHttpResponse(String URL) throws IOException {
    String result;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost poster = new HttpPost(URL);
      System.out.println("Executing request: " + poster.getRequestLine());
      try (CloseableHttpResponse response = httpclient.execute(poster)) {
        if (200 != response.getStatusLine().getStatusCode()) {
          throw new IllegalStateException("Got invalid response code " + response.getStatusLine().toString());
        }
        result = new String(ByteStreams.toByteArray(response.getEntity().getContent()));
      }
    }
    return result;
  }

}
