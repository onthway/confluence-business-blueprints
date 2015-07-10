package com.atlassian.confluence.plugins.sharelinksbookmarklet.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.setup.settings.DarkFeaturesManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.spaces.SpaceType;
import com.atlassian.confluence.spaces.SpacesQuery;
import com.atlassian.confluence.user.ConfluenceUser;
import com.opensymphony.webwork.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.confluence.security.SpacePermission.CREATEEDIT_PAGE_PERMISSION;

public class BookmarkletAction extends ConfluenceActionSupport implements ServletRequestAware
{
    private static final Logger log = LoggerFactory.getLogger(BookmarkletAction.class);
    private HttpServletRequest request;

    private SpaceManager spaceManager;
    private DarkFeaturesManager darkFeaturesManager;

    private List<Space> globalSpaces;
    private List<Space> favouriteSpaces;
    private Space personalSpace;
    private String loginURL;

    private String bookmarkedURL;

    @Override
    public String execute() throws Exception
    {
        // Get all global space which user has create page permission
        SpacesQuery globalSpacesListBuilder = SpacesQuery.newQuery()
                                                        .forUser(getRemoteUser())
                                                        .withSpaceType(SpaceType.GLOBAL)
                                                        .withPermission(CREATEEDIT_PAGE_PERMISSION)
                                                        .build();

        globalSpaces = spaceManager.getAllSpaces(globalSpacesListBuilder);

        ConfluenceUser authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser != null)
            personalSpace = spaceManager.getPersonalSpace(authenticatedUser.getName());

        favouriteSpaces = computeFavouriteSpaces(globalSpaces);

        // favourite spaces shouldn't appear in the global list as
        globalSpaces.removeAll(favouriteSpaces);

        loginURL = computeLoginURL();

        return SUCCESS;
    }

    public List<Space> getAvailableGlobalSpaces()
    {
        return globalSpaces;
    }

    public String getPersonalSpaceKey()
    {
        return personalSpace != null ? personalSpace.getKey() : null;
    }

    public String getLoginURL()
    {
        return loginURL;
    }

    private String computeLoginURL() throws UnsupportedEncodingException
    {
        String contextPath = request.getContextPath();
        String currentURLWithoutContextPath = request.getRequestURI().substring(contextPath.length());
        if (request.getQueryString() != null)
        {
            currentURLWithoutContextPath += "?" + request.getQueryString();
        }
        return contextPath + "/login.action?os_destination=" + URLEncoder.encode(currentURLWithoutContextPath, "UTF-8");
    }

    private List<Space> computeFavouriteSpaces(List<Space> permittedGlobalSpaces)
    {
        if (getRemoteUser() == null)
            return Collections.emptyList();

        List<Space> favouriteSpaces = labelManager.getFavouriteSpaces(getRemoteUser().getName());

        if (personalSpace != null)
            favouriteSpaces.remove(personalSpace);

        // only get favourites space which user has create page permission 
        if (permittedGlobalSpaces.isEmpty())
        {
            for (Iterator<Space> spaceIterator = favouriteSpaces.iterator(); spaceIterator.hasNext();)
            {
                Space space = spaceIterator.next();
                if (!permissionManager.hasCreatePermission(getRemoteUser(), space, Page.class))
                    spaceIterator.remove();
            }
        }
        else
        {
            favouriteSpaces.retainAll(permittedGlobalSpaces);
        }

        return favouriteSpaces;
    }

    public List<Space> getFavouriteSpaces()
    {
        return favouriteSpaces;
    }

    @Override
    public void setServletRequest(HttpServletRequest request)
    {
        this.request = request;
    }
    
    public String getBookmarkedURL()
    {
        return bookmarkedURL;
    }

    public void setBookmarkedURL(String bookmarkedURL)
    {
        this.bookmarkedURL = bookmarkedURL;
    }

    public void setSpaceManager(SpaceManager spaceManager)
    {
        this.spaceManager = spaceManager;
    }

    public void setDarkFeaturesManager(final DarkFeaturesManager darkFeaturesManager)
    {
        this.darkFeaturesManager = darkFeaturesManager;
    }
}
