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

public interface PushcrewClient {

    public PushcrewResponses.SendResponse sendToAll(String title, String message, String url) throws IOException, PushcrewResponses.PushcrewException;

    public PushcrewResponses.SendResponse sendToList(String title, String message, String url, List<String> subscribers) throws IOException, PushcrewResponses.PushcrewException;

    public PushcrewResponses.SendResponse sendToSubscriber(String title, String message, String url, String subscriber) throws IOException, PushcrewResponses.PushcrewException;

    public PushcrewResponses.NotificationStatus checkStatus(PushcrewResponse response) throws IOException, PushcrewResponses.PushcrewException;

    public PushcrewResponses.NotificationStatus checkStatus(long requestId) throws IOException, PushcrewResponses.PushcrewException;
}
