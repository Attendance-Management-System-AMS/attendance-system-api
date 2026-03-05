package com.attendance.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;

@RestController
public class ApiGatewayController {

    private static final String API_PREFIX = "/api";

    @Value("${gateway.targets.hr}")
    private String hrServiceUrl;

    @Value("${gateway.targets.attendance}")
    private String attendanceServiceUrl;

    @Value("${gateway.targets.request}")
    private String requestServiceUrl;

    @Value("${gateway.targets.system}")
    private String systemServiceUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @RequestMapping("/api/hr/**")
    public ResponseEntity<String> proxyHr(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(request, body, "/hr", hrServiceUrl);
    }

    @RequestMapping("/api/attendance/**")
    public ResponseEntity<String> proxyAttendance(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(request, body, "/attendance", attendanceServiceUrl);
    }

    @RequestMapping("/api/request/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(request, body, "/request", requestServiceUrl);
    }

    @RequestMapping("/api/system/**")
    public ResponseEntity<String> proxySystem(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(request, body, "/system", systemServiceUrl);
    }

    private ResponseEntity<String> forward(HttpServletRequest servletRequest, String body, String routePrefix, String targetBaseUrl) {
        try {
            URI targetUri = buildTargetUri(servletRequest, routePrefix, targetBaseUrl);
            HttpRequest outboundRequest = buildOutboundRequest(servletRequest, body, targetUri);
            HttpResponse<String> outboundResponse = httpClient.send(outboundRequest, HttpResponse.BodyHandlers.ofString());

            HttpHeaders responseHeaders = new HttpHeaders();
            outboundResponse.headers().map().forEach((name, values) -> responseHeaders.put(name, values));

            return new ResponseEntity<>(
                    outboundResponse.body(),
                    responseHeaders,
                    HttpStatus.valueOf(outboundResponse.statusCode())
            );
            } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Gateway error while forwarding request: " + e.getMessage());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Gateway error while forwarding request: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid gateway route: " + e.getMessage());
        }
    }

    private URI buildTargetUri(HttpServletRequest servletRequest, String routePrefix, String targetBaseUrl) {
        String requestPath = servletRequest.getRequestURI();
        String expectedPrefix = API_PREFIX + routePrefix;
        if (!requestPath.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("Request path does not match expected route prefix");
        }

        String suffixPath = requestPath.substring(expectedPrefix.length());
        String normalizedBase = targetBaseUrl.endsWith("/")
                ? targetBaseUrl.substring(0, targetBaseUrl.length() - 1)
                : targetBaseUrl;

        StringBuilder uriBuilder = new StringBuilder(normalizedBase).append(suffixPath);
        if (servletRequest.getQueryString() != null && !servletRequest.getQueryString().isBlank()) {
            uriBuilder.append("?").append(servletRequest.getQueryString());
        }

        return URI.create(uriBuilder.toString());
    }

    private HttpRequest buildOutboundRequest(HttpServletRequest servletRequest, String body, URI targetUri) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(targetUri)
                .timeout(Duration.ofSeconds(30));

        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(HttpHeaders.HOST)
                    || headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }

            Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                requestBuilder.header(headerName, headerValues.nextElement());
            }
        }

        HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod());
        boolean hasBody = method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.PATCH
                || method == HttpMethod.DELETE;

        if (hasBody) {
            requestBuilder.method(method.name(), HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else {
            requestBuilder.method(method.name(), HttpRequest.BodyPublishers.noBody());
        }

        return requestBuilder.build();
    }
}
