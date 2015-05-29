package com.atlassian.confluence.plugins.sharelinks;

import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.blueprint.DefaultDetailSummaryIndexContextProvider;
import com.atlassian.confluence.plugins.createcontent.TemplateRendererHelper;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.google.common.collect.Maps;

import java.util.HashMap;

public class SharelinksIndexContextProvider extends DefaultDetailSummaryIndexContextProvider
{
    public SharelinksIndexContextProvider(BusinessBlueprintsContextProviderHelper helper, TemplateRendererHelper templateRendererHelper)
    {
        super(helper, templateRendererHelper);
    }

    @Override
    protected String getIntroParagraph(BlueprintContext context, I18NBean i18NBean)
    {
        String soyBookmarkletGuideTemplateName = "Confluence.Blueprints.Sharelinks.bookmarkletGuide.soy";

        HashMap<String, Object> soyBookmarkletGuideContext = Maps.newHashMap();

        return helper.renderFromSoy(
                "com.atlassian.confluence.plugins.confluence-business-blueprints:sharelinks-resources",
                soyBookmarkletGuideTemplateName, soyBookmarkletGuideContext);
    }
}
