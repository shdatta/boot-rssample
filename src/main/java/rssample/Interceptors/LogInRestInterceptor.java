package rssample.Interceptors;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingMessage;
import org.springframework.stereotype.Service;

@Service
public class LogInRestInterceptor extends LoggingInInterceptor {
    @Override
    protected String formatLoggingMessage(LoggingMessage loggingMessage) {
        return "This is incoming message ***** " + loggingMessage.getPayload() + "********";
    }
}
