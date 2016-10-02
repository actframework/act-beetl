package act.view.beetl;

import act.Act;
import act.app.ActionContext;
import act.view.TemplateBase;
import org.beetl.core.Template;
import org.osgl.http.H;

import java.util.Map;

public class BeetlTemplate extends TemplateBase {

    private Template beetlTemplate;
    private BeetlView view;

    BeetlTemplate(Template beetlTemplate, BeetlView view) {
        this.beetlTemplate = beetlTemplate;
        this.view = view;
    }

    @Override
    protected void merge(Map<String, Object> renderArgs, H.Response response) {
        if (Act.isDev()) {
            super.merge(renderArgs, response);
            return;
        }
        beetlTemplate.binding(renderArgs);
        view.templateModifier.apply(beetlTemplate);
        if (view.directByteOutput) {
            beetlTemplate.renderTo(response.outputStream());
        } else {
            beetlTemplate.renderTo(response.writer());
        }
    }

    @Override
    protected String render(Map<String, Object> renderArgs) {
        Thread t0 = Thread.currentThread();
        ClassLoader loader0 = t0.getContextClassLoader();
        try {
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

}
