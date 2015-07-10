package com.atlassian.confluence.plugins.meetingnotes;

import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.createcontent.TemplateRendererHelper;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;

public class MeetingNotesIndexContextProvider extends AbstractBlueprintContextProvider
{
    private final BusinessBlueprintsContextProviderHelper helper;

    public MeetingNotesIndexContextProvider(BusinessBlueprintsContextProviderHelper helper,
        TemplateRendererHelper templateRendererHelper)
    {
        super(templateRendererHelper);
        this.helper = helper;
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext context)
    {
        String blueprintId = context.getBlueprintId().toString();
        String createLabel = helper.getI18nBean().getText(
            "com.atlassian.confluence.plugins.confluence-business-blueprints.meeting-notes-blueprint.create-button-label");
        String spaceKey = context.getSpaceKey();
        String blueprintKey = context.getBlueprintModuleCompleteKey().getCompleteKey();
        String templateLabel = context.getTemplateLabel();
        String blankTitle = helper.getI18nBean().getText(
            "com.atlassian.confluence.plugins.confluence-business-blueprints.meeting-notes-blueprint.blank-title");
        String blankDescription = helper.getI18nBean().getText("com.atlassian.confluence.plugins.confluence-business-blueprints.meeting-notes-blueprint.blank-description");

        context.put("taskReportMacro", renderTaskReportMacro(spaceKey, templateLabel));
        context.put("createFromTemplateMacro", renderCreateFromTemplateMacro(blueprintId, createLabel, "", blueprintKey));
        context.put("contentReportTableMacro", renderContentReportTableMacro(templateLabel, context.getAnalyticsKey(), spaceKey, blankTitle, blankDescription, createLabel, blueprintId, blueprintKey));

        return context;
    }

    private String renderTaskReportMacro(String spaceKey, String label)
    {
        HashMap<String, String> soyContext = newHashMap();
        soyContext.put("status", "incomplete");
        soyContext.put("spaceAndPage", "space:" + spaceKey);
        soyContext.put("spaces", spaceKey);
        soyContext.put("labels", label);
        soyContext.put("pageSize", "10");

        return templateRendererHelper.renderMacroXhtml("tasks-report-macro", soyContext);
    }
}
