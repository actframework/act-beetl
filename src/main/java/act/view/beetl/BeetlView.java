package act.view.beetl;

/*-
 * #%L
 * ACT Beetl
 * %%
 * Copyright (C) 2017 ActFramework
 * %%
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
 * #L%
 */

import act.Act;
import act.app.App;
import act.inject.util.ConfigResourceLoader;
import act.view.Template;
import act.view.View;
import org.beetl.core.Configuration;
import org.beetl.core.DefaultNativeSecurityManager;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.beetl.ext.web.WebRenderExt;
import org.osgl.$;
import org.osgl.exception.ConfigurationException;
import org.osgl.inject.BeanSpec;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;
import osgl.version.Version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class BeetlView extends View {

    public static final Version VERSION = Version.get();

    public static final String ID = "beetl";

    transient GroupTemplate beetl;
    $.Visitor<org.beetl.core.Template> templateModifier = new $.Visitor<org.beetl.core.Template>() {
        @Override
        public void visit(org.beetl.core.Template template) throws $.Break {
            // do nothing visitor
        }
    };
    boolean directByteOutput;
    private String suffix;


    @Override
    public String name() {
        return ID;
    }

    @Override
    protected Template loadTemplate(String resourcePath) {
        if (!beetl.getResourceLoader().exist(resourcePath)) {
            if (resourcePath.endsWith(suffix)) {
                return null;
            }
            return loadTemplate(S.concat(resourcePath, suffix));
        }
        return new BeetlTemplate(resourcePath, this);
    }

    private static final StringTemplateResourceLoader STRING_TEMPLATE_RESOURCE_LOADER = new StringTemplateResourceLoader();

    @Override
    protected Template loadInlineTemplate(String content) {
        return new BeetlTemplate(content, this, true);
    }

    @Override
    protected void init(final App app) {
        try {
            Map<String, String> map = C.Map("value", "/beetl.properties");
            ConfigResourceLoader confLoader = new ConfigResourceLoader();
            confLoader.init(map, BeanSpec.of(InputStream.class, app.injector()));
            InputStream is = (InputStream) confLoader.get();
            Properties p = null == is ? null : new Properties();
            if (null != p) {
                p.load(is);
            }
            Configuration conf = null == is ? Configuration.defaultConfiguration() : new Configuration(p);
            conf.setErrorHandlerClass("org.beetl.core.ReThrowConsoleErrorHandler");
            conf.setNativeSecurity("act.view.beetl.BeetlView$ACTDefaultNativeSecurityManager");
            ClassLoader cl = app.classLoader();
            ResourceLoader loader = new ClasspathResourceLoader(cl, templateHome());
            beetl = new GroupTemplate(loader, conf);
            beetl.setClassLoader(app.classLoader());

            String strWebAppExt = beetl.getConf().getWebAppExt();
            initTemplateModifier(strWebAppExt);
            directByteOutput = conf.isDirectByteOutput();
            suffix = app.config().get("view.beetl.suffix");
            if (null == suffix) {
                suffix = ".beetl";
            } else {
                suffix = suffix.startsWith(".") ? suffix : S.concat(".", suffix);
            }
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
                public void visit(org.beetl.core.Template template) throws $.Break {
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
