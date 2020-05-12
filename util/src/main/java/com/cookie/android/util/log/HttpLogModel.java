package com.cookie.android.util.log;

import java.util.Map;

public class HttpLogModel {
    private String url;
    private String method;
    private Map<String, String> requestHeaders;
    private Map<String, String> params;
    private Map<String, String> responseHeaders;
    private Throwable throwable;
    private String content;
    private long cost;

    private HttpLogModel(Builder builder) {
        setUrl(builder.url);
        setMethod(builder.method);
        setRequestHeaders(builder.requestHeaders);
        setParams(builder.params);
        setResponseHeaders(builder.responseHeaders);
        setThrowable(builder.throwable);
        setContent(builder.content);
        setCost(builder.cost);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getCost() {
        return cost;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public static final class Builder {
        private String url;
        private String method;
        private Map<String, String> requestHeaders;
        private Map<String, String> params;
        private Map<String, String> responseHeaders;
        private Throwable throwable;
        private String content;
        private long cost;

        public Builder() {
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder method(String val) {
            method = val;
            return this;
        }

        public Builder requestHeaders(Map<String, String> val) {
            requestHeaders = val;
            return this;
        }

        public Builder params(Map<String, String> val) {
            params = val;
            return this;
        }

        public Builder responseHeaders(Map<String, String> val) {
            responseHeaders = val;
            return this;
        }

        public Builder throwable(Throwable val) {
            throwable = val;
            return this;
        }

        public Builder content(String val) {
            content = val;
            return this;
        }

        public Builder cost(long ms) {
            this.cost = ms;
            return this;
        }

        public HttpLogModel build() {
            return new HttpLogModel(this);
        }
    }
}
