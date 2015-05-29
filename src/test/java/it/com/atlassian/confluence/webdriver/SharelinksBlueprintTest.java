package it.com.atlassian.confluence.webdriver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.atlassian.pageobjects.elements.PageElement;
import it.com.atlassian.confluence.plugins.createcontent.BlueprintWebDriverTestHelper;
import it.com.atlassian.confluence.plugins.createcontent.pageobjects.DashboardPage;
import it.com.atlassian.confluence.plugins.createcontent.pageobjects.ViewPage;
import it.com.atlassian.confluence.webdriver.pageobjects.BlankIndexPage;
import it.com.atlassian.confluence.webdriver.pageobjects.CustomHtmlPage;
import it.com.atlassian.confluence.webdriver.pageobjects.SharelinksWizard;

import javax.mail.internet.MimeMessage;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.mail.MailFacade;
import com.atlassian.confluence.pageobjects.page.content.CommentsSection;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;
import com.atlassian.confluence.webdriver.WebDriverConfiguration;

public class SharelinksBlueprintTest extends AbstractWebDriverTest
{
    private static final String PLUGIN_KEY = "com.atlassian.confluence.plugins.confluence-business-blueprints";
    private static final String CREATE_DIALOG_MODULE_KEY = PLUGIN_KEY + ":sharelinks-blueprint-item";
    private static final String INDEX_PAGE_TITLE = "Shared links";

    private static final String IMAGE_META_TITLE = "Title image test";
    private static final String IMAGE_META_TITLE_TAG = "<meta property='og:title' content='" + IMAGE_META_TITLE + "' />";
    private static final String IMAGE_META_TAG = "<meta property='og:image' content='http://example.com/image-example.jpg' />";
    private static final String DESCRIPTION_META_TAG = "<meta property='og:description' content='Description test' />";
    private static final String VIDEO_META_TITLE_TAG = "<meta property='og:title' content='Title video test' />";
    private static final String VIDEO_META_TAG = "<meta property='og:video' content='http://youtube.com/watch?v=example' />";

    private static final String BASE_URL = WebDriverConfiguration.getBaseUrl();
    private static final String TEST_SPACE_DIR_URL = BASE_URL + "/spacedirectory/view.action";
    private static final String TEST_DASHBOARD_URL = BASE_URL + "/dashboard.action";

    private BlueprintWebDriverTestHelper helper;
    private MailFacade mail;

    private boolean isCustomMetaTagsSet = false;

    @Before
    public void setUp() throws Exception
    {
        isCustomMetaTagsSet = false;
        helper = new BlueprintWebDriverTestHelper(rpc, product, User.TEST, CREATE_DIALOG_MODULE_KEY);
        rpc.grantPermission(SpacePermission.COMMENT, Space.TEST, User.TEST);
        rpc.enableWebSudo(false);
        mail = new MailFacade(rpc);
        mail.start();
    }

    @After
    public void tearDown()
    {
        if (isCustomMetaTagsSet)
        {
            rpc.clearCustomHtml();
        }
        mail.stop();
        rpc.logOut();
    }

    @Test
    public void testTitleFieldIsAutomaticallySet()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        String url = BASE_URL + "/login.action";
        wizard = wizard.setUrl(url);

        String expectedTitle = "Log In - Confluence";
        assertThat(wizard.getPreviewTitle(), is(expectedTitle));
        assertThat(wizard.getTitle(), is(expectedTitle));
    }

    @Test
    public void testLinkPreviewIsRenderedInWizard()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        String url = BASE_URL + "/login.action";
        wizard = wizard.setUrl(url);

        wizard.validateLinkPreviewRendered();
    }

    @Test
    public void testValidationRequired()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        wizard.clickSubmit();

        assertThat(wizard.getURLValidationError(), is("URL is required"));
        assertThat(wizard.getTitleValidationError(), is("Title is required"));
    }

    // CONFDEV - 22717
    @Test
    public void testBookmarkletIsPresent()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        wizard.clickSubmit();

        wizard.validateBookmarkletPresent();
    }

    @Test
    public void testCommentsPostedCorrectly()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        String comment = "This is comment 1\n" +
                            "This is comment 2";
        ViewPage viewPage = wizard.setUrl(TEST_SPACE_DIR_URL)
                                  .waitForPreviewLoaded()
                                  .setComment(comment)
                                  .submit();

        CommentsSection comments = viewPage.getComments();
        assertThat(comments.size(), is(1));
        String comment1 = comments.get(0).getContent().byDefaultTimeout();
        assertThat(comment, is(comment1));
    }

    @Test
    public void testIndexHasPageAndHasButton()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        String title = "Sharelinks1 title";
        wizard.setUrl(TEST_DASHBOARD_URL)
              .setTitle(title)
              .submit();

        Page blueprintPage = rpc.getExistingPage(Space.TEST, title);
        helper.goToIndexPageAndCheckContent(Space.TEST, INDEX_PAGE_TITLE, blueprintPage);
        ViewPage index = product.getPageBinder().bind(ViewPage.class);

        SharelinksWizard wizard2 = index.getCreateFromTemplateButton().clickAndExpectWizard(SharelinksWizard.class);
        wizard2.assertWizardIsShown();
    }

    @Test
    public void testBlankIndexPageHasStylesAndButton()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        String title = "Sharelinks1 title";
        wizard.setUrl(TEST_DASHBOARD_URL)
              .waitForPreviewLoaded()
              .setTitle(title)
              .submit();

        // remove blueprint page to get blank index page
        Page blueprintPage = rpc.getExistingPage(Space.TEST, title);
        rpc.removePage(blueprintPage);

        helper.goToIndexPageAndCheckContent(Space.TEST, INDEX_PAGE_TITLE);
        BlankIndexPage blankIndexPage = product.getPageBinder().bind(BlankIndexPage.class);
        SharelinksWizard wizard2 = blankIndexPage.clickOnCreateButtonAndExpectWizard(SharelinksWizard.class);
        wizard2.assertWizardIsShown();
    }

    @Test
    public void testPageIsAChildOfTheIndexPage()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        wizard.setUrl(TEST_DASHBOARD_URL)
              .setTitle("My Title");
        ViewPage page = wizard.submit();

        Page indexPage = rpc.getExistingPage(Space.TEST, INDEX_PAGE_TITLE);
        helper.assertParentPage(page, indexPage);
    }

    @Test
    public void testPageTitleValidation()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        wizard.setUrl(TEST_DASHBOARD_URL)
              .setTitle(Page.TEST.getTitle()) //use an existing name, expect error message
              .clickSubmit();
        assertThat(wizard.getTitleValidationError(), is("A page with this name already exists"));

        //change the title, should now be able to create the page
        String title = "Sharelinks title";
        ViewPage viewPage = wizard.setTitle(title)
                                  .submit();
        assertThat(viewPage.getTitle(), is(title));
    }

    @Test
    public void testPreviewLinkMetaTitleNotEmpty()
    {
        updateConfluenceMetaTags(IMAGE_META_TITLE_TAG, IMAGE_META_TAG, DESCRIPTION_META_TAG);
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        wizard.setUrl(TEST_SPACE_DIR_URL);
        assertTrue(wizard.getPreviewTitle().equals(IMAGE_META_TITLE));
    }

    @Test
    public void testPreviewLinkUsingPasteUrl()
    {
        updateConfluenceMetaTags(IMAGE_META_TITLE_TAG, IMAGE_META_TAG, DESCRIPTION_META_TAG);
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        wizard.pasteUrl(TEST_SPACE_DIR_URL);
        assertTrue(wizard.getPreviewTitle().equals(IMAGE_META_TITLE));
    }
    
    @Test
    public void testDisplayLinkMetaDataInPageContent()
    {
        updateConfluenceMetaTags(IMAGE_META_TITLE_TAG, IMAGE_META_TAG, DESCRIPTION_META_TAG);
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        String title = "Sharelinks display link meta data";
        ViewPage viewPage = wizard.setUrl(TEST_SPACE_DIR_URL)
                                  .setTitle(title)
                                  .submit();

        String mainContentText = viewPage.getMainContent()
                                         .getText();
        // page has meta data description 
        String description = "Description test";
        assertThat(mainContentText, CoreMatchers.containsString(description));
        // page has link open link 
        String openLink = "Open link";
        assertThat(mainContentText, CoreMatchers.containsString(openLink));
        // page has meta data image
        String imageSelector = "div.sharelinks-link-meta-data h3 img";
        assertTrue(viewPage.getMainContent().find(By.cssSelector(imageSelector)).isPresent());
    }

    @Test
    public void testDisplayWhenNoMetaDataAvailable()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        String title = "Sharelinks display link meta data";
        ViewPage viewPage = wizard.setUrl(TEST_SPACE_DIR_URL)
                                  .setTitle(title)
                                  .submit();

        String mainContentText = viewPage.getMainContent()
                .getText();

        assertTrue(mainContentText.contains("No link preview available. Please open the link for details."));
    }

    //CONFDEV-18682
    @Test
    public void testDisplayWithImageAndNoDescription()
    {
        updateConfluenceMetaTags(IMAGE_META_TAG);
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
        String title = "Sharelinks display link meta data";
        ViewPage viewPage = wizard.setUrl(TEST_SPACE_DIR_URL)
                .setTitle(title)
                .submit();

        String mainContentText = viewPage.getMainContent()
                .getText();

        assertTrue(mainContentText.contains("No description available. Please open the link for details."));
        // page has meta data image
        String imageSelector = "div.sharelinks-link-meta-data h3 img";
        assertTrue(viewPage.getMainContent().find(By.cssSelector(imageSelector)).isPresent());
    }
    
    @Test
    public void testDisplayVideoLinkMetaDataInPageContent()
    {
        updateConfluenceMetaTags(VIDEO_META_TITLE_TAG, IMAGE_META_TAG, VIDEO_META_TAG, DESCRIPTION_META_TAG);
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        String title = "Sharelinks display video link meta data";
        ViewPage viewPage = wizard.setUrl(TEST_SPACE_DIR_URL)
                                  .setTitle(title)
                                  .submit();

        String mainContentText = viewPage.getMainContent().getText();
        // page has meta data description 
        String description = "Description test";
        assertThat(mainContentText, CoreMatchers.containsString(description));
        // page has link open link 
        String openLink = "Open link";
        assertThat(mainContentText, CoreMatchers.containsString(openLink));
        // page has video image
        String videoImageSelector = "img[src='http://example.com/image-example.jpg']";
        assertTrue(viewPage.getMainContent().find(By.cssSelector(videoImageSelector)).isPresent());
    }

    // CONFVN-255
    @Test
    public void anonymousUserWithoutCommentPermissionCannotComment()
    {
        try
        {
            rpc.logIn(User.ADMIN);
            rpc.grantAnonymousUsePermission();
            rpc.grantPermission(SpacePermission.VIEW, Space.TEST, User.ANONYMOUS);
            rpc.grantPermission(SpacePermission.PAGE_EDIT, Space.TEST, User.ANONYMOUS); // grant page edit permissions

            DashboardPage dashboardPage = product.visit(DashboardPage.class);
            assertFalse(dashboardPage.getHeader().isLoggedIn());

            SharelinksWizard wizard = helper.openCreateDialogAndChooseBlueprint(SharelinksWizard.class);

            String newTitle = "New title for anon user";
            String url = WebDriverConfiguration.getBaseUrl() + "/spacedirectory/view.action";
            String comment = "this is a comment";
            ViewPage viewPage = wizard.setUrl(url)
                                      .setTitle(newTitle)
                                      .waitForCommentFieldToBeDisabled() // comment field is disabled
                                      .submit(); // user should be able to create the page

            assertTrue(viewPage.getTitle().equals(newTitle));

            //CommentSection has no nice way to test if the comments are empty - CONFDEV-19115
            try
            {
                viewPage.getComments().get(0);
                fail("There should be no comments on this page.");
            }
            catch (NoSuchElementException e)
            {
                //No comments found, test successful
            }
        }
        finally
        {
            rpc.revokeAnonymousUsePermission();
        }

    }
    
    @Test
    public void testLabelInPreviewAndLabelInTemplateContent()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);
                
        String title = "Sharelinks label title";
        String labelName = "labeltest";
        
        wizard.setUrl(TEST_SPACE_DIR_URL)
              .setTitle(title)
              .addLabel(labelName);

        ViewPage viewPage = wizard.submit();
        assertTrue(viewPage.getLabelSection().hasLabel(labelName));
    }

    @Test
    public void testEscapesLabel()
    {
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        wizard.setUrl(TEST_SPACE_DIR_URL)
              .setTitle("Label XSS Test")
              .inputLabel("<u>ShouldNotBeUnderlined</u>");

        PageElement labelResultsField = wizard.getLabelPicker().getLabelResultsField();
        assertFalse("Label error message is not escaped!", labelResultsField.find(By.cssSelector("u")).isPresent());
    }

    @Test
    public void testSharingLinkWithUser() throws Exception
    {
        mail.addMailboxForUser(User.TEST);
        SharelinksWizard wizard = helper.loginAndChooseBlueprint(SharelinksWizard.class);

        String title = "Sharelinks label title";
        String comment = "Share with you";
        
        wizard.setUrl(TEST_SPACE_DIR_URL)
              .setTitle(title)
              .addUserToShareWith(User.TEST)
              .setComment(comment)
              .submit();

        MimeMessage smtpMessage = mail.getReceivedMessage();
        assertTrue(smtpMessage.getSubject().contains(User.TEST.getDisplayName() + " shared \"" + title + "\" with you"));
    }

    /**
     * Open the page Custom HTML in Confluence Admin, update meta tags follow
     * open graph using Insert Custom HTML - At end of the head html
     * 
     * @param metaTags meta tags need input
     */
    private void updateConfluenceMetaTags(String... metaTags)
    {
        StringBuilder tags = new StringBuilder();

        for (String metaTag : metaTags) {
            tags.append(metaTag);
        }

        isCustomMetaTagsSet = true;
        product.logOut();
        product.login(User.ADMIN, DashboardPage.class);
        String editCustomHtmlUrl = BASE_URL + "/admin/editcustomhtml.action";
        product.getTester().gotoUrl(editCustomHtmlUrl);
        CustomHtmlPage page = product.getPageBinder().bind(
                CustomHtmlPage.class);
        page.setCustomHeadHtml(tags.toString()).clickConfirmCustomHtml();
        product.logOut();
    }
}
