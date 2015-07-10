package com.atlassian.confluence.plugins.sharelinks;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
public class LinkMetaData
{
    @XmlElement
    private final String sourceURL;
    
    @XmlElement
    private String excerptedURL;

    @XmlElement
    private String title;

    @XmlElement
    private String description;

    @XmlElement
    private String imageURL;
    
    @XmlElement
    private String videoURL;

    @XmlElement
    private String faviconURL;

    // extract domain from sourceURL for display ( sourceURL is too long )
    @XmlElement
    private String domain;

    // The host that the redirect points to if there is a redirect
    private URI responseHost;

    private String charset;

    public LinkMetaData(String url)
    {
        sourceURL = url;
    }

    public String getSourceURL()
    {
        return sourceURL;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getImageURL()
    {
        return imageURL;
    }

    public void setImageURL(String imageURL)
    {
        this.imageURL = imageURL;
    }

    public String getVideoURL()
    {
        return videoURL;
    }

    public void setVideoURL(String videoURL)
    {
        this.videoURL = videoURL;
    }

    public String getFaviconURL()
    {
        return faviconURL;
    }

    public void setFaviconURL(String faviconURL)
    {
        this.faviconURL = faviconURL;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public URI getResponseHost()
    {
        return responseHost;
    }

    public void setResponseHost(URI responseHost)
    {
        this.responseHost = responseHost;
    }

    public String getExcerptedURL()
    {
        return excerptedURL;
    }

    public void setExcerptedURL(String excerptedURL)
    {
        this.excerptedURL = excerptedURL;
    }

    public String getCharset()
    {
        return charset;
    }
    public void setCharset(String charset)
    {
        this.charset = charset;
    }
}
