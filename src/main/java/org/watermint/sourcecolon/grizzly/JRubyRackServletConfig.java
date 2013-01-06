package org.watermint.sourcecolon.grizzly;

import org.glassfish.grizzly.servlet.ServletConfigImpl;
import org.glassfish.grizzly.servlet.WebappContext;

import javax.servlet.Servlet;

/**
 *
 */
public class JRubyRackServletConfig extends ServletConfigImpl {
    public JRubyRackServletConfig(WebappContext servletContextImpl) {
        super(servletContextImpl);
    }
}
