package it.com.atlassian.confluence.webdriver.pageobjects;

import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

public class BookmarkletSpacePicker extends ConfluenceAbstractPageComponent
{
    @ElementBy(cssSelector = ".select2-with-searchbox input.select2-input")
    private PageElement spaceField;

    @ElementBy(cssSelector = ".select2-results .select2-result-sub")
    private PageElement spaceResults;

    @ElementBy(cssSelector = "div.bookmarklet-space-container .select2-choice")
    private PageElement spaceSelect;

    @ElementBy(cssSelector = "div.bookmarklet-space-container .select2-choice span")
    private PageElement spaceSelected;

    /**
     * This is the selector before the upgrade to select 3.4.1. We can remove this once we bump the version of confluence
     * in this plugin and the plugin in confluencE :)
     */
    @Deprecated
    @ElementBy(cssSelector = "div.bookmarklet-space-container .aui-select2-choice")
    private PageElement oldSpaceSelect;

    /**
     * This is the selector before the upgrade to select 3.4.1. We can remove this once we bump the version of confluence
     * in this plugin and the plugin in confluencE :)
     */
    @Deprecated
    @ElementBy(cssSelector = "div.bookmarklet-space-container .aui-select2-choice span")
    private PageElement oldSpaceSelected;

    public BookmarkletSpacePicker inputSpace(String spaceName)
    {
        if (oldSpaceSelect.isPresent())
            oldSpaceSelect.click();
        else
            spaceSelect.click();

        spaceField.type(spaceName);
        return this;
    }

    public BookmarkletSpacePicker selectSpace(String spaceName)
    {
        PageElement element = spaceResults.find(bySpaceName(spaceName));
        Poller.waitUntilTrue(element.timed().isPresent());
        element.click();
        return this;
    }

    public String getSelectedSpace()
    {
        if (oldSpaceSelected.isPresent())
            return oldSpaceSelected.getText();

        return spaceSelected.getText();
    }

    private By bySpaceName(String spaceName)
    {
        return By.cssSelector(".select2-highlighted");
    }
}
