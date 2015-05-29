package it.com.atlassian.confluence.webdriver.pageobjects;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Multiuser select component that can be used to search and select multiple users.
 * @deprecated as of Create Content Plugin release 2.0.2, replaced by {@Link MultiUserPicker}
 */
// TODO https://jira.atlassian.com/browse/CONFDEV-21871: remove this class, using Create Content Plugin test support
@Deprecated
public class MultiUserPicker extends ConfluenceAbstractPageComponent
{
    @ElementBy(cssSelector="#create-dialog .dialog-components:not([style*=\"display: none\"]) .select2-input")
    private PageElement searchField;

    @ElementBy(cssSelector=".users-dropdown .select2-results")
    private PageElement results;

    @WaitUntil
    public void inputVisible()
    {
        waitUntilTrue(searchField.timed().isVisible());
    }

    public MultiUserPicker search(String searchStr)
    {
        searchField.type(searchStr);
        waitUntilTrue(results.timed().isVisible());
        return this;
    }

    public MultiUserPicker waitForResult(User user)
    {
        waitUntilTrue(results.find(byUsername(user.getUsername())).timed().isVisible());
        return this;
    }

    public MultiUserPicker selectUser(String username)
    {
        waitUntilTrue(results.find(byUsername(username)).timed().isVisible());
        results.find(byUsername(username)).click();
        return this;
    }

    private By byUsername(String username)
    {
        // TODO remove the <div> version when this plugin no longer runs tests against 5.4
        return By.cssSelector("span[data-username='" + username + "'], div[data-username='" + username + "']");
    }

    public boolean containsUserWithFullName(String fullName)
    {
        List<PageElement> lis = results.findAll(By.tagName("li"));
        for (PageElement li : lis)
        {
            if (fullName.equals(li.getText()))
                return true;
        }
        return false;
    }
}
