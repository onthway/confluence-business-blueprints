package com.atlassian.confluence.plugins.sharelinks.metaextractor;

import com.atlassian.confluence.plugins.sharelinks.LinkMetaData;
import com.atlassian.confluence.util.http.HttpRequest;
import com.atlassian.confluence.util.http.HttpResponse;
import com.atlassian.confluence.util.http.HttpRetrievalService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.confluence.plugins.sharelinks.metaextractor.SimpleDOMMetadataExtractor.getFaviconUri;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MasterLinkMetaDataExtractor}
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterLinkMetaDataExtractorTest
{
    private static final String URL = "http://test.atlassian.com/confluence/blueprints/business/sharedlinks.html";
    private static final URI URI;
    static {
        try
        {
            URI = new URI(URL);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("The URL " + URL + " is malformed.", e);
        }
    }

    private MasterLinkMetaDataExtractor masterLinkMetaDataExtractor;
    @Mock private HttpRetrievalService httpRetrievalService;

    @Before
    public void setUp()
    {
        masterLinkMetaDataExtractor = new MasterLinkMetaDataExtractor(httpRetrievalService);
    }

    @Test
    public void testTitleExtraction() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html><head><title>My Awesome Title</title></head><body>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("My Awesome Title", metaData.getTitle());
    }

    @Test
    public void testTitleExtractionWithUpperCaseHtml() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<HTML><HEAD><TITLE>My Awesome Title</TITLE></HEAD><BODY>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("My Awesome Title", metaData.getTitle());
    }

    @Test
    public void testNon200Code() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 203, "text/html",
            "<html><head><title>My Awesome Title</title></head><body>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("My Awesome Title", metaData.getTitle());
    }

    @Test
    public void testTitleExtractionWithNoEndHead() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html><head><title>My Awesome Title</title><body></body></html>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("My Awesome Title", metaData.getTitle());
    }

    @Test
    public void testTitleExtractionWithNewLines() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html>\n<head>\n<title>My Awesome Title</title>\n</head>\n<body>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("My Awesome Title", metaData.getTitle());
    }

    @Test
    public void testTitleOutsideHeadIgnored() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html>\n<head></head>\n<title>My Awesome Title</title>\n<body>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertThat("My Awesome Title", not(metaData.getTitle()));
    }

    @Test
    public void testNonTextContentTypeIgnored() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "application/octet-stream",
            "<html>\n<head></head>\n<title>My Awesome Title</title>\n<body>");
        whenUrlThenResponse(URI, response);

        final LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertThat("My Awesome Title", not(metaData.getTitle()));
    }

    // CONF-31437
    @Test
    public void testGetHeadHtmlWhenCharsetIsNull() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"><title>My Awesome Title</title></head><body>");
        when(response.getCharset()).thenReturn(null);
        whenUrlThenResponse(URI, response);

        LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("Shift_JIS", metaData.getCharset());
    }

    // CONF-31437
    @Test
    public void testCharsetDefaultsToUTF8() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html><head><title>My Awesome Title</title></head><body>");
        when(response.getCharset()).thenReturn(null);
        whenUrlThenResponse(URI, response);

        LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("UTF-8", metaData.getCharset());
    }

    // CONF-31437
    @Test
    public void testInvalidMetaContentTypeTag() throws Exception
    {
        final HttpResponse response = mockResponse(URI, 200, "text/html",
            "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; otherStuff; charset=Shift_JIS\"><title>My Awesome Title</title></head><body>");
        when(response.getCharset()).thenReturn(null);
        whenUrlThenResponse(URI, response);

        LinkMetaData metaData = masterLinkMetaDataExtractor.parseMetaData(URL, false);
        assertEquals("Shift_JIS", metaData.getCharset());
    }

    private void whenUrlThenResponse(URI uri, HttpResponse response) throws IOException, URISyntaxException
    {
        final HttpRequest mockRequest = mock(HttpRequest.class);
        when(httpRetrievalService.getDefaultRequestFor(uri.toString())).thenReturn(mockRequest);
        when(httpRetrievalService.get(mockRequest)).thenReturn(response);

        final URI faviconUri = getFaviconUri(uri);
        final String faviconUrl = faviconUri.toString();
        final HttpRequest mockFaviconRequest = mock(HttpRequest.class);
        final HttpResponse mockFaviconResponse = mockResponse(faviconUri, 200, "image/icon", "");
        when(httpRetrievalService.getDefaultRequestFor(faviconUrl)).thenReturn(mockFaviconRequest);
        when(httpRetrievalService.get(mockFaviconRequest)).thenReturn(mockFaviconResponse);
    }

    private HttpResponse mockResponse(URI uri, int responseCode, String responseType, String responseText)
        throws IOException
    {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusCode()).thenReturn(responseCode);
        when(httpResponse.getResponseURI()).thenReturn(uri);
        when(httpResponse.getCharset()).thenReturn("UTF-8");
        when(httpResponse.getContentType()).thenReturn(responseType);
        when(httpResponse.getResponse()).thenReturn(new ByteArrayInputStream(responseText.getBytes()));

        return httpResponse;
    }
}
