package rssample.Interceptors;

import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.stereotype.Service;

@Service
public class LogClientOutRestInterceptor extends LoggingOutInterceptor {
    @Override
    protected String formatLoggingMessage(LoggingMessage loggingMessage) {
        return "This is outgoing message ***** " + loggingMessage.getPayload() + "********";
    }
}
