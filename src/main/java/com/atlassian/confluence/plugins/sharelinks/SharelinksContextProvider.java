package com.atlassian.confluence.plugins.sharelinks;


import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.blueprint.business.PluginConstants;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.plugins.sharelinks.widgetconnector.WidgetConnectorSupport;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.core.util.XMLUtils;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.net.URISyntaxException;
import java.util.HashMap;

public class SharelinksContextProvider extends AbstractBlueprintContextProvider
{
    private final LinkMetaDataExtractor linkMetaDataExtractor;
    private final BusinessBlueprintsContextProviderHelper helper;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final WidgetConnectorSupport widgetConnectorSupport;

    public SharelinksContextProvider(LinkMetaDataExtractor linkMetaDataExtractor, 
            BusinessBlueprintsContextProviderHelper helper, WebResourceUrlProvider webResourceUrlProvider,
            WidgetConnectorSupport widgetConnectorSupport)
    {
        this.linkMetaDataExtractor = linkMetaDataExtractor;
        this.helper = helper;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.widgetConnectorSupport = widgetConnectorSupport;
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext context)
    {
        String url = (String) context.get("url");
        LinkMetaData meta = new LinkMetaData(url);
        
        try
        {
            meta = linkMetaDataExtractor.parseMetaData(url, false);
        } 
        catch (URISyntaxException e) 
        {
            meta.setDomain(url);
        }
        
        String title = meta.getTitle();
        if (StringUtils.isEmpty(title))
        {
            title = meta.getSourceURL();
        }
        context.setTitle(title);

        //put favicon of shared link into context, if it does not exist, use default shared links favicon
        String faviconURL = meta.getFaviconURL();
        if (StringUtils.isEmpty(faviconURL))
        {
            faviconURL = getDefaultSharedLinksFavicon();
        }
        String faviconImg = String.format("<img src=\"%s\" height=\"16px\"/>", XMLUtils.escape(faviconURL));
        context.put("faviconImg", faviconImg);

        String htmlLink = String.format("<a href=\"%s\">%s</a>",
                XMLUtils.escape(meta.getSourceURL()),
                XMLUtils.escape(meta.getExcerptedURL()));
        context.put("htmlLink", htmlLink);

        context.put("createdDate", helper.getFormattedLocalDate(null));

        addLinkMetaDataToContextMap(meta, context);

        return context;
    }

    private void addLinkMetaDataToContextMap(LinkMetaData linkMetaData, BlueprintContext context)
    {
        String soyTemplateName;
        HashMap<String, Object> soyLinkMetaDataContext = Maps.newHashMap();
        
        if(isTwitterCardUrl(linkMetaData)) {
            soyTemplateName = "Confluence.Blueprints.Sharelinks.twitterMetaDataHtml.soy";
        }
        else if (StringUtils.isNotEmpty(linkMetaData.getVideoURL()))
        {
            soyTemplateName = "Confluence.Blueprints.Sharelinks.videoMetaDataHtml.soy";
            boolean  isSupportedMediaDomain = widgetConnectorSupport.isSupported(linkMetaData.getDomain());
            soyLinkMetaDataContext.put("isSupportedMediaDomain", isSupportedMediaDomain);
        }
        else
        {
            soyTemplateName = "Confluence.Blueprints.Sharelinks.metaDataHtml.soy";
        }

        soyLinkMetaDataContext.put("linkMetaData", linkMetaData);
        
        String faviconURL = linkMetaData.getFaviconURL();
        if (StringUtils.isEmpty(faviconURL))
        {
            faviconURL = getDefaultSharedLinksFavicon();
        }
        soyLinkMetaDataContext.put("faviconURL", faviconURL);

        I18NBean i18nBean = helper.getI18nBean();

        String descriptionMessage = linkMetaData.getDescription();
        if(StringUtils.isEmpty(descriptionMessage))
        {
            if(StringUtils.isEmpty(linkMetaData.getImageURL()))
            {
                descriptionMessage = i18nBean.getText("sharelinks.blueprint.page.preview.unavailable");
            }
            else
            {
                descriptionMessage = i18nBean.getText("sharelinks.blueprint.page.description.unavailable");
            }
        }
        soyLinkMetaDataContext.put("descriptionMessage", descriptionMessage);
        
        String soyLinkMetaDataHtml = helper.renderFromSoy(PluginConstants.SHARELINKS_RESOURCE_KEY,
                                                                soyTemplateName, soyLinkMetaDataContext);
        context.put("linkMetaDataHtml", soyLinkMetaDataHtml);
    }

    private boolean isTwitterCardUrl(LinkMetaData linkMetaData)
    {
        String domain = linkMetaData.getDomain();
        return domain != null && domain.contains("twitter.com") &&
                (linkMetaData.getSourceURL().contains("/status/") || linkMetaData.getSourceURL().contains("/statuses/"));
    }

    private String getDefaultSharedLinksFavicon()
    {
        return webResourceUrlProvider.getStaticPluginResourceUrl(PluginConstants.SHARELINKS_RESOURCE_KEY, 
                                                                    "default-sharelinks-favicon-16.png", UrlMode.ABSOLUTE);
    }
}