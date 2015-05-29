package com.atlassian.confluence.plugins.sharelinks;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.*;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.google.common.collect.Maps;

public class URLMacro implements Macro, EditorImagePlaceholder
{
    private final SettingsManager settingsManager;
    private final BusinessBlueprintsContextProviderHelper helper;
    private static final String IMAGE_PATH = "/download/resources/com.atlassian.confluence.plugins.confluence-business-blueprints:sharelinks-urlmacro-editor-resources/sharelinks-urlmacro-placeholder.png";

    public URLMacro(SettingsManager settingsManager, BusinessBlueprintsContextProviderHelper helper)
    {
        this.settingsManager = settingsManager;
        this.helper = helper;
    }

    @Override
    public String execute(Map<String, String> parameters, String bodyText, ConversionContext conversionContext) throws MacroExecutionException
    {
        String soyBookmarkletLinkTemplateName = "Confluence.Blueprints.SharelinksUrlMacro.bookmarkletLink.soy";

        String bookmarkletActionURL = settingsManager.getGlobalSettings().getBaseUrl() + "/plugins/sharelinksbookmarklet/bookmarklet.action";
        HashMap<String, Object> soyLinkMetaDataContext = Maps.newHashMap();
        soyLinkMetaDataContext.put("bookmarkletActionURL", bookmarkletActionURL);

        String soyBookmarkletGuideHtml = helper.renderFromSoy(
                "com.atlassian.confluence.plugins.confluence-business-blueprints:sharelinks-urlmacro-resources",
                soyBookmarkletLinkTemplateName, soyLinkMetaDataContext);
        return soyBookmarkletGuideHtml;
    }

    @Override
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType()
    {
        return OutputType.BLOCK;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> params, ConversionContext ctx)
    {
        return new DefaultImagePlaceholder(IMAGE_PATH, new Dimensions(175, 30), false);
    }

}
