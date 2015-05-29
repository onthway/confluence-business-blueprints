package com.atlassian.confluence.plugins.decisions;

import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.util.i18n.I18NBean;

import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;


public class DecisionsContextProvider extends AbstractBlueprintContextProvider
{
    private BusinessBlueprintsContextProviderHelper helper;

    public DecisionsContextProvider(BusinessBlueprintsContextProviderHelper helper)
    {
        this.helper = helper;
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext context)
    {
        I18NBean i18nBean = helper.getI18nBean();

        addStatusAndOutcomeToContextMap(context, i18nBean);
        addMentionsToContextMap(context, i18nBean, "stakeholders", "mentions","decisions.stakeholders.mentions.placeholder");
        addMentionsToContextMap(context, i18nBean, "owner", "owner", "decisions.blueprint.wizard.form.owner.placeholder");
        addPlaceholderToContextMap(context, i18nBean, "background", "background-placeholder", "decisions.blueprint.wizard.form.background.placeholder");

        context.put("due-date", helper.createStorageFormatForDate((String) context.get("due-date")));
        addPlaceholderToContextMap(context, i18nBean, "due-date", "due-date-placeholder", "decisions.blueprint.template.duedate.placeholder");

        return context;
    }

    private void addPlaceholderToContextMap(BlueprintContext context, I18NBean i18nBean, String field, String placeholderField, String placeholderI18nKey)
    {
        String fieldValue = (String) context.get(field);
        if (isBlank(fieldValue))
        {
            String placeholderText = i18nBean.getText(placeholderI18nKey);
            HashMap<String, Object> soyContext = newHashMap();
            soyContext.put("placeholderText", placeholderText);
            String placeholder = helper.renderFromSoy("com.atlassian.confluence.plugins.confluence-business-blueprints:decisions-resources",
                                                      "Confluence.Blueprints.Decisions.placeholder.soy", soyContext);
            context.put(placeholderField, placeholder);
        }
        else
        {
            context.put(field, fieldValue);
        }
    }

    private void addStatusAndOutcomeToContextMap(BlueprintContext context, I18NBean i18nBean)
    {
        StatusMacroDetails status;
        try
        {
            status = StatusMacroDetails.valueOf((String) context.get("status"));

            if (status != StatusMacroDetails.GREEN)
            {
                context.put("final-decision",""); //clear the outcomes field if the decision has not been decided.
            }
        }
        catch(IllegalArgumentException e)
        {
            status = StatusMacroDetails.getDefault();
        }
        catch(NullPointerException e)
        {
            status = StatusMacroDetails.getDefault();
        }

        //template for status
        String statusTemplate = "";
        HashMap<String, Object> soyContext = newHashMap();
        soyContext.put("status", i18nBean.getText(status.getI18nKey()));
        soyContext.put("statusColour", status.getMacroColour());
        statusTemplate = helper.renderFromSoy("com.atlassian.confluence.plugins.confluence-business-blueprints:decisions-resources",
                                                "Confluence.Blueprints.Decisions.statusTemplate.soy", soyContext);
        context.put("status", statusTemplate);

        addPlaceholderToContextMap(context, i18nBean, "final-decision", "final-decision-placeholder",
                "decisions.blueprint.wizard.form.final.decision.placeholder");
    }

    private void addMentionsToContextMap(BlueprintContext context, I18NBean i18nBean, String field,
        String templateVariable, String placeholderText)
    {
        String people = (String) context.get(field);
        String mentions = "";
        HashMap<String, Object> soyContext = newHashMap();

        if (isNotBlank(people))
        {
            String[] names = people.split(",");
            soyContext.put("names", names);
            mentions = helper.renderFromSoy("com.atlassian.confluence.plugins.confluence-business-blueprints:decisions-resources",
                                            "Confluence.Blueprints.Decisions.mentionXml.soy", soyContext);
        }
        else
        {
            soyContext.put("placeholderText", i18nBean.getText(placeholderText));
            mentions = helper.renderFromSoy("com.atlassian.confluence.plugins.confluence-business-blueprints:decisions-resources",
                                            "Confluence.Blueprints.Decisions.mentionsPlaceholder.soy", soyContext);
        }
        context.put(templateVariable, mentions);
    }

    public static enum StatusMacroDetails
    {
        GREY("Grey", "decisions.blueprint.wizard.form.status.open"),
        GREEN("Green", "decisions.blueprint.wizard.form.status.closed"),
        YELLOW("Yellow", "decisions.blueprint.wizard.form.status.progress");

        private final String macroColour;
        private final String i18nKey;

        StatusMacroDetails(String macroColour, String i18nKey)
        {
            this.macroColour = macroColour;
            this.i18nKey = i18nKey;
        }

        public String getMacroColour()
        {
            return macroColour;
        }

        public String getI18nKey()
        {
            return i18nKey;
        }

        public static StatusMacroDetails getDefault()
        {
            return GREY;
        }
    }

}

