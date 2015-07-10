package com.atlassian.confluence.plugins.meetingnotes;

import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.user.User;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;

public class MeetingNotesContextProvider extends AbstractBlueprintContextProvider
{
    private static final String TEMPLATE_PROVIDER_PLUGIN_KEY =
        "com.atlassian.confluence.plugins.confluence-business-blueprints:meeting-notes-resources";

    private static final String TEMPLATE_NAME = "Confluence.Templates.Meeting.Notes.userMention.soy";

    private static final String USERNAME_KEY = "username";

    private TemplateRenderer templateRenderer;
    private final LocaleManager localeManager;
    private final I18NBeanFactory i18NBeanFactory;
    private final BusinessBlueprintsContextProviderHelper helper;

    public MeetingNotesContextProvider(LocaleManager localeManager, I18NBeanFactory i18NBeanFactory,
            TemplateRenderer templateRenderer, BusinessBlueprintsContextProviderHelper helper)
    {
        //commit
        this.localeManager = localeManager;
        this.i18NBeanFactory = i18NBeanFactory;
        this.templateRenderer = templateRenderer;
        this.helper = helper;
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext context)
    {
        final String pageTitle = i18nBean().getText("meeting.notes.blueprint.page.title", asList(helper.getFormattedLocalDate("yyyy-MM-dd")));

        String username = AuthenticatedUserThreadLocal.getUsername();
        StringBuilder userMention = new StringBuilder();
        Map<String, Object> soyContext = new HashMap<String, Object>();

        if (username != null) {
            soyContext.put(USERNAME_KEY, username);
        }

        templateRenderer.renderTo(userMention, TEMPLATE_PROVIDER_PLUGIN_KEY, TEMPLATE_NAME, soyContext);

        context.put("documentOwner", userMention.toString());
        context.put("currentDate", helper.getFormattedLocalDate(null)); //for old edited blueprint templates
        context.put("currentDateLozenge", helper.createStorageFormatForToday());
        context.setTitle(pageTitle);

        return context;
    }

    private Locale getLocale()
    {
        return localeManager.getLocale(getUser());
    }

    private User getUser()
    {
        return AuthenticatedUserThreadLocal.get();
    }

    private I18NBean i18nBean()
    {
        return i18NBeanFactory.getI18NBean(getLocale());
    }
}
