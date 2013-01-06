package org.watermint.sourcecolon.grizzly;

import org.glassfish.grizzly.servlet.ServletHandler;
import org.glassfish.grizzly.servlet.WebappContext;
import org.jruby.rack.RackServlet;

/**
 *
 */
public class JRubyRackServletHandler extends ServletHandler {
    public JRubyRackServletHandler(WebappContext context) {
        super(new JRubyRackServletConfig(context));
        setServletClass(RackServlet.class);
    }
}
