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
