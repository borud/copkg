package org.cloudname.copkg;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.DefaultHandler;

/**
 * Simple HTTP server for serving static resources.  Does exactly no
 * attempts at understanding or cleaning up exceptions since this is
 * for embedded testing.
 *
 * http://download.eclipse.org/jetty/stable-8/apidocs/
 *
 * @author borud
 */
public class StaticHttpServer {
    private final int port;
    private final String staticResourcePath;
    private final Server server = new Server();

    /**
     * @param port the port the HTTP server will listen to
     * @param staticResourcePath the path from which we serve the
     *   static resources -- fancytalk for "files".
     */
    public StaticHttpServer(int port, String staticResourcePath) {
        this.port = port;
        this.staticResourcePath = staticResourcePath;
    }

    /**
     * Populate the server with handlers and start it.
     *
     * @throws Exception if unable to set up and start Jetty server.
     */
    public void start() throws Exception {
        final SocketConnector connector = new SocketConnector();
        connector.setPort(port);
        server.setConnectors(new Connector[] {connector});

        // Configure a basic resource handler.
        ResourceHandler rh = new ResourceHandler();
        rh.setDirectoriesListed(true);
        rh.setWelcomeFiles(new String[] {"index.html"});
        rh.setResourceBase(staticResourcePath);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { rh, new DefaultHandler() });
        server.setHandler(handlers);

        server.start();
    }

    /**
     * Shut down server and block until the server has stopped.
     */
    public void shutdown() throws Exception {
        server.stop();
        server.join();
    }
}
