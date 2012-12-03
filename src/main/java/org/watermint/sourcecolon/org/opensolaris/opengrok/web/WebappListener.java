/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2007, 2010, Oracle and/or its affiliates. All rights reserved.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.web;

import org.watermint.sourcecolon.org.opensolaris.opengrok.OpenGrokLogger;
import org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.RuntimeEnvironment;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Populate the Mercurial Repositories
 *
 * @author Trond Norbye
 */
public final class WebappListener implements ServletContextListener {
    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        RuntimeEnvironment env = RuntimeEnvironment.getInstance();

        String config = context.getInitParameter("CONFIGURATION");
        if (config == null || !(new File(config).exists())) {
            config = RuntimeEnvironment.DEFAULT_SOURCECOLON_CONFIG;
        }

        if (!new File(config).exists()) {
            OpenGrokLogger.getLogger().severe("Missing configuration. CONFIGURATION section missing in web.xml");
        } else {
            try {
                env.readConfiguration(new File(config));
            } catch (IOException ex) {
                OpenGrokLogger.getLogger().log(Level.WARNING, "OpenGrok Configuration error. Failed to read config file: ", ex);
            }
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        RuntimeEnvironment.getInstance().stopConfigurationListenerThread();
    }
}
