package com.pushcrew.client;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

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
        private static final MediaType JsonEncoded = MediaType.parse("application/json; charset=utf-8");

        private Request postRequest(String path, Map<String,String> params) {
            RequestBody body = RequestBody.create(FormEncoded, urlEncodeUTF8(params));
            logger.debug("Creating post request for path {} with body {}", restEndpoint + path, urlEncodeUTF8(params));
            return authedReq().url(restEndpoint + path).post(body).build();
        }

        private Request postJsonRequest(String path, Object obj) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(obj);
                RequestBody body = RequestBody.create(JsonEncoded, json);
                logger.debug("Creating post request for path {} with body {}", restEndpoint + path, json);
                return authedReq().url(restEndpoint + path).post(body).build();
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("this should never occur");
            }
        }

        private Request deleteRequest(String path, Map<String,String> params) {
            RequestBody body = RequestBody.create(FormEncoded, urlEncodeUTF8(params));
            logger.debug("Creating post request for path {} with body {}", restEndpoint + path, urlEncodeUTF8(params));
            return authedReq().url(restEndpoint + path).delete(body).build();
        }

        public PushcrewResponses.SendResponse sendToAll(String title, String message, String url) throws IOException, PushcrewResponses.PushcrewException {
            Map<String,String> params = new java.util.HashMap<String,String>();
            params.put("title", title);
            params.put("message", message);
            params.put("url", url);
            logger.debug("Calling sendToAll at {}, title: {}, message: {}, url: {}", restEndpoint, title, message, url);
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
            logger.debug("Calling sendToList at {}, title: {}, message: {}, url: {}, subscriberList: {}", restEndpoint, title, message, url, subscribers);
            return new PushcrewResponses.SendResponse(client.newCall(postRequest("send/list", params)).execute());
        }

        public PushcrewResponses.SendResponse sendToSubscriber(String title, String message, String url, String subscriber) throws IOException, PushcrewResponses.PushcrewException {
            return sendToList(title, message, url, java.util.Arrays.asList(subscriber));
        }

        public PushcrewResponses.NotificationStatus checkStatus(PushcrewResponse response) throws IOException, PushcrewResponses.PushcrewException {
            return checkStatus(response.getRequestId());
        }

        public PushcrewResponses.NotificationStatus checkStatus(long requestId) throws IOException, PushcrewResponses.PushcrewException {
            logger.debug("Checking status of request {}", requestId);
            return new PushcrewResponses.NotificationStatus(client.newCall(getRequest("checkstatus/" + requestId)).execute());
        }

        public List<Segment> getSegments() throws IOException, PushcrewResponses.PushcrewException {
            logger.debug("Loading a list of segments for {}", apiKey);
            Response callResult = client.newCall(getRequest("segments")).execute();
            String jsonBody = callResult.body().string();
            callResult.body().close();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonBody);
            String status = rootNode.path("status").asText();
            if (!status.equals("success")) {
                throw new PushcrewResponses.InvalidResponse(status);
            }
            List<Segment> result = new ArrayList<Segment>();
            Iterator<JsonNode> segmentList = rootNode.path("segment_list").elements();
            while (segmentList.hasNext()) {
                JsonNode jsonSegment = segmentList.next();
                result.add(new Segment(jsonSegment.path("id").asLong(), jsonSegment.path("name").asText()));
            }
            return result;
        }

        public PushcrewResponses.CreateSegmentResponse addSegment(String segmentName) throws IOException, PushcrewResponses.PushcrewException {
            logger.debug("Creating a segment {}", segmentName);
            Map<String,String> params = new java.util.HashMap<String,String>();
            params.put("name", segmentName);
            return new PushcrewResponses.CreateSegmentResponse(client.newCall(postRequest("segments", params)).execute());
        }

        public Segment ensureSegmentExists(String segmentName) throws IOException, PushcrewResponses.PushcrewException {
            logger.debug("Ensuring segment {} exists", segmentName);
            try {
                PushcrewResponses.CreateSegmentResponse segmentResponse = addSegment(segmentName);
                return new Segment(segmentResponse.segment_id, segmentName);
            } catch (PushcrewResponses.SegmentAlreadyExists e) {
                List<Segment> segments = getSegments();
                for (Segment segment : segments) {
                    if (segment.name.equals(segmentName)) {
                        return segment;
                    }
                }
            }
            throw new PushcrewResponses.PushcrewException("Unable to create segment " + segmentName + " or to find an existing segment.");
        }

        public void deleteSegment(long segmentId) throws IOException, PushcrewResponses.PushcrewException {
            logger.debug("Deleting segment {}", segmentId);
            Response response = client.newCall(deleteRequest("segments/" + segmentId, new java.util.HashMap<String,String>())).execute();
            String body = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(body);
            String status = rootNode.path("status").asText();
            System.out.println(body);
            if (status.equals("success")) {
                return;
            } else {
                throw new PushcrewResponses.PushcrewException(rootNode.path("message").asText());
            }
        }

        public void addSubscribersToSegment(long segmentId, List<String> subscriberIds) throws IOException, PushcrewResponses.PushcrewException {
            Map<String,String> params = new HashMap<String,String>();
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> data = new HashMap<String,Object>();
            data.put("subscriber_list", subscriberIds);
            params.put("subscriber_list", mapper.writeValueAsString(data));

            Response response = client.newCall(postRequest("segments/" + segmentId + "/subscribers", params)).execute();
            JsonNode responseJson = mapper.readTree(response.body().string());
            if (responseJson.path("status").equals("success")) {
                return;
            } else {
                String message = responseJson.path("message").asText();
                if (message.equals("Invalid Segment ID")) {
                    throw new PushcrewResponses.InvalidSegment("Invalid Segment ID " + segmentId);
                } else if (message.equals("Invalid subscriber IDs present in list.")) {
                    throw new PushcrewResponses.InvalidSubscribers(message);
                } else {
                    throw new PushcrewResponses.PushcrewException(message);
                }
            }
        }
    }
}
