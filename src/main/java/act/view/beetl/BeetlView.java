package act.view.beetl;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.conf.AppConfig;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;
import org.beetl.ext.web.WebRenderExt;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.exception.ConfigurationException;
import org.osgl.http.H;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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
        _init();
        if (beetl.getResourceLoader().exist(resourcePath)) {
            return null;
        }
        org.beetl.core.Template template = beetl.getTemplate(resourcePath);
        return new BeetlTemplate(template, this);
    }

    private void _init() {
        if (null != beetl) {
            return;
        }
        synchronized (this) {
            if (null != beetl) {
                return;
            }
            doInit();
        }
    }

    private void doInit() {
        App app = Act.app();
        AppConfig config = app.config();
        try {
            Configuration conf = Configuration.defaultConfiguration();
            String templateHome = templateHome(config);
            templateHome = new File(app.layout().resource(app.base()), templateHome).getAbsolutePath();
            // loader = new  ClasspathResourceLoader(templateHome) 
            ResourceLoader loader = new FileResourceLoader(templateHome);
            beetl = new GroupTemplate(loader, conf);
            String strWebAppExt = beetl.getConf().getWebAppExt();
            initTemplateModifier(strWebAppExt);
            directByteOutput = conf.isDirectByteOutput();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    private String templateHome(AppConfig config) {
        String templateHome = config.templateHome();
        if (S.blank(templateHome) || "default".equals(templateHome)) {
            templateHome = "/" + name();
        }
        return templateHome;
    }

    @Override
    protected void reload(final App app) {
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

}
