(function ($) {

    function validate($container, spaceKey) {
        var $titleField = $container.find("#decisions-page-title"),
            pageTitle = $.trim($titleField.val()),
            error;

        if (!pageTitle) {
            error = AJS.I18n.getText("decisions.blueprint.wizard.form.validation.decision.required");
        }
        else if (!Confluence.Blueprint.canCreatePage(spaceKey, pageTitle)) {
            error = AJS.I18n.getText("decisions.blueprint.wizard.form.validation.decision.exists");
        }
        if (error) {
            $titleField.focus().siblings(".error").html(error);
            return false;
        }

        return true;
    }

    function pageSubmit(ev, state) {

        return validate(state.$container, state.wizardData.spaceKey);
    }

    function bindFields(ev, state) {
        $('#decisions-due-date').datepicker({
            dateFormat : "yy-mm-dd"
        });

        $("#decisions-status").on('change', function(){
            var $finalDecision = $("#decisions-final-decision");
            var $decisionLabel = $("label[for=decisions-final-decision]");
            if ($(this).find(":selected").val() == "GREEN"){
                $finalDecision.css({'display' : 'inline'});
                $decisionLabel.css({'display' : 'inline'});
            }
            else{
                $finalDecision.css({'display' : "none"});
                $decisionLabel.css({'display' : "none"});
            }
        });
    }

    Confluence.Blueprint.setWizard('com.atlassian.confluence.plugins.confluence-business-blueprints:decisions-blueprint-item', function(wizard) {
        wizard.on("post-render.decisions-page1", bindFields);
        wizard.on("submit.decisions-page1", pageSubmit);
    });

})(AJS.$);