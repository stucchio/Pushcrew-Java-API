package com.pushcrew.client;

import okhttp3.*;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class PushcrewResponses {

    public static class PushcrewException extends Exception {
        public PushcrewException(String message) {
            super(message);
        }
    }

    public static class Unauthorized extends PushcrewException {
        public Unauthorized(String message) {
            super(message);
        }
    }

    public static class InvalidResponse extends PushcrewException {
        public InvalidResponse(String message) {
            super(message);
        }
    }

    public static class SegmentAlreadyExists extends PushcrewException {
        public SegmentAlreadyExists(String message) {
            super(message);
        }
    }

    private static class ParsedJsonResponse {
        public final String json;
        public final long requestId;
        public final String status;
        public final JsonNode rootNode;
        ParsedJsonResponse(String j) throws IOException {
            json = j;
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(json);
            status = rootNode.path("status").asText();
            requestId = rootNode.path("request_id").asLong();
        }
    }

    private static void throwCommonExceptions(Response response) throws PushcrewException, IOException {
        if (response.code() == 401) {
            throw new Unauthorized("This request was unauthorized: " + response.body().string());
        }
        if (response.code() != 200) {
            throw new InvalidResponse("Status code was " + response.code() + ". Body: " + response.body().string());
        }
    }

    public static class CreateSegmentResponse {
        public final String status;
        public final Long segment_id;

        public CreateSegmentResponse(Response response) throws IOException, PushcrewException {
            throwCommonExceptions(response);
            String body = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(body);
            status = rootNode.path("status").asText();
            if (status.equals("failure")) {
                String message = rootNode.path("message").asText();
                if (message.equals("A segment with this name already exists.")) {
                    throw new SegmentAlreadyExists(message);
                } else {
                    throw new PushcrewException(message);
                }
            }
            segment_id = rootNode.path("segment_id").asLong();
        }

        public String toString() {
            return "CreateSegmentResponse(" + status + ", " + segment_id + ")";
        }
    }

    public static class SendResponse implements PushcrewResponse {
        public final long requestId;
        public final String status;

        public SendResponse(long rid, String st) {
            requestId = rid;
            status = st;
        }

        public SendResponse(Response response) throws IOException, PushcrewException {
            throwCommonExceptions(response);
            String body = response.body().string();
            ParsedJsonResponse parsed = new ParsedJsonResponse(body);
            requestId = parsed.requestId;
            status = parsed.status;
        }

        public String toString() {
            return "SendResponse(" + getRequestId() + ", " + getStatus() + ")";
        }

        public long getRequestId() { return requestId; }
        public String getStatus() { return status; }
    }


    public static class NotificationStatus  {
        public final long countDelivered;
        public final long countClicked;

        public NotificationStatus(Response response) throws IOException, PushcrewException {
            throwCommonExceptions(response);
            String body = response.body().string();

            JsonNode rootNode = (new ObjectMapper()).readTree(body);

            countDelivered = rootNode.path("count_delivered").asLong();
            countClicked = rootNode.path("count_clicked").asLong();
        }

        public String toString() {
            return "NotificationStatus(delivered=" + countDelivered + ", clicked=" + countClicked + ")";
        }
    }
}
