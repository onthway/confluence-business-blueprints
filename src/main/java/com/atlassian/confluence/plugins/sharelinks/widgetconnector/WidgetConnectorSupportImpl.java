package com.atlassian.confluence.plugins.sharelinks.widgetconnector;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class WidgetConnectorSupportImpl implements WidgetConnectorSupport{

    private final Set<String> supportedDomains;
    
    public WidgetConnectorSupportImpl() {
        supportedDomains = ImmutableSet.of("www.youtube.com", 
                                           "vids.myspace.com", 
                                           "video.yahoo.com", 
                                           "www.dailymotion.com",
                                           "app.episodic.com",
                                           "www.vimeo.com",
                                           "www.metacafe.com",
                                           "blip.tv",
                                           "www.viddler.com",
                                            "twitter.com");
    }
    
    @Override
    public boolean isSupported(String domain) {
        return supportedDomains.contains(domain);
    }
}
