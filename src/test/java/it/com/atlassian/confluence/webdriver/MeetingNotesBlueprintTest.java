package it.com.atlassian.confluence.webdriver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import it.com.atlassian.confluence.plugins.createcontent.BlueprintWebDriverTestHelper;
import it.com.atlassian.confluence.plugins.createcontent.pageobjects.HowTo;
import it.com.atlassian.confluence.webdriver.pageobjects.MeetingNotesViewPage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the "Meeting Notes" Blueprint.
 */
public class MeetingNotesBlueprintTest extends AbstractWebDriverTest
{
    private static final String PLUGIN_KEY = "com.atlassian.confluence.plugins.confluence-business-blueprints";
    private static final String MODULE_KEY = PLUGIN_KEY + ":meeting-notes-item";

    private BlueprintWebDriverTestHelper helper;

    @Before
    public void setUp() throws Exception
    {
        helper = new BlueprintWebDriverTestHelper(rpc, product, User.TEST, MODULE_KEY);
        rpc.enableWebSudo(false);
        // Need to set the locale explicitly because this test checks date formats
        rpc.setUserLocale(User.TEST, new Locale("en", "AU"));
    }

    @Test
    public void newMeetingNotesPageAppearOnIndexPage() throws Exception
    {
        final HowTo howTo = helper.loginAndChooseBlueprint(HowTo.class);
        final CreatePage editor = howTo.clickNext(CreatePage.class);

        final String expectedPageTitle = defaultMeetingNotesTitleForToday();
        assertThat(editor.getTitle(), is(expectedPageTitle));

        helper.saveEditorAndCheckIndexPageContent(editor, Space.TEST, "Meeting notes");
    }

    // CONFDEV-15858
    @Test
    public void createMeetingNotesFromAdminPage() throws Exception
    {
        ConfluenceAdminHomePage adminPage = product.login(User.ADMIN, ConfluenceAdminHomePage.class);
        HowTo howTo = helper.openCreateDialogAndChooseBlueprintFromPage(adminPage, HowTo.class);
        CreatePage editor = howTo.clickNext(CreatePage.class);

        final String expectedPageTitle = defaultMeetingNotesTitleForToday();
        assertThat(editor.getTitle(), is(expectedPageTitle));

        helper.saveEditorAndCheckIndexPageContent(editor, Space.TEST,  "Meeting notes");
    }

    // CONFDEV-15795
    @Test
    public void loggedInUserAppearsAsFirstAttendee() throws Exception
    {
        ConfluenceAdminHomePage adminPage = product.login(User.ADMIN, ConfluenceAdminHomePage.class);
        HowTo howTo = helper.openCreateDialogAndChooseBlueprintFromPage(adminPage, HowTo.class);
        howTo.clickNext(CreatePage.class).save();
        MeetingNotesViewPage meetingNotesPage = product.getPageBinder().bind(MeetingNotesViewPage.class);

        assertEquals(User.ADMIN.getUsername(), meetingNotesPage.getFirstAttendee());
    }

    // CONFDEV-15795
    @Test
    // TODO: Enable test when CONFDEV-33959 is fixed
    @Ignore
    public void anonymousUserDoesNotAppearsAsFirstAttendee() throws Exception
    {
        rpc.logIn(User.ADMIN);
        rpc.grantAnonymousUsePermission();
        rpc.grantPermission(SpacePermission.VIEW, Space.TEST, User.ANONYMOUS);
        rpc.grantPermission(SpacePermission.PAGE_EDIT, Space.TEST, User.ANONYMOUS);
        rpc.logOut();

        DashboardPage dashboardPage = product.visit(DashboardPage.class);
        assertFalse(dashboardPage.getHeader().isLoggedIn());

        HowTo howTo = helper.openCreateDialogAndChooseBlueprintFromPage(dashboardPage, HowTo.class);
        howTo.clickNext(CreatePage.class).save();
        MeetingNotesViewPage meetingNotesPage = product.getPageBinder().bind(MeetingNotesViewPage.class);

        assertTrue(meetingNotesPage.getFirstAttendee().isEmpty());
    }

    //CONFDEV-23720
    @Test
    public void newMeetingNotesHasDateLoz() throws Exception
    {
        final HowTo howTo = helper.loginAndChooseBlueprint(HowTo.class);
        final CreatePage editor = howTo.clickNext(CreatePage.class);
        assertTrue(editor.hasHtmlContent("</time>"));
        assertTrue(editor.hasHtmlContent("datetime=\"" + getTodayStr() + "\""));
    }

    private String defaultMeetingNotesTitleForToday()
    {
        return getTodayStr() + " Meeting notes";
    }

    private String getTodayStr()
    {
        final Locale locale = rpc.getUserLocale(User.TEST);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", locale);
        return dateFormat.format(new Date());
    }
}