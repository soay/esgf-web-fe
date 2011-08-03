/*****************************************************************************
 * Copyright � 2011 , UT-Battelle, LLC All rights reserved
 *
 * OPEN SOURCE LICENSE
 *
 * Subject to the conditions of this License, UT-Battelle, LLC (the
 * �Licensor�) hereby grants to any person (the �Licensee�) obtaining a copy
 * of this software and associated documentation files (the "Software"), a
 * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
 * copy, modify, merge, publish, distribute, and/or sublicense copies of the
 * Software.
 *
 * 1. Redistributions of Software must retain the above open source license
 * grant, copyright and license notices, this list of conditions, and the
 * disclaimer listed below.  Changes or modifications to, or derivative works
 * of the Software must be noted with comments and the contributor and
 * organization�s name.  If the Software is protected by a proprietary
 * trademark owned by Licensor or the Department of Energy, then derivative
 * works of the Software may not be distributed using the trademark without
 * the prior written approval of the trademark owner.
 *
 * 2. Neither the names of Licensor nor the Department of Energy may be used
 * to endorse or promote products derived from this Software without their
 * specific prior written permission.
 *
 * 3. The Software, with or without modification, must include the following
 * acknowledgment:
 *
 *    "This product includes software produced by UT-Battelle, LLC under
 *    Contract No. DE-AC05-00OR22725 with the Department of Energy.�
 *
 * 4. Licensee is authorized to commercialize its derivative works of the
 * Software.  All derivative works of the Software must include paragraphs 1,
 * 2, and 3 above, and the DISCLAIMER below.
 *
 *
 * DISCLAIMER
 *
 * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
 * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
 * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
 * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
 * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
 * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
 * liabilities, penalties, fines, claims, causes of action, and costs and
 * expenses, caused by, resulting from or arising out of, in whole or in part
 * the use, storage or disposal of the SOFTWARE.
 *
 *
 ******************************************************************************/


/**
 * On request mapping:
 *
 *     url rewrite filter will take over first, then we do regular Spring mapping.
 *     RedirectView is discouraged here as it will mess up the current rewrite
 *     rule, use "redirect:" prefix instead, and it is regarded as a better alternative
 *     anyway.
 *
 * For any redirect trouble, please refers to ROOT/urlrewrite.xml
 *
 * @author John Harney (harneyjf@ornl.gov)
 * @author Feiyi Wang (fwang2@ornl.gov)
 *
 */
package org.esgf.adminui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.XMLSerializer;
import org.esgf.commonui.UserOperations;
import org.esgf.commonui.Utils;
import org.esgf.manager.InputManager;
import org.esgf.manager.InputManagerImpl;
import org.esgf.manager.OutputManager;
import org.esgf.manager.OutputManagerImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Node;

import esg.search.query.api.FacetProfile;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;

@Controller
@RequestMapping(value="/usermanagement")

public class ManageUsersController {

    private final static String ManageUsers_INPUT = "ManageUsers_input";
    private final static String ManageUsers_USER = "ManageUsers_user";
    private final static String ManageUsers_MODEL = "ManageUsers_model";

    private final static Logger LOG = Logger.getLogger(ManageUsersController.class);
    private final static String USERS_FILE = "C:\\Users\\8xo\\esgProjects\\esgf-6-29\\esgf-web-fe\\esgf-web-fe\\src\\java\\main\\users.file";

    private final static String ROOT_USER = "https://pcmdi3.llnl.gov/esgcet/myopenid/jfharney";
    
    
    private final static boolean writeXMLTOLogFlag = false;

    private final static boolean writeLogFlag = false;
    private final static boolean editLogFlag = false;
    private final static boolean deleteLogFlag = false;
    
    
    /**
     * List of invalid text characters -
     * anything that is not within square brackets.
     */
    private static Pattern pattern =
        Pattern.compile(".*[^a-zA-Z0-9_\\-\\.\\@\\'\\:\\;\\,\\s/()].*");

    public ManageUsersController() {
        LOG.debug("IN ManageUsersController Constructor");
    }

    /**
     * Method invoked in response to a GET request:
     * -) if invoked directly, a new set of facets is retrieved (but no results)
     * -) if invoked in response to a POST-REDIRECT,
     * @param request
     * @param input
     * @param result
     * @return
     * @throws IOException 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView doGet(final HttpServletRequest request,
            final @ModelAttribute(ManageUsers_INPUT) String ManageUsersInput) throws IOException {

        LOG.debug("------ManageUsersController doGet------");
        Map<String,Object> model = getModel(request,ManageUsersInput);
        
        /*
        Map<String,Object> model = new HashMap<String,Object>();
        
        if (request.getParameter(ManageUsers_MODEL)!=null) {
            // retrieve model from session
            model = (Map<String,Object>)request.getSession().getAttribute(ManageUsers_MODEL);

        } else {
            final File file = new File(USERS_FILE);
            LOG.debug("FileInfo->" + file.getName());
            User [] users = getUsersFromXML(file);
            
            // populate model
            model.put(ManageUsers_INPUT, ManageUsersInput);
            model.put(ManageUsers_USER, users);
            request.getSession().setAttribute(ManageUsers_MODEL, model);
            
        }
        */
        
        LOG.debug("------End ManageUsersController doGet------\n\n\n\n");
        return new ModelAndView("usermanagement", model);
    }
    
    
    
    
    /**
     * Method invoked in response to a POST request:
     * -) if invoked directly, a new set of facets is retrieved (but no results)
     * -) if invoked in response to a POST-REDIRECT,
     * @param request
     * @param input
     * @param result
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView doPost(final HttpServletRequest request,
            final @ModelAttribute(ManageUsers_INPUT) String ManageUsersInput) throws IOException {
            
        LOG.debug("------ManageUsersController doPost------");
        
        Map<String,Object> model = getModel(request,ManageUsersInput);
        
        
        //obtain the file of user info - this will be deprecated
        File file = new File(USERS_FILE);
        
        //get the userId from the cookie
        String userId = Utils.getIdFromHeaderCookie(request);
        LOG.debug("UserId Retrieved: " + userId);
        
        //get the type of operation (add, edit, delete)
        String type = Utils.getTypeFromQueryString(request);
        
        LOG.debug("TYPE->" + type);
        
        //from the type perform the appropriate operation
        if(type.equalsIgnoreCase("add")) {
            addUser(request,file);
        }
        else if(type.equalsIgnoreCase("edit")){
            editUser(request);
        }
        else if(type.equalsIgnoreCase("delete")) {
            deleteUser(request);
        }
        
        
        
        
        LOG.debug("------End ManageUsersController doPost------\n\n\n\n");
        return new ModelAndView("usermanagement", model);
    }

    
    /* Helper function for extracting the model */
    private Map<String,Object> getModel(final HttpServletRequest request,
                                       final @ModelAttribute(ManageUsers_INPUT)  String ManageUsersInput) throws IOException {
        Map<String,Object> model = new HashMap<String,Object>();
        
        if (request.getParameter(ManageUsers_MODEL)!=null) {
            // retrieve model from session
            model = (Map<String,Object>)request.getSession().getAttribute(ManageUsers_MODEL);

        } else {
            final File file = new File(USERS_FILE);
            LOG.debug("FileInfo->" + file.getName());
            User [] users = getUsersFromXML(file);
            
            // populate model
            model.put(ManageUsers_INPUT, ManageUsersInput);
            model.put(ManageUsers_USER, users);
            request.getSession().setAttribute(ManageUsers_MODEL, model);
            
        }
        
        return model;
    }
    
    
    /*
     * Helper method to formulate the model for the usermanagement view
     */
    private Map<String,Object> formModel(final HttpServletRequest request,final @ModelAttribute(ManageUsers_INPUT) String ManageUsersInput) throws IOException{
        
        Map<String,Object> model = new HashMap<String,Object>();

        if (request.getParameter(ManageUsers_MODEL)!=null) {
            LOG.debug("Not null");
            // retrieve model from session
            model = (Map<String,Object>)request.getSession().getAttribute(ManageUsers_MODEL);

        } else {
            LOG.debug("ManageUsers Input: " + ManageUsersInput);
            final File file = new File(USERS_FILE);
            User [] users = getUsersFromXML(file);
            
            // populate model
            model.put(ManageUsers_INPUT, ManageUsersInput);
            model.put(ManageUsers_USER, users);
            
            request.getSession().setAttribute(ManageUsers_MODEL, model);
        }
        return model;
    }
    
    private void editUser(final HttpServletRequest request) throws IOException {
        LOG.debug("*****In EditUser*****");
        if(editLogFlag)
            Utils.queryStringInfo(request);
        
        editUserInfoInXML(request);
        
        LOG.debug("*****End EditUser*****\n");

    }
    
    
    
    private void deleteUser(final HttpServletRequest request) throws IOException {
        LOG.debug("*****In DeleteUser*****");
        if(deleteLogFlag)
            Utils.queryStringInfo(request);
        
        deleteUserInfoFromXML(request);
        
        
        LOG.debug("*****End DeleteUser*****\n");

    }
    
    
    
    private void addUser(final HttpServletRequest request,File file) throws IOException {
        LOG.debug("\n\n*****In AddUser*****");
        
        
        //using the xml store
        UserOperations.writeUserInfoToXML(request,file);
        
        //using the esgf-security store
        //writeUserInfoToDB(request);
        /**/
        
        LOG.debug("*****End In AddUser*****\n\n");
        
    }
    
   
 
    
    
    
    /* helper function for getting users from xml store */
    private User [] getUsersFromXML(File file) throws IOException {
        
        LOG.debug("------ManageUsersController getUsersFromXML------");
        
        /* this logic is deprecated and only used for testing - it utilizes the xml in users.store */
        

        SAXBuilder builder = new SAXBuilder();
        String xmlContent = "";
        User [] userArray = new User[1];
        
        try{
            LOG.debug("Building document");
            
            Document document = (Document) builder.build(file);
            
            Element rootNode = document.getRootElement();
            LOG.debug("root name: " + rootNode.getName());
            
            List users = (List)rootNode.getChildren();
            userArray =  new User[users.size()];
            
            for(int i=0;i<users.size();i++)
            {
                
                Element userEl = (Element) users.get(i);
                List attributes = (List)userEl.getChildren();
                
                String firstName = "";
                String lastName = "";
                String userName = "";
                String emailAddress = "";
                String status = "";
                String organization = "";
                String city = "";
                String state = "";
                String country = "";
                String openId = "";
                String DN = "";
                String groups = "";
                
                for(int j=0;j<attributes.size();j++)
                {
                    Element attEl = (Element) attributes.get(j);
                    if(attEl.getName().equals("firstName")) {
                        firstName = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("lastName")) {
                        lastName = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("userName")) {
                        userName = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("emailAddress")) {
                        emailAddress = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("status")) {
                        status = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("organization")) {
                        organization = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("city")) {
                        city = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("state")) {
                        state = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("country")) {
                        country = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("openId")) {
                        openId = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("DN")) {
                        DN = attEl.getTextNormalize();
                    }
                    else if(attEl.getName().equals("groups")) {
                        groups = attEl.getTextNormalize();
                        
                    }
                }
                
                //create new User object
                //generic groups for now
                Group grp = new Group("CDIAC","Standard","Valid");
                Group [] grps = {grp};
                User user = new User(firstName,lastName,userName,emailAddress,status,organization,city,state,country,openId,DN,grps);
                
                userArray[i] = user;
                
            }
            
        }catch(Exception e) {
            LOG.debug("File not found");
            e.printStackTrace();
        }

        LOG.debug("------End ManageUsersController getUsersFromXML------");
        return userArray;
    }
    
    
    
    /* Operations over the XML data source (users.file) */
    
    /* Editing user info */
    private void editUserInfoInXML(final HttpServletRequest request) {
        String userName = request.getParameter("userName");
        String lastName = request.getParameter("lastName");
        String firstName = request.getParameter("firstName");
        String emailAddress = request.getParameter("emailAddress");
        String status = request.getParameter("status");
        
        /* this logic is deprecated and only used for testing - it utilizes the xml in users.store */
        final File file = new File(USERS_FILE);
        SAXBuilder builder = new SAXBuilder();
        String xmlContent = "";
        
        try{

            Document document = (Document) builder.build(file);
            
            Element rootNode = document.getRootElement();
            if(editLogFlag)
                LOG.debug("root name: " + rootNode.getName());
            
            List users = (List)rootNode.getChildren();
            //userArray =  new User[users.size()];
            
            for(int i=0;i<users.size();i++)
            {
                Element userEl = (Element) users.get(i);
                if(editLogFlag)
                    LOG.debug(userName + " " + userEl.getChild("userName").getTextNormalize());
                if(userEl.getChild("userName").getTextNormalize().equals(userName)) {
                    if(editLogFlag)
                        LOG.debug("\t\t\tChange this one");
                    userEl.getChild("lastName").setText(lastName);
                    userEl.getChild("firstName").setText(firstName);
                    userEl.getChild("status").setText(status);
                    userEl.getChild("emailAddress").setText(emailAddress);
                    
                    /*Debugging for groups
                     * MUST TAKE THIS OUT FOR PRODUCTION
                     */
                    if(userEl.getChild("groups") == null) {
                        Element groupsEl = new Element("groups");
                        Element groupEl = new Element("group");
                        groupEl.setText("group1");
                        groupsEl.addContent(groupEl);
                    }
                    
                }
            }
            
            
            
            

            XMLOutputter outputter = new XMLOutputter();
            xmlContent = outputter.outputString(rootNode);
            
            if(editLogFlag) {
                if(writeXMLTOLogFlag) {
                    LOG.debug("NEW XMLCONTENT \n" + xmlContent);
                }
            } 
                    
            Writer output = null;
            output = new BufferedWriter(new FileWriter(file));
            output.write(xmlContent);
            if(editLogFlag)
                LOG.debug("Writing to file");
            output.close();
            
            
        }catch(Exception e) {
            LOG.debug("Couldn't write new xml to file");
        }
    }
    
    
    /* Deleting user info */
    private void deleteUserInfoFromXML(final HttpServletRequest request) {
        String userName = request.getParameter("user");
        
        /* this logic is deprecated and only used for testing - it utilizes the xml in users.store */
        final File file = new File(USERS_FILE);
        SAXBuilder builder = new SAXBuilder();
        String xmlContent = "";
        
        try{

            Document document = (Document) builder.build(file);
            
            Element rootNode = document.getRootElement();
            if(deleteLogFlag)
                LOG.debug("root name: " + rootNode.getName());
            
            List users = (List)rootNode.getChildren();
            //userArray =  new User[users.size()];
            
            for(int i=0;i<users.size();i++)
            {
                Element userEl = (Element) users.get(i);
                if(deleteLogFlag)
                    LOG.debug(userName + " " + userEl.getChild("userName").getTextNormalize());
                if(userEl.getChild("userName").getTextNormalize().equals(userName)) {
                    if(deleteLogFlag)
                        LOG.debug("\t\t\tDelete this one");
                    rootNode.removeContent(userEl);
                }
            }
            

            XMLOutputter outputter = new XMLOutputter();
            xmlContent = outputter.outputString(rootNode);
            
            if(deleteLogFlag) {
                if(writeXMLTOLogFlag) {
                    LOG.debug("NEW XMLCONTENT \n" + xmlContent);
                }
            }
            Writer output = null;
            output = new BufferedWriter(new FileWriter(file));
            output.write(xmlContent);
            if(deleteLogFlag)
                LOG.debug("Writing to file");
            output.close();
            
        }catch(Exception e) {
            LOG.debug("Couldn't write new xml to file");
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    /**
     *
     * @param request
     * @return
     * @throws Exception
     */
    @ModelAttribute(ManageUsers_INPUT)
    public String formManageUsersInputObject(final HttpServletRequest request) throws Exception {
        LOG.debug("formManageUsersInputObject called");
        return "ManageUsers_Table here";
    }

    
    private void writeUserInfoToDB(final HttpServletRequest request) {
        LOG.debug("\n*****In WriteUserInfoToDB*****");
        LOG.debug("*****End WriteUserInfoTODB*****\n");

    }
    
    private void deleteUserInfoFromDB(final HttpServletRequest request) {
        LOG.debug("\n*****In WriteUserInfoToDB*****");
        LOG.debug("*****End WriteUserInfoTODB*****\n");

    }
    
    
    
    
    
    
    
    /* Deprecated */
    private User [] getUsersHardCoded() {
        
        
        Group group1 = new Group("CDIAC","Standard","Valid");
        Group group2 = new Group("C-LAMP","Standard","Valid");
        
        Group [] user1_groups = {group1, group2}; 
        Group [] user2_groups = {group2}; 
       
        
        User user1 = new User("user1_lastName","user1_firstName","user1_userName","user1_emailAddress",
                "user1_status","user1_organization","user1_city","user1_state","user1_country","user1_openId","user1_DN",user1_groups);
        User user2 = new User("user2_lastName","user2_firstName","user2_userName","user2_emailAddress",
                "user2_status","user2_organization","user2_city","user2_state","user2_country","user2_openId","user2_DN",user2_groups);
        
        User [] users = {user1,user2};
        
        return users;
    }
    
    
}




/*
if (request.getParameter(ManageUsers_MODEL)!=null) {
    LOG.debug("Not null");
    // retrieve model from session
    model = (Map<String,Object>)request.getSession().getAttribute(ManageUsers_MODEL);
} else {
    LOG.debug("null");
    LOG.debug("ManageUsers Input: " + ManageUsersInput);
    User [] users = getUsersFromXML();
    // populate model
    model.put(ManageUsers_INPUT, ManageUsersInput);
    model.put(ManageUsers_USER, users);
    request.getSession().setAttribute(ManageUsers_MODEL, model);
}
*/