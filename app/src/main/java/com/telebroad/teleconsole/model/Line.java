package com.telebroad.teleconsole.model;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.telebroad.teleconsole.model.Line.LineType.FAX;
import static com.telebroad.teleconsole.model.Line.LineType.PHONE;
import static com.telebroad.teleconsole.model.Line.LineType.SMS;
import static com.telebroad.teleconsole.model.Line.LineType.VOICEMAIL;

public class Line {

    public Line(){
        super();
    }
    public Line(String name){
        this.name = name;
    }
    @SerializedName(value="name", alternate={"sms_line"})
    private String name;
    @SerializedName(value="pubnub_channel", alternate={"channel"})
    private String pubnub_channel;
    @SerializedName(value="owner", alternate={"owned"})
    private boolean owner;

    public LineType getType(){
        if(pubnub_channel == null){
            return null;
        }
        switch (pubnub_channel.substring(0, pubnub_channel.indexOf('_'))){
            case "pbx":
                return PHONE;
            case "sms":
                return SMS;
            case "voicemail":
                return VOICEMAIL;
            case "fax":
                return FAX;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPubnub_channel() {
        return pubnub_channel;
    }

    public void setPubnub_channel(String pubnub_channel){
        this.pubnub_channel = pubnub_channel;
    }
    public boolean isOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "Line{" +
                "type=" + getType() +
                ", name='" + name + '\'' +
                ", pubnub_channel='" + pubnub_channel + '\'' +
                ", owner=" + owner +
                '}';
    }

    public boolean equalsString(String other){
        return PhoneNumber.getPhoneNumber(name).phoneNumberEquals(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (!(obj instanceof Line)){
            if (obj instanceof String){
                return equalsString((String)obj);
            }
            return false;
        }else {
            Line other = (Line)obj;
            //android.util.Log.d("PhoneLine", "this " + name + " other " + other.name);
            return PhoneNumber.getPhoneNumber(name).equals(PhoneNumber.getPhoneNumber(other.name));
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    public static List<String> convertLineListToStringList(List<? extends Line> lines){
        if (lines == null || lines.isEmpty()){
            return new ArrayList<>();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return lines.stream().map(PhoneNumber::formatLine).collect(Collectors.toList());
        }else{
            List<String> strings = new ArrayList<>();
            for (Line line: lines) {
                strings.add(PhoneNumber.formatLine(line));
            }
            return strings;
            //return lines.parallelStream().map();
        }
    }

    public enum LineType{
        @SerializedName("phone_line")
        PHONE("phone"),
        @SerializedName("voicemail")
        VOICEMAIL("voicemail"),
        @SerializedName("fax")
        FAX("fax"),
        @SerializedName("sms_line")
        SMS("sms");
        String type;
        LineType(String type){
            this.type = type;
        }
    }
    public static class PhoneLine extends Line{
        String description;
        String callerID;
        String fcode;
        String secret;
        String callerIDint;
        String callerID_external;
        @SerializedName("appServer")
        String appServer;

        public static List<String> getFormattedPhoneLines(List<PhoneLine> lines){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                return lines.stream().map(PhoneLine::formatted).collect(Collectors.toList());
            }else{
                List<String> strings = new ArrayList<>();
                for (PhoneLine line: lines) {
                    strings.add(line.formatted());
                }
                return strings;
            }
        }


        @Override
        public String toString() {
            return "PhoneLine{" +
                    "type=" + getType() +
                    "\n name=" + getName() +
                    "\n pubnub_channel=" + getPubnub_channel() +
                    "\n owner=" + super.owner +
                    "\n description=" + description +
                    "\n callerID=" + callerID +
                    "\n fcode=" + fcode +
                    "\n secret=" + secret +
                    "\n callerIDint=" + callerIDint +
                    "\n callerID_external=" + callerID_external +
                    "\n appserver=" + appServer +
                    '}';
        }

        public String getDescription() {
            return description;
        }

        private String formatted(){
            String info = getFcode();
            if (isStringNull(info)){
                info = getCallerIDint();
            }
            if(isStringNull(info)){
                info = getDescription();
            }
            return getName() + " - " + info;
        }

        private boolean isStringNull(String string) {
            return string == null || string.isEmpty() || string.equals("null");
        }

        public String getCallerID() {
            return callerID;
        }

        public String getFcode() {
            return fcode;
        }

        public String getSecret() {
            return secret;
        }

        public String getCallerIDint() {
            return callerIDint;
        }

        public String getAppServer() {
            return appServer;
        }

        public void setAppServer(String appServer) {
            this.appServer = appServer;
        }

        public String getCallerID_external() {
            return callerID_external;
        }
        @Override
        public LineType getType(){
            return PHONE;
        }
    }

}
