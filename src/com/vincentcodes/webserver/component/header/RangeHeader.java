package com.vincentcodes.webserver.component.header;

/**
 * "byte" is the only unit option currently (2020). 
 * <pre>{@code
 * Range: <unit>=<range-start>-<range-end>
 * }</pre>
 * 
 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range
 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Ranges
 * @see https://tools.ietf.org/html/rfc7233#section-4.4
 */
public class RangeHeader {
    private String unit;
    private long rangeStart = -1;
    private long rangeEnd = -1;

    public RangeHeader(String unit, long rangeStart, long rangeEnd){
        this.unit = unit;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }
    
    public static RangeHeader parse(String rangeHeader){
        // skip "byte="
        String range = rangeHeader.substring(rangeHeader.indexOf('=') + 1);
        String[] startAndEnd = range.split("-");
        long rangeStart = Long.parseLong(startAndEnd[0]);
        long rangeEnd = -1;

        if(startAndEnd.length > 1) {
            rangeEnd = Long.parseLong(startAndEnd[1]);
        }
        return new RangeHeader("byte", rangeStart, rangeEnd);
    }

    /**
     * Check if there are enough info to determine the size of the 
     * content it wants.
     */
    public boolean isValid(){
        return (rangeStart != -1 || rangeEnd != -1) && rangeStart >= 0;
    }

    public boolean hasRangeEnd(){
        return rangeEnd != -1;
    }

    public String getUnit() {
        return unit;
    }

    public long getRangeStart() {
        return rangeStart;
    }

    public long getRangeEnd() {
        return rangeEnd;
    }
}
