/**
 * Modified 2011-12-12 by Kevin Ford (kefo@loc.gov, kefo@3windmills.com)
 * 
 * This file, but not necessarily others in this package, is in the 
 * Public Domain.
 * 
 * http://creativecommons.org/publicdomain/mark/1.0/
 * 
 */

package gov.loc.ndmso.proxyfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import gov.loc.ndmso.proxyfilter.Log;

import gov.loc.ndmso.proxyfilter.RequestProxy;

/**
 * This class is the Filter.  doFilter is the class called by Tomcat in web.xml.
 * 
 */
public final class ProxyFilter implements Filter {
		private static final Log log = Log.getLog(ProxyFilter.class);
		private MultiThreadedHttpConnectionManager connManager = null;
	  
	  public void init(FilterConfig config) throws ServletException { 
		  // log.info("In ProxyFilter init");
		  
		  connManager = new MultiThreadedHttpConnectionManager();
		  
		  HttpConnectionManagerParams connParams = new HttpConnectionManagerParams();
		  connParams.setDefaultMaxConnectionsPerHost(15); // or some number of connections
		  connManager.setParams(connParams);

	  }

    /**
     * Finds the search and replace strings in the configuration file. Looks for
     * matching searchX and replaceX parameters.
     */

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    	final HttpServletRequest hsRequest = (HttpServletRequest) request;
        final HttpServletResponse hsResponse = (HttpServletResponse) response;

        String scheme = hsRequest.getScheme();
        String serverName = hsRequest.getServerName();
        int portNumber = hsRequest.getServerPort();
        String contextPath = hsRequest.getContextPath();
        String servletPath = hsRequest.getServletPath();
        String pathInfo = hsRequest.getPathInfo();
        String queryString = hsRequest.getQueryString();
        
        // log.info("scheme is " + scheme + "; serverName is " + serverName + "; portNumber is " + portNumber + "; contextPath is " + contextPath + "; servletPath is " + servletPath + "; pathInfo is " + pathInfo + "; queryString is " + queryString);
        
        servletPath = servletPath.replaceAll("/lcds", "/nlc");
        servletPath = servletPath.replaceAll("/lcwanew", "/lcwa");
        servletPath = servletPath.replaceAll("/x-tohap", "/tohap");
        servletPath = servletPath.replaceAll("/id/", "/");
        
        servletPath = servletPath.replaceAll(".jsp", ".xqy");
        
        // collection will be someting like
        //		/tohap
        //		/lcwa
        String[] pathParts=servletPath.split("/");
        String collection="";
        if ( !(pathParts[1].matches("static|fcgi\\-bin|media|audio|share")) ) {
        	collection=pathParts[1];
        }
        
        if (queryString != null) {
        	queryString = "?" + queryString;
        } else {
        	queryString = "";
        }
        if (pathInfo == null) {
        	pathInfo = "";
        } else {
        	pathInfo = pathInfo.replaceAll(".jsp", ".xqy");
        }
        //log.info("In ProxyFilter doFilter:  requesting " + servletPath + pathInfo + queryString);
        RequestProxy.execute("http://mar04vlp.loc.gov:8081" + servletPath + pathInfo + queryString, collection, hsRequest, hsResponse, connManager);

    }

    public void destroy() {
    	// log.info("In ProxyFilter destroy");
    	// connManager.closeIdleConnections(100);
    	connManager.shutdown();
    }
    
}

