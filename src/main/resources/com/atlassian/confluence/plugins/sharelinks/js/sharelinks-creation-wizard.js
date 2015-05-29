(function ($) {
    var urlSyntaxError = false;
    
    function validateAll($container, spaceKey) {
        var hasError = false;
        if (validateUrl($container, !hasError))
            hasError = true;
        if (validateTitle($container, spaceKey, !hasError))
            hasError = true;
        return !hasError;
    }

    function validateUrl($container, focus) {
        var $urlField = $container.find("#sharelinks-url");
        var url = $.trim($urlField.val());
        var error = "";

        if (!url) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.url.required");
        }
        else if (urlSyntaxError) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.url.syntaxError");
        }

        return updateError($urlField, error, focus);
    }

    function validateTitle($container, spaceKey, focus) {
        var $titleField = $container.find("#sharelinks-title");
        var title = $.trim($titleField.val());
        var error = "";

        if (!title) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.title.required");
        }
        else if (!Confluence.Blueprint.canCreatePage(spaceKey, title)) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.title.duplicated");
            // analytic error duplicate page name when title change
            var errorAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.errorTypes.value.duplicatedPage;
            Confluence.Blueprints.Sharelinks.Analytics.triggerErrorTypes(errorAnalyticsProperties);
        }

        return updateError($titleField, error, focus);
    }

    // focus: focus to errorneous input or not
    function updateError($field, error, focus) {
        $field.siblings(".error").html(error);
        if (error && focus) {
            $field.focus();
        }
        return error;
    }
    
    // store received link title
    var linkTitle;

    function pageSubmit(ev, state) {
        var canSubmit = validateAll(state.$container, state.wizardData.spaceKey);
        //Analytics
        if (canSubmit) {
            // comment analytic
            var commentAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.submitData.value.noComment;
            var comment = $("#sharelinks-comment").val();
            comment = $.trim(comment);
            if (comment) {
                commentAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.submitData.value.comment;
            }
            Confluence.Blueprints.Sharelinks.Analytics.triggerSubmitData(commentAnalyticsProperties);

            // title analytic
            var titleAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.submitData.value.noEditTitle;
            if (linkTitle !== $("#sharelinks-title").val()) {
                titleAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.submitData.value.editTitle;
            }
            Confluence.Blueprints.Sharelinks.Analytics.triggerSubmitData(titleAnalyticsProperties);

            // share analytic
            var shareAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.submitData.value.noShare;
            var share = $("#sharelinks-sharewith").val();
            share = $.trim(share);
            if (share) {
                shareAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.submitData.value.share;
            }
            Confluence.Blueprints.Sharelinks.Analytics.triggerSubmitData(shareAnalyticsProperties);
        }

        //CONFDEV-18265 Hack to force the page to be a child of the index page.
        //Fix this when CONFDEV-18335 is fixed.
        state.wizardData.parentPageId = -1;

        return canSubmit;
    }

    var previousInputURL;

    function bindEvents(ev, state) {
        previousInputURL = '';
        $("#sharelinks-url").bind('paste', function() {
            setTimeout(function() {
                getLinkPreview(state.$container, state.wizardData.spaceKey, true);
            }, 0);
        });
        $("#sharelinks-url").change(function() {
            getLinkPreview(state.$container, state.wizardData.spaceKey, false);
        });
        $("#sharelinks-title").change(function() {
            if ($("#sharelinks-title").siblings(".error").html != "") {
                validateTitle(state.$container, state.wizardData.spaceKey, false);
            }
        });

        //create-dialog-page-description is the old class provided by create content,
        //remove this when we break compatibility with confluence 5.4.x
        var previewDiv = $(".dialog-wizard-page-description,.create-dialog-page-description");
        var bookmarkletActionURL = AJS.Meta.get("base-url") + "/plugins/sharelinksbookmarklet/bookmarklet.action";
        var bookmarkletGuideOnWizard = Confluence.Blueprints.Sharelinks.bookmarkletGuideOnWizard({"bookmarkletActionURL": bookmarkletActionURL});
        $(bookmarkletGuideOnWizard).appendTo(previewDiv);

        // bind label picker for topic
        Confluence.Blueprints.Sharelinks.autocompleteMultiLabel.build($("#sharelinks-label"));

        $(".create-dialog-sharelinks-page1 .sharelinks-urlmacro-button").click(function() {
            alert(AJS.I18n.getText("urlmacro.button.guide"));
            return false;
        });
    }

    function checkPermissions(ev, state) {
        $.ajax({
            type: "get",
            dataType: "json",
            url: Confluence.getContextPath() + "/rest/sharelinks/1.0/can-create-comment",
            data: {
                spaceKey: state.wizardData.spaceKey
            },
            success: function(data) {
                if (!data) {
                    var commentText = $("#sharelinks-comment");
                    commentText.attr("disabled", "disabled");
                    commentText.attr("placeholder", AJS.I18n.getText("sharelinks.blueprint.wizard.comment.nopermissions"));
                }
            }
        });
    }

    Confluence.Blueprint.setWizard("com.atlassian.confluence.plugins.confluence-business-blueprints:sharelinks-blueprint-item", function(wizard) {
        wizard.on("post-render.sharelinks-page1", bindEvents);
        wizard.on("post-render.sharelinks-page1", checkPermissions);
        wizard.on("submit.sharelinks-page1", pageSubmit);
    });

    function updateLinkPreview(linkMetaData) {
        //trim title & preview title
        var maxTitleLength = 255;
        var maxPreviewTitleLength = 180;
        
        var trimmedTitle = trimText(linkMetaData.title, maxTitleLength);
        var titleField = $("#sharelinks-title");
        titleField.val(trimmedTitle);

        // HACK: for browsers that don't support HTML5 placeholder input attributes,
        // the placeholder.js library in confluence core tries to mimic placeholder
        // behaviour by adding "placeholded" class to placeholder text.
        // Remove this "placeholded" class as title is not a placeholder.
        // note: Remove this hack when we drop support for IE 9.
        titleField.removeClass("placeholded");

        var previewMetaData = $.extend({}, linkMetaData);
        previewMetaData.title = trimText(previewMetaData.title, maxPreviewTitleLength);
        
        // analytic type of link
        var linkAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.linkTypes.value.noContent;
        var previewLinkContent;
        if (linkMetaData.videoURL) {
            previewLinkContent = Confluence.Blueprints.Sharelinks.previewVideoLink({"linkMetaData": previewMetaData});
            linkAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.linkTypes.value.video;
        }
        else {
            previewLinkContent = Confluence.Blueprints.Sharelinks.previewLink({"linkMetaData": previewMetaData});
            if (linkMetaData.imageURL) {
                linkAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.linkTypes.value.image;
            }
            else if (linkMetaData.title) {
                linkAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.linkTypes.value.noVideoImage;
            }
        }
        //create-dialog-page-description is the old class provided by create content,
        //remove this when we break compatibility with confluence 5.4.x
        var previewDiv = $(".create-dialog-page-description,.dialog-wizard-page-description");
        previewDiv.empty();
        $(previewLinkContent).appendTo(previewDiv);
        // analytic type of link
        Confluence.Blueprints.Sharelinks.Analytics.triggerLinkTypes(linkAnalyticsProperties);
    }

    function trimText(text, maxlength) {
        if (null != text && maxlength < text.length) {
            text = text.substring(0, maxlength - 1) + "\u2026";//ELLIPSIS
        }
        return text;
    }

    function disableUrlAndTitleInputs($urlField, $titleField) {
        $urlField.attr("disabled", "disabled");
        $titleField.attr("disabled", "disabled");
    }

    function enableUrlAndTitleInputs($urlField, $titleField) {
        $urlField.removeAttr("disabled");
        $titleField.removeAttr("disabled");
    }

    function getLinkPreview($container, spaceKey, isPasteUrl) {
        var $urlField = $("#sharelinks-url"),
            inputURL = $urlField.val(),
            $titleField = $("#sharelinks-title");
        inputURL = $.trim(inputURL);
        if (inputURL) {
            var restUrl = Confluence.getContextPath() + "/rest/sharelinks/1.0/link";
            if (inputURL !== previousInputURL) {
                previousInputURL = inputURL;
                var previewContainer = $(".create-dialog-page-description,.dialog-wizard-page-description").empty();
                var spinnyContent = Confluence.Blueprints.Sharelinks.previewLoading();
                var spinnyContainer = $(spinnyContent).appendTo(previewContainer);
                disableUrlAndTitleInputs($urlField, $titleField);
                $.ajax({
                    type: "get", 
                    url: restUrl,
                    data: {"url" : inputURL},
                    success: function (data, text) {
                        spinnyContainer.remove();
                        updateLinkPreview(data);
                        urlSyntaxError = false;
                        validateAll($container, spaceKey);
                        enableUrlAndTitleInputs($urlField, $titleField);
                    },
                    error: function (request, status, error) {
                        enableUrlAndTitleInputs($urlField, $titleField);
                        //invalid URL
                        if (400 == request.status) {
                            spinnyContainer.remove();

                            urlSyntaxError = true;
                            validateUrl($container, false);
                        }
                        //other errors
                        else {
                            spinnyContainer.remove();
                            var errorContent = Confluence.Blueprints.Sharelinks.previewError();
                            $(errorContent).appendTo(previewContainer);
                            urlSyntaxError = false;
                            validateAll($container, spaceKey);
                        }
                    }
                });
                // analytic input url by paste or type
                var inputAnalyticsProperties;
                if (isPasteUrl) {
                    inputAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.inputTypes.value.pasteUrl;
                }
                else {
                    inputAnalyticsProperties = Confluence.Blueprints.Sharelinks.Analytics.inputTypes.value.typeUrl;
                }
                Confluence.Blueprints.Sharelinks.Analytics.triggerInputTypes(inputAnalyticsProperties);
            }
        }
    }
})(AJS.$);