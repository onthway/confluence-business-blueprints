/**
 * This file defines the main behaviour for the Sharelinks Bookmarklet.
 * 
 */
AJS.toInit(function($) {
    var $bookmarkletForm = $("#bookmarklet-form"),
        $urlField = $("#bookmarklet-url"),
        $titleField = $("#bookmarklet-title"),
        urlSyntaxError = false,
        confluenceLocalStorage = Confluence.storageManager("confluence-shared-links-bookmarklet");

    /*
     * Update the error status, set focus to erroneous input, and return true if
     * there are errors
     */
    function updateError($field, error, shouldFocus) {
        $field.siblings(".error").html(error);
        var hasError = false;
        if (error) {
            hasError = true;
            if (shouldFocus) {
                // if field is space select will set focus for select2 control
                if ($field.attr("id") === "space-select") {
                    $("#space-select").select2("focus");
                } else {
                    $field.focus();
                }
            }
        }
        return hasError;
    }

    function validateUrl() {
        var url = $.trim($urlField.val());
        var error = "";
        if (!url) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.url.required");
        } else if (urlSyntaxError) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.url.syntaxError");
        }

        return updateError($urlField, error, true);
    }

    function getSelectedSpaceKey() {
        var space = $("#space-select").select2("data");
        return space.id;
    }

    function validateTitle() {
        var title = $.trim($titleField.val());
        var error = "";
        if (!title) {
            error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.title.required");
        } else {
            var selectedSpaceId = getSelectedSpaceKey();
            if (selectedSpaceId && !Confluence.Blueprint.canCreatePage(selectedSpaceId, title)) {
                error = AJS.I18n.getText("sharelinks.blueprint.wizard.form.validation.title.duplicated");
            }
        }
        return updateError($titleField, error, true);
    }

    function validateSpace() {
        var selectedSpaceId = getSelectedSpaceKey();
        var error = "";
        if (!selectedSpaceId) {
            error = AJS.I18n.getText("bookmarklet.form.validation.space.required");
        }

        return updateError($("#space-select"), error, true);
    }

    function validateAll() {
        return !(validateUrl() | validateTitle() | validateSpace());
    }

    var $messageView = $(".bookmarklet-action-messages");
    
    function displayError(errorMessage) {
        // will display the received message error from create page action
        var $errorResult = $(Confluence.Blueprints.SharelinksBookmarklet.bookmarkletShareError({
            "errorMessage" : errorMessage
        }));
        $messageView.empty();
        $errorResult.appendTo($messageView);
    }
    
    function bindEventsForResultView() {
        $("#bookmarklet-close-button").click(function() {
            window.close();
        });
        
        $("div.bookmarklet-result-text a").click(function(e) {
            e.preventDefault();
            url = this.getAttribute("href");
            window.open(url, '_blank');
            window.close();
        });
    }

    function displayShareSuccess(responseData) {
        var pageId = responseData.pageId;
        if (pageId) {
            var pageResultUrl = Confluence.getContextPath() + "/pages/viewpage.action?pageId=" + pageId;
            var $successResult = $(Confluence.Blueprints.SharelinksBookmarklet.bookmarkletShareSuccess({
                "pageResultUrl" : pageResultUrl
            }));
            var $resultView = $(".bookmarklet-content");
            $resultView.empty();
            $successResult.appendTo($resultView);

            // CONFDEV-18440: Shouldn't show the warning message if user has already finished sharing links
            $resultView.parent().find(".aui-message").remove();

            bindEventsForResultView();
        } else {
            displayError(AJS.I18n.getText("bookmarklet.page.create.error"));
        }
    }

    var formSpinner;

    function disableForm() {
        // disable
        $bookmarkletForm.find("input,textarea").attr("disabled", "disabled");
        // show waiting
        var $loadingContainer = $("#main-bookmarklet");
        var $spinnyContainer = $("<div class='loading-data'></div>").appendTo($loadingContainer);
        var radius = 30;
        formSpinner = Raphael.spinner($spinnyContainer[0], radius, "#666");
        $spinnyContainer.css('top', $loadingContainer.height() / 2 + radius);
    }

    function enableForm() {
        // stop waiting
        if (formSpinner) {
            formSpinner();
            formSpinner = null;
            $(".loading-data").remove();
        }
        // enable form input
        $bookmarkletForm.find("input").removeAttr("disabled");
        if (canCreateComment) {
            $("#bookmarklet-comment").removeAttr("disabled");
        }
    }

    function createBlueprintShareLinks() {
        var contextJson = {
            url : $urlField.val(),
            label : $("#bookmarklet-label").val(),
            sharewith : $("#bookmarklet-sharewith").val(),
            comment : $("#bookmarklet-comment").val(),
            title : $titleField.val()
        };

        var submitData = {
            spaceKey : $("#space-select").val(),
            moduleCompleteKey : "com.atlassian.confluence.plugins.confluence-business-blueprints:sharelinks-blueprint",
            context : contextJson
        };

        disableForm();
        $.ajax({
            type : "POST",
            contentType : "application/json",
            url : Confluence.getContextPath() + "/rest/create-dialog/1.0/content-blueprint/create-content",
            data : JSON.stringify(submitData),
            success : function(response) {
                enableForm();
                displayShareSuccess(response);
            },
            error : function(request, status, error) {
                enableForm();
                displayError(AJS.I18n.getText("bookmarklet.page.create.error"));
            }
        });
    }

    // Create select2 for select space input
    $("#space-select").auiSelect2({
        escapeMarkup : function(markup) {
            return markup;
        },
        formatNoMatches : function() {
            return Confluence.Blueprints.SharelinksBookmarklet.spaceNoMatches();
        },
        formatResult : function(result) {
            return Confluence.Blueprints.SharelinksBookmarklet.displaySpace({"spaceName": result.text});
        },
        formatSelection : function(result) {
            return Confluence.Blueprints.SharelinksBookmarklet.displaySpace({"spaceName": result.text});
        },
        containerCssClass : "bookmarklet-space-container"
    });

    $bookmarkletForm.submit(function() {
        if (validateAll()) {
            confluenceLocalStorage.setItem("lastSelectedSpace", getSelectedSpaceKey());
            createBlueprintShareLinks();
        }
        // don't submit page, only call ajax for create page
        return false;
    });

    // Add placeholder for input filter space
    var $searchInput = $bookmarkletForm.find(".bookmarklet-space-container .select2-input");
    if ($searchInput.length && "placeholder" in $searchInput[0]) {
        $searchInput.attr("placeholder", (AJS.I18n.getText("bookmarklet.form.label.space.placeholder") + " ..."));
    }

    /** *** Implement get title ***** */

    var previousInputURL;

    var $spinnyUrl = $("#spinnyUrl");
    function disableUrlAndTitleInputs() {
        $spinnyUrl.addClass("aui-icon aui-icon-wait");
        $urlField.attr("disabled", "disabled");
        $titleField.attr("disabled", "disabled");
    }

    function enableUrlAndTitleInputs() {
        $spinnyUrl.removeClass("aui-icon aui-icon-wait");
        $urlField.removeAttr("disabled");
        $titleField.removeAttr("disabled");
    }

    function createEscapedLink(inputURL) {
        var hrefURL = inputURL;
        if (!inputURL.match('^http://') && !inputURL.match('^https://')) {
            hrefURL = 'http://' + inputURL;
        }
        var tagLink = $("<a/>").attr({
            href : hrefURL,
            target : "_blank"
        }).text(inputURL);

        var escapedLink = $("<div/>").append(tagLink).html();
        return escapedLink;
    }
    
    function displayErrorLink(url) {
        // create escaped URL from provided URL
        var escapedLink = createEscapedLink(url);
        var errorMessage = AJS.I18n.getText("sharelinks.blueprint.wizard.preview.error", escapedLink);
        displayError(errorMessage);
    }
    
    function updateLinkInformation(linkMetaData) {
        if(linkMetaData.title) {
            $titleField.val(linkMetaData.title);
            $messageView.empty();
        } else {
            displayErrorLink(linkMetaData.sourceURL);
        }
    }

    function getLinkPreview() {
        var inputURL = $urlField.val();
        inputURL = $.trim(inputURL);
        if (inputURL) {
            var restUrl = Confluence.getContextPath() + "/rest/sharelinks/1.0/link";
            if (inputURL !== previousInputURL) {
                disableUrlAndTitleInputs();
                previousInputURL = inputURL;
                $.ajax({
                    type : "get",
                    url : restUrl,
                    data : {
                        "url" : inputURL
                    },
                    success : function(data, text) {
                        enableUrlAndTitleInputs();
                        urlSyntaxError = false;
                        updateLinkInformation(data);
                        validateAll();
                    },
                    error : function(request, status, error) {
                        enableUrlAndTitleInputs();
                        // invalid URL
                        if (400 === request.status) {
                            urlSyntaxError = true;
                        }
                        // other errors
                        else {
                            displayErrorLink(inputURL);
                            urlSyntaxError = false;
                        }
                        validateUrl();
                    }
                });
            }
        }
    }

    function bindEvents() {
        previousInputURL = '';
        $urlField.bind('paste', function() {
            setTimeout(getLinkPreview, 0);
        });
        $urlField.change(getLinkPreview);
        $titleField.change(function() {
            if ($titleField.siblings(".error").html !== "") {
                validateTitle();
            }
        });
    }

    // Add more extra input fields    
    var canCreateComment = false;
    function updateCommentFieldPermission() {
        var commentText = $("#bookmarklet-comment");
        if (canCreateComment) {
            commentText.removeAttr("disabled");
            commentText.attr("placeholder", AJS.I18n.getText("sharelinks.blueprint.wizard.form.label.comment.placeholder"));
        } else {
            commentText.val("");
            commentText.attr("disabled", "disabled");
            commentText.attr("placeholder", AJS.I18n.getText("sharelinks.blueprint.wizard.comment.nopermissions"));
        }
    }

    function checkCommentPermission() {
        $.ajax({
            type : "get",
            dataType : "json",
            url : Confluence.getContextPath() + "/rest/sharelinks/1.0/can-create-comment",
            data : {
                spaceKey : getSelectedSpaceKey()
            },
            success : function(data) {
                canCreateComment = data;
                updateCommentFieldPermission();
            }
        });
    }

    function setSelectedSpace() {
        var lastSelected = confluenceLocalStorage.getItem("lastSelectedSpace");

        //check if the last selected space does not exists in the list
        if ($("#space-select option[value='" + lastSelected + "']").length != 0)
        {
            $("#space-select").data('select2').val(lastSelected);
        };
    }

    $("#space-select").on("change", function(e) {
        checkCommentPermission();
        validateTitle();
    });

    if($urlField.val()) {
        getLinkPreview();
    }

    // bind label picker for topic
    Confluence.Blueprints.Sharelinks.autocompleteMultiLabel.build($("#bookmarklet-label"));

    setSelectedSpace();
    checkCommentPermission();
    bindEvents();
});
