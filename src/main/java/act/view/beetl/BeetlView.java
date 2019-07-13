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
import act.app.event.SysEventId;
import act.inject.util.ConfigResourceLoader;
import act.util.SubClassFinder;
import act.view.Template;
import act.view.View;
import org.beetl.core.*;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.beetl.ext.web.WebRenderExt;
import org.osgl.$;
import org.osgl.exception.ConfigurationException;
import org.osgl.inject.BeanSpec;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.Keyword;
import org.osgl.util.S;
import osgl.version.Version;

import javax.inject.Named;
import javax.inject.Provider;
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

    public GroupTemplate getBeetl() {
        return beetl;
    }


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
            ConfigResourceLoader confLoader = new ConfigResourceLoader(true);
            confLoader.init(map, BeanSpec.of(InputStream.class, app.injector()));
            InputStream is = (InputStream) confLoader.get();
            Properties p = null == is ? null : new Properties();
            if (null != p) {
                p.load(is);
            } else {
                info("beetl.properties not found, will use default configuration to init beetl template engine");
            }
            Configuration conf = null == is ? Configuration.defaultConfiguration() : new Configuration(p);
            conf.setErrorHandlerClass("org.beetl.core.ReThrowConsoleErrorHandler");
            conf.setNativeSecurity("act.view.beetl.BeetlView$ACTDefaultNativeSecurityManager");
            ClassLoader cl = app.classLoader();
            ResourceLoader loader = new ClasspathResourceLoader(cl, templateHome());
            beetl = new GroupTemplate(loader, conf, app.classLoader());

            String strWebAppExt = beetl.getConf().getWebAppExt();
            initTemplateModifier(strWebAppExt);
            directByteOutput = conf.isDirectByteOutput();
            suffix = app.config().get("view.beetl.suffix");
            if (null == suffix) {
                suffix = ".beetl";
            } else {
                suffix = suffix.startsWith(".") ? suffix : S.concat(".", suffix);
            }
            app.jobManager().on(SysEventId.DEPENDENCY_INJECTOR_LOADED, new Runnable() {
                @Override
                public void run() {
                    app.injector().registerProvider(GroupTemplate.class, new Provider<GroupTemplate>() {
                        @Override
                        public GroupTemplate get() {
                            return beetl;
                        }
                    });
                }
            });
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

    @SubClassFinder
    public void autoRegisterFunction(Class<Function> functionClass) {
        Named named = functionClass.getAnnotation(Named.class);
        String name = null != named ? named.value() : Keyword.of(functionClass.getName()).javaVariable();
        beetl.registerFunction(name, Act.getInstance(functionClass));
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
