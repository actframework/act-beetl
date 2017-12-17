package act.view.beetl;

import act.app.SourceInfo;
import act.view.TemplateException;
import org.apache.commons.io.input.ReaderInputStream;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.exception.ErrorInfo;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.util.List;

public class BeetlTemplateException extends TemplateException {

    private ErrorInfo errorInfo;
    private BeetlException beetlException;

    public BeetlTemplateException(BeetlException t) {
        super(t);
    }

    @Override
    protected void populateSourceInfo(Throwable t0) {
        BeetlException t = (BeetlException) t0;
        beetlException = t;
        errorInfo = new ErrorInfo(t);
        templateInfo = new BeetlSourceInfo(beetlException, errorInfo);
        if (isNativeException()) {
            sourceInfo = getJavaSourceInfo(t.getCause());
        }
    }

    @Override
    public List<String> stackTrace() {
        return isNativeException() ? super.stackTrace() : C.<String>list();
    }

    @Override
    public String errorMessage() {
        if (isNativeException()) {
            Throwable cause = errorInfo.getCause();
            while (null != cause) {
                if (null == cause.getCause()) {
                    return cause.getMessage();
                }
                cause = cause.getCause();
            }
            return S.concat(errorInfo.getErrorCode(), ": ", errorInfo.getMsg());
        }
        StringBuilder sb = new StringBuilder(errorInfo.getType());
        String tokenText = errorInfo.getErrorTokenText();
        if (S.notBlank(tokenText)) {
            sb.append(":<b>").append(errorInfo.getErrorTokenText()).append(" </b>");
        }
        String msg = errorInfo.getMsg();
        if (S.notBlank(msg)) {
            sb.append("<br><pre>").append(msg).append("</pre>");
        }

        return sb.toString();
    }

    @Override
    protected boolean isTemplateEngineInvokeLine(String s) {
        return s.contains("org.beetl.core.om.ObjectUtil.invoke");
    }

    @Override
    public boolean isErrorSpot(String traceLine, String nextTraceLine) {
        if (!traceLine.contains("sun.reflect") && isTemplateEngineInvokeLine(nextTraceLine)) {
            return true;
        }
        return super.isErrorSpot(traceLine, nextTraceLine);
    }

    private boolean isNativeException() {
        return errorInfo.getErrorCode().startsWith("NATIVE_");
    }

    private static class BeetlSourceInfo extends SourceInfo.Base {

        BeetlSourceInfo(BeetlException be, ErrorInfo errorInfo) {
            lineNumber = errorInfo.getErrorTokenLine();
            ResourceLoader loader = be.gt.getResourceLoader();
            Resource resource = be.resource;
            String content = IO.readContentAsString(new ReaderInputStream(resource.openReader()));
            lines = S.notBlank(content) ? C.listOf(content.split("[\n\r]+")) : C.<String>list();
            fileName = resource.getId();
        }

    }
}
