package com.github.rico.utils;

import lombok.Getter;
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

    @Getter
    private final String paramViewstate = "__VIEWSTATE";

    @Getter
    private final String paramViewStateGenerator = "__VIEWSTATEGENERATOR";

    @Getter
    private final String paramFundsDropdown = "FundosDropDownList";

    @Getter
    private final String paramDate = "Data";

    @Getter
    private final String paramX = "simuladorCotacoesBtn.x";

    @Getter
    private final String paramY = "simuladorCotacoesBtn.y";

    @Getter
    private final int maxX = 113;

    @Getter
    private final int maxY = 30;

    /**
     * Private constructor. Use SystemProperties.PROPERTIES instead.
     */
    private SystemProperties() {
    }

    private String getValue(String key, Object defaultValue) {
        String value = String.valueOf(defaultValue);

        if (System.getProperty(key) == null) {
            LOGGER.warn("System property '{}' does not exist or is empty. Using default...", key);
        } else {
            value = System.getProperty(key);
        }

        return value;
    }

    public String getDateTimePattern() {
        return getValue("caixagest.date.time.pattern", dateTimePattern);
    }

    public String getDatePattern() {
        return getValue("caixagest.date.pattern", datePattern);
    }

    public String getInitialDate() {
        return getValue("caixagest.initial.date", initialDate);
    }

    public String getUrl() {
        return getValue("caixagest.url", url);
    }

}
