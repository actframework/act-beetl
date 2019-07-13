package act.view.beetl;

/*-
 * #%L
 * ACT Beetl
 * %%
 * Copyright (C) 2017 - 2019 ActFramework
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
import act.app.ActionContext;
import act.app.event.SysEventId;
import act.job.OnSysEvent;
import act.util.SubClassFinder;
import act.view.rythm.RythmView;
import org.beetl.core.Context;
import org.beetl.core.Format;
import org.beetl.core.Function;
import org.beetl.core.GroupTemplate;
import org.beetl.core.tag.TagFactory;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.Keyword;
import org.rythmengine.RythmEngine;
import org.rythmengine.extension.ICodeType;
import org.rythmengine.internal.compiler.TemplateClass;
import org.rythmengine.template.ITag;
import org.rythmengine.template.ITemplate;
import org.rythmengine.template.JavaTagBase;
import org.rythmengine.utils.Escape;
import org.rythmengine.utils.JSONWrapper;
import org.rythmengine.utils.S;
import org.rythmengine.utils.TextBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

@Singleton
public class BeetlRegisterHelper {

    @Inject
    private GroupTemplate beetl;


    @SubClassFinder
    public void foundFunction(Function function) {
        beetl.registerFunction(getName(function), function);
    }

    @SubClassFinder
    public void foundFormat(Format format) {
        beetl.registerFormat(getName(format), format);
    }

    @SubClassFinder
    public void foundTagFactory(TagFactory tagFactory) {
        beetl.registerTagFactory(getName(tagFactory), tagFactory);
    }

    @OnSysEvent(SysEventId.PRE_START)
    public void bridgeRythmTags() {
        final RythmView rythmView = Act.getInstance(RythmView.class);
        final RythmEngine rythm = rythmView.getEngine(Act.app());
        final ITemplate modelTemplate = new ITemplate() {
            @Override
            public RythmEngine __engine() {
                return rythm;
            }

            @Override
            public TemplateClass __getTemplateClass(boolean useCaller) {
                return null;
            }

            @Override
            public ITemplate __setOutputStream(OutputStream os) {
                return this;
            }

            @Override
            public ITemplate __setWriter(Writer writer) {
                return this;
            }

            @Override
            public ITemplate __setUserContext(Map<String, Object> userContext) {
                return this;
            }

            @Override
            public Map<String, Object> __getUserContext() {
                return C.Map();
            }

            @Override
            public ITemplate __setRenderArgs(Map<String, Object> args) {
                return this;
            }

            @Override
            public ITemplate __setRenderArgs(Object... args) {
                return this;
            }

            @Override
            public ITemplate __setRenderArg(String name, Object arg) {
                return this;
            }

            @Override
            public <T> T __getRenderArg(String name) {
                return null;
            }

            @Override
            public ITemplate __setRenderArg(int position, Object arg) {
                return this;
            }

            @Override
            public ITemplate __setRenderArg(JSONWrapper jsonData) {
                return this;
            }

            @Override
            public String render() {
                return null;
            }

            @Override
            public void render(OutputStream os) {
            }

            @Override
            public void render(Writer w) {
            }

            @Override
            public void __init() {
            }

            @Override
            public StringBuilder __getBuffer() {
                return null;
            }

            @Override
            public ITemplate __setSecureCode(String secureCode) {
                return this;
            }

            @Override
            public ITemplate __cloneMe(RythmEngine engine, ITemplate caller) {
                return this;
            }

            @Override
            public Locale __curLocale() {
                ActionContext ctx = ActionContext.current();
                return null == ctx ? Act.appConfig().locale() : ctx.locale(true);
            }

            @Override
            public Escape __curEscape() {
                return null;
            }

            @Override
            public ICodeType __curCodeType() {
                return null;
            }

            @Override
            public String __getName() {
                return null;
            }

            @Override
            public ITag __setBodyContext(__Body body) {
                return null;
            }

            @Override
            public void __call(int line) {
            }
        };
        Field tagsField = $.fieldOf(RythmEngine.class, "_tags");
        Map<String, JavaTagBase> tags = $.getFieldValue(rythm, tagsField);
        for (Map.Entry<String, JavaTagBase> entry : tags.entrySet()) {
            String key = entry.getKey();
            final JavaTagBase rythmTag = entry.getValue();
            beetl.registerFunction(key, new Function() {
                @Override
                public Object call(Object[] paras, Context ctx) {
                    ITag.__ParameterList paramList = new ITag.__ParameterList();
                    if (null != paras) {
                        for (Object o : paras) {
                            paramList.add(null, o);
                        }
                    }
                    JavaTagBase runTag = $.deepCopy(rythmTag).to((JavaTagBase) rythmTag.clone(null));
                    runTag.__setRenderArgs0(paramList);
                    return runTag.build().toString();
                }
            });
        }
        beetl.registerFunction("i18n", new Function() {
            @Override
            public Object call(Object[] paras, Context ctx) {
                String key = S.string(paras[0]);
                Object params = new Object[paras.length - 1];
                System.arraycopy(paras, 1, params, 0, paras.length - 1);
                return S.i18n(modelTemplate, key, params);
            }
        });
    }

    @OnSysEvent(SysEventId.PRE_START)
    public void bridgeRythmFormats() {
        final RythmView rythmView = Act.getInstance(RythmView.class);
        final RythmEngine rythm = rythmView.getEngine(Act.app());
        final ITemplate modelTemplate = new ITemplate() {
            @Override
            public RythmEngine __engine() {
                return rythm;
            }

            @Override
            public TemplateClass __getTemplateClass(boolean useCaller) {
                return null;
            }

            @Override
            public ITemplate __setOutputStream(OutputStream os) {
                return this;
            }

            @Override
            public ITemplate __setWriter(Writer writer) {
                return this;
            }

            @Override
            public ITemplate __setUserContext(Map<String, Object> userContext) {
                return this;
            }

            @Override
            public Map<String, Object> __getUserContext() {
                return C.Map();
            }

            @Override
            public ITemplate __setRenderArgs(Map<String, Object> args) {
                return this;
            }

            @Override
            public ITemplate __setRenderArgs(Object... args) {
                return this;
            }

            @Override
            public ITemplate __setRenderArg(String name, Object arg) {
                return this;
            }

            @Override
            public <T> T __getRenderArg(String name) {
                return null;
            }

            @Override
            public ITemplate __setRenderArg(int position, Object arg) {
                return this;
            }

            @Override
            public ITemplate __setRenderArg(JSONWrapper jsonData) {
                return this;
            }

            @Override
            public String render() {
                return null;
            }

            @Override
            public void render(OutputStream os) {
            }

            @Override
            public void render(Writer w) {
            }

            @Override
            public void __init() {
            }

            @Override
            public StringBuilder __getBuffer() {
                return null;
            }

            @Override
            public ITemplate __setSecureCode(String secureCode) {
                return this;
            }

            @Override
            public ITemplate __cloneMe(RythmEngine engine, ITemplate caller) {
                return this;
            }

            @Override
            public Locale __curLocale() {
                ActionContext ctx = ActionContext.current();
                return null == ctx ? Act.appConfig().locale() : ctx.locale(true);
            }

            @Override
            public Escape __curEscape() {
                return null;
            }

            @Override
            public ICodeType __curCodeType() {
                return null;
            }

            @Override
            public String __getName() {
                return null;
            }

            @Override
            public ITag __setBodyContext(__Body body) {
                return null;
            }

            @Override
            public void __call(int line) {
            }
        };
        beetl.registerFormat("format", new Format() {
            @Override
            public Object format(Object data, String pattern) {
                return S.format(modelTemplate, data, pattern, modelTemplate.__curLocale(), null);
            }
        });
    }

    private String getName(Object target) {
        Class<?> type = target.getClass();
        Named named = type.getAnnotation(Named.class);
        return null != named ? named.value() : Keyword.of(type.getSimpleName()).javaVariable();
    }

}
