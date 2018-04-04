package com.github.rico.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to access system properties
 * <p>
 * Created by Rico on 19/01/2017
 */
public final class SystemProperties {

    /**
     * The System Properties instance property.
     */
    public static final SystemProperties PROPERTIES = new SystemProperties();

    private final Logger LOGGER = LoggerFactory.getLogger(SystemProperties.class);

    private final String dateTimePattern = "dd-MM-yyyy HH:mm:ss";

    private final String datePattern = "dd-MM-yyyy";

    private final String initialDate = "01-01-2000";

    private final String url = "http://www.caixagest.pt/simulador_cotacoes.aspx";

    private final String paramViewstate = "__VIEWSTATE";

    private final String paramViewStateGenerator = "__VIEWSTATEGENERATOR";

    private final String paramFundsDropdown = "FundosDropDownList";

    private final String paramDate = "Data";

    private final String paramX = "simuladorCotacoesBtn.x";

    private final String paramY = "simuladorCotacoesBtn.y";

    private final int maxX = 113;

    private final int maxY = 30;


    /**
     * Private constructor. Use SystemProperties.PROPERTIES instead.
     */
    private SystemProperties() {
    }

    /**
     * Check fot the values on the system or else return default ones
     *
     * @param key          the key to search for
     * @param defaultValue the default channelName
     * @return a string channelName
     */
    private String getValue(String key, Object defaultValue) {
        String value = String.valueOf(defaultValue);

        if (System.getProperty(key) == null) {
            LOGGER.warn("System property '{}' does not exist or is empty. Using default...", key);
        } else {
            value = System.getProperty(key);
        }

        return value;
    }

    /**
     * Get the date time second pattern
     *
     * @return pattern string
     */
    public String getDateTimePattern() {
        return getValue("caixagest.date.time.pattern", dateTimePattern);
    }

    /**
     * Get the date pattern
     *
     * @return a pattern string
     */
    public String getDatePattern() {
        return getValue("caixagest.date.pattern", datePattern);
    }


    /**
     * Get the date pattern
     *
     * @return a pattern string
     */
    public String getInitialDate() {
        return getValue("caixagest.initial.date", initialDate);
    }

    public String getUrl() {
        return getValue("caixagest.url", url);
    }

    public String getParamViewstate() {
        return paramViewstate;
    }

    public String getParamViewStateGenerator() {
        return paramViewStateGenerator;
    }

    public String getParamFundsDropdown() {
        return paramFundsDropdown;
    }

    public String getParamDate() {
        return paramDate;
    }

    public String getParamX() {
        return paramX;
    }

    public String getParamY() {
        return paramY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }
}
