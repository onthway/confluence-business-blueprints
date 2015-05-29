package com.atlassian.confluence.plugins.sharelinks.metaextractor;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

import com.atlassian.confluence.plugins.sharelinks.DOMMetadataExtractor;
import com.atlassian.confluence.plugins.sharelinks.LinkMetaData;

public class OpenGraphDOMMetadataExtractor implements DOMMetadataExtractor
{
    private static final String META_TITLE_QUERY = "meta[property=og:title]";
    private static final String META_IMAGE_QUERY = "meta[property=og:image]";
    private static final String META_DESCRIPTION_QUERY = "meta[property=og:description]";
    private static final String META_VIDEO_QUERY = "meta[property=og:video]";

    @Override
    public void updateMetadata(LinkMetaData meta, Document head)
    {
        if (StringUtils.isBlank(meta.getTitle()))
        {
            meta.setTitle(JsoupUtil.getMetaContent(head, META_TITLE_QUERY));
        }

        if (StringUtils.isBlank(meta.getDescription()))
        {
            meta.setDescription(JsoupUtil.getMetaContent(head, META_DESCRIPTION_QUERY));
        }

        if (StringUtils.isBlank(meta.getImageURL()))
        {
            meta.setImageURL(JsoupUtil.getMetaContent(head, META_IMAGE_QUERY));
        }

        if (StringUtils.isBlank(meta.getVideoURL()))
        {
            meta.setVideoURL(JsoupUtil.getMetaContent(head, META_VIDEO_QUERY));
        }
    }
}
