package com.atlassian.confluence.plugins.decisions;

import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.plugins.BusinessBlueprintsContextProviderHelper;
import com.atlassian.confluence.plugins.decisions.DecisionsContextProvider.StatusMacroDetails;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class DecisionsContextProviderTest
{
    DecisionsContextProvider decisionsContextProvider;
    @Mock
    BusinessBlueprintsContextProviderHelper helper;
    @Mock
    private I18NBeanFactory mockI18NBeanFactory;
    @Mock
    private LocaleManager mockLocaleManager;
    @Mock
    private TemplateRenderer mockTemplateRenderer;
    @Mock
    private I18NBean mockI18nBean;

    private String YELLOW_STATUS = "YELLOW";
    private String GREEN_STATUS = "GREEN";
    private String GREY_STATUS = "GREY";

    @Before
    public void setUp() throws Exception
    {
        decisionsContextProvider = new DecisionsContextProvider(helper);
        when(mockI18NBeanFactory.getI18NBean(any(Locale.class))).thenReturn(mockI18nBean);
        when(mockI18nBean.getText("decisions.blueprint.wizard.form.status.progress")).thenReturn("In Progress");
        when(mockI18nBean.getText("decisions.blueprint.wizard.form.status.closed")).thenReturn("Decided");
        when(mockI18nBean.getText("decisions.blueprint.wizard.form.status.open")).thenReturn("Not Started");
        when(helper.getI18nBean()).thenReturn(mockI18nBean);
    }

    @Test
    public void testCorrectColoursForStatusMacro() throws Exception
    {
        String statusColourHtml = "<ac:parameter ac:name=\"colour\">%s</ac:parameter>";
        Assert.assertTrue(statusColourResult(YELLOW_STATUS).contains(String.format(statusColourHtml, "Yellow")));
        Assert.assertTrue(statusColourResult(GREEN_STATUS).contains(String.format(statusColourHtml, "Green")));
        Assert.assertTrue(statusColourResult(GREY_STATUS).contains(String.format(statusColourHtml, "Grey")));
    }

    private String statusColourResult(String status)
    {
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Map<String, Object> contextMap = (Map<String, Object>) invocation.getArguments()[2];
                
                String expectedResult = "<ac:macro ac:name=\"status\">" +
                                        "   <ac:parameter ac:name=\"title\">" + contextMap.get("status") + "</ac:parameter>" +
                                        "   <ac:parameter ac:name=\"colour\">" + contextMap.get("statusColour") + "</ac:parameter>" +
                                        "</ac:macro>";
                return expectedResult;
            }
        }).when(helper).renderFromSoy(anyString(), eq("Confluence.Blueprints.Decisions.statusTemplate.soy"), anyMap());
        
        Map<String, Object> contextMap = Maps.newHashMap();
        contextMap.put("status", status);
        String statusTemplate = (String) decisionsContextProvider.getContextMap(contextMap).get("status");
        return statusTemplate;
    }
}
