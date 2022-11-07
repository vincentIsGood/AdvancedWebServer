package com.vincentcodes.webserver.http2;

import com.vincentcodes.webserver.http2.types.SettingsFrame;

/**
 * The defined parameter IDs can be found at {@link SettingsFrame}
 */
public class SettingParameter {
    private int identifier;
    private long value;

    public SettingParameter(int identifier, long value) {
        this.identifier = identifier;
        this.value = value;
    }

    public int getIdentifier() {
        return identifier;
    }

    public long getValue() {
        return value;
    }

    public String toString(){
        return String.format("{SettingParameter identifier: %d, value: %d}", identifier, value);
    }
}
