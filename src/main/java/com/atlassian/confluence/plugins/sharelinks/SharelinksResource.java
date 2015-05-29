package com.atlassian.confluence.plugins.sharelinks;

import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.user.User;

import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
public class SharelinksResource
{
    private final LinkMetaDataExtractor linkMetaDataExtractor;
    private final PermissionManager permissionManager;
    
    private final static Status BAD_REQUEST = Response.Status.BAD_REQUEST;
    private final SpaceManager spaceManager;

    public SharelinksResource (LinkMetaDataExtractor linkMetaDataExtractor, PermissionManager permissionManager,
        SpaceManager spaceManager)
    {
        this.linkMetaDataExtractor = linkMetaDataExtractor;
        this.permissionManager = permissionManager;
        this.spaceManager = spaceManager;
    }

    @GET
    @Path("link")
    @Produces({ MediaType.APPLICATION_JSON })
    @AnonymousAllowed
    public Response getLinkMetaData(@QueryParam("url") String url)
    {
        try
        {
            LinkMetaData link = linkMetaDataExtractor.parseMetaData(url, true);
            return Response.ok(link).build();
        }
        catch (URISyntaxException e)
        {
            return Response.status(BAD_REQUEST).entity("The provided URL is invalid").build();
        }
    }

    @GET
    @Path("can-create-comment")
    @Produces(MediaType.APPLICATION_JSON)
    @AnonymousAllowed
    public Response canCreateComment(@QueryParam("spaceKey") String spaceKey)
    {
        final Space space = spaceManager.getSpace(spaceKey);
        if (space == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("No space found for spacekey").build();
        }
        final boolean canCreate = permissionManager.hasCreatePermission(getUser(), space, Comment.class);
        return Response.ok(canCreate).build();
    }

    private User getUser()
    {
        return AuthenticatedUserThreadLocal.getUser();
    }
}