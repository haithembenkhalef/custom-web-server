package com.webby.model;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class HttpRequest {
  private final HttpMethod httpMethod;
  private final String protocolVersion;
  private final URI uri;
  private final Map<String, List<String>> requestHeaders;

  private HttpRequest(HttpMethod opCode, String protocolVersion,
                      URI uri,
                      Map<String, List<String>> requestHeaders) {
      this.httpMethod = opCode;
      this.protocolVersion = protocolVersion;
      this.uri = uri;
      this.requestHeaders = requestHeaders;
  }

  public URI getUri() {
      return uri;
  }

  public HttpMethod getHttpMethod() {
      return httpMethod;
  }

  public Map<String, List<String>> getRequestHeaders() {
      return requestHeaders;
  }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public static class Builder {
      private HttpMethod httpMethod;
      private URI uri;
      private Map<String, List<String>> requestHeaders;
      private String protocolVersion;


      public Builder() {
     }

      public void setProtocolVersion(String protocolVersion) {
          this.protocolVersion = protocolVersion;
      }

      public void setHttpMethod(HttpMethod httpMethod) {
         this.httpMethod = httpMethod;
     }

     public void setUri(URI uri) {
         this.uri = uri;
     }

     public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
         this.requestHeaders = requestHeaders;
     }

     public HttpRequest build() {
         return new HttpRequest(httpMethod, protocolVersion, uri, requestHeaders );
     }
  }
}