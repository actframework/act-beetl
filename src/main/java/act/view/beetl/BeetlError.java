package act.view.beetl;

import act.app.SourceInfo;
import act.view.ActServerError;
import org.beetl.core.exception.BeetlException;
import org.osgl.util.E;
import org.rythmengine.exception.RythmException;

import java.util.List;

public class BeetlError extends ActServerError {
    private SourceInfo templateInfo;

    public BeetlError(BeetlException t) {
        super(t);
    }

    public RythmException rythmException() {
        return (RythmException) getCause();
    }

    public SourceInfo templateSourceInfo() {
        return templateInfo;
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        BeetlException re = (BeetlException) t;
        sourceInfo = new BeetlSourceInfo(re, true);
        templateInfo = new BeetlSourceInfo(re, false);
    }

    private static class BeetlSourceInfo implements SourceInfo {

        BeetlSourceInfo(BeetlException e, boolean javaSource) {
            throw E.tbd();
//            fileName = e.templateName;
//            if (javaSource) {
//                lineNumber = e.javaLineNumber;
//                String jsrc = e.javaSource;
//                lines = null != jsrc ? C.listOf(jsrc.split("[\n]")) : C.<String>list();
//            } else {
//                lineNumber = e.templateLineNumber;
//                lines = C.listOf(e.templateSource.split("[\n]"));
//            }
        }

        private String fileName;
        private List<String> lines;
        private int lineNumber;

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
