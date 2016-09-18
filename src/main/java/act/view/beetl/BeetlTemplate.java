package act.view.beetl;

import act.view.TemplateBase;
import org.beetl.core.Template;
import org.osgl.http.H;
import org.osgl.util.E;

import java.util.Map;

public class BeetlTemplate extends TemplateBase {

    private Template beetlTemplate;
    private BeetlView view;

    BeetlTemplate(Template beetlTemplate, BeetlView view) {
        this.beetlTemplate = beetlTemplate;
        this.view = view;
    }

    @Override
    protected String render(Map<String, Object> renderArgs) {
        throw E.unsupport();
    }

    @Override
    protected void merge(Map<String, Object> renderArgs, H.Response response) {
        beetlTemplate.binding(renderArgs);
        view.templateModifier.apply(beetlTemplate);
        if (view.directByteOutput) {
            beetlTemplate.renderTo(response.outputStream());
        } else {
            beetlTemplate.renderTo(response.writer());
        }
    }
}
