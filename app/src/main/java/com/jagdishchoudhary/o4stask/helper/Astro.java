package com.jagdishchoudhary.o4stask.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.jagdishchoudhary.o4stask.helper.Moon.hm;

public class Astro implements Constants {
    public static final int RA = 0; /* RightAsension from solarEphemeris */
    public static final int DEC = 1; /* DeclinatiOn from solarEphemeris */
    /*
     * JD = MJD + Astro.epochMJD;
     */
    public static final double epochMJD = 2400000.5;
    /*
     * Constants for solarEphemeris.
     */
    private static final double CosEPS = 0.91748;
    private static final double SinEPS = 0.39778;
    private static final double P2 = Math.PI * 2.0;
    /*
     * Constants for lunarEphemeris
     */
    private static final double ARC = 206264.8062;
    public static final long YEAR_START = 0;
    public static final long YEAR_MID = 183;
    public static final long YEAR_END = 365;

    public static double acos(double value) {
        return (Math.acos(value) / DegRad);
    }

    /**
     * atan2Deg computes atan2, returning the result in degrees, rather than
     * radians. It also handles the case where the denomenator is zero or NaN,
     * where the standard function is undefined.
     */
    public static double atan2Deg(double numerator, double denomenator) {
        double result;
        if ((denomenator == 0.0 || Double.isNaN(denomenator))
                && (numerator == 0.0 || Double.isNaN(numerator)))
            result = 0.0;
        else {
            result = Math.atan2(numerator, denomenator);
        }
        return (result / DegRad);
    }

    public static double cos(double value) {
        return (Math.cos(value * DegRad));
    }

    /**
     * Pascal fraction function. Rounds towards zero.
     *
     * @param value
     * @result integer (rounded towards zero)
     */
    public static double FRAC(double value) {
        double result = value - TRUNC(value);
        if (result < 0.0)
            result += 1.0;
        return (result);
    }

    /*
     * Get the transition date for this TimeZone. Call with startDate ==
     * YEAR_START , endDate == YEAR_MID, Then with startDate == YEAR_MID and
     * endDate == YEAR_END.
     */
    public static String getDSTChange(TimeZone tz, long startDay, long endDay) {
        /*
         * This mess lets us define the start and end times within the current year.
         * Since there is no TimeZone.getRule, we have to use a binary-chop
         * algorithm.
         */
        TimeZone tzGMT = TimeZone.getTimeZone("GMT");
        Calendar cal = GregorianCalendar.getInstance(tzGMT);
        int year = cal.get(Calendar.YEAR);
        cal.clear();
        cal.set(year, 0, 1); /* Jan 1 of this year */
        Date yearStartDate = cal.getTime();
        long yearStart = yearStartDate.getTime();
        long startTime = yearStart + (startDay * 86400000L);
        long endTime = yearStart + (endDay * 86400000L);
        /* */
        boolean dstAtStart = tz.inDaylightTime(new Date(startTime));
        String result;
        if (dstAtStart == tz.inDaylightTime(new Date(endTime))) {
            result = "no DST transition";
        } else {
            Date midDate = new Date();
            long midTime = 0;
            long delta;
            while ((delta = (endTime - startTime) / 2) > 0) {
                midTime = startTime + delta;
                boolean startDST = tz.inDaylightTime(new Date(startTime));
                boolean midDST = tz.inDaylightTime(new Date(midTime));
                if (midDST == startDST) {
                    startTime = midTime;
                } else {
                    endTime = midTime;
                }
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            String midDateText = df.format(new Date(midTime));
            if (dstAtStart) {
                result = "DST ends at " + midDateText;
            } else {
                result = "DST starts at " + midDateText;
            }
        }
        return (result);
    }

    /**
     * Compute the string corresponding to this Sunrise/Sunset.
     *
     * @param //riseSetValue
     *          the time of sunrise or sunset
     * @param whichEvent
     *          If there is an event, this will be prepended to the time.
     * @return The proper string: null string if no event.
     */
    public static String getRiseSetString(double eventValue, String whichEvent /*
     * "Sunrise"
     * or
     * "Sunset"
     */
    ) {
        if (Astro.isEvent(eventValue)) {
            return (whichEvent + hm(eventValue));
        } else {
            return ("");
        }
    }

    public static TimeZone getTimeZone(String timeZoneID) {
        TimeZone result = TimeZone.getTimeZone(timeZoneID);
        int value = 0;
        if (result == null) {
            /*
             * Hmm, maybe it's a time string. First, try a minute offset (as an
             * integer).
             */
            try {
                value = Integer.parseInt(timeZoneID);
            } catch (NumberFormatException e) {
                /*
                 * Well, that failed. Try to parse a HH:MM [E | W} string.
                 */
                int colon = timeZoneID.indexOf(':');
                String hourString = "0";
                String minuteString = "0";
                if (colon > 0) {
                    int sign = 1;
                    hourString = timeZoneID.substring(colon - 1);
                    minuteString = timeZoneID.substring(colon + 1, timeZoneID.length());
                    if (minuteString.endsWith("E") || minuteString.endsWith("e")) {
                        minuteString = minuteString.substring(minuteString.length() - 1);
                    } else if (minuteString.endsWith("W") || minuteString.endsWith("w")) {
                        minuteString = minuteString.substring(minuteString.length() - 1);
                        sign = -1;
                    }
                    try {
                        int hour = Integer.parseInt(hourString);
                        int minute = Integer.parseInt(minuteString);
                        value = sign * ((hour * 60) + minute);
                    } catch (NumberFormatException except) {
                        /*
                         * Sigh. Nothing works.
                         */
                    }
                }
            }
            String[] ids = TimeZone.getAvailableIDs(value);
            if (ids != null && ids.length > 0) {
                result = TimeZone.getTimeZone(ids[0]);
            }
        }
        return (result);
    }

    /**
     * Return info about this timezone. This is always of the form TimeZoneID ' '
     * offset (DST if enabled) The TimeZone choice needs the space.
     */
    public static String getTimeZoneInfo(String timezoneID, Date thisDate) {
        return (getTimeZoneInfo(timezoneID, thisDate, false));
    }

    public static String getTimeZoneInfo(String timezoneID, Date thisDate,
                                         boolean appendDSTRule) {
        StringBuffer text = new StringBuffer(timezoneID);
        try {
            TimeZone tz = TimeZone.getTimeZone(timezoneID);
            if (tz == null) {
                text.append(" undefined");
            } else {
                long tzOffset = tz.getRawOffset();
                if (tz.inDaylightTime(thisDate)) {
                    tzOffset += 3600000;
                }
                text.append(" " + timeZoneString(tzOffset));
                if (tz.inDaylightTime(thisDate)) {
                    text.append(" (DST)");
                }
                if (appendDSTRule) {
                    if (tz.useDaylightTime()) {
                        text.append(". " + Astro.getDSTChange(tz, YEAR_START, YEAR_MID));
                        text.append(" and " + Astro.getDSTChange(tz, YEAR_MID, YEAR_END));
                    } else {
                        text.append(" (never uses DST)");
                    }
                }
            }
        } catch (Exception e) {
            text.append(", error: " + e);
        }
        return (text.toString());
    }

    public static String timeZoneString(long timezone) {
        TimeZone timeZone = TimeZone.getDefault();
        timeZone.getTimeZone("GMT+5:30");

        return (timeZone.toString());
    }

    public static long getTimeZoneOffset() {
        return (getTimeZoneOffset(new Date()));
    }

    public static long getTimeZoneOffset(Date javaDate) {
        return (getTimeZoneOffset(javaDate, TimeZone.getDefault()));
    }

    public static long getTimeZoneOffset(Date javaDate, TimeZone timeZone) {
        long timezoneOffset = 0;
        try {
            timezoneOffset = timeZone.getRawOffset();
            if (timeZone.inDaylightTime(javaDate)) {
                timezoneOffset += 3600000;
            }
        } catch (Exception e) {
            System.err.println("Can't get System timezone: " + e);
        }
        return (timezoneOffset);
    }



    public static boolean isEvent(double value) {
        return (value != ABOVE_HORIZON && value != BELOW_HORIZON);
    }

    /**
     * Compute Local Mean Sidereal Time (LMST).
     *
     * @param MJD
     *          Modified Julian Day number
     * @param longitude
     *          Longitude in degrees, East is positive.
     * @result The local mean sidereal time.
     * @see //Section 3.3 (p41) in Astronomy on the Personal Computer. (Note: While
     *      Astronomy on the Personal Computer reckons longitude positive towards
     *      the West, this routine recons it positive towards the East.
     */
    public static double LMST( /* Used by Moon.java */
            double MJD, double longitude) {
        double MJD0 = Math.floor(MJD);
        double UT = (MJD - MJD0) * 24.0;
        double T = (MJD0 - 51544.5) / 36525.0;
        double GMST = 6.697374558 + 1.0027379093 * UT
                + (8640184.812866 + (0.093104 - 6.2E-6 * T) * T) * T / 3600.0;
        double LMST = 24.0 * FRAC((GMST + longitude / 15.0) / 24.0);
        return (LMST);
    }

    /**
     * This is the low-precision lunar ephemeris, MiniMoon, from Astronomy on the
     * Personal Computer, p. 38. It is accurate to about 5'.
     *
     * @parameter actualMJD The modified Julian Date for the actual time to be
     *            computed.
     * @return result[0] = rightAscension, result[1] = declination
     */
    public static double[] lunarEphemeris(double MJD) {
        double T = (MJD - 51544.5) / 36525.0;
        double L0 = Astro.FRAC(0.606433 + 1336.855225 * T); /*
         * Mean longitude
         * (revolutions)
         */
        double L = P2 * Astro.FRAC(0.374897 + 1325.552410 * T); /* Moon mean anomaly */
        double LS = P2 * Astro.FRAC(0.993133 + 99.997361 * T); /* Sun mean anomaly */
        double D = P2 * Astro.FRAC(0.827361 + 1236.853086 * T); /* Moon - Sun */
        double F = P2 * Astro.FRAC(0.259086 + 1342.227825 * T); /*
         * mean latitude
         * argument
         */
        double DL = 22640 * Math.sin(L) - 4586 * Math.sin(L - 2 * D) + 2370
                * Math.sin(2 * D) + 769 * Math.sin(2 * L) - 668 * Math.sin(LS) - 412
                * Math.sin(2 * F) - 212 * Math.sin(2 * L - 2 * D) - 206
                * Math.sin(L + LS - 2 * D) + 192 * Math.sin(L + 2 * D) - 165
                * Math.sin(LS - 2 * D) - 125 * Math.sin(D) - 110 * Math.sin(L + LS)
                + 148 * Math.sin(L - LS) - 55 * Math.sin(2 * F - 2 * D);
        double S = F + (DL + 412 * Math.sin(2 * F) + 541 * Math.sin(LS)) / ARC;
        double H = F - 2 * D;
        double N = -526 * Math.sin(H) + 44 * Math.sin(L + H) - 31
                * Math.sin(-L + H) - 23 * Math.sin(LS + H) + 11 * Math.sin(-LS + H)
                - 25 * Math.sin(-2 * L + F) + 21 * Math.sin(-L + F);
        double L_MOON = P2 * Astro.FRAC(L0 + DL / 1296.0E3); /* L in radians */
        double B_MOON = (18520.0 * Math.sin(S) + N) / ARC; /* B in radians */
        /* Equatorial coordinates */
        double CB = Math.cos(B_MOON);
        double X = CB * Math.cos(L_MOON);
        double V = CB * Math.sin(L_MOON);
        double W = Math.sin(B_MOON);
        double Y = CosEPS * V - SinEPS * W;
        double Z = SinEPS * V + CosEPS * W;
        double RHO = Math.sqrt(1.0 - Z * Z);
        double[] result = new double[2];
        result[DEC] = (360.0 / P2) * Math.atan(Z / RHO);
        result[RA] = (48.0 / P2) * Math.atan(Y / (X + RHO));
        if (result[RA] < 0.0) {
            result[RA] += 24.0;
        }
        return (result);
    }



    public static String riseSetString(double sunrise, double sunset) {
        StringBuffer text = new StringBuffer();
        if (sunrise == ABOVE_HORIZON && sunset == ABOVE_HORIZON) {
            text.append("Sun is above horizon all day");
        } else if (sunrise == BELOW_HORIZON && sunset == BELOW_HORIZON) {
            text.append("Sun does not rise today");
        } else {
            if (Astro.isEvent(sunrise)) {
                text.append(getRiseSetString(sunrise, "Sunrise at "));
                if (Astro.isEvent(sunset)) {
                    text.append(getRiseSetString(sunset, ", Sunset at "));
                } else {
                    text.append(", Sun does not set");
                }
            } else {
                text.append(getRiseSetString(sunset, "Sunset at "));
            }
        }
        return (text.toString());
    }

    /**
     * Compute the time of sunrise and sunset for a specific time and location.
     *
     * @param date
     *          Java date
     * @param timezone
     *          Timezone offset from GMT (minutes)
     * @param latitude
     *          Observer's latitude (North is positive).
     * @param longitude
     *          Observer's longitude (East is positive).
     * @return a text string with the times.
     */


    /**
     * Trignometric classes that take degree arguments.
     */
    public static double sin(double value) {
        return (Math.sin(value * DegRad));
    }


    /**
     * Pascal truncate function. Returns the integer nearest to zero. (This
     * behaves differently than C/Java Math.floor() for negative values.)
     *
     * @param value
     *          The value to convert
     * @result Integral value nearest zero
     * @see //java.Math.floor
     */
    public static double TRUNC(double value) {
        double result = Math.floor(Math.abs(value));
        if (value < 0.0)
            result = (-result);
        return (result);
    }



}