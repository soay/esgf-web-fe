package org.esgf.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.esgf.propertiesreader.PropertiesReader;
import org.esgf.propertiesreader.PropertiesReaderFactory;
import org.esgf.srm.SRMControls;
import org.esgf.srm.utils.SRMUtils;
import org.esgf.srmcache.SRMCacheStore;
import org.esgf.srmcache.SRMCacheStoreFactory;
import org.esgf.srmcache.SRMEntry;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SRMCacheStoreController {

    
    public static String DB_TYPE = "postgres";
    
    private SRMCacheStore srm_cache;
    private String failure_message;
    private String success_message;
    
    public static void main(String [] args) {
        
        /*
        SRMCacheStoreController srm_cache_controller = new SRMCacheStoreController();
        
        
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        
        String dataset_id = "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov";
        mockRequest.addParameter("dataset_id", dataset_id);
        
        String file_id = "b";
        mockRequest.addParameter("file_id", file_id);
        
        String openid = "openid1";
        mockRequest.addParameter("openid", openid);
        */
        
        SRMCacheStoreFactory srmCacheStore = new SRMCacheStoreFactory();
        
        SRMCacheStore srm_cache = srmCacheStore.makeSRMCacheStore(DB_TYPE); 
        
        System.out.println("Initializing cache ");
        srm_cache.initializeCacheStore();
        System.out.println("End Initializing cache ");
        
    }
    
    public SRMCacheStoreController() {



        if(!SRMUtils.srm_disabled) {
            System.out.println("From SRMCacheStoreController->");
            SRMCacheStoreFactory srmCacheStore = new SRMCacheStoreFactory();
            
            srm_cache = srmCacheStore.makeSRMCacheStore(DB_TYPE); 
    
            
            PropertiesReaderFactory factory = new PropertiesReaderFactory();
            PropertiesReader srm_props = factory.makePropertiesReader("SRM");
    
            this.success_message = srm_props.getValue("success_message");
            this.failure_message = srm_props.getValue("failure_message");
        }
        //srm_cache.initializeCacheStore();
        
        //System.exit(0);
        
    }
    
    
    
    
    //input srmentry stuff (dataset_id and file_id)
    @RequestMapping(method=RequestMethod.POST, value="/addSRMEntry")
    public @ResponseBody String addSRMEntry(HttpServletRequest request) {
    
        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        String file_id = request.getParameter("file_id");
        if(file_id == null) {
            return failure_message;
        }
        String openid = request.getParameter("openid");
        if(openid == null) {
            return failure_message;
        }
        
        
        //add a dummy value for isCached until we find a better use for it
        String timeStamp = Long.toString(System.currentTimeMillis());
        
        long expirationLong = Long.parseLong(timeStamp) + SRMControls.expiration;
        String expiration = Long.toString(expirationLong);
        
        String bestmannumber = "V-0.0.0000";
        
        //SRMEntry srm_entry = new SRMEntry(file_id,dataset_id,isCached,timeStamp,expiration,openid);
        SRMEntry srm_entry = new SRMEntry(file_id,dataset_id,timeStamp,expiration,bestmannumber);

        if(srm_cache.addSRMEntry(srm_entry) == 0) {
            return success_message;
        } else {
            return failure_message;
        }
        
    }
    
    //automatically populate the db
    @RequestMapping(method=RequestMethod.POST, value="/initializeSRMEntryList")
    public @ResponseBody String initializeCacheDBFromSolr(HttpServletRequest request) {
    
        String openid = request.getParameter("openid");
        openid = "rootAdmin";
        if(openid == null) {
            return failure_message;
        }
        
        this.srm_cache.initializeCacheStore();
        return success_message;
    }
    
    @RequestMapping(method=RequestMethod.GET, value="/isCachedFile")
    public @ResponseBody String isCachedFile(HttpServletRequest request) {
        
        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        String file_id = request.getParameter("file_id");
        if(file_id == null) {
            return failure_message;
        }
        SRMEntry srm_entry = this.srm_cache.getSRMEntryForFile_id(dataset_id, file_id);

        if(srm_entry == null) {
            return failure_message;
        }

        long currentTimeStamp = System.currentTimeMillis();
        
        String expiration = srm_entry.getExpiration();
        
        if(Long.parseLong(expiration) > currentTimeStamp) {
            /*
            if(file_id.equals("ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1.t341f02.FAMIPr.cam2.h0.1978-09.nc|esg2-sdnl1.ccs.ornl.gov")){
                System.out.println("\n\nFILEID: " + file_id + "\n\t" + currentTimeStamp + " " + expiration + "\n");
                System.out.println("success");
            }
            */
            return success_message;
        } else {
            /*
            if(file_id.equals("ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1.t341f02.FAMIPr.cam2.h0.1978-09.nc|esg2-sdnl1.ccs.ornl.gov")){
                System.out.println("\n\nFILEID: " + file_id + "\n\t" + currentTimeStamp + " " + expiration + "\n");
            
                System.out.println("failure");
            }
            */
            return failure_message;
        }
        
        
    }
    
    
    //input dataset_id
    @RequestMapping(method=RequestMethod.GET, value="/isCachedDataset")
    public @ResponseBody String isCachedDataset(HttpServletRequest request) {
        
        
        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        
        long currentTimeStamp = System.currentTimeMillis();
        
        List<SRMEntry> srm_entries = this.srm_cache.getSRMEntriesForDataset_id(dataset_id);
        for(int i=0;i<srm_entries.size();i++) {
            String timestamp = srm_entries.get(i).getExpiration();
            long expirationTimeStamp = Long.parseLong(timestamp);
            if(expirationTimeStamp < currentTimeStamp) {
                return failure_message;
            }
        }
        
        
        return success_message;
    
    }
    
    //input dataset_id and file_id
    @RequestMapping(method=RequestMethod.GET, value="/getSRMEntry")
    public @ResponseBody String getSRMEntry(HttpServletRequest request) {

        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        String file_id = request.getParameter("file_id");
        if(file_id == null) {
            return failure_message;
        }
        String openid = request.getParameter("openid");
        if(openid == null) {
            return failure_message;
        }
        
        SRMEntry srm_entry = srm_cache.getSRMEntryForFile_id(dataset_id, file_id);
        if(srm_entry == null) {
            return failure_message;
        } else {
            return srm_entry.toXML();
        }
        
        
    }
    
    //input dataset_id
    @RequestMapping(method=RequestMethod.GET, value="/getSRMEntryDatasetId")
    public @ResponseBody String getSRMEntryDatasetId(HttpServletRequest request) {
        
        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        
        
        List<SRMEntry> srm_entries = srm_cache.getSRMEntriesForDataset_id(dataset_id);
        if(srm_entries == null) {
            return failure_message;
        } else {
            return srmEntryListXML(srm_entries);
        }
        
    }
    
    //input open_id
    @RequestMapping(method=RequestMethod.GET, value="/getSRMEntryOpenId")
    public @ResponseBody String getSRMEntryOpenId(HttpServletRequest request) {
        
        String openid = request.getParameter("openid");
        if(openid == null) {
            return failure_message;
        }
        
        List<SRMEntry> srm_entries = srm_cache.getSRMEntriesForOpenid(openid);
        if(srm_entries == null) {
            return failure_message;
        } else {
            return srmEntryListXML(srm_entries);
        }
        
    }

    //input srmentry stuff (dataset_id and file_id)
    @RequestMapping(method=RequestMethod.PUT, value="/updateSRMEntry")
    public @ResponseBody String updateSRMEntry(HttpServletRequest request) {

        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        String file_id = request.getParameter("file_id");
        if(file_id == null) {
            return failure_message;
        }
        String openid = request.getParameter("openid");
        if(openid == null) {
            return failure_message;
        }
        
        //System.out.println("\nDatasetid to updated->" + dataset_id);
        
        SRMEntry srm_entry = srm_cache.getSRMEntryForFile_id(dataset_id, file_id);
        
        String timeStamp = Long.toString(System.currentTimeMillis());
        long expirationLong = Long.parseLong(timeStamp) + SRMControls.expiration;

        srm_entry.setTimeStamp(timeStamp);
        srm_entry.setExpiration(Long.toString(expirationLong));
        
        
        if(this.srm_cache.updateSRMEntry(srm_entry) == 1) {
            //System.out.println("UPDATE SUCCESSFUL");
            return success_message;
        } else {
            return failure_message;
        }
        
        
    }
    

    //input srmentry stuff (dataset_id and file_id)
    @RequestMapping(method=RequestMethod.DELETE, value="/deleteSRMEntry")
    public @ResponseBody String deleteSRMEntry(HttpServletRequest request) {

        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            return failure_message;
        }
        String file_id = request.getParameter("file_id");
        if(file_id == null) {
            return failure_message;
        }
        String openid = request.getParameter("openid");
        if(openid == null) {
            return failure_message;
        }
        
        SRMEntry srm_entry = srm_cache.getSRMEntryForFile_id(dataset_id, file_id);
        
        if(this.srm_cache.deleteSRMEntry(srm_entry) == 1) {
            return success_message;
        } else {
            return failure_message;
        }
        
    
    }
    
    
    public static String srmEntryListXML(List<SRMEntry> entries) {
        String xml = "";
        
        Element srmentryListingEl = new Element("srm_entry");//this.toElement();
        
        for(int i=0;i<entries.size();i++) {
            SRMEntry srm_entry = entries.get(i);
            srmentryListingEl.addContent(srm_entry.toElement());
        }
        

        XMLOutputter outputter = new XMLOutputter();
        xml = outputter.outputString(srmentryListingEl);
        
        return xml;
    }
    
}
