package com.pushcrew.client;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushcrewClientFactory {
    public static PushcrewClient getClient(String apiKey) throws MalformedURLException {
        return new RESTPushcrewClient(apiKey);
    }

    private static class RESTPushcrewClient implements PushcrewClient {
        final String apiKey;
        final String restEndpoint;
        final URL restEndpointURL;

        Logger logger = LoggerFactory.getLogger("com.pushcrew.client");

        public RESTPushcrewClient(String key, String endpoint) throws MalformedURLException {
            apiKey = key;
            restEndpoint = endpoint;
            restEndpointURL = new URL(restEndpoint);
            logger.debug("Initialized pushcrew client");
        }

        public RESTPushcrewClient(String key) throws MalformedURLException {
            apiKey = key;
            restEndpoint = "https://pushcrew.com/api/v1/";
            restEndpointURL = new URL(restEndpoint);
            logger.debug("Initialized pushcrew client");
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
            logger.debug("Creating post request for path {} with body {}", restEndpoint + path, urlEncodeUTF8(params));
            return authedReq().url(restEndpoint + path).post(body).build();
        }

        public PushcrewResponses.SendResponse sendToAll(String title, String message, String url) throws IOException, PushcrewResponses.PushcrewException {
            Map<String,String> params = new java.util.HashMap<String,String>();
            params.put("title", title);
            params.put("message", message);
            params.put("url", url);
            logger.info("Calling sendToAll at {}, title: {}, message: {}, url: {}", restEndpoint, title, message, url);
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
            logger.info("Calling sendToList at {}, title: {}, message: {}, url: {}, subscriberList: {}", restEndpoint, title, message, url, subscribers);
            return new PushcrewResponses.SendResponse(client.newCall(postRequest("send/list", params)).execute());
        }

        public PushcrewResponses.SendResponse sendToSubscriber(String title, String message, String url, String subscriber) throws IOException, PushcrewResponses.PushcrewException {
            return sendToList(title, message, url, java.util.Arrays.asList(subscriber));
        }

        public PushcrewResponses.NotificationStatus checkStatus(PushcrewResponse response) throws IOException, PushcrewResponses.PushcrewException {
            return checkStatus(response.getRequestId());
        }

        public PushcrewResponses.NotificationStatus checkStatus(long requestId) throws IOException, PushcrewResponses.PushcrewException {
            logger.info("Checking status of request {}", requestId);
            return new PushcrewResponses.NotificationStatus(client.newCall(getRequest("checkstatus/" + requestId)).execute());
        }
    }
}
