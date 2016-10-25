package act.view.beetl;

import act.Act;
import act.app.App;
import act.app.event.AppEventId;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import org.beetl.core.Configuration;
import org.beetl.core.DefaultNativeSecurityManager;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.ext.web.WebRenderExt;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.exception.ConfigurationException;
import org.osgl.util.E;
import org.osgl.util.S;

import java.io.IOException;

public class BeetlView extends View {

    transient GroupTemplate beetl;
    $.Visitor<org.beetl.core.Template> templateModifier = new $.Visitor<org.beetl.core.Template>() {
        @Override
        public void visit(org.beetl.core.Template template) throws Osgl.Break {
            // do nothing visitor
        }
    };
    boolean directByteOutput;

    @Override
    public String name() {
        return "beetl";
    }

    @Override
    protected Template loadTemplate(String resourcePath, ActContext context) {
        if (!beetl.getResourceLoader().exist(resourcePath)) {
            return null;
        }
        org.beetl.core.Template template = beetl.getTemplate(resourcePath);
        return new BeetlTemplate(template, this);
    }

    @Override
    protected void init(final App app) {
        try {
            Configuration conf = Configuration.defaultConfiguration();
            conf.setErrorHandlerClass("org.beetl.core.ReThrowConsoleErrorHandler");
            conf.setNativeSecurity("act.view.beetl.BeetlView$ACTDefaultNativeSecurityManager");
            ClassLoader cl = app.classLoader();
            ResourceLoader loader = new ClasspathResourceLoader(cl, templateHome());
            beetl = new GroupTemplate(loader, conf);
            beetl.setClassLoader(app.classLoader());

            String strWebAppExt = beetl.getConf().getWebAppExt();
            initTemplateModifier(strWebAppExt);
            directByteOutput = conf.isDirectByteOutput();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    private WebRenderExt getWebRenderExt(String clsName) {
        try {
            return Act.app().getInstance(clsName);
        } catch (Exception ex) {
            throw new ConfigurationException(ex, "Error loading WebRenderExt: %s", ex.getMessage());
        }
    }

    private void initTemplateModifier(String webAppExt) {
        if (S.notBlank(webAppExt)) {
            final WebRenderExt ext = getWebRenderExt(webAppExt);
            templateModifier = new $.Visitor<org.beetl.core.Template>() {
                @Override
                public void visit(org.beetl.core.Template template) throws Osgl.Break {
                    // TODO: convert H.Request to HttpServletRequest
                    // TODO: convert H.Response to HttpServletResponse
                    ext.modify(template, beetl, null, null);
                }
            };
        }
    }

    @SuppressWarnings("unused")
    public static class ACTDefaultNativeSecurityManager extends DefaultNativeSecurityManager {
        // Code copied from https://github.com/javamonkey/beetl2.0/blob/master/beetl-core/src/main/java/org/beetl/core/DefaultNativeSecurityManager.java
        public boolean permit(String resourceId, Class c, Object target, String method) {
            if (c.isArray()) {
                //允许调用，但实际上会在在其后调用中报错。不归此处管理
                return true;
            }
            String className = c.getName();
            String name = c.getSimpleName();
            if (className.startsWith("java.lang")) {
                if (name.equals("Runtime") || name.equals("Process") || name.equals("ProcessBuilder")
                        || name.equals("System")) {
                    return false;
                }
            }

            return true;
        }
    }

}
