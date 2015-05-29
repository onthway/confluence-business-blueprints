Confluence.Blueprints.Sharelinks.autocompleteMultiLabel = (function ($) {
    function build($labelsField) {
        $labelsField.auiSelect2(Confluence.UI.Components.LabelPicker.build({
            formatInputTooShort: function () {
                return AJS.I18n.getText("sharelinks.blueprint.label.multiselect.prompt");
            },
            formatResult: function (result) {
                return Confluence.Blueprints.Sharelinks.labelResult({
                    label: {
                        labelName: result.text,
                        isNew: result.isNew
                    }
                });
            }
        }));
    }

    return {
        build: build
    }
})(AJS.$);