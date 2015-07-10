package it.com.atlassian.confluence.webdriver;

import com.atlassian.confluence.it.AbstractPageEntity;
import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;
import com.atlassian.plugin.ModuleCompleteKey;
import it.com.atlassian.confluence.plugins.createcontent.BlueprintWebDriverTestHelper;
import it.com.atlassian.confluence.plugins.createcontent.model.ItContentBlueprint;
import it.com.atlassian.confluence.plugins.createcontent.model.ItContentTemplateRef;
import it.com.atlassian.confluence.plugins.createcontent.pageobjects.ListBlueprintTemplates;
import it.com.atlassian.confluence.webdriver.pageobjects.BlankIndexPage;
import it.com.atlassian.confluence.webdriver.pageobjects.DecisionsWizard;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DecisionsBlueprintTest extends AbstractWebDriverTest
{
    private static final String PLUGIN_KEY = "com.atlassian.confluence.plugins.confluence-business-blueprints";
    private static final String CREATE_DIALOG_MODULE_KEY = PLUGIN_KEY + ":decisions-blueprint-item";
    private static final String INDEX_PAGE_TITLE = "Decision log";

    private BlueprintWebDriverTestHelper helper;


    @Before
    public void setUp() throws Exception
    {
        helper = new BlueprintWebDriverTestHelper(rpc, product, User.TEST, CREATE_DIALOG_MODULE_KEY);
    }

    @Test
    public void createDecisionGoesToEditPage() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        String title = "My Decision";

        CreatePage createPage = wizard.setDecision(title).submit();
        assertThat(createPage.getTitle(), is(title));
    }

    @Test
    public void createDecisionSaveEditorPageGoesToViewPage() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        String title = "My Decision";

        ViewPage viewPage = wizard.setDecision(title).submit().save();
        assertThat(viewPage.getTitle(), is(title));
    }

    @Test
    public void newDecisionsPageAppearsOnIndexPage() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        String title = "My Decisions";
        wizard.setDecision(title).submit().save();

        Page decision = rpc.getExistingPage(Space.TEST, title);
        helper.goToIndexPageAndCheckContent(Space.TEST, INDEX_PAGE_TITLE, decision);
    }

    @Test
    public void blankIndexPageHasStylesAndButton()
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        String title = "My Decisions";
        wizard.setDecision(title).submit().save();

        // remove blueprint page to get blank index page
        Page blueprintPage = rpc.getExistingPage(Space.TEST, title);
        rpc.removePage(blueprintPage);
        rpc.flushIndexQueue();

        helper.goToIndexPageAndCheckContent(Space.TEST, INDEX_PAGE_TITLE);
        BlankIndexPage blankIndexPage = product.getPageBinder().bind(BlankIndexPage.class);
        DecisionsWizard wizard2 = blankIndexPage.clickOnCreateButtonAndExpectWizard(DecisionsWizard.class);
        assertTrue(wizard2.isShown());
    }

    @Test
    public void decisionsWizardPreventsDuplicatePageTitle() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);

        wizard.setDecision(Page.TEST.getTitle()).submitAndExpectWizardError();
        assertThat(wizard.getTitleError(), is("A page with this name already exists."));

        String title = "My Decisions";
        CreatePage createPage = wizard.setDecision(title).submit();
        assertThat(createPage.getTitle(), is(title));
    }

    @Test
    public void decisionsWizardRequiresPageTitle() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);

        wizard.submitAndExpectWizardError();
        assertThat(wizard.getTitleError(), is("Decision is required."));

        String title = "My Decisions";
        CreatePage createPage = wizard.setDecision(title).submit();
        assertThat(createPage.getTitle(), is(title));
    }

    @Test
    public void anonymousUserWithCreateContentPermissionCanCreateDecisionsPage()
    {
        rpc.logIn(User.ADMIN);
        rpc.grantAnonymousUsePermission();
        rpc.grantPermission(SpacePermission.VIEW, Space.TEST, User.ANONYMOUS);
        rpc.grantPermission(SpacePermission.PAGE_EDIT, Space.TEST, User.ANONYMOUS);

        DashboardPage dashboardPage = product.visit(DashboardPage.class);
        assertFalse(dashboardPage.getHeader().isLoggedIn());

        String newTitle = "New title for anon user";

        DecisionsWizard wizard = helper.openCreateDialogAndChooseBlueprint(DecisionsWizard.class);
        CreatePage createPage = wizard.setDecision(newTitle).submit();
        assertThat(createPage.getTitle(), is(newTitle));
    }

    @Test
    public void outcomeFieldIsUsed() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);

        wizard.setDecision("Decided decision")
              .setStatus("Decided")
              .setOutcome("This is stupid.\n" +
                          "This is a more stupid line")
              .submit()
              .save();

        Page decisionPage = rpc.getExistingPage(Space.TEST, "Decided decision");
        assertTrue(decisionPage.getContent().contains("This is stupid."));
        assertTrue(decisionPage.getContent().contains("<br />This is a more stupid line"));
    }

    @Test
    public void outcomeIsToggledOnStatusChange() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        assertFalse(wizard.isOutcomeVisible());

        wizard = wizard.setStatus("Decided");
        assertTrue(wizard.isOutcomeVisible());

        wizard = wizard.setStatus("Not started");
        assertFalse(wizard.isOutcomeVisible());
    }

    // CONFDEV-17222
    @Test
    public void outcomesIsBlankIfStatusIsNotDecided() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        wizard = wizard.setDecision("A Decision")
                       .setStatus("Decided")
                       .setOutcome("I have decided");

        wizard.setStatus("Not started").submit().save();
        Page decisionPage = rpc.getExistingPage(Space.TEST, "A Decision");
        assertFalse(decisionPage.getContent().contains("I have decided"));
    }

    @Test
    public void backgroundFieldInsertedCorrectly() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);

        wizard.setDecision("What to eat for lunch?")
              .setBackground("Anna is hungry and wants to buy lunch.\n" +
                             "Alice is hungry too.")
              .submit()
              .save();

        Page decisionPage = rpc.getExistingPage(Space.TEST, "What to eat for lunch?");
        assertTrue(decisionPage.getContent().contains("Anna is hungry and wants to buy lunch."));
        assertTrue(decisionPage.getContent().contains("<br />Alice is hungry too."));
    }

    @Test
    public void editedIndexPageTemplateWorksAtSpaceScope() throws Exception
    {
        final Space space = new Space("BS", "Blah Space");
        rpc.createSpace(space);

        ListBlueprintTemplates blueprintTemplates = product.login(User.ADMIN, ListBlueprintTemplates.class, space);

        ModuleCompleteKey blueprintKey = new ModuleCompleteKey(PLUGIN_KEY, "decisions-blueprint");
        ModuleCompleteKey templateKey = new ModuleCompleteKey(PLUGIN_KEY, "decisions-index-page");
        ItContentBlueprint blueprint = new ItContentBlueprint(blueprintKey.getCompleteKey());
        ItContentTemplateRef contentTemplate = new ItContentTemplateRef(templateKey.getCompleteKey(), blueprint);
        blueprintTemplates.edit(contentTemplate).save();

        BlueprintWebDriverTestHelper helper = new BlueprintWebDriverTestHelper(rpc, product, User.ADMIN, CREATE_DIALOG_MODULE_KEY);

        DecisionsWizard wizard = helper.openCreateDialogAndChooseBlueprint(DecisionsWizard.class);

        String pageTitle = "Should we make the index page template editable?";
        wizard.setDecision(pageTitle)
              .submit()
              .save();

        AbstractPageEntity requirements = rpc.getExistingPage(space, pageTitle);
        helper.goToIndexPageAndCheckContent(space, INDEX_PAGE_TITLE, requirements);
    }

    //CONFDEV-24099
    @Test
    public void decisionsPageHasDateLozenge() throws Exception
    {
        DecisionsWizard wizard = helper.loginAndChooseBlueprint(DecisionsWizard.class);
        String title = "My Decisions";
        String date = "2014-03-21";
        CreatePage editor = wizard.setDecision(title).setDate(date).submit();

        assertTrue(editor.hasHtmlContent("</time>"));
        assertTrue(editor.hasHtmlContent("datetime=\"" + date + "\""));
    }
}
