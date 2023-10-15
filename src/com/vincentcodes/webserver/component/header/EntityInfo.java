package com.vincentcodes.webserver.component.header;

/**
 * <p>
 * Entity Headers directly modifies the incoming content (Body) from, for
 * example, POST requests (Request Body).
 * </p>
 * <p>
 * The {@link com.vincentcodes.webserver.request.RequestParser RequestParser}
 * supports some headers from the section "Message body information" in this
 * page only: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers
 * </p>
 * 
 * Currently not implemented: Content-Disposition Content-Language
 * Content-Location
 */
public class EntityInfo {
    private long length;
    private String type;
    private String encoding;
    private String transferEncoding;
    private RangeHeader range;
    
    public EntityInfo(long length, String type, String encoding, String transferEncoding, RangeHeader range) {
        this.length = length;
        this.type = type;
        this.encoding = encoding;
        this.transferEncoding = transferEncoding;
        this.range = range;
    }

    public static EntityInfo create(long length, String type, String encoding, String transferEncoding, String range){
        return new EntityInfo(
            length < 0? 0 : length,
            type == null? "" : type,
            encoding == null? "" : encoding,
            transferEncoding == null? "" : transferEncoding,
            range == null? null : RangeHeader.parse(range));
    }
    public static EntityInfo create(String length, String type, String encoding, String transferEncoding, String range){
        try{
            return create(Long.parseLong(length), type, encoding, transferEncoding, range);
        }catch(NumberFormatException e){
            return create(0, type, encoding, transferEncoding, range);
        }
    }

    /**
     * From "content-length"
     * @return 0 if no content is provided
     */
    public long getLength() {
        return length;
    }

    /**
     * From "content-type" (eg. multipart/form-data)
     * @return empty string if no type is found
     */
    public String getType() {
        return HttpHeaders.extractDirectValue(type);
    }

    /**
     * From "content-type" (eg. ----WebKitFormBoundaryabcdefghijklmnop)
     * @return null if no boundary is found
     */
    public String getBoundary(){
        return HttpHeaders.extractParameter(type, "boundary");
    }

    /**
     * From "content-encoding"
     * @return empty string if no encoding is found
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * From "transfer-encoding"
     * @return empty string if no encoding is found
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * From "range"
     * @return <code>null</code> if no range is found
     */
    public RangeHeader getRange(){
        return range;
    }
}
