package com.sap.sse.gwt.linker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.impl.SelectionInformation;

/**
 * This class creates an application manifest file for HTML 5 Offline Application Caching in the 
 * output folder of your client side gwt compilation results.</br></br>
 * Follow those steps to use HTML 5 Offline Application Caching in your bundle:
 * <ol>
 * <li>Add {@code manifest="YOURMODULENAME/appcache.manifest"} to the {@code <html>} tag in your base html file.</li>
 * <li>Subclass {@link ManifestLinker} and override the method {@link #staticCachedFiles()}.</li>
 * <li>Define a mime type mapping for the <code>manifest</code> extension in your web.xml file.
 * <pre>
 * {@code <mime-mapping>
 * <extension>manifest</extension>
 * <mime-type>text/cache-manifest</mime-type>
 * </mime-mapping>
 * }</pre>
 * </li>
 * <li>Define the subclassed linker in your gwt.xml file and add the linker.</li>
 * <pre>
 * {@code <define-linker name="manifest" class="YOUR SUBCLASSED LINKER"/>
 * <add-linker name="manifest"/>
 * }</pre>
 * <li>Make sure to import the those packages into your MANIFEST.MF file.</li>
 * com.google.gwt.core.ext</br>
 * com.google.gwt.core.ext.linker</br>
 * com.google.gwt.core.ext.linker.impl
 * </ol>
 * 
 * @author Alexander Ries
 * 
 */
@LinkerOrder(LinkerOrder.Order.POST)
public abstract class ManifestLinker extends AbstractLinker {

    private static final String MANIFEST = "appcache.manifest";
    private static final String MANIFESTTEMPLATE = "cache.manifest.template";

    @Override
    public String getDescription() {
        return ManifestLinker.class.getName();
    }

    @Override
    public ArtifactSet link(TreeLogger logger, LinkerContext context, ArtifactSet artifacts, boolean onePermutation) throws UnableToCompleteException {
        ArtifactSet result = new ArtifactSet(artifacts);
        if (onePermutation) {
            return result;
        }
        if (result.find(SelectionInformation.class).isEmpty()) {
            logger.log(TreeLogger.INFO, "Warning: Clobbering " + MANIFEST + " to allow debugging. " + "Recompile before deploying your app!");
            artifacts = null;
        } else {
            result.add(emitLandingPageCacheManifest(context, logger, artifacts));
        }
        return result;
    }

    private Artifact<?> emitLandingPageCacheManifest(LinkerContext context, TreeLogger logger, ArtifactSet artifacts) throws UnableToCompleteException {
        StringBuilder publicSourcesSb = new StringBuilder();
        StringBuilder staticResoucesSb = new StringBuilder();
        if (artifacts != null) {
            for (@SuppressWarnings("rawtypes") 
            Artifact artifact : artifacts) {
                if (artifact instanceof EmittedArtifact) {
                    EmittedArtifact ea = (EmittedArtifact) artifact;
                    String pathName = ea.getPartialPath();
                    if (pathName.endsWith("symbolMap") ||
                        pathName.endsWith(".xml.gz") ||
                        pathName.endsWith("rpc.log") ||
                        pathName.endsWith("gwt.rpc") ||
                        pathName.endsWith("manifest.txt") ||
                        pathName.startsWith("rpcPolicyManifest")) {
                    } else {
                        publicSourcesSb.append(pathName + "\n");
                    }
                }
            }
            String[] cacheExtraFiles = getCacheExtraFiles();
            for (int i = 0; i < cacheExtraFiles.length; i++) {
                staticResoucesSb.append(cacheExtraFiles[i]);
                staticResoucesSb.append("\n");
            }
        }
        String cacheManifestString = createCache(logger, context, publicSourcesSb, staticResoucesSb);
        return emitString(logger, cacheManifestString, MANIFEST);
    }

    protected String createCache(TreeLogger logger, LinkerContext context, StringBuilder publicSourcesSb, StringBuilder staticResoucesSb) throws UnableToCompleteException {
        try {
            String manifest = IOUtils.toString(getClass().getResourceAsStream(appCacheManifestTemplate()));
            manifest = manifest.replace("$UNIQUEID$", (new Date()).getTime() + "." + Math.random()).toString();
            manifest = manifest.replace("$STATICAPPFILES$", staticResoucesSb.toString());
            manifest = manifest.replace("$GENAPPFILES$", publicSourcesSb.toString());
            return manifest;
        } catch (IOException e) {
            //logger.log(TreeLogger.ERROR, "Could not read cache manifest template.", e);
            throw new UnableToCompleteException();
        }
    }

    private String[] getCacheExtraFiles() {
        String[] cacheExtraFiles = staticCachedFiles();
        return cacheExtraFiles == null ? new String[0] : Arrays.copyOf(cacheExtraFiles, cacheExtraFiles.length);
    }

    /**
     * Override this method to provide static files for caching. 
     * Include at least your root HTML and CSS file.
     * Also include all files referenced in your root HTML file.
     * See {@link DashboardManifestLinker} as an example.
     * */
    abstract protected String[] staticCachedFiles();

    protected String appCacheManifestTemplate() {
        return MANIFESTTEMPLATE;
    }
}
