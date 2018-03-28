/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.airhacks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.time.Duration.ofDays;
import static java.time.Instant.now;

/**
 * TODO add a description here
 *
 * @author rico
 */
public class Teste {

    private static final String URL = "http://www.caixagest.pt/simulador_cotacoes.aspx";

    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR";
    private static final String FUNDS_DROPDOWN = "FundosDropDownList";
    private static final String DATE = "Data";
    private static final String X = "simuladorCotacoesBtn.x";
    private static final String Y = "simuladorCotacoesBtn.y";

    private static final int MAX_X = 113;
    private static final int MAX_Y = 30;

    private static final String DATE_PATTERN = "dd-MM-yyyy";

    private static final Map<String, String> FUNDS = new HashMap<>(10);
    private static final Map<String, String> PARAMS = new HashMap<>();

    private static String COOKIE_SESSION_ID;

    public static void main(String[] args) {
        init();
    }

    private static void init() {
        doGet();

        FUNDS.keySet().forEach(key -> {

            PARAMS.put(FUNDS_DROPDOWN, FUNDS.get(key));
            PARAMS.put(DATE, new SimpleDateFormat(DATE_PATTERN).format(new Date(now().minus(ofDays(1)).toEpochMilli())));
            PARAMS.put(X, Integer.toString(getRandom(MAX_X)));
            PARAMS.put(Y, Integer.toString(getRandom(MAX_Y)));

            String response = doPost(URL);

            Document document;
            if (response != null) {
                document = Jsoup.parse(response);
                System.out.println(key + " " + (
                        document.getElementById("cotacao") == null ? "null" : document.getElementById("cotacao").wholeText()));
            }
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void doGet() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
            con.setRequestMethod("GET");
            getCookie(con);
            parseFormValues(readStream(con.getInputStream()));
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String doPost(final String targetURL) {
        HttpURLConnection connection = null;
        String postData = encodeParams();

        try {
            //Create connection
            final URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Cookie", COOKIE_SESSION_ID);
//            connection.setRequestProperty(
//                    "User-Agent",
//                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36"
//            );
//            connection.setRequestProperty(
//                    "Accept",
//                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
//            );
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

    private static String encodeParams() {
        final StringBuilder builder = new StringBuilder();
        PARAMS.keySet().forEach(key -> {
            try {
                builder.append(key).append("=").append(URLEncoder.encode(
                        PARAMS.get(key), "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        return builder.toString();
    }

    private static void getCookie(HttpURLConnection con) {
        for (int i = 0; ; i++) {
            String headerName = con.getHeaderFieldKey(i);
            String headerValue = con.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                break;
            }
            if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                String[] fields = headerValue.split(";\\s*");
                COOKIE_SESSION_ID = fields[0];
            }
        }
    }

    private static void parseFormValues(String html) {
        Document doc = Jsoup.parse(html);

        PARAMS.put(VIEW_STATE, doc.select("input[name=" + VIEW_STATE + "]").val());
        PARAMS.put(VIEW_STATE_GENERATOR, doc.select("input[name=" + VIEW_STATE_GENERATOR + "]").val());

        doc.getElementsByTag("select").get(1).select("option[value!=\"\"]").forEach(node -> {
            if (node.val().compareTo(node.wholeText()) != 0)
                FUNDS.put(node.wholeText(), node.val());
        });

    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                content.append('\n');
            }
        }
        return content.toString();
    }

    private static int getRandom(int maxRandom) {
        return ThreadLocalRandom.current().nextInt(0, maxRandom + 1);
    }
}
