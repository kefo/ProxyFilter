/**
 * Modified 2011-12-12 by John Smith (jsmith@name.com)
 * 
 * Removed references to slf4j
 * 
 */

/**
 * Copyright (c) 2005-2007, Paul Tuckey
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

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;


/**
 * Log copies the style of commons logging.  The only reason this exists is that many problems
 * were had with Log4j and commons-logging interfering with peoples installed configuration.
 * It is very easy to change this to log4j, slf4j or commons-logging by settting log level to "log4j", "slf4j" or "commons"
 * Note, this will fall back to system.log if not initialised with a context.
 *
 * @author Paul Tuckey
 * @version $Revision: 42 $ $Date: 2006-10-29 09:43:56 +1300 (Sun, 29 Oct 2006) $
 */
public class Log {

    private static Log localLog = Log.getLog(Log.class);

    // static vars
    private static ServletContext context = null;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static boolean usingSystemOut = true;
    private static boolean usingSystemErr = false;
    private static boolean usingLog4j = true;
    private static boolean usingCommonsLogging = true;

    private static boolean traceLevelEnabled = false;
    private static boolean debugLevelEnabled = false;
    private static boolean infoLevelEnabled = true;
    private static boolean warnLevelEnabled = false;
    private static boolean errorLevelEnabled = true;
    private static boolean fatalLevelEnabled = false;

    @SuppressWarnings("rawtypes")
	private Class clazz = null;

    private org.apache.log4j.Logger log4jLogger = null;
    private org.apache.commons.logging.Log commonsLog = null;

    @SuppressWarnings("rawtypes")
	private Log(Class clazz) {
        this.clazz = clazz;
        // check for log4j or commons
        isUsingLog4j();
        isUsingCommonsLogging();
    }

    public boolean isUsingLog4j() {
        if (usingLog4j && log4jLogger == null) {
            this.log4jLogger = org.apache.log4j.Logger.getLogger(clazz);
        }
        return usingLog4j;
    }

    public boolean isUsingCommonsLogging() {
        if (usingCommonsLogging && commonsLog == null) {
            this.commonsLog = org.apache.commons.logging.LogFactory.getLog(clazz);
        }
        return usingCommonsLogging;
    }

    public boolean isUsingSystemOut() {
        return usingSystemOut;
    }

    public boolean isUsingSystemErr() {
        return usingSystemErr;
    }

    public boolean isTraceEnabled() {
        if (isUsingLog4j()) return log4jLogger.isEnabledFor(org.apache.log4j.Priority.DEBUG);
        if (isUsingCommonsLogging()) return commonsLog.isTraceEnabled();
        return traceLevelEnabled;
    }

    public boolean isDebugEnabled() {
        if (isUsingLog4j()) return log4jLogger.isEnabledFor(org.apache.log4j.Priority.DEBUG);
        if (isUsingCommonsLogging()) return commonsLog.isDebugEnabled();
        return traceLevelEnabled || debugLevelEnabled;
    }

    public boolean isInfoEnabled() {
        if (isUsingLog4j()) return log4jLogger.isEnabledFor(org.apache.log4j.Priority.INFO);
        if (isUsingCommonsLogging()) return commonsLog.isInfoEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled;
    }

    public boolean isWarnEnabled() {
        if (isUsingLog4j()) return log4jLogger.isEnabledFor(org.apache.log4j.Priority.WARN);
        if (isUsingCommonsLogging()) return commonsLog.isWarnEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled || warnLevelEnabled;
    }

    public boolean isErrorEnabled() {
        if (isUsingLog4j()) return log4jLogger.isEnabledFor(org.apache.log4j.Priority.ERROR);
        if (isUsingCommonsLogging()) return commonsLog.isErrorEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled || warnLevelEnabled || errorLevelEnabled;
    }

    public boolean isFatalEnabled() {
        if (isUsingLog4j()) return log4jLogger.isEnabledFor(org.apache.log4j.Priority.FATAL);
        if (isUsingCommonsLogging()) return commonsLog.isFatalEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled || warnLevelEnabled || errorLevelEnabled || fatalLevelEnabled;
    }


    public void trace(Object o) {
        if (!isTraceEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.debug(o);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.trace(o);
            return;
        }
        write("TRACE", o);
    }

    public void trace(Object o, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.debug(o, throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.trace(o, throwable);
            return;
        }
        write("TRACE", o, throwable);
    }

    public void trace(Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.debug(throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.trace(throwable);
            return;
        }
        write("TRACE", throwable, throwable);
    }

    public void debug(Object o) {
        if (!isDebugEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.debug(o);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.debug(o);
            return;
        }
        write("DEBUG", o);
    }

    public void debug(Object o, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.debug(o, throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.debug(o, throwable);
            return;
        }
        write("DEBUG", o, throwable);
    }

    public void debug(Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.debug(throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.debug(throwable);
            return;
        }
        write("DEBUG", throwable, throwable);
    }

    public void info(Object o) {
        if (!isInfoEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.info(o);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.info(o);
            return;
        }
        write("INFO", o);
    }

    public void info(Object o, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.info(o, throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.info(o, throwable);
            return;
        }
        write("INFO", o, throwable);
    }

    public void info(Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.info(throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.info(throwable);
            return;
        }
        write("INFO", throwable, throwable);
    }

    public void warn(Object o) {
        if (!isWarnEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.warn(o);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.warn(o);
            return;
        }
        write("WARN", o);
    }

    public void warn(Object o, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.warn(o, throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.warn(o, throwable);
            return;
        }
        write("WARN", o, throwable);
    }

    public void warn(Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.warn(throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.warn(throwable);
            return;
        }
        write("WARN", throwable, throwable);
    }

    public void error(Object o) {
        if (!isErrorEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.error(o);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.error(o);
            return;
        }
        write("ERROR", o);
    }

    public void error(Object o, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.error(o, throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.error(o, throwable);
            return;
        }
        write("ERROR", o, throwable);
    }

    public void error(Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.error(throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.error(throwable);
            return;
        }
        write("ERROR", throwable, throwable);
    }

    public void fatal(Object o) {
        if (!isFatalEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.fatal(o);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.fatal(o);
            return;
        }
        write("FATAL", o);
    }

    public void fatal(Object o, Throwable throwable) {
        if (!isFatalEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.fatal(o, throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.fatal(o, throwable);
            return;
        }
        write("FATAL", o, throwable);
    }

    public void fatal(Throwable throwable) {
        if (!isFatalEnabled()) {
            return;
        }
        if (isUsingLog4j()) {
            log4jLogger.fatal(throwable);
            return;
        }
        if (isUsingCommonsLogging()) {
            commonsLog.fatal(throwable);
            return;
        }
        write("FATAL", throwable, throwable);
    }

    /**
     * Will get an instance of log for a given class.
     *
     * @param aClass to log for
     * @return the log instance
     */
    @SuppressWarnings("rawtypes")
	public static Log getLog(Class aClass) {
        return new Log(aClass);
    }

    /**
     * Set the logging level (options are TRACE, DEBUG, INFO, WARN, ERROR, FATAL).
     *
     * @param level the level to log with
     */
    public static void setLevel(String level) {
        level = level == null ? null : level.toUpperCase();
        usingSystemOut = false;
        usingSystemErr = false;
        // check for log type on the front
        if ("LOG4J".equalsIgnoreCase(level)) {
            usingLog4j = true;
        } else if ("COMMONS".equalsIgnoreCase(level)) {
            usingCommonsLogging = true;
        } else {
            // log need to parse level also
            if (level != null) {
                if (level.startsWith("SYSOUT:")) {
                    usingSystemOut = true;
                    level = level.substring("SYSOUT:".length());
                }
                if (level.startsWith("STDOUT:")) {
                    usingSystemOut = true;
                    level = level.substring("STDOUT:".length());
                }
                if (level.startsWith("STDERR:")) {
                    usingSystemErr = true;
                    level = level.substring("STDERR:".length());
                }
                if (level.startsWith("SYSERR:")) {
                    usingSystemErr = true;
                    level = level.substring("SYSERR:".length());
                }
            }
            setLevelInternal(level);
        }
    }

    /**
     * actually set the level.  setLevel is really setting type.
     */
    private static void setLevelInternal(String level) {
        // reset all level info
        traceLevelEnabled = false;
        debugLevelEnabled = false;
        infoLevelEnabled = false;
        warnLevelEnabled = false;
        errorLevelEnabled = false;
        fatalLevelEnabled = false;

        // set correct level
        boolean levelSelected = false;
        if ("TRACE".equalsIgnoreCase(level)) {
            traceLevelEnabled = true;
            levelSelected = true;
        }
        if ("DEBUG".equalsIgnoreCase(level)) {
            debugLevelEnabled = true;
            levelSelected = true;
        }
        if ("INFO".equalsIgnoreCase(level)) {
            infoLevelEnabled = true;
            levelSelected = true;
        }
        if ("WARN".equalsIgnoreCase(level)) {
            warnLevelEnabled = true;
            levelSelected = true;
        }
        if ("ERROR".equalsIgnoreCase(level)) {
            errorLevelEnabled = true;
            levelSelected = true;
        }
        if ("FATAL".equalsIgnoreCase(level)) {
            fatalLevelEnabled = true;
            levelSelected = true;
        }
        if (!levelSelected) {
            infoLevelEnabled = true;
        }
    }

    /**
     * Handles writing for throwable.
     *
     * @param level     log level to log for
     * @param throwable to log
     * @param o object to log
     */
    private void write(String level, Object o, Throwable throwable) {
        String msg = getMsg(level, o).toString();
        if (usingSystemOut || context == null) {
            System.out.println(msg);
            throwable.printStackTrace(System.out);
        } else if (usingSystemErr) {
            System.err.println(msg);
            throwable.printStackTrace(System.err);
        } else {
            context.log(msg, throwable);
        }
    }

    /**
     * Handles writing of log lines.
     *
     * @param level log level to log for
     * @param o     object to log (runs toString)
     */
    private void write(String level, Object o) {
        String msg = getMsg(level, o).toString();
        if (usingSystemOut || context == null) {
            System.out.println(msg);
        } else if (usingSystemErr) {
            System.err.println(msg);
        } else {
            context.log(msg);
        }
    }

    private StringBuffer getMsg(String level, Object o) {
        StringBuffer msg = new StringBuffer();
        if (clazz == null) {
            msg.append("null");
        } else {
            msg.append(clazz.getName());
        }
        msg.append(" ");
        msg.append(level);
        msg.append(": ");
        msg.append(o.toString());
        return msg;
    }

    /**
     * Resets log to default state.
     */
    public static void resetAll() {
        Log.context = null;
        setLevel(DEFAULT_LOG_LEVEL);
        Log.usingSystemOut = false;
        Log.usingSystemErr = false;
        Log.usingLog4j = false; 
        Log.usingCommonsLogging = false;
    }

    /**
     * Will setup Log based on the filter config.  Uses init paramater "logLevel" to get the log level.
     * Defaults to "INFO".
     *
     * @param filterConfig the filter config to use
     */
    public static void setConfiguration(final FilterConfig filterConfig) {
        resetAll();

        if (filterConfig == null) {
            localLog.error("no filter config passed");
            return;
        }
        Log.context = filterConfig.getServletContext();

        String logLevelConf = filterConfig.getInitParameter("logLevel");

        if (logLevelConf != null) {
            logLevelConf = trimStr(logLevelConf);
        }

        setLevel(logLevelConf);
        localLog.debug("logLevel set to " + logLevelConf);
    }
    
    public static String trimStr(final String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }


}
