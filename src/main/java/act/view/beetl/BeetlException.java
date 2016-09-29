package act.view.beetl;

import act.app.SourceInfo;
import act.view.TemplateException;
import org.apache.commons.io.input.ReaderInputStream;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.beetl.core.exception.ErrorInfo;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

public class BeetlException extends TemplateException {

    public BeetlException(org.beetl.core.exception.BeetlException t) {
        super(t);
    }

    public org.beetl.core.exception.BeetlException beetlException() {
        return (org.beetl.core.exception.BeetlException) getCause();
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        org.beetl.core.exception.BeetlException re = (org.beetl.core.exception.BeetlException) t;
        templateInfo = new BeetlSourceInfo(re);
    }

    @Override
    public String errorMessage() {
        ErrorInfo error = new ErrorInfo((org.beetl.core.exception.BeetlException) getCause());
        StringBuilder sb = new StringBuilder(error.getType());
        String tokenText = error.getErrorTokenText();
        if (S.notBlank(tokenText)) {
            sb.append(":<b>").append(error.getErrorTokenText()).append(" </b>");
        }
        String msg = error.getMsg();
        if (S.notBlank(msg)) {
            sb.append("<br><pre>").append(msg).append("</pre");
        }

        return sb.toString();
    }

    private static class BeetlSourceInfo extends SourceInfo.Base {

        BeetlSourceInfo(org.beetl.core.exception.BeetlException e) {
            ErrorInfo error = new ErrorInfo(e);
            lineNumber = error.getErrorTokenLine();
            ResourceLoader loader = e.gt.getResourceLoader();
            Resource resource = loader.getResource(e.resourceId);
            String content = IO.readContentAsString(new ReaderInputStream(resource.openReader()));
            lines = S.notBlank(content) ? C.listOf(content.split("[\n\r]+")) : C.<String>list();
            fileName = resource.getId();
        }

    }
}
