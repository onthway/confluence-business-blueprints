package com.atlassian.confluence.plugins.sharelinks;

import org.jsoup.nodes.Document;

public interface DOMMetadataExtractor
{
    void updateMetadata(LinkMetaData meta, Document head);
}
