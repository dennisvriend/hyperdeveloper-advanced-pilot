package dev.ilionx.workshop.support.util;

import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

public class WebMvcConfigurator extends TestContextInitializer {

    /**
     * The servlet context path configured in application.yml
     * ({@code server.servlet.context-path: /api}). MockMvc does not apply
     * the context path, so tests may use either the full "/api/..." prefix
     * or the path within the application directly.
     */
    private static final String CONTEXT_PATH = "/api";

    protected MockMvc mockMvc;

    @BeforeEach
    public void setUpMockMvc() {
        this.mockMvc = configureMockMvc();
    }

    private MockMvc configureMockMvc() {
        return MockMvcBuilders
            .webAppContextSetup(applicationContext)
            .addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
            .addFilter(
                (request, response, chain) -> {
                    final HttpServletRequest httpRequest = (HttpServletRequest) request;
                    if (httpRequest.getRequestURI().startsWith(CONTEXT_PATH + "/")) {
                        chain.doFilter(new ContextPathStrippingRequest(httpRequest), response);
                    } else {
                        chain.doFilter(request, response);
                    }
                }
            )
            .build();
    }

    /**
     * Wraps a request so that the servlet context path prefix is stripped,
     * mirroring how the real servlet container resolves paths under
     * {@code server.servlet.context-path}.
     */
    private static final class ContextPathStrippingRequest extends HttpServletRequestWrapper {

        private ContextPathStrippingRequest(final HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getRequestURI() {
            final String uri = super.getRequestURI();
            return uri.startsWith(CONTEXT_PATH) ? uri.substring(CONTEXT_PATH.length()) : uri;
        }

        @Override
        public StringBuffer getRequestURL() {
            final String url = super.getRequestURL().toString();
            final int index = url.indexOf(CONTEXT_PATH + "/");
            return new StringBuffer(index >= 0 ? url.substring(0, index) + url.substring(index + CONTEXT_PATH.length()) : url);
        }

        @Override
        public String getServletPath() {
            final String path = super.getServletPath();
            return path.startsWith(CONTEXT_PATH) ? path.substring(CONTEXT_PATH.length()) : path;
        }
    }
}
