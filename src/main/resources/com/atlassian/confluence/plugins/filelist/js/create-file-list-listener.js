(function ($) {

    function validate($container, spaceKey) {
        var $titleField = $container.find("#file-list-page-title"),
            pageTitle = $.trim($titleField.val()),
            error;

        if (!pageTitle) {
            error = AJS.I18n.getText("file.list.blueprint.wizard.form.validation.name.required");
        }
        else if (!Confluence.Blueprint.canCreatePage(spaceKey, pageTitle)) {
            error = AJS.I18n.getText("file.list.blueprint.wizard.form.validation.name.exists");
        }
        if (error) {
            $titleField.focus().siblings(".error").html(error);
            return false;
        }

        return true;
    }

    function page1Submit(ev, state) {
        return validate(state.$container, state.wizardData.spaceKey);
    }

    Confluence.Blueprint.setWizard('com.atlassian.confluence.plugins.confluence-business-blueprints:file-list-item', function(wizard) {
        wizard.on("submit.file-list-page1", page1Submit);
    });
})(AJS.$);
