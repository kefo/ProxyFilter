/**
 * Modified 2011-12-12 by Kevin Ford (kefo@loc.gov, kefo@3windmills.com)
 * 
 * It's a little ironic that the below copyright and license must pollute the 
 * top part of this file.  It appears that Tuckey took the work of Ansorg and placed 
 * it under his (Tuckey's) own copyright.  Nothing wrong with that; makes for a better world.
 * 
 * By the same logic, as a federal employee who modified this for work-related purposes, 
 * can I push this into the public domain?
 * 
 * Also, in a networked world, could we not just include a link to the license? Oy.
 * 
 */

/**
 * Copyright (c) 2008, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package gov.loc.ndmso.proxyfilter;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import gov.loc.ndmso.proxyfilter.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

/**
 * This class is responsible for a proxy http request.
 * It takes the incoming request and then it creates a new request to the target address and copies the response of that proxy request
 * to the response of the original request.
 * <p/>
 * This class uses the commons-httpclient classes from Apache.
 * <p/>
 * User: Joachim Ansorg, <jansorg@ksi.gr>
 * Date: 19.06.2008
 * Time: 16:02:54
 */
public class RequestProxy {
    private static final Log log = Log.getLog(RequestProxy.class);

    /**
     * This method performs the proxying of the request to the target address.
     *
     * @param target     The target address. Has to be a fully qualified address. The request is send as-is to this address.
     * @param hsRequest  The request data which should be send to the
     * @param hsResponse The response data which will contain the data returned by the proxied request to target.
     * @throws java.io.IOException Passed on from the connection logic.
     */
    public static void execute(final String target, final HttpServletRequest hsRequest, final HttpServletResponse hsResponse, MultiThreadedHttpConnectionManager connManager) throws IOException {
        // log.info("execute, target is " + target);
        // log.info("response commit state: " + hsResponse.isCommitted());

        if (target == null || "".equals(target) || "".equals(target.trim())) {
            // log.error("The target address is not given. Please provide a target address.");
            return;
        }

        // log.info("checking url");
        final URL url;
        try {
            url = new URL(target);
        } catch (MalformedURLException e) {
            // log.error("The provided target url is not valid.", e);
            return;
        }

        // log.info("setting up the host configuration");

        final HostConfiguration config = new HostConfiguration();

        ProxyHost proxyHost = getUseProxyServer((String) hsRequest.getAttribute("use-proxy"));
        if (proxyHost != null) config.setProxyHost(proxyHost);

        final int port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
        config.setHost(url.getHost(), port, "http");

        // log.info("config is " + config.toString());

        final HttpMethod targetRequest = setupProxyRequest(hsRequest, url);
        if (targetRequest == null) {
            // log.error("Unsupported request method found: " + hsRequest.getMethod());
            return;
        }

        //perform the request to the target server
        final HttpClient client = new HttpClient(connManager);
        //if (log.isInfoEnabled()) {
            // log.info("client state" + client.getState());
            // log.info("client params" + client.getParams().toString());
            // log.info("executeMethod / fetching data ...");
        //}

        final int result = client.executeMethod(config, targetRequest);

        //copy the target response headers to our response
        setupResponseHeaders(targetRequest, hsResponse);
        
        String binRegex = ".*\\.(?i)(jpg|tif|png|gif|bmp|mp3|mpg)(.*$)*";
        String binRegexRedux = ".*(?i)(\\/thumb)(.*$)*";
        
        if ( target.matches(binRegex) || target.matches(binRegexRedux) ) {
        	// log.info("binRegex matched: " + target);
            InputStream originalResponseStream = targetRequest.getResponseBodyAsStream();
            
            if (originalResponseStream != null) {

            	if (targetRequest.getResponseHeaders().toString().matches("(?i).*content-type.*")) {
            		PrintWriter responseStream = hsResponse.getWriter();
            		copyStreamText(targetRequest.getResponseBodyAsString(), responseStream);
            	} else {
            		OutputStream responseStream = hsResponse.getOutputStream();
            		copyStreamBinary(originalResponseStream, responseStream);
            	}
            }
        	
        } else {
        	// log.info("binRegex NOT matched: " + target);
        	String proxyResponseStr = targetRequest.getResponseBodyAsString();
        	// the body might be null, i.e. for responses with cache-headers which leave out the body
        
        	if (proxyResponseStr != null) {
        		//proxyResponseStr = proxyResponseStr.replaceAll("xqy", "jsp");
        	
        		proxyResponseStr = proxyResponseStr.replaceAll("National Library Catalog", "Library of Congress Data Service");
        		proxyResponseStr = proxyResponseStr.replaceAll("Library of Congress collections", "Library of Congress bibliographic data");
        		proxyResponseStr = proxyResponseStr.replaceAll("Library of Congress Collections", "Library of Congress Bibliographic Data");
        		
        		proxyResponseStr = proxyResponseStr.replaceAll("action=\"/", "action=\"/diglib/");
        		proxyResponseStr = proxyResponseStr.replaceAll("href=\"/", "href=\"/diglib/");
        		proxyResponseStr = proxyResponseStr.replaceAll("src=\"/", "src=\"/diglib/");
        		proxyResponseStr = proxyResponseStr.replaceAll("url\\(/", "url\\(/diglib/");
        		
        		proxyResponseStr = proxyResponseStr.replaceAll("/nlc/", "/lcds/");
        		
        		PrintWriter responseStream = hsResponse.getWriter();
        		copyStreamText(proxyResponseStr, responseStream);
        	}
        }

        // log.info("set up response, result code was " + result);
        targetRequest.releaseConnection();
        
        // SimpleHttpConnectionManager connManager = (SimpleHttpConnectionManager) client.getHttpConnectionManager();
        // connManager.closeIdleConnections(1000);
        
        // HttpConnection httpConn = connManager.getConnection(config);
        // httpConn.releaseConnection();
        
    }

    public static void copyStreamText(String in, PrintWriter out) throws IOException {
        out.write(in);
    }
    
    public static void copyStreamBinary(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[65536];
        int count;
        while ((count = in.read(buf)) != -1) {
            out.write(buf, 0, count);
        }
    }


    public static ProxyHost getUseProxyServer(String useProxyServer) {
        ProxyHost proxyHost = null;
        if (useProxyServer != null) {
            String proxyHostStr = useProxyServer;
            int colonIdx = proxyHostStr.indexOf(':');
            if (colonIdx != -1) {
                proxyHostStr = proxyHostStr.substring(0, colonIdx);
                String proxyPortStr = useProxyServer.substring(colonIdx + 1);
                if (proxyPortStr != null && proxyPortStr.length() > 0 && proxyPortStr.matches("[0-9]+")) {
                    int proxyPort = Integer.parseInt(proxyPortStr);
                    proxyHost = new ProxyHost(proxyHostStr, proxyPort);
                } else {
                    proxyHost = new ProxyHost(proxyHostStr);
                }
            } else {
                proxyHost = new ProxyHost(proxyHostStr);
            }
        }
        return proxyHost;
    }

    private static HttpMethod setupProxyRequest(final HttpServletRequest hsRequest, final URL targetUrl) throws IOException {
        final String methodName = hsRequest.getMethod();
        final HttpMethod method;
        if ("POST".equalsIgnoreCase(methodName)) {
            PostMethod postMethod = new PostMethod();
            InputStreamRequestEntity inputStreamRequestEntity = new InputStreamRequestEntity(hsRequest.getInputStream());
            postMethod.setRequestEntity(inputStreamRequestEntity);
            method = postMethod;
        } else if ("GET".equalsIgnoreCase(methodName)) {
            method = new GetMethod();
        } else {
            // log.warn("Unsupported HTTP method requested: " + hsRequest.getMethod());
            return null;
        }

        method.setFollowRedirects(false);
        method.setPath(targetUrl.getPath());
        method.setQueryString(targetUrl.getQuery());

        Enumeration e = hsRequest.getHeaderNames();
        if (e != null) {
            while (e.hasMoreElements()) {
                String headerName = (String) e.nextElement();
                if ("host".equalsIgnoreCase(headerName)) {
                    //the host value is set by the http client
                    continue;
                } else if ("content-length".equalsIgnoreCase(headerName)) {
                    //the content-length is managed by the http client
                    continue;
                } else if ("accept-encoding".equalsIgnoreCase(headerName)) {
                    //the accepted encoding should only be those accepted by the http client.
                    //The response stream should (afaik) be deflated. If our http client does not support
                    //gzip then the response can not be unzipped and is delivered wrong.
                    continue;
                } else if (headerName.toLowerCase().startsWith("cookie")) {
                    //fixme : don't set any cookies in the proxied request, this needs a cleaner solution
                    continue;
                }

                Enumeration values = hsRequest.getHeaders(headerName);
                while (values.hasMoreElements()) {
                    String headerValue = (String) values.nextElement();
                    // log.info("setting proxy request parameter:" + headerName + ", value: " + headerValue);
                    method.addRequestHeader(headerName, headerValue);
                }
            }
        }
        
        // add rs5/tomcat5 request header for ML
        method.addRequestHeader("X-Via", "tomcat5");

        // log.info("proxy query string " + method.getQueryString());
        return method;
    }

    private static void setupResponseHeaders(HttpMethod httpMethod, HttpServletResponse hsResponse) {
        //if ( log.isInfoEnabled() ) {
            // log.info("setupResponseHeaders");
            // log.info("status text: " + httpMethod.getStatusText());
            // log.info("status line: " + httpMethod.getStatusLine());
        //}

        //filter the headers, which are copied from the proxy response. The http lib handles those itself.
        //Filtered out: the content encoding, the content length and cookies
        for (int i = 0; i < httpMethod.getResponseHeaders().length; i++) {
            Header h = httpMethod.getResponseHeaders()[i];
            if ("content-encoding".equalsIgnoreCase(h.getName())) {
                continue;
            } else if ("content-length".equalsIgnoreCase(h.getName())) {
                continue;
            } else if ("transfer-encoding".equalsIgnoreCase(h.getName())) {
                continue;
            } else if (h.getName().toLowerCase().startsWith("cookie")) {
                //retrieving a cookie which sets the session id will change the calling session: bad! So we skip this header.
                continue;
            } else if (h.getName().toLowerCase().startsWith("set-cookie")) {
                //retrieving a cookie which sets the session id will change the calling session: bad! So we skip this header.
                continue;
            }

            hsResponse.addHeader(h.getName(), h.getValue());
            // log.info("setting response parameter:" + h.getName() + ", value: " + h.getValue());
        }
        // fix me what about the response footers? (httpMethod.getResponseFooters())

        if (httpMethod.getStatusCode() != 200) {
            hsResponse.setStatus(httpMethod.getStatusCode());
        }
    }
}

