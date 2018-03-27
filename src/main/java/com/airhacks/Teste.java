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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * TODO add a description here
 *
 * @author rico
 */
public class Teste {

    private static final String URL = "https://www.caixagest.pt/simulador_cotacoes.aspx";

    private static final String VIEW_STATE = "__VIEWSTATE";
    private static String VIEW_STATE_VALUE;
    private static final String VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR";
    private static String VIEW_STATE_GENERATOR_VALUE;
    private static final String FUNDS_DROPDOWN = "FundosDropDownList";
    private static final String DATE = "Data";
    private static final String X = "simuladorCotacoesBtn.x";
    private static final String Y = "simuladorCotacoesBtn.x";

    private static final int MAX_X = 113;
    private static final int MAX_Y = 30;

    private static final String DATE_PATTERN = "dd-MM-yyyy";

    private static final Map<String, String> FUNDS = new HashMap<>(10);

    private static String SESSION_ID;

    public static void main(String[] args) {
        System.out.println("Starting");
        init();
        makePost();
    }


    private static void init() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
            con.setRequestMethod("GET");
            processHTML(readRequest(con.getInputStream()));
            getCookie(con);

            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makePost() {
        Stream<String> targetStream = StreamSupport.stream(FUNDS.keySet().spliterator(), false);
        targetStream.forEach(key -> {
            String id = FUNDS.get(key);

            String params = VIEW_STATE + "=" + VIEW_STATE_VALUE + "&" +
                    VIEW_STATE_GENERATOR + "=" + VIEW_STATE_GENERATOR_VALUE + "&" +
                    FUNDS_DROPDOWN + "=" + id + "&" +
                    DATE + "=" + new SimpleDateFormat(DATE_PATTERN).format(new Date(Instant.now().toEpochMilli())) + "&" +
                    X + "=" + getRandom(MAX_X) + "&" +
                    Y + "=" + getRandom(MAX_Y) ;

            String response = excutePost(URL, params);

            Document document = Jsoup.parse(response);
            System.out.println(key + " " + document.getElementById("cotacao").val());

        });
    }

    public static String excutePost(String targetURL, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36");

            connection.setRequestProperty("Cookie", SESSION_ID);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

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


    private static void processHTML(String html) {
        Document doc = Jsoup.parse(html);

        VIEW_STATE_VALUE = doc.select("input[name=" + VIEW_STATE + "]").val();
        VIEW_STATE_GENERATOR_VALUE = doc.select("input[name=" + VIEW_STATE_GENERATOR + "]").val();

        doc.getElementsByTag("select").get(1).select("option[value!=\"\"]")
                .stream().forEach(node -> {
            if (node.val().compareTo(node.wholeText()) != 0)
                FUNDS.put(node.wholeText(), node.val());
        });

    }


    private static String readRequest(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    private static int getRandom(int maxRandom) {
        return ThreadLocalRandom.current().nextInt(0, maxRandom + 1);
    }


}
