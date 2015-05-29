package it.com.atlassian.confluence.webdriver.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.atlassian.confluence.pageobjects.page.ConfluenceAbstractPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;

public class SharelinksBookmarkletPage extends ConfluenceAbstractPage
{
    @ElementBy(id = "bookmarklet-url")
    private PageElement urlField;

    @ElementBy(id = "bookmarklet-title")
    private PageElement titleField;

    @ElementBy(id = "bookmarklet-submit")
    private PageElement submit;
    
    @ElementBy(id = "bookmarklet-comment")
    private PageElement commentField;

    @ElementBy(cssSelector = ".bookmarklet-section .aui-message")
    private PageElement message;

    @Override
    public String getUrl()
    {
        return "/plugins/sharelinksbookmarklet/bookmarklet.action";
    }

    public String getMessageLink()
    {
        return message.find(By.cssSelector("a")).getAttribute("href");
    }

    public boolean isMessageShown()
    {
        return message.isPresent();
    }

    public boolean isFormShown()
    {
        return urlField.isPresent();
    }

    public SharelinksBookmarkletPage submit()
    {
        submit.click();
        return this;
    }

    public SharelinksBookmarkletPage setUrl(String url)
    {
        urlField.clear().type(url);
        urlField.javascript().execute("jQuery(arguments[0]).trigger(\"change\")");
        return this;
    }

    public SharelinksBookmarkletPage setTitle(String title)
    {
        titleField.clear().type(title);
        return this;
    }

    public String getURLValidationError()
    {
        WebElement errorDiv = driver.findElement(By.cssSelector("#bookmarklet-url ~ .error"));
        return errorDiv.getText();
    }

    public String getTitleValidationError()
    {
        WebElement errorDiv = driver.findElement(By.cssSelector("#bookmarklet-title + .error"));
        return errorDiv.getText();
    }

    public SharelinksBookmarkletPage waitForCreatePageResultLoaded()
    {
        Poller.waitUntilTrue(pageElementFinder.find(By.cssSelector("div.bookmarklet-result-text a")).timed()
                .isVisible());
        return this;
    }

    public String getCreatedPageId()
    {
        waitForCreatePageResultLoaded();
        WebElement createdPageLink = driver.findElement(By.cssSelector("div.bookmarklet-result-text a"));
        String linkHref = createdPageLink.getAttribute("href");
        String pageIdSearch = "pageId=";
        int pageIdIndex = linkHref.indexOf(pageIdSearch);
        String pageId = linkHref.substring(pageIdIndex + pageIdSearch.length());
        return pageId;
    }

    public SharelinksBookmarkletPage selectSpace(String spaceName)
    {
        BookmarkletSpacePicker spacePicker = pageBinder.bind(BookmarkletSpacePicker.class);
        spacePicker.inputSpace(spaceName);
        spacePicker.selectSpace(spaceName);
        return this;
    }

    public String getSelectedSpace()
    {
        BookmarkletSpacePicker spacePicker = pageBinder.bind(BookmarkletSpacePicker.class);
        return spacePicker.getSelectedSpace();
    }
    
    public SharelinksBookmarkletPage pasteUrl(String pasteValue)
    {
        urlField.type(pasteValue);
        urlField.javascript().execute("jQuery(arguments[0]).trigger(\"paste\")");
        return this;
    }
    
    public String getLoadedTitleForUrl(String title)
    {
        waitUntilTitleIsLoadedForUrl(title);
        return titleField.getValue();
    }

    public SharelinksBookmarkletPage waitUntilTitleIsLoadedForUrl(String title) {
        Poller.waitUntilEquals("Title field value should change", title, titleField.timed().getValue());
        return this;
    }
    
    public SharelinksBookmarkletPage setComment(String comment)
    {
        Poller.waitUntilTrue(commentField.timed().isEnabled());
        commentField.clear().type(comment);
        return this;
    }
    
    public SharelinksBookmarkletPage waitForDisabledCommentInput()
    {
        Poller.waitUntilFalse(commentField.timed().isEnabled());
        return this;
    }
    
    public boolean isCommentFieldEnabled()
    {
        return commentField.isEnabled();
    }
    
    public SharelinksBookmarkletPage addLabel(String labelName)
    {
        LabelPicker labelPicker = pageBinder.bind(LabelPicker.class);
        labelPicker.inputLabel(labelName);
        labelPicker.selectLabel(labelName);
        return this;
    }
}
