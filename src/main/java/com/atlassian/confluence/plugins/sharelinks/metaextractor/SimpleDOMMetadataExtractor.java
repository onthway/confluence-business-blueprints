package com.atlassian.confluence.plugins.sharelinks.metaextractor;

import com.atlassian.confluence.plugins.sharelinks.DOMMetadataExtractor;
import com.atlassian.confluence.plugins.sharelinks.LinkMetaData;
import com.atlassian.confluence.util.http.HttpRequest;
import com.atlassian.confluence.util.http.HttpResponse;
import com.atlassian.confluence.util.http.HttpRetrievalService;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class SimpleDOMMetadataExtractor implements DOMMetadataExtractor
{
    private static final Logger log = LoggerFactory.getLogger(SimpleDOMMetadataExtractor.class);

    private static final String TITLE_QUERY = "title";
    private static final String META_TITLE_QUERY = "meta[name=title]";
    private static final String META_DESCRIPTION_QUERY = "meta[name=description]";
    private static final String FAVICON_QUERY = "link[rel=shortcut icon]";

    private final HttpRetrievalService httpRetrievalService;

    public SimpleDOMMetadataExtractor(HttpRetrievalService httpRetrievalService) {
        this.httpRetrievalService = httpRetrievalService;
    }
    
    @Override
    public void updateMetadata(LinkMetaData meta, Document head)
    {
        if (StringUtils.isBlank(meta.getTitle()))
        {
            String title = JsoupUtil.getMetaContent(head, META_TITLE_QUERY);
            if (title == null)
            {
                Elements titleElements = head.select(TITLE_QUERY);
                if (!titleElements.isEmpty())
                {
                    title = titleElements.text();
                }
            }

            meta.setTitle(title);
        }

        if (StringUtils.isBlank(meta.getDescription()))
        {
            meta.setDescription(JsoupUtil.getMetaContent(head, META_DESCRIPTION_QUERY));
        }

        if (StringUtils.isBlank(meta.getFaviconURL()))
        {
            Elements faviconElements = head.select(FAVICON_QUERY);
            if (!faviconElements.isEmpty() && faviconElements.get(0).attr("href") != null)
            {
                String faviconURL = faviconElements.get(0).attr("href");
                // need verify and get absolute path for favorite icon if it was stored with relative path
                faviconURL = getAbsolutePath(faviconURL, meta.getResponseHost());
                meta.setFaviconURL(faviconURL);
            }
            else
            {
                retrieveWebRootFavicon(meta);
            }
        }
    }

    private void retrieveWebRootFavicon(LinkMetaData meta) {
        if (meta.getFaviconURL() == null)
        {
            URI webRootFavicon = getFaviconUri(meta.getResponseHost());
            String webRootFaviconPath = webRootFavicon.toString();
            HttpRequest request = httpRetrievalService.getDefaultRequestFor(webRootFaviconPath);
            try
            {
                HttpResponse response = httpRetrievalService.get(request);
                if (response.getStatusCode() == 200)
                {
                    meta.setFaviconURL(webRootFaviconPath);
                }
            }
            catch (IOException e)
            {
                log.error("Error with io exception: ", e);
            }
        }
    }

    /**
     * Check and get absolute path from the path of an element for display on
     * sharelinks page if the path is relative path
     * 
     * @param path the path of an element
     * @param host the response host of the path
     * @return String absolute path
     */
    private String getAbsolutePath(String path, URI host)
    {
        try
        {
            URI uri = host.resolve(path.trim()); // Trim for CONF-31211
            return uri.toString();
        }
        catch(IllegalArgumentException e)
        {
            log.info("Favicon path {} could not be resolved.", path);
        }
        return null;
    }

    protected static URI getFaviconUri(URI uri)
    {
        return uri.resolve("/favicon.ico");
    }
}
