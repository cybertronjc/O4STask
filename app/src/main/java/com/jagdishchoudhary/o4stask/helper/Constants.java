package com.jagdishchoudhary.o4stask.helper;

public interface Constants {
    /**
     * These bit-set values are passed from SunClockData to notification clients
     * to indicate changes in the underlying data.
     */
    public static final int LOCATION_CHANGED = 0x0001;
    public static final int TIME_CHANGED = 0x0002;
    public static final int SUN_UPDATE_NEEDED = 0x0004;
    public static final int MOON_UPDATE_NEEDED = 0x0008;
    public static final int DISPLAY_FONT_CHANGED = 0x0010;
    public static final int WORLD_MAP_SIZE_CHANGED = 0x0020;
    /**
     * All observers watch for STOP_NOW and kill all asynchronous processes
     * (including image loading) when it is received.
     */
    public static final int STOP_NOW = 0x10;
    /**
     * The sunrise and moonrise algorithms return values in a double[] vector
     * where result[RISE] is the time of sun (moon) rise and result[SET] is the
     * value of sun (moon) set. Sun.riseSetLST() also returns result[ASIMUTH_RISE]
     * and result[ASIMUTH_SET].
     */
    public static final int RISE = 0;
    public static final int SET = 1;
    public static final int ASIMUTH_RISE = 2;
    public static final int ASIMUTH_SET = 3;
    /**
     * ABOVE_HORIZON and BELOW_HORIZON are returned for sun and moon calculations
     * where the astronomical object does not cross the horizon.
     */
    public static final double ABOVE_HORIZON = Double.POSITIVE_INFINITY;
    public static final double BELOW_HORIZON = Double.NEGATIVE_INFINITY;
    /**
     * RA and DEC are indexes into the result vector from several astronomical
     * functions.
     */
    public static final int RA = 0;
    public static final int DEC = 1;
    /**
     * Degrees -> Radians: degree * DegRad Radians -> Degrees: radians / DegRad
     */
    public static final double DegRad = (Math.PI / 180.0);

}