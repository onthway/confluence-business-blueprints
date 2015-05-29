package com.atlassian.confluence.plugins.sharelinks.metaextractor;

import com.atlassian.confluence.plugins.sharelinks.LinkMetaData;
import com.atlassian.confluence.util.http.HttpRetrievalService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SimpleDOMMetadataExtractor}
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleDOMMetadataExtractorTest
{
    private static final String URL = "http://test.atlassian.com/confluence/blueprints/business/sharedlinks.html";

    private SimpleDOMMetadataExtractor simpleDomMetadataExtractor;
    private LinkMetaData linkMetaData;
    @Mock private HttpRetrievalService httpRetrievalService;

    @Before
    public void setUp() throws Exception
    {
        simpleDomMetadataExtractor = new SimpleDOMMetadataExtractor(httpRetrievalService);

        linkMetaData = new LinkMetaData(URL);
        linkMetaData.setTitle("My Awesome Title");
        linkMetaData.setDescription("Awesomeness");
        linkMetaData.setResponseHost(new URI(URL));
    }

    // CONF-31211
    @Test
    public void extractDocumentWithSpaceInFavicon() throws Exception
    {
        Document head = mock(Document.class);
        Element faviconElement = mock(Element.class);
        when(faviconElement.attr("href")).thenReturn("/path/to/favicon.ico "); // The trailing space is deliberate

        Elements faviconElements = new Elements(faviconElement);
        when(head.select("link[rel=shortcut icon]")).thenReturn(faviconElements);
        simpleDomMetadataExtractor.updateMetadata(linkMetaData, head);

        assertEquals("http://test.atlassian.com/path/to/favicon.ico", linkMetaData.getFaviconURL());
    }

    @Test
    public void extractDocumentWithIllegalCharsInFavicon() throws Exception
    {
        Document head = mock(Document.class);
        Element faviconElement = mock(Element.class);
        when(faviconElement.attr("href")).thenReturn("/path/to/fav&%#$^icon.ico");

        Elements faviconElements = new Elements(faviconElement);
        when(head.select("link[rel=shortcut icon]")).thenReturn(faviconElements);
        simpleDomMetadataExtractor.updateMetadata(linkMetaData, head);

        // Expected behaviour is to fail silently instead of dying with an IllegalArgumentException
        assertNull(linkMetaData.getFaviconURL());
    }
}
