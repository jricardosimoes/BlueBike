package com.ricardosimoes.bluebike;
import java.util.HashMap;

/**
 * Created by ricardo on 22/12/14.
 */
public class TireAttributes {

    private static HashMap<String, String> attributes = new HashMap();

    public static String MTB29er100  = "2113.66";
    public static String MTB29er125  = "2153.56";
    public static String MTB29er15   = "2193.46" ;
    public static String MTB29er175  = "2233.36";
    public static String MTB29er190  = "2257.30";
    public static String MTB29er195  = "2265.09";
    public static String MTB29er200  = "2273.26";
    public static String MTB29er210  = "2289.22";
    public static String MTB29er2125 = "2293.36";
    public static String MTB29er220  = "2305.18";
    public static String MTB29er225  = "2313.15";
    public static String MTB29er230  = "2321.13";
    public static String MTB29er235  = "2329.11";
    public static String MTB29er240  = "2335.40";


    static {
        attributes.put(MTB29er100,  "700c/29er 1.00 inch");
        attributes.put(MTB29er125,  "700c/29er 1.25 inch");
        attributes.put(MTB29er15 ,  "700c/29er 1.50 inch");
        attributes.put(MTB29er175,  "700c/29er 1.75 inch");
        attributes.put(MTB29er190,  "700c/29er 1.90 inch");
        attributes.put(MTB29er195,  "700c/29er 1.95 inch");
        attributes.put(MTB29er200,  "700c/29er 2.00 inch");
        attributes.put(MTB29er210,  "700c/29er 2.10 inch");
        attributes.put(MTB29er2125, "700c/29er 2.125 inch");
        attributes.put(MTB29er220,  "700c/29er 2.20 inch");
        attributes.put(MTB29er225,  "700c/29er 2.25 inch");
        attributes.put(MTB29er230,  "700c/29er 2.30 inch");
        attributes.put(MTB29er235,  "700c/29er 2.35 inch");
        attributes.put(MTB29er240,  "700c/29er 2.40 inch");
    }

    public static String tirelookup(String tireLen, String defaultName) {
            String name = attributes.get(tireLen);
          return name == null ? defaultName : name;
    }

}
