<%@ include file="/WEB-INF/views/common/include.jsp" %>

<ti:insertDefinition name="main-layout" >

    <ti:putAttribute name="extrastyle">
        <link rel="stylesheet" href='<c:url value="/styles/security.css" />' type="text/css">
    </ti:putAttribute>

    <ti:putAttribute name="main">
        <div style="margin:0 auto; width:600px">

            <!-- retrieve current user status -->
            <sec:authentication property="principal" var="principal"/>
            <!-- retrieve openid cookie -->
            <c:set var="openid_cookie" value="esgf.idp.cookie"/>

            <c:choose>

                <c:when test="${principal=='anonymousUser'}">
                  
                    <!-- User is not authenticated -->
                    <p/>
	             	<h1>ESGF Login</h1>
	            	                           	                         	
	                <!-- the value of the action attribute must be the same as the URL intercepted by the spring security filter  -->
	                <form name="loginForm" action='<c:url value="/j_spring_openid_security_check"/>' >
	                    <div class="panel" style="width:600px">     
	                    	<c:if test="${param['failed']==true}">
	                    		<span class="myerror">Error: unable to resolve OpenID identifier.</span>
	                		</c:if>                           
	                        <table border="0" align="center">
	                            <tr>
	                                <td>
	                                    <img src='<c:url value="/images/openid.png"/>' width="40" hspace="10px" style="vertical-align:top" />
	                                </td>
	                                <td>
	                                    <table>
	                                        <tr>
	                                            <td align="right" class="required">Openid:</td>
	                                            <td align="left"><input type="text" name="openid_identifier" size="80" value="${cookie[openid_cookie].value}"/ ></td>
	                                            <td><input type="submit" value="Login" class="button"/></td>
	                                        </tr>
	                                        <tr>
	                                            <td>&nbsp;</td>
	                                            <td align="center" colspan="2">
	                                                <input type="checkbox" name="remember_openid" checked="checked" /> <span class="strong">Remember my OpenID</span> on this computer
	                                            </td>
	                                        </tr>
	                                    </table>
	                                </td>
	                            </tr>
	                        </table>
	                    </div>
	                </form>
	                <p/>
	                <div align="center">Please enter your OpenID. You will be redirected to your registration web site to login.</div>
	                <p/>
                </c:when>

                <c:otherwise>

					<p/>
                    <h1>ESGF Logout</h1>

                    <!-- User is authenticated -->
                    <table align="center">
                        <tr>
                            <td align="center" valign="top">
                                <div class="panel">
                                    Thanks, you are now logged in.
                                    <!-- <p/><span class="openidlink">&nbsp;</span> Your OpenID : <b><c:out value="${principal.username}"/></b> -->
                                    <form name="logoutForm" action='<c:url value="/j_spring_security_logout"/>'>
                                        <input type="submit" value="Logout" class="button"/>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </table>
                </c:otherwise>

            </c:choose>

        </div>
    </ti:putAttribute>

</ti:insertDefinition>