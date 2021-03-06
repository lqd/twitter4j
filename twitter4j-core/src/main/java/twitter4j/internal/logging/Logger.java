/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.internal.logging;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.0
 */
public abstract class Logger {
    private static final LoggerFactory LOGGER_FACTORY;
    private static final String LOGGER_FACTORY_IMPLEMENTATION = "twitter4j.loggerFactory";

    static {
        LoggerFactory loggerFactory = null;
        try {
            //-Dtwitter4j.debug=true -Dtwitter4j.loggerFactory=twitter4j.internal.logging.StdOutLoggerFactory
            String loggerFactoryImpl = System.getProperty(LOGGER_FACTORY_IMPLEMENTATION);
            if (null != loggerFactoryImpl) {
                loggerFactory = (LoggerFactory) Class.forName(loggerFactoryImpl).newInstance();
            }
        } catch (NoClassDefFoundError ignore) {
        } catch (ClassNotFoundException ignore) {
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException ignore) {
        } catch (SecurityException ignore) {
            // Unsigned applets are not allowed to access System properties
        }
        // use SLF4J if it's found in the classpath
        if (null == loggerFactory) {
            try {
                // To use SLF4J, StaticLoggerBinder should be existing in the classpath
                // http://www.slf4j.org/codes.html#StaticLoggerBinder
                Class.forName("org.slf4j.impl.StaticLoggerBinder");
                loggerFactory = getLoggerFactory("org.slf4j.Logger", "twitter4j.internal.logging.SLF4JLoggerFactory");
            } catch (ClassNotFoundException ignore) {
            }
        }
        // otherwise, use commons-logging if it's found in the classpath
        if (null == loggerFactory) {
            loggerFactory = getLoggerFactory("org.apache.commons.logging.Log", "twitter4j.internal.logging.CommonsLoggingLoggerFactory");
        }
        // otherwise, use log4j if it's found in the classpath
        if (null == loggerFactory) {
            loggerFactory = getLoggerFactory("org.apache.log4j.Logger", "twitter4j.internal.logging.Log4JLoggerFactory");
        }
        // otherwise, use the default logger
        if (null == loggerFactory) {
            loggerFactory = new StdOutLoggerFactory();
        }
        LOGGER_FACTORY = loggerFactory;
        loggerFactory.getLogger(Logger.class).debug("Will use " + loggerFactory.getClass() + " as logging factory.");
    }

    private static LoggerFactory getLoggerFactory(String checkClassName, String implementationClass) {
        LoggerFactory logger = null;
        try {
            Class.forName(checkClassName);
            logger = (LoggerFactory) Class.forName(implementationClass).newInstance();
        } catch (ClassNotFoundException ignore) {
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        return logger;
    }

    /**
     * Returns a Logger instance associated with the specified class.
     *
     * @param clazz class
     * @return logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LOGGER_FACTORY.getLogger(clazz);
    }

    /**
     * tests if debug level logging is enabled
     *
     * @return if debug level logging is enabled
     */
    public abstract boolean isDebugEnabled();

    /**
     * tests if info level logging is enabled
     *
     * @return if info level logging is enabled
     */
    public abstract boolean isInfoEnabled();

    /**
     * tests if warn level logging is enabled
     *
     * @return if warn level logging is enabled
     */
    public abstract boolean isWarnEnabled();

    /**
     * @param message message
     */
    public abstract void debug(String message);

    /**
     * @param message  message
     * @param message2 message2
     */
    public abstract void debug(String message, String message2);

    /**
     * @param message message
     */
    public abstract void info(String message);

    /**
     * @param message  message
     * @param message2 message2
     */
    public abstract void info(String message, String message2);

    /**
     * @param message message
     */
    public abstract void warn(String message);

    /**
     * @param message  message
     * @param message2 message2
     */
    public abstract void warn(String message, String message2);

}
