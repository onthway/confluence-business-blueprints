package it.com.atlassian.confluence.webdriver.pageobjects;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

import org.openqa.selenium.By;

import com.atlassian.confluence.pageobjects.component.ConfluenceAbstractPageComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import java.util.List;

public class LabelPicker extends ConfluenceAbstractPageComponent
{
    @ElementBy(cssSelector = "div.labels-autocomplete input.select2-input")
    private PageElement labelField;

    @ElementBy(cssSelector = ".labels-dropdown .select2-results")
    private PageElement labelResults;

    private String dropdownItemsClass = "select2-result-label";

    public LabelPicker inputLabel(String labelName)
    {
        labelField.type(labelName);
        waitUntilTrue(labelResults.timed().isVisible());
        return this;
    }

    public LabelPicker selectLabel(String labelName)
    {
        String newLabel = "\"" + labelName + "\" - add a new topic";

        waitUntilTrue(labelResults.find(By.className(dropdownItemsClass)).timed().isVisible());

        List<PageElement> all = labelResults.findAll(By.className(dropdownItemsClass));
        for (PageElement pageElement : all)
        {
            String text = pageElement.getText();
            if (labelName.equals(text) || newLabel.equals(text))
            {
                pageElement.click();
            }
        }

        return this;
    }

    public PageElement getLabelResultsField()
    {
        return labelResults;
    }
}