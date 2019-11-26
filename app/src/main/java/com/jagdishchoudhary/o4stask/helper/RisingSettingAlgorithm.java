package com.jagdishchoudhary.o4stask.helper;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.StrictMath.sin;

public class RisingSettingAlgorithm {

    private static final DecimalFormat df = new DecimalFormat("#.###");
    private static double sinDecF = 0;

    public  String getRisingSettingTime(Date date, LatLng latLng, String type){


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Log.d("Date", month+1 + " "+ year + " "+ day);

        //calculate day of year
        double dayOfYear = calculateDayofYear(day, month+1, year);

        Log.d("LngHourFInal", Double.toString(latLng.longitude / 15));

        //convert Longitude to hour
        double lngHour = convertLongitudeToHour(latLng.longitude, type, dayOfYear);
        Log.d("LngHour", Double.toString(lngHour));

        //calculate sun mean anamoly
        double sunMean = calculateSunMeanAnamoly(lngHour);
        Log.d("Sun mean", Double.toString(sunMean));

        //right ascesntion
        double RA = rightAscension(sunMean);

        //calculate the Sun's true longitude
        double trueLng = sunTrueLongitude(sunMean);
        Log.d("True L", Double.toString(trueLng));

        //calculate the Sun's declination
        double declination = declination(trueLng);

        //calculate the Sun's local hour angle
        double localHourAngle = localHourAngle(sinDecF, latLng.latitude, declination, type);
        Log.d("Local hour angle", Double.toString(localHourAngle));


        //calculate local mean time of rising/setting
        double meanTime = calculateMeanTime(localHourAngle, RA, lngHour);

        //back to UTC
        double UTC = backToUTC(meanTime, Double.parseDouble(df.format(latLng.longitude / 15)));

        //get time
        String time = getLocalTimeAsString(BigDecimal.valueOf(UTC));

        return time;





    }

    private static double calculateDayofYear(int day,int month, int year){
        double n1 = floor((275 * month) / 9);
        double n2 = floor((month + 9) / 12);
        double n3 = (1 + floor((year - 4 * floor(year / 4) + 2)/3));

        double xday = n1 - (n2 * n3) + day - 30;

        return Double.parseDouble(df.format(xday));


    }

    private static double convertLongitudeToHour(double longitude, String type, double n){

        double lngHour = longitude / 15;
        double t;
        if (type.equals("Rising")){
            t =  n + ((6 - lngHour) / 24);
        }
        else {
            t =  n + ((18 - lngHour) / 24);
        }

        return  Double.parseDouble(df.format(t));



    }

    private static double calculateSunMeanAnamoly(double t){
        return Double.parseDouble(df.format(((0.9856 * t) - 3.289)));
    }

    //calculate the Sun's true longitude
    private static double sunTrueLongitude(double M){

        double mInRadian =M;//(M*180)/3.14;
        Log.d("M: ",  Double.toString(M));

        double L = mInRadian  + (1.916 * Math.sin(3.14*mInRadian/180)) + (0.020 * Math.sin(2 * (3.14*mInRadian/180))) + 282.634;
        //double L = mInRadian  + (1.916 * Math.sin(mInRadian)) + (0.020 * Math.sin(2 * (mInRadian))) + 282.634;

        Log.d("L before", Double.toString(L));

        if (L > 360){
            L =  L - 360;

        }
        else if (L < 0){
            L =   L + 360;
        }

        Log.d("L", Double.toString(L));

        return Double.parseDouble(df.format(L));
    }

    //calculate the Sun's right ascension
    //right ascension value needs to be in the same quadrant as L
    //right ascension value needs to be converted into hours
    private static double rightAscension(double L){
        double RA = (180*Math.atan(0.91764 * Math.tan(3.14*L/180))/3.14);

        if (RA > 360){
            RA =  RA - 360;
        }
        else {
            RA =  RA + 360;
        }

        //right ascension value needs to be in the same qua
        double Lquadrant = (floor(L / (90))) * 90;
        double RAquadrant = (floor(RA / 90)) * 90;
        RA = RA + (Lquadrant - RAquadrant);

        Log.d("RA Before " , Double.toString(RA));

        //right ascension value needs to be converted into hours
        RA = RA / 15;

        return Double.parseDouble(df.format(RA));
    }

    //calculate the Sun's declination
    private static double declination(double L){
        double sinDec = 0.39782 * Math.sin((3.14 * L)/180);
        sinDecF = Double.parseDouble(df.format(0.39782 * Math.sin((3.14* L)/180)));
        double cosDec = cos((Math.asin((sinDec))));

        Log.d("DEC", "SinDec:" + sinDec + " cosDec: "+ cosDec + " L:" + L +  " singL : "+ Math.sin((Math.PI * L)/180));

        return Double.parseDouble(df.format(cosDec));
    }

    //calculate the Sun's local hour angle
    private static double localHourAngle(double sinDec, double latitude, double cosDec, String type){

        double a =  cos(90*3.14/180) -  (sinDec * Math.sin(3.14* latitude/180)) ;


        Log.d("a", Double.toString(a));
        Log.d("Value", Double.toString(0.01454 + (sinDec * Math.sin(latitude))) );

        double b = (cosDec * cos((3.14 * latitude)/180));
        double cosH =  a / b;
        Log.d("aCosH", Double.toString(a));

        Log.d("cosH", cosH + " a:"+ a + " b: "+ b+  "Here:" + (0.01454 +(sinDec * Math.sin((180*latitude)/3.14))) + "H2:" + (cosDec * Math.cos((180*latitude)/3.14)));

//        if (cosH > 1){
//
//        }


        double H;
        if (type.equals("Rising")) {

            H = 360 -  180*Math.acos(cosH)/3.14;
            Log.d("HourAngle", Double.toString(180*Math.acos(cosH)/3.14));

        } else {
          H = 180*Math.acos(cosH)/3.14;
        }

        H = H / 15;



        return Double.parseDouble(df.format(H));
    }

    //calculate local mean time of rising/setting
    private static double calculateMeanTime(double H, double RA, double t){
        double T = H + RA - (0.06571 * t) - 6.622;

        Log.d("LocalMeanTime", "H: " + H + " RA: "+ RA + " t: "+ t + " T:" + Double.toString(T));

        return Double.parseDouble(df.format(T));
    }

    //adjust back to UTC
    private static double backToUTC(double T, double lngHour){

        double UT = T - lngHour;
        if (UT > 24) {
            UT = UT - 24;
        }
        else if (UT < 0) {
            UT = UT + 24;
        }

        Log.d("UTC", Double.toString(UT));

        return Double.parseDouble(df.format(UT));
    }

    //get the local time
   // private static
    public  String getLocalTimeAsString(BigDecimal localTimeParam) {
        if (localTimeParam == null) {
            return "99:99";
        }

        BigDecimal localTime = localTimeParam;
        if (localTime.compareTo(BigDecimal.ZERO) == -1) {
            localTime = localTime.add(BigDecimal.valueOf(24.0D));
        }
        String[] timeComponents = localTime.toPlainString().split("\\.");
        int hour = Integer.parseInt(timeComponents[0]);

        BigDecimal minutes = new BigDecimal("0." + timeComponents[1]);
        minutes = minutes.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN);
        if (minutes.intValue() == 60) {
            minutes = BigDecimal.ZERO;
            hour += 1;
        }
        if (hour == 24) {
            hour = 0;
        }

        String minuteString = minutes.intValue() < 10 ? "0" + minutes.toPlainString() : minutes.toPlainString();
        String hourString = (hour < 10) ? "0" + String.valueOf(hour) : String.valueOf(hour);
        return hourString + ":" + minuteString;
    }



}
