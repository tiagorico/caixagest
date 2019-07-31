/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.github.rico.utils.SystemProperties.PROPERTIES;

/**
 * Helper class to deal with HTTP requests.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static final String COOKIE = "COOKIE";

    public static final String HTML = "HTML";

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String USER_AGENT = "User-Agent";

    private static final String ACCEPT = "Accept";

    private static final String BROWSER = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36";

    private static final String ACCEPT_ALL = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";

    private static final String UTF_8 = "UTF-8";

    private static final String SET_COOKIE = "Set-Cookie";

    public static final String NO_DATA = "NO_DATA";

    /**
     * Performs a GET method against specified URL
     *
     * @return
     */
    public static Map<String, String> doGet() {
        LOGGER.trace("Do GET to {}", PROPERTIES.getUrl());
        final Map<String, String> values = new HashMap<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(PROPERTIES.getUrl()).openConnection();
            con.setRequestMethod(GET);
            values.put(COOKIE, getCookie(con));
            values.put(HTML, readStream(con.getInputStream()));
            con.disconnect();
        } catch (IOException e) {
            LOGGER.error("Error while reading from http connection", e);
        }
        return values;
    }

    public static String doPost(String cookieSessionId, Map<String, String> params) {
        LOGGER.trace("Do POST to {} with {}.", PROPERTIES.getUrl(), cookieSessionId);
        HttpURLConnection connection = null;
        try {
            final String postData = encodeParams(params);
            //Create connection
            final URL url = new URL(PROPERTIES.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(POST);

            connection.setRequestProperty(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
            connection.setRequestProperty(COOKIE, cookieSessionId);
            connection.setRequestProperty(USER_AGENT, BROWSER);
            connection.setRequestProperty(ACCEPT, ACCEPT_ALL);
            connection.setRequestProperty(CONTENT_LENGTH, Integer.toString(postData.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            try (final DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData.getBytes());
                wr.flush();
            }

            //Get Response
            return readStream(connection.getInputStream());
        } catch (Exception e) {
            LOGGER.error("Error while posting to caixagest.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return NO_DATA;
    }

    private static String encodeParams(Map<String, String> params) {
        final StringBuilder builder = new StringBuilder();
        params.keySet().forEach(key -> {
            LOGGER.trace("Adding param {}={}", key, params.get(key));
            try {
                builder.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), UTF_8))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Error while encoding params.", e);
            }
        });
        return builder.toString();
    }

    private static String getCookie(HttpURLConnection con) {
        for (int i = 0; ; i++) {
            final String headerName = con.getHeaderFieldKey(i);
            final String headerValue = con.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                break;
            }
            if (SET_COOKIE.equalsIgnoreCase(headerName)) {
                final String[] fields = headerValue.split(";\\s*");
                return fields[0];
            }
        }
        return null;
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
        }
        LOGGER.trace("Received {}",content);
        return content.toString();
    }

}
