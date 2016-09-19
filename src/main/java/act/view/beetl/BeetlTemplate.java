package act.view.beetl;

import act.view.TemplateBase;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;

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
        try {
            beetlTemplate.binding(renderArgs);
            view.templateModifier.apply(beetlTemplate);
            return beetlTemplate.render();
        } catch (BeetlException be) {
            throw new BeetlError(be);
        }
    }

}
