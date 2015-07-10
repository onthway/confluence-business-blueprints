package it.com.atlassian.confluence.webdriver.pageobjects;

import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class DecisionsWizard extends ConfluenceAbstractPageComponent
{
    @ElementBy(id = "decisions-page-title")
    private PageElement decision;

    @ElementBy(xpath = "(//*[contains(concat(' ', @class, ' '), 'create-dialog-create-button')])[last()]")
    private PageElement submit;

    @ElementBy(id = "decisions-final-decision")
    private PageElement outcome;

    @ElementBy(id = "decisions-background")
    private PageElement backgroundField;

    @ElementBy(id = "decisions-due-date")
    private PageElement dueDate;

    private Select statusField;

    public DecisionsWizard setDecision(String title)
    {
        decision.clear();
        decision.type(title);
        return this;
    }

    public DecisionsWizard setOutcome(String decision)
    {
        outcome.clear();
        outcome.type(decision);
        return this;
    }

    public CreatePage submit()
    {
        submit.click();
        return pageBinder.bind(CreatePage.class);
    }

    public String getTitleError()
    {
        WebElement errorDiv = driver.findElement(By.cssSelector("#decisions-page-title + .error"));
        return errorDiv.getText();
    }

    public DecisionsWizard submitAndExpectWizardError()
    {
        // just click submit - we can check for errors in the calling code.
        submit.click();
        return this;
    }

    public DecisionsWizard setStatus(String status)
    {
        statusField = new Select(driver.findElement(By.id("decisions-status")));
        statusField.selectByVisibleText(status);
        return this;
    }

    public boolean isOutcomeVisible()
    {
        return outcome.isVisible();
    }

    public DecisionsWizard setBackground(String background)
    {
        backgroundField.clear();
        backgroundField.type(background);
        return this;
    }

    public boolean isShown()
    {
        return decision.isVisible();
    }

    public DecisionsWizard setDate(String date)
    {
        dueDate.clear();
        dueDate.type(date);
        return this;
    }
}
