<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<%@ page session="false" buffer="none" %>
<%@ page import="org.apache.pluto.portalImpl.core.PortalURL" %>
<%@ page import="org.apache.pluto.portalImpl.core.PortalEnvironment" %>
<%@ page import="org.apache.pluto.portalImpl.aggregation.navigation.Navigation" %>
<%@ page import="org.apache.pluto.portalImpl.aggregation.navigation.NavigationTreeBean" %>
<jsp:useBean id="fragment" type="org.apache.pluto.portalImpl.aggregation.navigation.TabNavigation" scope="request" />
<table width="200px"  border="0" cellpadding="0" cellspacing="0"> 
  				 <tr><td><div class="Selection"><table width="100%" border="0" cellpadding="0" cellspacing="0">
              <tr>
							  <td class="CollapsedLeft"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
								<td class="Indent"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
							  <td class="TopMiddle"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
                <td class="CollapsedRight"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td> 
              </tr>
						</table></div></td></tr>
<%
    PortalURL url = PortalEnvironment.getPortalEnvironment(request).getRequestedPortalURL();
    NavigationTreeBean[] tree = fragment.getNavigationView(url);

    // Aaron's code -- to force the navigation tree to render fully expanded, and not offer links to non-leaf nodes
    //                 todo this is pretty bad and should be replaced by a subclass of TabNavigation
    //                 see http://svn.apache.org/repos/asf/portals/pluto/trunk/portal/src/java/org/apache/pluto/portalImpl/aggregation/navigation/
    java.util.List list = new java.util.ArrayList();
    java.util.Stack stack = new java.util.Stack();
    Navigation root = fragment.getRootNavigation();
    java.util.List childList = (java.util.List)root.getChildren();
    java.util.Map map = new java.util.HashMap();
    for(int i=childList.size()-1; i>=0; i--) {
        stack.push(childList.get(i));
        map.put(stack.peek(), new Integer(0));
    }
    while(!stack.isEmpty()) {
        Navigation nav = (Navigation) stack.pop();
        Integer depth = (Integer) map.get(nav);
        list.add(new NavigationTreeBean(nav, nav.getChildren().size() > 0 || url.isPartOfGlobalNavigation(nav.getLinkedFragment().getId()), depth.intValue()));
        childList = (java.util.List)nav.getChildren();
        for(int i=childList.size()-1; i>=0; i--) {
            stack.push(childList.get(i));
            map.put(stack.peek(), new Integer(depth.intValue()+1));
        }
    }
    tree = (NavigationTreeBean[]) list.toArray(new NavigationTreeBean[list.size()]);
    // End Aaron's Code

    for (int i=0; i<tree.length; i++) {
%>
<%
            Navigation nav = tree[i].navigation;
            boolean partOfNav = tree[i].partOfGlobalNav;

						if (tree[i].depth>0)
						{
%>
								<tr><td><div class="Subselection"><table width="100%" border="0" cellpadding="1" cellspacing="0"> 
                <tr>
                  <td class="Left">&nbsp;</td> 
                  <td class="Indent">&nbsp;</td> 
                  <td class="Middle">
<%
              for (int k=0; k<tree[i].depth; k++) 
							{
%>
  							&nbsp;&nbsp;&nbsp;
<%
              }
							  if (!partOfNav)
								{
%>
                    <a href="<%=new PortalURL(request, nav.getLinkedFragment()).toString()%>"><%=nav.getTitle()%></a>
<%
								}
								else
								{
%>                  <%=nav.getTitle()%>
<%
								}
%>
                  </td> 
                  <td class="Right">&nbsp;</td> 
                </tr> 
								</table></div></td></tr>
<%
						}
						else
						{
%>						
		
  						<tr><td><div class="Selection"><table width="100%" border="0" cellpadding="1" cellspacing="0">
              <tr>
							  <td class="CollapsedLeft">&nbsp;</td>
								<td class="Indent">&nbsp;</td>
							  <td class="TopMiddle">
<%
							  if (!partOfNav)
								{
%>                  <a href="<%=new PortalURL(request, nav.getLinkedFragment()).toString()%>"><%=nav.getTitle()%></a>
<%
								}
								else
								{
%>                  <%=nav.getTitle()%>
<%
								}
%>

 							      
	 						  </td>
                <td class="CollapsedRight">&nbsp;</td> 
              </tr>
						</table></div></td></tr>
<%
						}
    }
%>
  				 <tr><td><div class="Selection"><table width="100%" border="0" cellpadding="0" cellspacing="0">
              <tr>
							  <td class="CollapsedLeft"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
								<td class="Indent"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
							  <td class="TopMiddle"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
                <td class="CollapsedRight"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td> 
              </tr>
						</table></div></td></tr>
  </table>


