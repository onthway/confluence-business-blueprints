package com.atlassian.confluence.plugins.sharelinks.metaextractor;

import com.atlassian.confluence.plugins.sharelinks.DOMMetadataExtractor;
import com.atlassian.confluence.plugins.sharelinks.LinkMetaData;
import com.atlassian.confluence.plugins.sharelinks.LinkMetaDataExtractor;
import com.atlassian.confluence.util.http.HttpRequest;
import com.atlassian.confluence.util.http.HttpResponse;
import com.atlassian.confluence.util.http.HttpRetrievalService;
import com.google.common.collect.ImmutableList;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

public class MasterLinkMetaDataExtractor implements LinkMetaDataExtractor
{
    private static final Pattern UNTIL_END_HEAD_OR_EOF_PATTERN = Pattern.compile(".*?</head>|.*", CASE_INSENSITIVE | DOTALL);
    private static final int MAX_HEAD_SIZE = 128 * 1024; // 128K chars ought to be enough for anybody!
    private static final int DESCRIPTION_MAX_LENGTH = 180;
    private static final int DOMAIN_MAX_LENGTH = 50;
    private static final int EXCERPT_URL_MAX_LENGTH = 30;

    private static final Logger log = Logger.getLogger(MasterLinkMetaDataExtractor.class);

    private final List<DOMMetadataExtractor> metadataExtractors;
    private final HttpRetrievalService httpRetrievalService;

    public MasterLinkMetaDataExtractor(HttpRetrievalService httpRetrievalService)
    {
        this.httpRetrievalService = httpRetrievalService;
        metadataExtractors = ImmutableList.of(
                new OpenGraphDOMMetadataExtractor(),
                new TwitterDOMMetadataExtractor(),
                new SimpleDOMMetadataExtractor(httpRetrievalService)
                );
    }

    @Override
    public LinkMetaData parseMetaData(String url, boolean isPreview) throws URISyntaxException
    {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            url = "http://" + url;
        }

        LinkMetaData meta = new LinkMetaData(url);
        meta.setExcerptedURL(getExcerptedUrl(url));

        //invalid URIs return early
        URI uri = new URI(url);
        String domain = StringUtils.isBlank(uri.getHost()) ? url : uri.getHost();
        meta.setDomain(getPreviewText(domain, DOMAIN_MAX_LENGTH));

        String htmlData = getHeadHtmlData(url, meta);
        Document jsoupDoc = Jsoup.parse(htmlData);
        if (!htmlData.isEmpty())
        {
            for (DOMMetadataExtractor metadataExtractor : metadataExtractors)
            {
                metadataExtractor.updateMetadata(meta, jsoupDoc);
            }
        }

        // cut the description for preview panel
        if (isPreview)
        {
            meta.setDescription(getPreviewText(meta.getDescription(), DESCRIPTION_MAX_LENGTH));
        }

        return meta;
    }

    /**
     * Extract everything from the start of the HTML document until the head tag is closed, or {@link #MAX_HEAD_SIZE}
     * is reached.
     * 
     * @param url full url to a web page
     * @return A chunk of HTML which is the head of the document
     */
    private String getHeadHtmlData(String url, LinkMetaData meta)
    {
        InputStream inputStream = null;
        try
        {
            final HttpRequest request = httpRetrievalService.getDefaultRequestFor(url);
            request.setHeader("accept-charset", "utf-8"); //
            final HttpResponse response = httpRetrievalService.get(request);
            meta.setResponseHost(getResponseURI(response, url));
            
            if (!isValidResponse(response))
                return "";

            inputStream = response.getResponse();
            String charset = response.getCharset();
            if (charset == null)
            {
                inputStream.mark(Integer.MAX_VALUE);
                final Scanner bodyScanner = new Scanner(inputStream, "UTF-8");
                String attempt = bodyScanner.findWithinHorizon(UNTIL_END_HEAD_OR_EOF_PATTERN, MAX_HEAD_SIZE);
                Document jsoupDoc = Jsoup.parse(attempt);
                String contentType = jsoupDoc.select("meta[http-equiv=Content-Type][content]").attr("content");
                HeaderElement[] contents = HeaderElement.parseElements(contentType);
                for (HeaderElement headerElement : contents)
                {
                    NameValuePair charsetparam = headerElement.getParameterByName("charset");
                    if (charsetparam != null)
                    {
                        charset = charsetparam.getValue();
                        break;
                    }
                }
                inputStream.reset();
            }

            meta.setCharset(charset == null ? "UTF-8" : charset);
            final Scanner responseScanner = new Scanner(inputStream, meta.getCharset());
            return responseScanner.findWithinHorizon(UNTIL_END_HEAD_OR_EOF_PATTERN, MAX_HEAD_SIZE);
        }
        catch (IOException e)
        {
            log.error("Error with io exception: ", e);
        }
        catch (Exception e)
        {
            log.error("Error in parse data: ", e);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }

        return "";
    }

    /**
     * Return the full URI where the response actually comes from.
     * This may be different from the original URI because of redirects.
     *
     * @param response the response got by fetching the URL and following redirects
     * @param url the original URL
     */
    private URI getResponseURI(HttpResponse response, String url) throws URISyntaxException {
        URI responseURI = response.getResponseURI();
        if (responseURI.getHost() == null)
        {
            responseURI = new URI(url);
        }
        return responseURI;
    }

    /**
     * Return the text that is cut at word boundaries and doesn't exceed max
     * length
     *
     * @param text
     * @param maxLength
     * @return String
     */
    private static String getPreviewText(String text, int maxLength)
    {
        if (text == null || text.length() <= maxLength)
        {
            return text;
        }

        text = text.substring(0, maxLength);

        int lastSpaceIndex = text.lastIndexOf(' ');
        if (lastSpaceIndex != -1)
        {
            text = text.substring(0, lastSpaceIndex);
        }

        final char ELLIPSIS = '\u2026';
        return text + ELLIPSIS;
    }

    private String getExcerptedUrl(String sourceUrl)
    {
        String excerptedUrl = sourceUrl;
        // Remove the http/https in the source url
        String split = "//";
        int splitIndex = excerptedUrl.indexOf(split);
        excerptedUrl = excerptedUrl.substring(splitIndex + split.length());
        if (excerptedUrl.length() > EXCERPT_URL_MAX_LENGTH)
        {
            excerptedUrl = excerptedUrl.substring(0, EXCERPT_URL_MAX_LENGTH -1);
            excerptedUrl = excerptedUrl + '\u2026';
        }
        return excerptedUrl;
    }

    /**
     * Ensures the response was successful and the content type is valid if set.
     * @param response the HTTP response
     * @return true if the response is valid for DOM extraction, false otherwise.
     */
    private boolean isValidResponse(HttpResponse response)
    {
        final int statusCode = response.getStatusCode();
        final String mimeType = response.getMIMEType();
        return statusCode >= 200 && statusCode < 300 && (mimeType == null || mimeType.startsWith("text/"));
    }
}
