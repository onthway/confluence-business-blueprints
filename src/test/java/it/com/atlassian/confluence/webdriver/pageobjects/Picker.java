package it.com.atlassian.confluence.webdriver.pageobjects;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

import org.openqa.selenium.By;

import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

public class Picker extends ConfluenceAbstractPageComponent
{
    @ElementBy(id = "create-dialog")
    protected PageElement createDialog;

    @ElementBy(id = "select2-drop")
    protected PageElement dropdownResults;

    private PageElement select2Element;
    private PageElement inputField;

    public void bindingElements(String pickerId)
    {
        select2Element = createDialog.find(By.id("s2id_" + pickerId));
        inputField = select2Element.find(By.className("select2-input"));
    }

    public Picker search(String searchText)
    {
        inputField.type(searchText);
        waitUntilTrue(dropdownResults.timed().isVisible());
        return this;
    }

    public Picker selectUser(String username)
    {
        waitUntilTrue(dropdownResults.find(byUsername(username)).timed().isVisible());
        dropdownResults.find(byUsername(username)).click();
        return this;
    }

    private By byUsername(String username)
    {
        return By.cssSelector("span[data-username='" + username + "'], div[data-username='" + username + "']");
    }
}
