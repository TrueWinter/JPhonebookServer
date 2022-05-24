package dev.truewinter.jphonebookserver;

import dev.truewinter.jphonebookserver.routes.*;
import io.javalin.Javalin;
import io.javalin.core.util.Header;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import org.eclipse.jetty.server.session.SessionHandler;

import java.io.IOException;
import java.util.List;

public class WebServer extends Thread {
    private int port;
    private Config appConfig;
    private Database database;
    private Javalin server;
    private SessionHandler sessionHandler;

    protected WebServer(Database database, Config config) {
        this.port = config.getPort();
        this.appConfig = config;
        this.database = database;

        this.sessionHandler = new SessionHandler();
        Route.setDatabaseInstance(this.database);
    }

    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    private boolean isLoggedIn(Context ctx) {
        return ctx.sessionAttribute("logged-in-user") != null;
    }

    private void setAuthHeaders(Context ctx) {
        ctx.header("WWW-Authenticate", "Basic realm=\"JPhonebookServer\"");
        ctx.status(401);
        ctx.result("Login required");
    }

    private boolean doAuthIfEnabled(Context ctx) {
        if (appConfig.isAuthEnabled()) {
            if (!ctx.basicAuthCredentialsExist()) {
                setAuthHeaders(ctx);
                return true;
            }

            if (!appConfig.isValidLogin(ctx.basicAuthCredentials().getUsername(), ctx.basicAuthCredentials().getPassword())) {
                setAuthHeaders(ctx);
                return true;
            }
        }

        return false;
    }

    @Override
    public void run() {
        server = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.addStaticFiles(staticFiles -> {
                staticFiles.hostedPath = "/admin/assets";
                staticFiles.directory = "web/assets";
                staticFiles.location = Location.CLASSPATH;
            });
            config.sessionHandler(() -> sessionHandler);
        }).start(port);

        server.before(context -> {
            if (!context.path().startsWith("/admin/assets/")) {
                // No need to cache or index here
                context.header(Header.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                context.header(Header.EXPIRES, "0");
                context.header("X-Robots-Tag", "noindex");
            } else {
                // Cache static files (JS, CSS)
                context.header(Header.CACHE_CONTROL, "max-age=86400");
                context.header(Header.EXPIRES, "86400");
            }

            try {
                context.header(Header.SERVER, "JPhonebookServer/" + Util.getVersion());
            } catch (Exception e) {
                context.header(Header.SERVER, "JPhonebookServer");
            }
        });

        server.get("/", ctx -> {
            if (doAuthIfEnabled(ctx)) {
                return;
            }

            try {
                List<Directory> directories = database.getAllDirectories();

                if (directories.size() == 0) {
                    ctx.status(400);
                    ctx.result("You need to add directories first");
                    return;
                }

                XMLDocument xml = new XMLDocument(XMLDocument.XMLDocumentType.MENU, true, Util.getURL(ctx));

                for (Directory directory : directories) {
                    xml.addDirectoryContactsLink(directory);
                }

                xml.build();
                ctx.header(Header.CONTENT_TYPE, "text/xml");
                ctx.result(xml.toString());
            } catch (Exception e) {
                System.err.println("An error occurred while generating XML");
                e.printStackTrace();
                ctx.status(500);
                ctx.result("An error occurred while generating XML");
            }
        });

        // for a future version supporting nested directories
        /*server.get("/{directory}_Menu.xml", ctx -> {
            if (doAuthIfEnabled(ctx)) {
                return;
            }

            database.getDirectoryByName(ctx.pathParam("directory")).ifPresentOrElse(directory -> {
                try {
                    XMLDocument xml = new XMLDocument(XMLDocument.XMLDocumentType.MENU, false, Util.getURL(ctx));

                    xml.addDirectoryContactsLink(directory);
                    xml.build();
                    ctx.header(Header.CONTENT_TYPE, "application/xml");
                    ctx.result(xml.toString());
                } catch (Exception e) {
                    System.err.println("An error occurred while generating XML");
                    e.printStackTrace();
                    ctx.status(500);
                    ctx.result("An error occurred while generating XML");
                }
            }, () -> {
                ctx.status(404);
                ctx.result("Directory not found");
            });
        });*/

        server.get("/{directory}_Contacts.xml", ctx -> {
            if (doAuthIfEnabled(ctx)) {
                return;
            }

            database.getDirectoryByName(ctx.pathParam("directory")).ifPresentOrElse(directory -> {
                try {
                    XMLDocument xml = new XMLDocument(XMLDocument.XMLDocumentType.DIRECTORY, false, Util.getURL(ctx));

                    xml.addContactsFromDirectory(directory);
                    xml.build();
                    ctx.header(Header.CONTENT_TYPE, "application/xml");
                    ctx.result(xml.toString());
                } catch (Exception e) {
                    System.err.println("An error occurred while generating XML");
                    e.printStackTrace();
                    ctx.status(500);
                    ctx.result("An error occurred while generating XML");
                }
            }, () -> {
                ctx.status(404);
                ctx.result("Directory not found");
            });
        });

        server.get("/admin", ctx -> {
            if (isLoggedIn(ctx)) {
                ctx.redirect("/admin/directories");
            } else {
                ctx.redirect("/admin/login");
            }
        });

        server.get("/admin/login", ctx -> {
            new LoginRoute().get(ctx);
        });

        server.post("/admin/login", ctx -> {
            new LoginRoute().post(ctx);
        });

        server.get("/admin/logout", ctx -> {
            ctx.req.getSession().invalidate();
            ctx.redirect("/admin");
        });

        server.get("/admin/directories", ctx -> {
            new DirectoriesRoute().verifyLogin(ctx).get(ctx);
        });

        server.get("/api/directories", ctx -> {
            new DirectoriesAPIRoute().verifyLogin(ctx).get(ctx);
        });

        server.get("/admin/directories/add", ctx -> {
            new AddDirectoryRoute().verifyLogin(ctx).get(ctx);
        });

        server.post("/admin/directories/add", ctx -> {
            new AddDirectoryRoute().verifyLogin(ctx).verifyCSRF(ctx, "add-directory").post(ctx);
        });

        server.get("/admin/directories/{id}/edit", ctx -> {
            new EditDirectoryRoute().verifyLogin(ctx).get(ctx);
        });

        server.post("/admin/directories/{id}/edit", ctx -> {
            new EditDirectoryRoute().verifyLogin(ctx).verifyCSRF(ctx, "error").post(ctx);
        });

        server.post("/admin/directories/{id}/delete", ctx -> {
            new DeleteDirectoryRoute().verifyLogin(ctx).verifyCSRF(ctx, "error").post(ctx);
        });

        server.get("/admin/directories/{id}/contacts", ctx -> {
            new ContactsRoute().verifyLogin(ctx).get(ctx);
        });

        server.get("/api/directories/{dirId}/contacts", ctx -> {
            new ContactsAPIRoute().verifyLogin(ctx).get(ctx);
        });

        server.get("/admin/directories/{id}/contacts/add", ctx -> {
            new AddContactRoute().verifyLogin(ctx).get(ctx);
        });

        server.post("/admin/directories/{id}/contacts/add", ctx -> {
            new AddContactRoute().verifyLogin(ctx).verifyCSRF(ctx, "add-contact").post(ctx);
        });

        server.get("/admin/directories/{dirId}/contacts/{contactId}/edit", ctx -> {
            new EditContactRoute().verifyLogin(ctx).get(ctx);
        });

        server.post("/admin/directories/{dirId}/contacts/{contactId}/edit", ctx -> {
            new EditContactRoute().verifyLogin(ctx).verifyCSRF(ctx, "error").post(ctx);
        });

        server.post("/admin/directories/{dirId}/contacts/{contactId}/delete", ctx -> {
            new DeleteContactRoute().verifyLogin(ctx).verifyCSRF(ctx, "error").post(ctx);
        });

        server.get("/admin/accounts", ctx -> {
            new AccountsRoute().verifyLogin(ctx).requireAdmin(ctx).get(ctx);
        });

        server.get("/api/accounts", ctx -> {
            new AccountsAPIRoute().verifyLogin(ctx).requireAdmin(ctx).get(ctx);
        });

        server.get("/admin/accounts/add", ctx -> {
            new AddAccountRoute().verifyLogin(ctx).requireAdmin(ctx).get(ctx);
        });

        server.post("/admin/accounts/add", ctx -> {
            new AddAccountRoute().verifyLogin(ctx).requireAdmin(ctx)
                    .verifyCSRF(ctx, "add-account")
                    .post(ctx);
        });

        server.get("/admin/accounts/{id}/edit", ctx -> {
            new EditAccountRoute().verifyLogin(ctx).get(ctx);
        });

        server.post("/admin/accounts/{id}/edit", ctx -> {
            new EditAccountRoute().verifyLogin(ctx).verifyCSRF(ctx, "error").post(ctx);
        });
    }

    public void stopServer() {
        server.stop();
        this.interrupt();
    }
}
