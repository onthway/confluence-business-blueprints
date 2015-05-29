package it.com.atlassian.confluence.webdriver.pageobjects;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.utils.by.ByJquery;
import it.com.atlassian.confluence.plugins.createcontent.pageobjects.ViewPage;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.webdriver.utils.by.ByJquery.$;

public class MeetingNotesViewPage extends ViewPage
{

    public String getFirstAttendee()
    {
        ByJquery selector = $("h2:contains('Attendees') + ul > li");
        return getMainContent().find(selector).getText();
    }

    public List<String> getAttendees()
    {
        ByJquery selector = $("h2:contains('Attendees') + ul > li");
        List<PageElement> attendees = getMainContent().findAll(selector);
        List<String> attendeesNames = new ArrayList<String>();
        for (PageElement attendee:attendees)
        {
            attendeesNames.add(attendee.getText());
        }
        return attendeesNames;
    }

}