package it.com.atlassian.confluence.webdriver.pageobjects;

import static com.atlassian.webdriver.utils.by.ByJquery.$;

import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.utils.by.ByJquery;

public class CustomHtmlPage extends ConfluenceAbstractPageComponent
{
    // The button confirm for submit the Custom Html form
    @ElementBy(id = "confirm")
    private PageElement confirmCustomHtml;

    /**
     * Using Confluence Admin - Custom HTML - Input custom html into At end of the head textarea
     * @param customHeadHtml the custom head html 
     * @return CustomHtmlWizard 
     */
    public CustomHtmlPage setCustomHeadHtml(String customHeadHtml)
    {
        // find At the end of the head textarea for input custom head html
        ByJquery jQuerySelector = $(":input[name='beforeHeadEnd']");
        PageElement endHeadTextArea = pageElementFinder.find(jQuerySelector);
        endHeadTextArea.clear().type(customHeadHtml);
        return this;
    }
    
    public CustomHtmlPage clickConfirmCustomHtml()
    {
        confirmCustomHtml.click();
        return this;
    }
}
