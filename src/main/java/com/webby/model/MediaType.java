package com.webby.model;

public class MediaType {
    private final String type;
    private final String subtype;
    private final String charset;

    // Constructor
    public MediaType(String type, String subtype) {
        this(type, subtype, null);
    }

    public MediaType(String type, String subtype, String charset) {
        this.type = type;
        this.subtype = subtype;
        this.charset = charset;
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getCharset() {
        return charset;
    }

    // Factory method for creating common media types
    public static MediaType parse(String mediaType) {
        if (mediaType == null || mediaType.isEmpty()) {
            throw new IllegalArgumentException("Media type cannot be null or empty");
        }

        String[] parts = mediaType.split(";");
        String[] typeAndSubtype = parts[0].split("/");

        if (typeAndSubtype.length != 2) {
            throw new IllegalArgumentException("Invalid media type format: " + mediaType);
        }

        String type = typeAndSubtype[0].trim();
        String subtype = typeAndSubtype[1].trim();
        String charset = null;

        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String[] param = parts[i].trim().split("=");
                if (param.length == 2 && "charset".equalsIgnoreCase(param[0])) {
                    charset = param[1].trim();
                }
            }
        }

        return new MediaType(type, subtype, charset);
    }

    // To check if the media type matches a specific type/subtype
    public boolean is(String type, String subtype) {
        return this.type.equalsIgnoreCase(type) && this.subtype.equalsIgnoreCase(subtype);
    }

    // To check if media type matches a given type
    public boolean isType(String type) {
        return this.type.equalsIgnoreCase(type);
    }

    // Overriding equals() and hashCode() for comparisons
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MediaType mediaType = (MediaType) obj;

        if (!type.equals(mediaType.type)) return false;
        if (!subtype.equals(mediaType.subtype)) return false;
        return charset != null ? charset.equals(mediaType.charset) : mediaType.charset == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + subtype.hashCode();
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        return result;
    }

    // ToString method
    @Override
    public String toString() {
        return charset == null ? type + "/" + subtype : type + "/" + subtype + "; charset=" + charset;
    }

    // Common media type constants
    public static final MediaType APPLICATION_JSON = new MediaType("application", "json", "UTF-8");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain", "UTF-8");
    public static final MediaType APPLICATION_XML = new MediaType("application", "xml");
    public static final MediaType IMAGE_PNG = new MediaType("image", "png");

    // Example usage
    public static void main(String[] args) {
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        System.out.println("Parsed Media Type: " + mediaType);

        // Check if it matches
        System.out.println("Is application/json? " + mediaType.is("application", "json"));
    }
}
