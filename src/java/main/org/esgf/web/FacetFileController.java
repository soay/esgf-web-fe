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

package org.esgf.web;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.esgf.metadata.JSONArray;
import org.esgf.metadata.JSONException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 *
 *
 * @author john.harney
 *
 */
@Controller
@RequestMapping("/facetfileproxy")
public class FacetFileController {
    
    private final static Logger LOG = Logger.getLogger(FacetFileController.class);

    //Name and location of the file (In this case, the base package location)
    private final static String FACET_FILE = "facets.properties";
    
    /**
     * This method gives a response to a request (called by esgf-web-fe/web/scripts/esgf/solr.js) for the facets defined in the file facets.properties.  
     * The logic places all facets (delimited by a ; for the time being) into a json array, where it is parsed in solr.js
     * 
     * @return String Json representation of the facet array
     * 
     * @throws IOException
     * @throws JSONException
     * @throws ParserConfigurationException
     */
    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody String doGet() throws IOException, ParserConfigurationException, JSONException {
      
        final File file = new ClassPathResource(FACET_FILE).getFile();
      
        String [] tokens = parseFacets(file);
        JSONArray facet_arrJSON = new JSONArray(tokens);
  
        String jsonContent = facet_arrJSON.toString();
  
        LOG.debug("doGet facetfileproxy");
  
        return jsonContent;
    } //end doGet

    
    /**
     * Helper method for assembling a facet array string from the file facets.properties.  
     * The file exists in the root package. It will return an empty array if the file does not exist.
     * 
     * @return String Json representation of the facet array
     * 
     */
    private String [] parseFacets(File file) {
        
        try {
            String fileContents = FileUtils.readFileToString(file);
            
            LOG.debug("FileContents: " + fileContents + "\n---\n");
            
            String delims = ";";
            String [] tokens = fileContents.split(delims);
            
            for(int i=0;i<tokens.length;i++) {
                LOG.debug(tokens[i]);
            }
            return tokens;
        } catch(Exception e) {
            LOG.debug("Could not find file: " + file.getName());
            return new String[0];		
        }
        
        
    }
    
}
