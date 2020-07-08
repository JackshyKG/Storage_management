package com.example.management;

import android.content.res.Resources;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.RequiresApi;

public class Document implements Parcelable {

    private static int MAX_LENGTH_AS_STRING = 30;

    private int number;
    private int date;// date of the document, int 20200603 - 03.06.2020 (dd.MM.yyyy)
    private int time;// date of the document, int 1603 - 16:03 (HH:mm)
    private boolean documentIn;/*in - true, out - false*/
    private ArrayList<String[]> tableList;// Document table - item and count (as a String)

    public Document(int number, int date, int time, boolean documentIn, ArrayList<String[]> tableList) {
        this.number = number;
        this.date = date;
        this.time = time;
        this.tableList = tableList;
        this.documentIn = documentIn;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getDate() {
        return date;
    }

    public String getDateAsStringType() {

        String dateTime = "";
        Date docDate = getDateAsDateType();

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        dateTime = format.format(docDate);

        return dateTime;

    }

    public Date getDateAsDateType() {

        Date docDate = null;
        String strTime;

        if (time < 1) {
            strTime = "0000";
        } else if (time < 10) {
            strTime = "000" + time;
        } else if (time < 100) {
            strTime = "00" + time;
        } else if (time < 1000) {
            strTime = "0" + time;
        } else {
            strTime = String.valueOf(time);
        }

        try {

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
            docDate = format.parse(date + strTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return docDate;

    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean getDocumentIn() {
        return documentIn;
    }

    public void setDocumentIn (boolean documentIn) {
        this.documentIn = documentIn;
    }

    public ArrayList<String[]> getTableList() {
        return tableList;
    }

    public String getTableListAsString() {

        String asString = "";

        for (String[] str : tableList) {

            asString = asString + str[0] + " - " + str[1]+", ";

            if (asString.length() > MAX_LENGTH_AS_STRING) {
                asString = asString.substring(0, MAX_LENGTH_AS_STRING + 1).concat("...");
                return asString;
            }

        }

        if (asString != null && asString.length() > 0) {
            asString = asString.substring(0, asString.length() - 2);
        }

        return asString;
    }

    public void setTableList(ArrayList<String[]> tableList) {
        this.tableList = tableList;
    }

    public static int getDateOrTimeAsInt(String dateString, boolean toDate) {

        int dateOrTime = 0;

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {

            Date newDate = format.parse(dateString);

            if (toDate) {
                format = new SimpleDateFormat("yyyyMMdd");
            } else {
                format = new SimpleDateFormat("HHmm");
            }


            String dateOrTimeString = format.format(newDate);
            dateOrTime = Integer.parseInt(dateOrTimeString);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateOrTime;

    }






    /*---PARCELABLE---*/
    protected Document(Parcel in) {
        number = in.readInt();
        date = in.readInt();
        time = in.readInt();
        documentIn = in.readByte() != 0;
        tableList = in.readArrayList(null);
    }

    public static final Creator<Document> CREATOR = new Creator<Document>() {
        @Override
        public Document createFromParcel(Parcel in) {
            return new Document(in);
        }

        @Override
        public Document[] newArray(int size) {
            return new Document[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(number);
        dest.writeInt(date);
        dest.writeInt(time);
        dest.writeByte((byte) (documentIn ? 1 : 0));
        dest.writeList(tableList);
    }

}
