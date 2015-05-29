package it.com.atlassian.confluence.webdriver.pageobjects;

import org.openqa.selenium.By;

import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;

public class BlankIndexPage extends ConfluenceAbstractPageComponent
{
    public <T> T clickOnCreateButtonAndExpectWizard(Class<T> clazz)
    {
        String buttonSelector = ".blueprint-blank-experience .create-from-template-button";
        pageElementFinder.find(By.cssSelector(buttonSelector)).click();
        return pageBinder.bind(clazz);
    }
}
