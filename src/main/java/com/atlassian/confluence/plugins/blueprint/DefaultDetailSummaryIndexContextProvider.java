package com.atlassian.confluence.plugins.blueprint;

import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.createcontent.TemplateRendererHelper;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.plugin.PluginParseException;

public class DefaultDetailSummaryIndexContextProvider extends AbstractBlueprintContextProvider
{
    protected BusinessBlueprintsContextProviderHelper helper;
    private String i18nKeyPrefix;

    public DefaultDetailSummaryIndexContextProvider(BusinessBlueprintsContextProviderHelper helper, TemplateRendererHelper templateRendererHelper)
    {
        super(templateRendererHelper);
        this.helper = helper;
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext context)
    {
        I18NBean i18NBean = helper.getI18nBean();

        addDetailsSummaryMacroToContextMap(context, i18NBean);
        addCreateFromTemplateMacroToContextMap(context, i18NBean);

        return context;
    }

    protected String getI18nKeyPrefix(BlueprintContext context)
    {
        return (i18nKeyPrefix != null) ? i18nKeyPrefix :
            context.getBlueprintModuleCompleteKey().getCompleteKey().replace(':', '.');
    }

    /**
     * Override to provide the optional paragraph to be rendered above the main table
     * 
     * @return Template fragment that will be inserted just above the main table. Otherwise, return null.
     */
    protected String getIntroParagraph(BlueprintContext context, I18NBean i18NBean)
    {
        return null;
    }

    private void addCreateFromTemplateMacroToContextMap(BlueprintContext context, I18NBean i18NBean)
    {
        String blueprintKey = context.getBlueprintModuleCompleteKey().getCompleteKey();
        String buttonLabel = i18NBean.getText(getI18nKeyPrefix(context) + ".create-button-label");

        String createFromTemplateMacro = renderCreateFromTemplateMacro(context.getBlueprintId().toString(), buttonLabel,"", blueprintKey);

        context.put("createFromTemplateMacro", createFromTemplateMacro);
    }

    private void addDetailsSummaryMacroToContextMap(BlueprintContext context, I18NBean i18NBean)
    {
        String templateLabel = context.getTemplateLabel();
        String spaceKey = context.getSpaceKey();

        String i18nPrefix = getI18nKeyPrefix(context);
        String firstColumn = i18NBean.getText(i18nPrefix + ".first-column");
        String headings = i18NBean.getText(i18nPrefix + ".headings");
        String blankTitle = i18NBean.getText(i18nPrefix + ".blank-title");
        String blankDescription = i18NBean.getText(i18nPrefix + ".blank-description");
        String createButtonLabel = i18NBean.getText(i18nPrefix + ".create-button-label");

        // Key or UUID may be null - if so, inject into Soy template as blank string
        String blueprintModuleCompleteKey = "";
        if (context.getBlueprintModuleCompleteKey() != null)
        {
            blueprintModuleCompleteKey = context.getBlueprintModuleCompleteKey().getCompleteKey();
        }
        String contentBlueprintId = "";
        if (context.getBlueprintId() != null)
        {
            contentBlueprintId = context.getBlueprintId().toString();
        }

        HashMap<String, Object> soyContext = newHashMap();
        soyContext.put("label", templateLabel);
        soyContext.put("spaces", spaceKey);
        soyContext.put("firstcolumn", firstColumn);
        soyContext.put("headings", headings);
        soyContext.put("blueprintModuleCompleteKey", blueprintModuleCompleteKey);
        soyContext.put("blankTitle", blankTitle);
        soyContext.put("blankDescription", blankDescription);
        soyContext.put("createButtonLabel", createButtonLabel);
        soyContext.put("contentBlueprintId", contentBlueprintId);

        String detailsSummaryMacro = helper.renderFromSoy("com.atlassian.confluence.plugins.confluence-business-blueprints:common-template-resources",
                                                          "Confluence.Blueprints.Common.Index.detailsSummaryMacro.soy", soyContext);
        context.put("detailsSummaryMacro", detailsSummaryMacro);

        String introParagraph = getIntroParagraph(context, i18NBean);
        context.put("introParagraph", (introParagraph != null) ? introParagraph : "");
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        i18nKeyPrefix = params.get("i18nKeyPrefix");
    }
}
