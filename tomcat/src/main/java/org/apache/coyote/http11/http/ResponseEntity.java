package org.apache.coyote.http11.http;

import org.apache.coyote.http11.common.ContentType;
import org.apache.coyote.http11.common.HttpVersion;
import org.apache.coyote.http11.common.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ResponseEntity {

    private final HttpVersion httpVersion;
    private final ResponseStatus responseStatus;
    private final List<String> responseHeaders;
    private final String responseBody;
    private final Map<String, String> cookies = new HashMap<>();

    public ResponseEntity(HttpVersion httpVersion, ResponseStatus responseStatus, List<String> responseHeaders, String responseBody) {
        this.httpVersion = httpVersion;
        this.responseStatus = responseStatus;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public static ResponseEntity redirect(String redirectionFile) {
        List<String> headers = List.of(String.join(" ", "Location:", redirectionFile));
        return new ResponseEntity(HttpVersion.HTTP_1_1, ResponseStatus.FOUND, headers, "");
    }

    public static ResponseEntity ok(String fileData, String endPoint) {
        List<String> headers = List.of(
                String.join(" ", "Content-Type:", ContentType.findMatchingType(endPoint).getContentType()),
                String.join(" ", "Content-Length:", String.valueOf(fileData.getBytes().length))
        );
        return new ResponseEntity(HttpVersion.HTTP_1_1, ResponseStatus.OK, headers, fileData);
    }

    public void setCookie(String key, String value) {
        cookies.put(key, value);
    }

    public String generateResponseMessage() {
        return String.join(
                "\r\n",
                generateStatus(),
                generateHeader(),
                "",
                responseBody
        );
    }

    private String generateStatus() {
        return String.join(
                " ",
                httpVersion.getVersion(),
                String.valueOf(responseStatus.getStatusCode()),
                responseStatus.name(),
                ""
        );
    }

    private String generateHeader() {
        StringJoiner headers = new StringJoiner("\r\n");
        for (String header : responseHeaders) {
            headers.add(
                    String.join(" ", header, "")
            );
        }
        if (cookies.containsKey("JSESSIONID")) {
            String jsessionid = cookies.get("JSESSIONID");
            headers.add("Set-Cookie: JSESSIONID=" + jsessionid + " ");
        }
        return headers.toString();
    }

    public Map<String, String> getCookies() {
        return cookies;
    }
}
