package it.com.atlassian.confluence.webdriver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.com.atlassian.confluence.webdriver.pageobjects.SharelinksBookmarkletPage;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.page.content.CommentsSection;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;
import com.atlassian.confluence.webdriver.WebDriverConfiguration;

public class SharelinksBookmarkletTest extends AbstractWebDriverTest
{
    private static final String BASE_URL = WebDriverConfiguration.getBaseUrl();
    private static final String TEST_LOGIN_URL = BASE_URL + "/login.action";
    private static final String TEST_LOGIN_URL_TITLE = "Log In - Confluence";
    private static final String SHARELINKS_PAGE_LABEL = "shared-links";
    private static final String SPACE_TEST_NAME = "Bookmarklet Test";
    private static final String SPACE_TEST_KEY = "bookmarkletspacekey";

    @Before
    public void setUp() throws Exception
    {
        createTestSpace();
    }

    @After
    public void tearDown()
    {
        deleteTestSpace();
        product.getTester().getDriver().executeScript("localStorage && localStorage.clear();");
    }

    @Test
    public void testValidationRequired()
    {
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        bookmarkletPage.submit();

        assertThat(bookmarkletPage.getURLValidationError(), is("URL is required"));
        assertThat(bookmarkletPage.getTitleValidationError(), is("Title is required"));
    }

    @Test
    public void testCreateDuplicatePageValidate()
    {
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        
        // Create page
        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                        .selectSpace(SPACE_TEST_NAME)
                        .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                        .submit();

        bookmarkletPage.waitForCreatePageResultLoaded();

        // Create the same page
        bookmarkletPage = product.visit(SharelinksBookmarkletPage.class);
        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                        .selectSpace(SPACE_TEST_NAME)
                        .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                        .submit();
        // Check duplicate validate error
        assertThat(bookmarkletPage.getTitleValidationError(), is("A page with this name already exists"));
    }

    @Test
    public void testCreateSharelinksPage()
    {
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        assertTrue(!bookmarkletPage.isMessageShown());
        
        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                        .selectSpace(SPACE_TEST_NAME)
                        .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                        .submit();

        String createdPageId = bookmarkletPage.getCreatedPageId();

        ViewPage viewPage = product.viewPage(createdPageId);
        // check title of page
        assertThat(viewPage.getTitle(), is(TEST_LOGIN_URL_TITLE));
        // check sharelinks label of page
        assertTrue(viewPage.getLabels().contains(SHARELINKS_PAGE_LABEL));
    }

    @Test
    public void testMessageForAnonymousWithPermission()
    {
        rpc.grantAnonymousUsePermission();
        rpc.grantAnonymousPermission(SpacePermission.PAGE_EDIT, Space.TEST);
        SharelinksBookmarkletPage bookmarkletPage = product.visit(SharelinksBookmarkletPage.class);
        assertThat(bookmarkletPage.getMessageLink(), CoreMatchers.containsString("login.action?os_destination="));
        assertTrue(bookmarkletPage.isFormShown());

    }

    // CONFDEV-18440: Shouldn't show the warning message if user has already finished sharing links
    @Test
    public void testNotShowWarningMessageForAnonymousAfterSuccessfullySharingALink()
    {
        rpc.grantAnonymousUsePermission();
        rpc.grantAnonymousPermission(SpacePermission.PAGE_EDIT, new Space(SPACE_TEST_KEY, SPACE_TEST_NAME));
        SharelinksBookmarkletPage bookmarkletPage = product.visit(SharelinksBookmarkletPage.class);

        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                       .selectSpace(SPACE_TEST_NAME)
                       .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                       .submit();

        bookmarkletPage.waitForCreatePageResultLoaded();

        assertTrue(!bookmarkletPage.isMessageShown());
    }

    @Test
    public void testMessageForAnonymousWithoutPermission()
    {
        rpc.grantAnonymousUsePermission();
        SharelinksBookmarkletPage bookmarkletPage = product.visit(SharelinksBookmarkletPage.class);
        assertThat(bookmarkletPage.getMessageLink(), CoreMatchers.containsString("login.action?os_destination="));
        assertTrue(!bookmarkletPage.isFormShown());
    }

    @Test
    public void testMessageForUserWithoutPermission()
    {
        rpc.revokePermission(SpacePermission.PAGE_EDIT, Space.TEST, User.TEST);
        //TODO: when the space SPACE_TEST_KEY isn't created in setUp(), this line isn't required
        rpc.revokePermission(SpacePermission.PAGE_EDIT, new Space(SPACE_TEST_KEY, SPACE_TEST_NAME), User.TEST);
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        assertThat(bookmarkletPage.getMessageLink(), CoreMatchers.containsString("/confluence/wiki/contactadministrators.action"));
        assertTrue(!bookmarkletPage.isFormShown());
    }

    public void testGetLinkTitleUsingPasteUrl()
    {
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);

        bookmarkletPage.pasteUrl(TEST_LOGIN_URL);

        assertTrue(bookmarkletPage.getLoadedTitleForUrl(TEST_LOGIN_URL_TITLE).equals(TEST_LOGIN_URL_TITLE));
    }
    
    @Test
    public void testCommentPostedCorrectly()
    {
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        
        String comment = "This is a comment";
        
        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                        .selectSpace(SPACE_TEST_NAME)
                        .setComment(comment)
                        .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                        .submit();
        
        String createdPageId = bookmarkletPage.getCreatedPageId();
        ViewPage viewPage = product.viewPage(createdPageId);

        CommentsSection comments = viewPage.getComments();
        assertThat(comments.size(), is(1));
        String comment1 = comments.get(0).getContent().byDefaultTimeout();
        assertThat(comment, is(comment1));
    }
    
    @Test
    public void testCannotAddCommentWithUserNoAddCommentPermission()
    {
        rpc.logIn(User.ADMIN);
        Space testSpace = rpc.getSpace(SPACE_TEST_KEY);
        rpc.revokePermission(SpacePermission.COMMENT, testSpace, User.TEST);

        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        
        bookmarkletPage.selectSpace(SPACE_TEST_NAME);
        bookmarkletPage.waitForDisabledCommentInput();
        assertTrue(!bookmarkletPage.isCommentFieldEnabled());
    }
    
    @Test
    public void testLabelInPageAndLabelInTemplateContent()
    {
        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);
        
        String labelName = "labeltest";
        
        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                        .selectSpace(SPACE_TEST_NAME)
                        .addLabel(labelName)
                        .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                        .submit();
        
        String createdPageId = bookmarkletPage.getCreatedPageId();
        ViewPage viewPage = product.viewPage(createdPageId);
        assertTrue(viewPage.getLabelSection().hasLabel(labelName));
    }

    @Test
    public void testLastUsedSpaceIsSelectedByDefault()
    {
        rpc.logIn(User.ADMIN);
        rpc.createPersonalSpace(User.TEST);

        SharelinksBookmarkletPage bookmarkletPage = product.login(User.TEST, SharelinksBookmarkletPage.class);

        assertEquals("Personal space", bookmarkletPage.getSelectedSpace());

        bookmarkletPage.setUrl(TEST_LOGIN_URL)
                       .selectSpace(SPACE_TEST_NAME)
                       .waitUntilTitleIsLoadedForUrl(TEST_LOGIN_URL_TITLE)
                       .submit();

        String createdPageId = bookmarkletPage.getCreatedPageId();
        product.viewPage(createdPageId);

        bookmarkletPage = product.visit(SharelinksBookmarkletPage.class);
        assertEquals(SPACE_TEST_NAME, bookmarkletPage.getSelectedSpace());
    }

    //TODO: A new space isn't needed, use Space.TEST instead
    private void createTestSpace()
    {
        rpc.logIn(User.ADMIN);
        Space testSpace = rpc.createSpace(SPACE_TEST_KEY, SPACE_TEST_NAME, "Space for test");
        rpc.grantPermission(SpacePermission.VIEW, testSpace, User.TEST);
        rpc.grantPermission(SpacePermission.PAGE_EDIT, testSpace, User.TEST);
        rpc.grantPermission(SpacePermission.COMMENT, testSpace, User.TEST);
    }

    private void deleteTestSpace()
    {
        rpc.logIn(User.ADMIN);
        rpc.removeSpace(SPACE_TEST_KEY);
    }
}
