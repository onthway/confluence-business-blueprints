package it.com.atlassian.confluence.webdriver;

import com.atlassian.confluence.it.ContentPermissionType;
import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.content.security.ContentPermissionEntityType;
import com.atlassian.confluence.it.content.security.ContentPermissionEntry;
import com.atlassian.confluence.it.plugin.SimplePlugin;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;
import it.com.atlassian.confluence.plugins.createcontent.BlueprintWebDriverTestHelper;
import it.com.atlassian.confluence.webdriver.pageobjects.FileListWizard;
import it.com.atlassian.confluence.webdriver.pageobjects.MultiUserPicker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the "File List" (formerly known as "Shared Files") Blueprint.
 */
public class FileListBlueprintTest extends AbstractWebDriverTest
{
    static final String PLUGIN_KEY = "com.atlassian.confluence.plugins.confluence-business-blueprints";
    static final SimplePlugin FILE_LIST_BLUEPRINT_PLUGIN = new SimplePlugin(PLUGIN_KEY, null);

    static final String CONTENT_TEMPLATE_MODULE_KEY = PLUGIN_KEY + ":file-list-page";
    static final String CREATE_DIALOG_MODULE_KEY = PLUGIN_KEY + ":file-list-item";

    private BlueprintWebDriverTestHelper helper;

    @Before
    public void setUp() throws Exception
    {
        helper = new BlueprintWebDriverTestHelper(rpc, product, User.TEST, CREATE_DIALOG_MODULE_KEY);
    }

    @Test
    public void newFileListPageAppearsOnIndexPage() throws Exception
    {
        FileListWizard wizard = helper.loginAndChooseBlueprint(FileListWizard.class);

        final ViewPage viewPage = wizard.setTitle("My Files").submit();
        assertThat(viewPage.getTitle(), is("My Files"));

        Page fileListPage = rpc.getExistingPage(Space.TEST, "My Files");
        helper.goToIndexPageAndCheckContent(Space.TEST, "File lists", fileListPage);
    }

    @Test
    public void fileListWizardPreventsDuplicatePageTitle() throws Exception
    {
        FileListWizard wizard = helper.loginAndChooseBlueprint(FileListWizard.class);

        wizard.setTitle(Page.TEST.getTitle()).submitAndExpectWizardError();
        assertThat(wizard.getTitleError(), is("A page with this name already exists."));

        final ViewPage viewPage = wizard.setTitle("My Files").submit();
        assertThat(viewPage.getTitle(), is("My Files"));
    }

    @Test
    public void fileListWizardRequiresPageTitle() throws Exception
    {
        FileListWizard wizard = helper.loginAndChooseBlueprint(FileListWizard.class);

        wizard.submitAndExpectWizardError();
        assertThat(wizard.getTitleError(), is("Name is required."));

        final ViewPage viewPage = wizard.setTitle("My Files").submit();
        assertThat(viewPage.getTitle(), is("My Files"));
    }

    @Test
    public void fileListWizardFieldsAreUsed() throws Exception
    {
        FileListWizard wizard = helper.loginAndChooseBlueprint(FileListWizard.class);

        ViewPage viewPage = wizard
                .setTitle("Here are some Shared Files")
                .setDescription("These files are awesome.")
                .submit();

        assertThat(viewPage.getTitle(), is("Here are some Shared Files"));
        assertThat(viewPage.getTextContent(), allOf(
                containsString("These files are awesome."),
                containsString("No files shared here yet")
        ));
    }

    // CONFDEV-15653
    @Test
    public void anonymousUserWithCreateContentPermissionCanCreateFileListPage()
    {
        rpc.logIn(User.ADMIN);
        rpc.grantAnonymousUsePermission();
        rpc.grantPermission(SpacePermission.VIEW, Space.TEST, User.ANONYMOUS);
        rpc.grantPermission(SpacePermission.PAGE_EDIT, Space.TEST, User.ANONYMOUS);

        DashboardPage dashboardPage = product.visit(DashboardPage.class);
        assertFalse(dashboardPage.getHeader().isLoggedIn());

        String newTitle = "New title for anon user";
        FileListWizard wizard = helper.openCreateDialogAndChooseBlueprint(FileListWizard.class);
        ViewPage viewPage = wizard.setTitle(newTitle).submit();
        assertTrue(viewPage.getTitle().equals(newTitle));
    }

    @Test
    public void filesAreRestrictedToSelectedUsers()
    {
        FileListWizard wizard = helper.loginAndChooseBlueprint(FileListWizard.class);
        rpc.grantPermission(SpacePermission.PAGE_RESTRICT, Space.TEST, User.TEST);

        ViewPage fileListPage = wizard
            .setTitle("Here are some Shared Files")
            .setDescription("These files are awesome.")
            .addUserRestriction(User.ADMIN)
            .addUserRestriction(User.TEST)
            .submit();
        long pageId = Long.valueOf(fileListPage.getMetadata("page-id"));

        Collection<ContentPermissionEntry> pagePermissions = rpc.getPagePermissions(pageId);
        final ContentPermissionEntry adminRestriction = new ContentPermissionEntry(ContentPermissionType.VIEW,
            ContentPermissionEntityType.USER, User.ADMIN.getName());
        final ContentPermissionEntry testUserRestriction = new ContentPermissionEntry(ContentPermissionType.VIEW,
            ContentPermissionEntityType.USER, User.TEST.getName());
        assertEquals(2, pagePermissions.size());
        Assert.assertThat(pagePermissions, containsInAnyOrder(adminRestriction, testUserRestriction));
    }

    @Test
    public void usernameIsEscapedProperly()
    {
        rpc.createUser(User.EVIL);
        rpc.flushIndexQueue();
        FileListWizard wizard = helper.loginAndChooseBlueprint(FileListWizard.class);
        MultiUserPicker restrictedUserPicker = wizard.searchUserRestriction(User.EVIL);
        restrictedUserPicker.waitForResult(User.EVIL);
        assertTrue(restrictedUserPicker.containsUserWithFullName(User.EVIL.getFullName()));
    }
}