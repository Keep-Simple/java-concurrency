package bsa.java.concurrency.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class RequestResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        log.info("REQUEST [{} {}]", req.getMethod(), req.getRequestURI());

        chain.doFilter(request, response);

        if (res.getStatus() == 500) {
            log.info("RESPONSE [Server error]");
            return;
        }

        if (res.getContentType() == null) {
            log.info("RESPONSE [{}]", res.getStatus());
            return;
        }

        log.info("RESPONSE [{} {}]", res.getStatus(), res.getContentType());
    }

}
