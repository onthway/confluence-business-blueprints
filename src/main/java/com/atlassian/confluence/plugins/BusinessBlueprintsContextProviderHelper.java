package com.atlassian.confluence.plugins;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.atlassian.confluence.core.DateFormatter;
import com.atlassian.confluence.core.FormatSettingsManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUserPreferences;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.user.User;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BusinessBlueprintsContextProviderHelper
{
    public static final String STORAGE_DATE_FORMAT = "yyyy-MM-dd";
    private I18NBeanFactory i18NBeanFactory;
    private LocaleManager localeManager;
    private TemplateRenderer templateRenderer;
    private final UserAccessor userAccessor;
    private final FormatSettingsManager formatSettingsManager;

    public BusinessBlueprintsContextProviderHelper(I18NBeanFactory i18NBeanFactory, LocaleManager localeManager,
        TemplateRenderer templateRenderer, UserAccessor userAccessor, FormatSettingsManager formatSettingsManager)
    {
        this.i18NBeanFactory = i18NBeanFactory;
        this.localeManager = localeManager;
        this.templateRenderer = templateRenderer;
        this.userAccessor = userAccessor;
        this.formatSettingsManager = formatSettingsManager;
    }

    public String renderFromSoy(String pluginKey, String soyTemplate, Map<String, Object> soyContext)
    {
        StringBuilder output = new StringBuilder();
        templateRenderer.renderTo(output, pluginKey,
                                  soyTemplate, soyContext);
        return output.toString();
    }

    private Locale getLocale()
    {
        return localeManager.getSiteDefaultLocale();
    }

    public I18NBean getI18nBean()
    {
        return i18NBeanFactory.getI18NBean(getLocale());
    }

    public Locale getAuthenticatedUserLocale()
    {
        return localeManager.getLocale(getUser());
    }
    
    public User getUser()
    {
        return AuthenticatedUserThreadLocal.get();
    }

    public String getFormattedLocalDate(String dateFormat)
    {
        Date today = new Date();
        ConfluenceUserPreferences preferences = userAccessor.getConfluenceUserPreferences(getUser());
        DateFormatter dateFormatter = preferences.getDateFormatter(formatSettingsManager, localeManager);

        if (null == dateFormat)
        {
            return dateFormatter.format(today);
        }
        else
        {
            return dateFormatter.formatGivenString(dateFormat, today);
        }
    }

    /**
     * TODO: replace this method by using another one in core:
     * com.atlassian.confluence.content.render.xhtml.DefaultStorageFormatService#createStorageFormatForDate
     * The method is from CONF v5.6 onwards and that using the method will break backwards compatibility of meeting notes
     *
     * @return string that is represented a time tag
     */
    public String createStorageFormatForToday()
    {
        return createStorageFormatForDate(getFormattedLocalDate(STORAGE_DATE_FORMAT));
    }

    /**
     * TODO: replace this method by using another one in core: com.atlassian.confluence.content.render.xhtml.DefaultStorageFormatService#createStorageFormatForDate
     * The method is from CONF v5.6 onwards and that using the method will break backwards compatibility of meeting
     * notes
     *
     * @return string that is represented a time tag
     */
    public String createStorageFormatForDate(String date)
    {
        if (isBlank(date))
            return "";

        return String.format("<time datetime=\"%s\"></time>", date);
    }
}
