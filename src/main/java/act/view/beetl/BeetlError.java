package act.view.beetl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.input.ReaderInputStream;
import org.beetl.core.ConsoleErrorHandler;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.exception.ErrorInfo;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

import act.app.SourceInfo;
import act.view.TemplateError;

public class BeetlError extends TemplateError {

    public BeetlError(BeetlException t) {
        super(t);
    }

    public BeetlException beetlException() {
        return (BeetlException) getCause();
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        BeetlException re = (BeetlException) t;
        templateInfo = new BeetlSourceInfo(re);
    }

    @Override
    public String errorMessage() {
        ErrorInfo error = new ErrorInfo((BeetlException) getCause());
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

    private static class BeetlSourceInfo extends ConsoleErrorHandler implements SourceInfo {

        private String fileName;
        private List<String> lines;
        private int lineNumber;


        BeetlSourceInfo(BeetlException e) {
            ErrorInfo error = new ErrorInfo(e);
            lineNumber = error.getErrorTokenLine();
            ResourceLoader loader = e.gt.getResourceLoader();
            Resource resource = loader.getResource(e.resourceId);
            String content = IO.readContentAsString(new ReaderInputStream(resource.openReader()));
            lines = S.notBlank(content) ? C.listOf(content.split("[\n\r]+")) : C.<String>list();
            fileName = resource.getId();
        }

        @Override
        public String fileName() {
            return fileName;
        }

        @Override
        public List<String> lines() {
            return lines;
        }

        @Override
        public Integer lineNumber() {
            return lineNumber;
        }

        @Override
        public boolean isSourceAvailable() {
            return true;
        }
    }
}
