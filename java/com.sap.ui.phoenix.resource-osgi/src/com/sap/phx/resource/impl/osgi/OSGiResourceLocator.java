/*
 * Copyright 2010 by SAP AG, Walldorf., http://www.sap.com.
 * All rights reserved. Use is subject to license terms.
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.phx.resource.impl.osgi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import com.sap.phx.resource.Resource;
import com.sap.phx.resource.ResourceLocator;
import com.sap.phx.resource.ResourceUtil;
import com.sap.phx.resource.impl.ServletResource;
import com.sap.sailing.gwt.ui.server.Activator;


/**
 * The class <b><code>OSGiResourceLocator</code></b> is used to find the 
 * Phoenix resource within an OSGi environment. Is extracts the bundle 
 * context out of the servlet context and uses the bundles to determine 
 * the resources from.
 * <br>
 * @author D039071@exchange.sap.corp
 * @author <code>Revision Author: ($Author: d039071 $)</code>
 * @version 1.0 - 20.12.2010 13:20:23
 * @version <code>Revision Version: ($Revision: #5 $)</code>
 * @version <code>Revision Date: ($Date: 2011/08/18 $)</code>
 */
public class OSGiResourceLocator implements ResourceLocator, BundleListener {

        
        /** pattern to split the require-bundle header to determine each bundle */
        private final static String REQUIRE_BUNDLE_SPLITTER = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        
        
        /** reference to the logger */
        private final static Logger LOG = Logger.getLogger(OSGiResourceLocator.class.getName());


        /** reference to the <code>ServletContext</code> */
        private ServletContext context = null;
        
        /** reference to the <code>BundleContext</code> */
        private BundleContext bundleContext = null;

        
        /** list of <code>BundleInfo</code>s of the required bundles */
        private Map<String, BundleInfo> requiredBundles = null;

        /** list of library <code>Bundle</code>s */
        private List<Bundle> libraryBundles = null;
        
        /** list of <code>Bundle</code>s to be aware of */
        private List<Bundle> resourceBundles = null;
        
        
        /**
         * constructs the class <code>OSGiResourceLocator</code>
         */
        public OSGiResourceLocator() {
                super();
        } // constructor
        
        
        /* (non-Javadoc)
         * @see com.sap.phx.resource.ResourceLocator#init(javax.servlet.ServletContext)
         */
        @Override
        public void init(ServletContext context) {
            this.bundleContext = Activator.getDefault();
                // keep the references to the servlet context and retrieve the
                // bundle context
                this.context = context;
//                this.bundleContext = (BundleContext) this.context.getAttribute(ATTR_BUNDLECONTEXT);
                this.bundleContext.addBundleListener(this);
                
                // determine the bundles which should be observed for resources
                this.libraryBundles = new ArrayList<Bundle>();
                this.libraryBundles.add(this.bundleContext.getBundle());
                for (Bundle bundle : this.bundleContext.getBundles()) {
                        if (this.isLibraryBundle(bundle)) {
                                this.libraryBundles.add(bundle);
                        }
                }
                
                // find the list of the required bundles in the list of 
                // available UI library bundles depending on their version
                // (this feature is not supported by the OSGi framework)
                String requireBundleString = (String) this.bundleContext.getBundle().getHeaders().get(Constants.REQUIRE_BUNDLE);
    String[] bundles = requireBundleString != null ? requireBundleString.split(REQUIRE_BUNDLE_SPLITTER) : new String[0];
    this.requiredBundles = new HashMap<String, BundleInfo>();
    for (String b : bundles) {
        BundleInfo info = new BundleInfo(b);
        this.requiredBundles.put(info.getSymbolicName(), info);
    }
                
    // determine the resource bundles
    this.determineResourceBundles();
          
        } // method: init


        /* (non-Javadoc)
         * @see com.sap.phx.resource.ResourceLocator#findResource(java.lang.String)
         */
        @Override
        public Resource findResource(String requestPath) {

                // check for a valid context
                assert(context != null);
                
                try {
                        
                        // resolve the resource either out of the bundle/bundle classpath or  
                        // out of the resource bundles
                        URL resourceUrl = this.bundleContext.getBundle().getResource(requestPath);
                        if (resourceUrl == null) {
                                for (Bundle bundle : this.resourceBundles) {
                                        resourceUrl = bundle.getResource(ResourceUtil.CLASSPATH_PATH_PREFIX + requestPath);
                                        if (resourceUrl != null) {
                                                return new ServletResource(this.context, requestPath, resourceUrl, "CLASSPATH");                        
                                        }
                                }
                        } else {
                                return new ServletResource(this.context, requestPath, resourceUrl, "CLASSPATH");                        
                        }
                        
                        // check locally => if not in classpath
                        resourceUrl = ResourceLocator.class.getClassLoader().getResource(ResourceUtil.CLASSPATH_PATH_PREFIX + requestPath);
                        if (resourceUrl != null) {
                                return new ServletResource(this.context, requestPath, resourceUrl, "CLASSPATH");                        
                        }
                        
          } catch (MalformedURLException ex) {
                        LOG.log(Level.WARNING, ex.getMessage(), ex); // (should never occur)
          }
  
    // check for themes request => negotiate
                if (Resource.isThemeRequest(requestPath)) {
                        // prevent additional negotiation of request path (once it has been done!)
                        String newRequestPath = Resource.negotiateThemeRequest(requestPath);
                        if (!requestPath.equals(newRequestPath)) {
                                return this.findResource(newRequestPath);
                        }
                }
                
                return null;
                
        } // method: findResource

        
        /* (non-Javadoc)
         * @see com.sap.phx.resource.ResourceLocator#listResources(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        @Override
        public List<Resource> listResources(String requestPath) {

                // check for a valid context
                assert(context != null);
                
                if (!Resource.isFileRequest(requestPath)) {

                        List<Resource> resources = new ArrayList<Resource>();
                        
                        // check the current bundle for relevant entries
                        Enumeration<String> resourcePaths = this.bundleContext.getBundle().getEntryPaths(requestPath);
                        if (resourcePaths != null) {
                                while (resourcePaths.hasMoreElements()) {
                                        resources.add(this.findResource(resourcePaths.nextElement()));
                                }
                        }

                        // check the classpath bundles for relevant entries
                        for (Bundle bundle : this.resourceBundles) {
                                resourcePaths = bundle.getEntryPaths(ResourceUtil.CLASSPATH_PATH_PREFIX + requestPath);
                                if (resourcePaths != null) {
                                        while (resourcePaths.hasMoreElements()) {
                                                resources.add(this.findResource(resourcePaths.nextElement().substring(ResourceUtil.CLASSPATH_PATH_PREFIX.length())));
                                        }
                                }
                        }
        
                        return resources;

                }
                
                return Collections.EMPTY_LIST;
                
        } // method: listResources


        /* (non-Javadoc)
         * @see com.sap.phx.resource.ResourceLocator#listResourcePaths(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        @Override
        public List<String> listResourcePaths(String requestPath) {

                // check for a valid context
                assert(context != null);
                
                if (!Resource.isFileRequest(requestPath)) {

                        List<String> paths = new ArrayList<String>();
                        
                        // check the current bundle for relevant entries
                        Enumeration<String> resourcePaths = this.bundleContext.getBundle().getEntryPaths(requestPath);
                        if (resourcePaths != null) {
                                while (resourcePaths.hasMoreElements()) {
                                        paths.add(resourcePaths.nextElement());
                                }
                        }

                        // check the classpath bundles for relevant entries
                        for (Bundle bundle : this.resourceBundles) {
                                resourcePaths = bundle.getEntryPaths(ResourceUtil.CLASSPATH_PATH_PREFIX + requestPath);
                                if (resourcePaths != null) {
                                        while (resourcePaths.hasMoreElements()) {
                                                paths.add(resourcePaths.nextElement().substring(ResourceUtil.CLASSPATH_PATH_PREFIX.length()));
                                        }
                                }
                        }
        
                        return paths;

                }
                
                return Collections.EMPTY_LIST;
                
        } // method: listResourcePaths


        /* (non-Javadoc)
         * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
         */
        @Override
        public void bundleChanged(BundleEvent event) {
                
                // update the list of library bundles
                Bundle b = event.getBundle();
                if (this.isLibraryBundle(b)) {

                        switch (event.getType()) {
                                case BundleEvent.INSTALLED:
                                        this.libraryBundles.add(b);
                                        break;
                                case BundleEvent.UNINSTALLED:
                                        if (this.libraryBundles.contains(b)) {
                                                this.libraryBundles.remove(b);
                                        }
                                        break;
                                case BundleEvent.UPDATED:
                                        if (this.libraryBundles.contains(b)) {
                                                this.libraryBundles.remove(b);
                                        }
                                        this.libraryBundles.add(b);
                        }
                
            // update the list of the resource bundles only when the new bundle 
                        // is in specified as required bundle for this bundle
                        if (this.requiredBundles.containsKey(b.getSymbolicName()) &&
                                        this.requiredBundles.get(b.getSymbolicName()).isBundleInRange(b)) {
                    this.determineResourceBundles();
                        }
                        
                }
          
        } // method: bundleChanged

        
        /**
         * determines the resource bundles, which can be used for resource 
         * determination by the OSGi resource handler
         */
        private void determineResourceBundles() {
                
                // determine the resource bundles
                Map<String, Bundle> bundles = new HashMap<String, Bundle>();
                for (Bundle b : this.libraryBundles) {
                        // only check bundles, which are required
                        BundleInfo bi = this.requiredBundles.get(b.getSymbolicName());
                        if (bi != null) {
                                // check the bundle to be in the range as defined in require-bundle
                                if (bi.isBundleInRange(b)) {
                                        // is the bundle already added - cause it could only be added once!
                                        if (!bundles.containsKey(b.getSymbolicName())) {
                                                // no: so add it!
                                                bundles.put(b.getSymbolicName(), b);
                                        } else {
                                                // yes: so only add the bundle with the latest version!
                                                Bundle old = bundles.get(b.getSymbolicName());
                                                if (b.getVersion().compareTo(old.getVersion()) > 0) {
                                                        bundles.put(b.getSymbolicName(), b);
                                                }
                                        }
                                }
                        }
                }
                this.resourceBundles = new ArrayList<Bundle>(bundles.values());
                
        } // method: determineResourceBundles
        

        /**
         * checks within the MANIFEST for containing the information to be a UILIbrary
         * @param bundle reference to the <code>Bundle</code>
         * @return true, if the <code>Bundle</code> is a UILibrary in Phoenix manner
         */
        private boolean isLibraryBundle(Bundle bundle) {
                String header = (String) bundle.getHeaders().get("x-sap-ui-ContentTypes");
                return header != null && header.contains("UILibrary");
        } // method: isLibraryBundle
        
        
  /**
   * The inner class <b><code>BundleInfo</code></b> parses the require-bundle
   * string and provides support for checking a <code>Bundle</code> to be in
   * the range of the require-bundle specification. 
   * <br>
   * @author D039071@exchange.sap.corp
   * @author <code>Revision Author: ($Author: d039071 $)</code>
   * @version 1.0 - 18.08.2011 09:47:18
   * @version <code>Revision Version: ($Revision: #5 $)</code>
   * @version <code>Revision Date: ($Date: 2011/08/18 $)</code>
   */
  static class BundleInfo {
        
        
        /** pattern to parse the bundle version information */
        private static final Pattern BUNDLE_VERSION = Pattern.compile(".*bundle-version=\"([\\(\\[])?([^,]*)?(,([^,\\)\\]]*)?([\\)\\]])?)?\".*", Pattern.DOTALL);


        /** symbolic name of the bundle */
        private String symbolicName = null;
        
        /** min version */
        private Version minVersion = null;
        
        /** min version inclusive */
        private boolean minInclusive = false;
        
        /** max version */
        private Version maxVersion = null;
        
        /** max version inclusive */
        private boolean maxInclusive = false;
        
        
        /**
         * constructs the class <code>BundleInfo</code>
         * @param requireBundleString require bundle string
         */
        public BundleInfo(String requireBundleString) {
                super();
                
                // options are separated with a semicolon
                if (requireBundleString.indexOf(";") == -1) {
                        // no additional options, only the symbolic name of the bundle
                        this.symbolicName = requireBundleString;
                } else {
                        
                        // split the symbolic name and the options
                        this.symbolicName = requireBundleString.substring(0, requireBundleString.indexOf(";"));
                        String options = requireBundleString.substring(requireBundleString.indexOf(";") + 1);
                        
                        // extract the bundle version information with a regex to fetch the 
                        // inclusive/exclusive information and the min and max version if specified
        Matcher m = BUNDLE_VERSION.matcher(options);
        if (m.matches()) {
                for (int i = 0; i <= m.groupCount(); i++) {
                        this.minInclusive = m.group(1) != null && m.group(1).equals("[");
                        this.minVersion = m.group(2) != null ? Version.parseVersion(m.group(2)) : null;
                        this.maxVersion = m.group(4) != null ? Version.parseVersion(m.group(4)) : null;
                        this.maxInclusive = m.group(5) != null && m.group(5).equals("]");
                }
        }
                        
                }
                
        } // constructor

        
        /**
         * returns the symbolic name of the bundle
         * @return symbolic name of the bundle
         */
        public String getSymbolicName() {
                return this.symbolicName;
        } // method: getSymbolicName
        
        
        /**
         * check the given <code>Bundle</code> to be in the specifed range of the  
         * require-bundle information
         * @param bundle <code>Bundle</code> to be checked
         * @return true, if the <code>Bundle</code> is in range
         */
        public boolean isBundleInRange(Bundle bundle) {

                        // new org.osgi.framework.Version("0.18.0").compareTo(new org.osgi.framework.Version("0.17.0"))
                        //  (int) 1
                        // new org.osgi.framework.Version("0.17.0").compareTo(new org.osgi.framework.Version("0.18.0"))
                        //  (int) -1
                        // new org.osgi.framework.Version("0.18.0").compareTo(new org.osgi.framework.Version("0.18.0"))
                        //  (int) 0
                
                if (bundle != null) {
                        // check for being in range
                        if (this.minVersion == null && this.maxVersion == null) {
                        // no versions are specified: supported! 
                                return true;
                        } else if (this.minVersion != null && this.maxVersion == null) {
                                // only the min version is specified so we accept the same 
                                // an larger versions of the bundle
                                return this.minVersion.compareTo(bundle.getVersion()) <= 0;
                        } else {
                                // in case of min and max version is specified we check that the
                                // given bundle is between those versions by checking the inlusion
                                // and exclusion information of the specified range
                                int result = this.minVersion.compareTo(bundle.getVersion());
                                if (result <= (this.minInclusive ? 0 : -1)) {
                                        result = this.maxVersion.compareTo(bundle.getVersion());
                                        return (result >= (this.maxInclusive ? 0 : 1));
                                }
                                
                        }
                        
                }
                
                return false;
                
        } // method: isBundleInRange
        
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
                
                StringBuffer infoString = new StringBuffer(this.symbolicName);
                
                if (minVersion != null && maxVersion == null) {
                        infoString.append(" - ");
                        infoString.append(this.minVersion);
                } else if (minVersion != null && maxVersion != null) {
                        infoString.append(" - ");
                        infoString.append(minInclusive ? "[" : "(");
                        infoString.append(minVersion);
                        infoString.append(",");
                        infoString.append(maxVersion);
                        infoString.append(maxInclusive ? "]" : ")");
                }

                return infoString.toString();
                
        } // method: toString
        
        
  } // inner class: BundleInfo
  
  
} // class: OSGiResourceLocator 