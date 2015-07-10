package it.com.atlassian.confluence.webdriver.pageobjects;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Panel(s) in the CreateDialog to collect context data for the File List blueprint.
 *
 */
public class FileListWizard extends ConfluenceAbstractPageComponent
{
    @ElementBy(id = "file-list-page-title")
    private PageElement titleField;

    @ElementBy(id = "file-list-page-description")
    private PageElement descriptionField;

    @ElementBy(xpath = "(//*[contains(concat(' ', @class, ' '), 'create-dialog-create-button')])[last()]")
    private PageElement submit;

    public FileListWizard setTitle(String title)
    {
        titleField.clear();
        titleField.type(title);
        return this;
    }

    public FileListWizard setDescription(String description)
    {
        descriptionField.clear();
        descriptionField.type(description);
        return this;
    }

    public MultiUserPicker searchUserRestriction(User user)
    {
        MultiUserPicker restrictedUserPicker = pageBinder.bind(MultiUserPicker.class);
        restrictedUserPicker.search(user.getUsername());

        return restrictedUserPicker;
    }

    public FileListWizard addUserRestriction(User user)
    {
        MultiUserPicker restrictedUserPicker = searchUserRestriction(user);
        restrictedUserPicker.selectUser(user.getUsername());
        return this;
    }

    public ViewPage submit()
    {
        submit.click();
        Poller.waitUntilFalse(submit.timed().isVisible());
        return pageBinder.bind(ViewPage.class);
    }

    public CreatePage submitAndExpectEditor()
    {
        submit.click();
        return pageBinder.bind(CreatePage.class);
    }

    public String getTitleError()
    {
        WebElement errorDiv = driver.findElement(By.cssSelector("#file-list-page-title + .error"));
        return errorDiv.getText();
    }

    public FileListWizard submitAndExpectWizardError()
    {
        // just click submit - we can check for errors in the calling code.
        submit.click();
        return this;
    }
}
