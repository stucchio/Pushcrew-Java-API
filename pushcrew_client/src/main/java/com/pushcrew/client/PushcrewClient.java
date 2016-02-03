package com.pushcrew.client;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

public class PushcrewClient {
    public final String apiKey;
    public final String restEndpoint;
    public final URL restEndpointURL;

    public PushcrewClient(String key, String endpoint) throws MalformedURLException {
        apiKey = key;
        restEndpoint = endpoint;
        restEndpointURL = new URL(restEndpoint);
    }

    public PushcrewClient(String key) throws MalformedURLException {
        apiKey = key;
        restEndpoint = "https://pushcrew.com/api/v1/";
        restEndpointURL = new URL(restEndpoint);
    }

    private final OkHttpClient client = new OkHttpClient();

    private Request.Builder authedReq() {
        return (new Request.Builder()).addHeader("Authorization", apiKey);
    }

    private Request getRequest(String path) {
        return authedReq().url(restEndpoint + path).build();
    }

    private static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static String urlEncodeUTF8(Map<String,String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                                    urlEncodeUTF8(entry.getKey().toString()),
                                    urlEncodeUTF8(entry.getValue().toString())
                                    ));
        }
        return sb.toString();
    }

    private static final MediaType FormEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private Request postRequest(String path, Map<String,String> params) {
        RequestBody body = RequestBody.create(FormEncoded, urlEncodeUTF8(params));
        return authedReq().url(restEndpoint + path).post(body).build();
    }

    public PushcrewResponses.SendResponse sendToAll(String title, String message, String url) throws IOException, PushcrewResponses.PushcrewException {
        Map<String,String> params = new java.util.HashMap<String,String>();
        params.put("title", title);
        params.put("message", message);
        params.put("url", url);
        return new PushcrewResponses.SendResponse(client.newCall(postRequest("send/all", params)).execute());
    }

    public PushcrewResponses.SendResponse sendToList(String title, String message, String url, List<String> subscribers) throws IOException, PushcrewResponses.PushcrewException {
        Map<String,String> params = new java.util.HashMap<String,String>();
        params.put("title", title);
        params.put("message", message);
        params.put("url", url);

        Map<String,Object> listObj = new HashMap<String,Object>();
        listObj.put("subscriber_list", subscribers);
        ObjectMapper mapper = new ObjectMapper();
        String subscriberListJson = mapper.writeValueAsString(listObj);

        params.put("subscriber_list", subscriberListJson);

        return new PushcrewResponses.SendResponse(client.newCall(postRequest("send/list", params)).execute());
    }

    public PushcrewResponses.NotificationStatus checkStatus(PushcrewResponse response) throws IOException, PushcrewResponses.PushcrewException {
        return checkStatus(response.getRequestId());
    }

    public PushcrewResponses.NotificationStatus checkStatus(long requestId) throws IOException, PushcrewResponses.PushcrewException {
        return new PushcrewResponses.NotificationStatus(client.newCall(getRequest("checkstatus/" + requestId)).execute());
    }
}
