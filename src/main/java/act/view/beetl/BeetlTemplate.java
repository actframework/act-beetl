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
import act.view.TemplateBase;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.osgl.http.H;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

public class BeetlTemplate extends TemplateBase {

    private static StringTemplateResourceLoader STRING_TEMPLATE_RESOURCE_LOADER = new StringTemplateResourceLoader();

    private boolean inline;
    private String resourcePath;
    private BeetlView view;

    BeetlTemplate(String resourcePath, BeetlView view) {
        this(resourcePath, view, false);
    }

    BeetlTemplate(String resourcePath, BeetlView view, boolean inline) {
        this.resourcePath = S.requireNotBlank(resourcePath);
        this.view = view;
        this.inline = inline;
    }


    @Override
    protected void merge(Map<String, Object> renderArgs, H.Response response) {
        if (Act.isDev()) {
            super.merge(renderArgs, response);
            return;
        }
        Template beetlTemplate = getTemplate();
        beetlTemplate.binding(renderArgs);
        view.templateModifier.apply(beetlTemplate);
        if (view.directByteOutput) {
            OutputStream os = response.outputStream();
            try {
                beetlTemplate.renderTo(response.outputStream());
            } finally {
                IO.close(os);
            }
        } else {
            Writer writer = response.writer();
            try {
                beetlTemplate.renderTo(writer);
            } finally {
                IO.close(writer);
            }
        }
    }

    @Override
    protected String render(Map<String, Object> renderArgs) {
        Thread t0 = Thread.currentThread();
        ClassLoader loader0 = t0.getContextClassLoader();
        try {
            Template beetlTemplate = getTemplate();
            t0.setContextClassLoader(Act.app().classLoader());
            beetlTemplate.binding(renderArgs);
            view.templateModifier.apply(beetlTemplate);
            return beetlTemplate.render();
        } catch (org.beetl.core.exception.BeetlException be) {
            throw new BeetlTemplateException(be);
        } finally {
            t0.setContextClassLoader(loader0);
        }
    }

    private Template getTemplate() {
        return inline ? view.beetl.getTemplate(resourcePath, STRING_TEMPLATE_RESOURCE_LOADER) : view.beetl.getTemplate(resourcePath);
    }

}
