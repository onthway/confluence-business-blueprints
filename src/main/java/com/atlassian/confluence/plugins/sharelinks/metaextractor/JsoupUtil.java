package com.atlassian.confluence.plugins.sharelinks.metaextractor;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JsoupUtil
{
    public static String getMetaContent(Document doc, String elementQuery)
    {
        Elements metaElements = doc.select(elementQuery);
        if (metaElements.isEmpty())
        {
            return null;
        }

        return metaElements.get(0).attr("content");
    }

    private JsoupUtil()
    {
    }
}
