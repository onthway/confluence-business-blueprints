package com.atlassian.confluence.plugins.sharelinks.widgetconnector;

public interface WidgetConnectorSupport {

    /**
     * determine if widget connector supports video domain
     * @param domain domain of provided url from user
     * @return
     */
    public boolean isSupported(String domain);

}
