package com.atlassian.confluence.plugins.sharelinks.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.pages.CommentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.plugins.blueprint.business.PluginConstants;
import com.atlassian.confluence.plugins.createcontent.api.events.BlueprintPageCreateEvent;
import com.atlassian.confluence.plugins.sharepage.api.SharePageService;
import com.atlassian.confluence.plugins.sharepage.api.ShareRequest;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleCompleteKey;

public class SharelinksBlueprintListener implements DisposableBean
{
    private static final Logger logger = LoggerFactory.getLogger(SharelinksBlueprintListener.class);

    private static final ModuleCompleteKey SHARELINKS_BLUEPRINT_KEY = new ModuleCompleteKey(
            PluginConstants.PLUGIN_KEY, PluginConstants.SHARELINKS_KEY);

    private final CommentManager commentManager;
    private final EventPublisher evenPublisher;
    private final SharePageService sharePageService;
    private final PermissionManager permissionManager;
    private final LabelManager labelManager;
    private final UserAccessor userAccessor;

    public SharelinksBlueprintListener(EventPublisher eventPublisher, CommentManager commentManager,
            SharePageService sharePageService, LabelManager labelManager, PermissionManager permissionManager, UserAccessor userAccessor)
    {
        this.evenPublisher = eventPublisher;
        this.commentManager = commentManager;
        this.sharePageService = sharePageService;
        this.labelManager = labelManager;
        this.permissionManager = permissionManager;
        this.userAccessor = userAccessor;
        eventPublisher.register(this);
    }

    @EventListener
    public void onBlueprintCreateEvent(BlueprintPageCreateEvent event)
    {
        ModuleCompleteKey moduleCompleteKey = event.getBlueprintKey();
        if (!SHARELINKS_BLUEPRINT_KEY.equals(moduleCompleteKey))
        {
            return;
        }

        Page blueprintPage = event.getPage();
        Map<String, Object> context = event.getContext();
        String comment = (String) context.get("comment");
        String shareWith = (String) context.get("sharewith");
        String label = (String) context.get("label");

        if (logger.isDebugEnabled())
        {
            logger.debug("Event caught with context {}", event.getContext());
        }

        if (permissionManager.hasCreatePermission(getUser(), event.getPage().getSpace(), Comment.class))
        {
            addCommentIfNotBlank(blueprintPage, comment);
        }
        addLabelIfNotBlank(blueprintPage, label);

        //send share-page request
        shareWithUsers(blueprintPage, shareWith, comment);
    }

    private User getUser()
    {
        return AuthenticatedUserThreadLocal.getUser();
    }

    private void addCommentIfNotBlank(Page blueprintPage, String comment)
    {
        if (StringUtils.isNotBlank(comment))
        {   
            comment = GeneralUtil.plain2html(comment);
            commentManager.addCommentToObject(blueprintPage, null, comment);
        }
    }
    
    private void addLabelIfNotBlank(Labelable blueprintPage, String label)
    {   
        if (StringUtils.isNotBlank(label))
        {
            Set<String> labels = new HashSet<String>();
            labels.addAll(Arrays.asList(label.split(",")));
            for (String labelValue : labels)
            {   
                Label newLabel = new Label(labelValue);   
                labelManager.addLabel(blueprintPage, newLabel);
            }     
        }
    }

    private void shareWithUsers(Page page, String sharewith, String note)
    {
        if (StringUtils.isNotBlank(sharewith))
        {
            Set<String> usersToShare = new HashSet<String>();
            usersToShare.addAll(getUserKeys(Arrays.asList(sharewith.split(","))));

            ShareRequest shareContent = new ShareRequest();
            shareContent.setEntityId(page.getId());
            shareContent.setEntityType(ShareRequest.EntityType.PAGE.getValue());
            shareContent.setUsers(usersToShare);
            shareContent.setNote(note);
            //avoid NPE in the service impl, should remove later
            shareContent.setEmails(Collections.<String>emptySet());

            sharePageService.share(shareContent);
        }
    }

    private List<String> getUserKeys(List<String> listUsername)
    {
        List<String> listUserkey = new ArrayList<String>();
        ConfluenceUser user;
        for (String username : listUsername)
        {
            user = userAccessor.getUserByName(username);
            if (null != user)
            {
                listUserkey.add(user.getKey().getStringValue());
            }
        }
        return listUserkey;
    }

    @Override
    public void destroy() throws Exception
    {
        evenPublisher.unregister(this);
    }
}
