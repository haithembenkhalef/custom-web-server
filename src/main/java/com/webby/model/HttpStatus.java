package com.webby.model;

public class HttpStatus {
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NO_CONTENT = 204;
    
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int REQUEST_TIMEOUT = 408;
    
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    
    public static String getStatusMessage(int code) {
        return switch (code) {
            case OK -> "OK";
            case CREATED -> "Created";
            case ACCEPTED -> "Accepted";
            case NO_CONTENT -> "No Content";
            case BAD_REQUEST -> "Bad Request";
            case UNAUTHORIZED -> "Unauthorized";
            case FORBIDDEN -> "Forbidden";
            case NOT_FOUND -> "Not Found";
            case METHOD_NOT_ALLOWED -> "Method Not Allowed";
            case REQUEST_TIMEOUT -> "Request Timeout";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            case NOT_IMPLEMENTED -> "Not Implemented";
            case BAD_GATEWAY -> "Bad Gateway";
            case SERVICE_UNAVAILABLE -> "Service Unavailable";
            default -> "Unknown Status";
        };
    }
}
