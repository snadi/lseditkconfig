/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdb;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 *
 * @author nadsa02
 */
public abstract class Utilities {

    public static String getDisplayDate(java.util.Date date) {
        SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        return sd.format(date);
    }

    public static String getMySQLDate(java.util.Date date) {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
        return sd.format(date);
    }

    public static String formatTime(int timeInSec) {
        int days, hours, minutes;
        days = timeInSec / (3600 * 24);
        timeInSec = timeInSec - (days * 3600 * 24);
        hours = timeInSec / 3600;

        timeInSec = timeInSec - hours * 3600;
        minutes = timeInSec / 60;
        timeInSec = timeInSec - minutes * 60;

        return days + "d " + hours + "h " + minutes + "m " + timeInSec + "s";
    }

    public static java.util.Date convertToDate(int intDate) {

        return new java.util.Date(((long) intDate) * 1000);
    }

    public static String removeUnwantedChar(String input) {


        String temp = input;

        if (temp != null) {
            temp.replace("-", " DASH ");
            temp.replace("*", "STAR");
            temp.replace("&", " AND ");
        }

        return temp;

    }

    public static String removeSpaces(String input){               
        return  input.replaceAll(" ", "");
    }
    public static String generateColor() {
        Random r = new Random();
        return "(" + r.nextInt(256) + " " + r.nextInt(256) + " " + r.nextInt(256) + ")";
    }


 
    static String getMonth(int i) {
        String months[] = {"Jan", "Feb", "March", "Apr", "May", "Jun", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};

        return months[i];

    }

    static String getStatus(int inactive) {
        if (inactive == 1) {
            return "Inactive";
        } else {
            return "Active";
        }
    }

    static double getTimeCategory(int timeLapse) {
        int ten_min = 10 * 3600;
        int one_hour = 3600;
        int three_hour = 3 * 3600;
        int six_hour = 6 * 3600;
        int twelve_hr = 12 * 3600;
        int one_day = 24 * 3600;
        int two_day = 2 * 24 * 3600;
        int three_day = 3 * 24 * 3600;

        if (timeLapse <= ten_min) {
            return 1;
        } else if (timeLapse <= one_hour) {
            return 2;
        } else if (timeLapse <= three_hour) {
            return 3;
        } else if (timeLapse <= six_hour) {
            return 5;
        } else if (timeLapse <= twelve_hr) {
            return 6;
        } else if (timeLapse <= one_day) {
            return 7;
        } else if (timeLapse <= two_day) {
            return 8;
        } else if (timeLapse <= three_day) {
            return 9;
        } else {
            return 10;
        }
    }

    public static ResultSet executeQuery(Connection conn, String query) throws SQLException {
        return conn.createStatement().executeQuery(query);
    }
}
