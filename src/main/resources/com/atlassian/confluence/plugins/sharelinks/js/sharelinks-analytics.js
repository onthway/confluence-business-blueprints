/*
 * Facade service for analytics event handling
 * */
Confluence.Blueprints.Sharelinks.Analytics = {

    inputTypes : {
        name : "blueprints.sharelinks.input",
        value : {
            pasteUrl : { type : "paste-url" }, 
            typeUrl : { type : "type-url" }
        }
    },
    
    linkTypes : {
        name : "blueprints.sharelinks.link",
        value : {
            video : { link : "video" }, 
            image : { link : "image" },
            noVideoImage: { link : "no-video-image" },
            noContent: { link : "no-content" }
        }
    },
    
    errorTypes : {
        name : "blueprints.sharelinks.error",
        value : {
            duplicatedPage : { error : "page-duplicated" }
        }
    },
    
    submitData : {
        name : "blueprints.sharelinks.submit",
        value : {
            editTitle : { submit : "edit-title" },
            noEditTitle : { submit : "no-edit-title" },
            comment : { submit : "comment" },
            noComment : { submit : "no-comment" },
            share : { submit : "share" },
            noShare : { submit : "no-share" }
        }
    },
    
    triggerInputTypes : function(properties) {
        AJS.EventQueue = AJS.EventQueue || [];
        AJS.EventQueue.push({
            name : Confluence.Blueprints.Sharelinks.Analytics.inputTypes.name,
            properties : properties
        });
    },

    triggerLinkTypes : function(properties) {
        AJS.EventQueue = AJS.EventQueue || [];
        AJS.EventQueue.push({
            name : Confluence.Blueprints.Sharelinks.Analytics.linkTypes.name,
            properties : properties
        });
    },
    
    triggerErrorTypes : function(properties) {
        AJS.EventQueue = AJS.EventQueue || [];
        AJS.EventQueue.push({
            name : Confluence.Blueprints.Sharelinks.Analytics.errorTypes.name,
            properties : properties
        });
    },
    
    triggerSubmitData : function(properties) {
        AJS.EventQueue = AJS.EventQueue || [];
        AJS.EventQueue.push({
            name : Confluence.Blueprints.Sharelinks.Analytics.submitData.name,
            properties : properties
        });
    }
};