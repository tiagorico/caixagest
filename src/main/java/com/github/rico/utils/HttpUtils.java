/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.github.rico.utils.SystemProperties.PROPERTIES;

/**
 * TODO add a description here
 *
 * @author rico
 */
public class HttpUtils {

    public static final String COOKIE = "COOKIE";

    public static final String HTML = "HTML";

    public static Map<String, String> doGet() {
        Map<String, String> values = new HashMap<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(PROPERTIES.getUrl()).openConnection();
            con.setRequestMethod("GET");
            values.put(COOKIE, getCookie(con));
            values.put(HTML, readStream(con.getInputStream()));
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    public static String doPost(String cookieSessionId, Map<String, String> params) {
        HttpURLConnection connection = null;
        String postData = encodeParams(params);

        try {
            //Create connection
            final URL url = new URL(PROPERTIES.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Cookie", cookieSessionId);
            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36"
            );
            connection.setRequestProperty(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
            );
            connection.setRequestProperty("Content-Length", Integer.toString(postData.getBytes().length));

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
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String encodeParams(Map<String, String> params) {
        final StringBuilder builder = new StringBuilder();
        params.keySet().forEach(key -> {
            try {
                builder.append(key).append("=").append(URLEncoder.encode(
                        params.get(key), "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        return builder.toString();
    }

    private static String getCookie(HttpURLConnection con) {
        for (int i = 0; ; i++) {
            String headerName = con.getHeaderFieldKey(i);
            String headerValue = con.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                break;
            }
            if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                String[] fields = headerValue.split(";\\s*");
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
        return content.toString();
    }

}
