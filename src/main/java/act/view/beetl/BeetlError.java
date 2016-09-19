package act.view.beetl;

import act.app.SourceInfo;
import act.view.ActServerError;
import act.view.TemplateError;
import org.apache.commons.io.input.ReaderInputStream;
import org.beetl.core.ConsoleErrorHandler;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.exception.ErrorInfo;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.util.List;

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
        return getCause().getMessage();
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
