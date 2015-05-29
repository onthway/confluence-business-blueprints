package com.atlassian.confluence.plugins.sharelinks;

import java.net.URISyntaxException;

/**
 * Extract webpage metadata from an URL 
 *
 */
public interface LinkMetaDataExtractor
{
    /**
     * Extract metadata from an URL
     * @param url full URL to a webpage
     * @param isPreview boolean true:get meta data for display preview
     * @return the LinkMetaData object reprensenting metadata of the webpage
     * @throws URISyntaxException 
     */
    LinkMetaData parseMetaData(String url, boolean isPreview) throws URISyntaxException;
}
