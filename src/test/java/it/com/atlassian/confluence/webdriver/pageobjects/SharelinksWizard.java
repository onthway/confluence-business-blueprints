package it.com.atlassian.confluence.webdriver.pageobjects;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;

import it.com.atlassian.confluence.plugins.createcontent.pageobjects.ViewPage;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;

public class SharelinksWizard extends ConfluenceAbstractPageComponent
{
    @ElementBy(id = "sharelinks-url")
    private PageElement urlField;

    @ElementBy(id = "sharelinks-title")
    private PageElement titleField;

    @ElementBy(id = "sharelinks-comment")
    private PageElement commentField;

    @ElementBy(xpath = "(//*[contains(concat(' ', @class, ' '), 'create-dialog-create-button')])[last()]")
    private PageElement submit;

    @ElementBy(cssSelector = ".sharelinks-urlmacro-button")
    private PageElement bookmarklet;

    @ElementBy(cssSelector = ".sharelinks-preview")
    private PageElement linkPreview;

    private final String TITLE_SELECTOR = "#create-dialog h3.sharelinks-preview-title";

    public ViewPage submit()
    {
        submit.click();
        Poller.waitUntilFalse(submit.timed().isVisible());
        return pageBinder.bind(ViewPage.class);
    }

    public SharelinksWizard clickSubmit()
    {
        submit.click();
        return this;
    }

    public SharelinksWizard setUrl(String url)
    {
        urlField.clear().type(url);
        //manually trigger change event since webdriver sendkeys only sometimes fires onchange event, even when elem is unfocused.
        urlField.javascript().execute("jQuery(arguments[0]).trigger(\"change\")");
        return this;
    }

    public SharelinksWizard setTitle(String title)
    {
        waitUntilTitleFieldIsEnabled();
        titleField.clear().type(title);
        return this;
    }

    public String getTitle()
    {
        return titleField.getValue();
    }

    public SharelinksWizard setComment(String comment)
    {
        Poller.waitUntilTrue(commentField.timed().isEnabled());
        commentField.clear().type(comment);
        return this;
    }

    public SharelinksWizard waitForCommentFieldToBeDisabled()
    {
        Poller.waitUntilFalse(commentField.timed().isEnabled());
        return this;
    }


    public String getURLValidationError()
    {
        WebElement errorDiv = driver.findElement(By.cssSelector("#sharelinks-url + .error"));
        return errorDiv.getText();
    }

    public String getTitleValidationError()
    {
        WebElement errorDiv = driver.findElement(By.cssSelector("#sharelinks-title + .error"));
        return errorDiv.getText();
    }

    public void assertWizardIsShown()
    {
        Assert.assertTrue(urlField.isVisible());
    }

    public String getPreviewTitle()
    {
        waitForPreviewLoaded();
        WebElement previewTitle = driver.findElement(By.cssSelector(TITLE_SELECTOR));
        return previewTitle.getText();
    }

    public SharelinksWizard validateLinkPreviewRendered()
    {
        waitForPreviewLoaded();
        linkPreview.isPresent();
        return this;
    }

    public SharelinksWizard waitForPreviewLoaded()
    {
        Poller.waitUntilTrue(pageElementFinder.find(By.cssSelector(TITLE_SELECTOR)).timed().isVisible());
        return this;
    }

    private SharelinksWizard waitUntilTitleFieldIsEnabled()
    {
        Poller.waitUntilTrue(titleField.timed().isEnabled());
        return this;
    }

    public void pasteUrl(String pasteValue)
    {
        // paste event is not triggered reliably when actually pasting in a url,
        // mimic user pasting instead by typing in the value and manually fire
        // a paste event.
        urlField.type(pasteValue);
        urlField.javascript().execute("jQuery(arguments[0]).trigger(\"paste\")");
    }

    public SharelinksWizard addUserToShareWith(User user)
    {
        Picker userPicker = pageBinder.bind(Picker.class);
        userPicker.bindingElements("sharelinks-sharewith");
        userPicker.search(user.getUsername()).selectUser(user.getUsername());
        return this;
    }

    public LabelPicker inputLabel(String labelName)
    {
        LabelPicker labelPicker = getLabelPicker();
        labelPicker.inputLabel(labelName);
        return labelPicker;
    }

    public LabelPicker addLabel(String labelName)
    {
        LabelPicker labelPicker = inputLabel(labelName);
        labelPicker.selectLabel(labelName);
        return labelPicker;
    }

    public LabelPicker getLabelPicker()
    {
        return pageBinder.bind(LabelPicker.class);
    }

    public SharelinksWizard validateBookmarkletPresent()
    {
        Assert.assertTrue(bookmarklet.isPresent());
        return this;
    }
}