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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * TODO add a description here
 *
 * @author rico
 */
public class Teste {

    private static final String URL = "http://www.caixagest.pt/simulador_cotacoes.aspx";

    private static final String VIEW_STATE = "__VIEWSTATE";
    private static String VIEW_STATE_VALUE;
    private static final String VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR";
    private static String VIEW_STATE_GENERATOR_VALUE;
    private static final String FUNDS_DROPDOWN = "FundosDropDownList";
    private static final String DATE = "Data";
    private static final String X = "simuladorCotacoesBtn.x";
    private static final String Y = "simuladorCotacoesBtn.y";

    private static final int MAX_X = 113;
    private static final int MAX_Y = 30;

    private static final String DATE_PATTERN = "dd-MM-yyyy";

    private static final Map<String, String> FUNDS = new HashMap<>(10);

    private static String SESSION_ID;

    public static void main(String[] args) {
        System.out.println("Starting");
        init();
    }


    private static void init() {
        doGet();

        FUNDS.keySet().forEach(key -> {
            String id = FUNDS.get(key);

            String params = VIEW_STATE + "=" + VIEW_STATE_VALUE + "&" +
                    VIEW_STATE_GENERATOR + "=" + VIEW_STATE_GENERATOR_VALUE + "&" +
                    FUNDS_DROPDOWN + "=" + id + "&" +
                    DATE + "=" + new SimpleDateFormat(DATE_PATTERN).format(
                            new Date(Instant.now().minus(Duration.ofDays(1)).toEpochMilli())) + "&" +
                    X + "=" + getRandom(MAX_X) + "&" +
                    Y + "=" + getRandom(MAX_Y);

            StringTokenizer st = new StringTokenizer(params, "&");
            while ((st.hasMoreTokens())) {
                System.out.println(st.nextToken());
            }
            System.out.println("Cookie="+SESSION_ID);

            String response = doPost(URL, params);
            System.out.println(response);

            Document document = Jsoup.parse(response);
            System.out.println(key + " " + (
                    document.getElementById("cotacao") == null ? "null" : document.getElementById("cotacao").val()));
            System.exit(0);
        });
    }

    private static void doGet() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
            con.setRequestMethod("GET");
            parseValues(readStream(con.getInputStream()));
            getCookie(con);
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String doPost(final String targetURL, final String urlParameters) {
        HttpURLConnection connection = null;
//
//        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
//        int postDataLength = postData.length;

        try {
            //Create connection
            final URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Cookie", SESSION_ID);
//            connection.setRequestProperty(
//                    "User-Agent",
//                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36"
//            );
//            connection.setRequestProperty(
//                    "Accept",
//                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
//            );
//            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            try (final DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                //wr.write(Base64.getEncoder().encode(urlParameters.getBytes()));
                wr.writeChars(URLEncoder.encode(urlParameters, "UTF-8"));
                wr.flush();
                wr.close();
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

    private static void getCookie(HttpURLConnection con) {
        for (int i = 0; ; i++) {
            String headerName = con.getHeaderFieldKey(i);
            String headerValue = con.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                break;
            }
            if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                String[] fields = headerValue.split(";\\s*");
                SESSION_ID = fields[0];
            }
        }
    }

    private static void parseValues(String html) {
        Document doc = Jsoup.parse(html);

        VIEW_STATE_VALUE = doc.select("input[name=" + VIEW_STATE + "]").val();
        VIEW_STATE_GENERATOR_VALUE = doc.select("input[name=" + VIEW_STATE_GENERATOR + "]").val();

        doc.getElementsByTag("select").get(1).select("option[value!=\"\"]")
                .stream().forEach(node -> {
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
