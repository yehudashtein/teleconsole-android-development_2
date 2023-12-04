package com.telebroad.teleconsole.model;
import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.Context;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.telebroad.teleconsole.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchContactsModel implements Contact {
    private int id;
    private String title;
    private String fname;
    private String lname;
    private String organization;
    private String username;
    private String email;
    private String pbx_line;
    private String chat_channel;
    private String extension;
    private String home;
    private String work;
    private String mobile;
    private String fax;
    private String website;
    private String photo;
    private Integer status;
    private String status_msg;
    private String contactType;
    @SerializedName("public")
    private Integer public_Field;
    private Integer owned;
    private String color;
    private Integer speeddial;
    private Integer w;

    public Integer getPublic_Field() {
        return public_Field;
    }

    public void setPublic_Field(Integer public_Field) {
        this.public_Field = public_Field;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPbx_line() {
        return pbx_line;
    }

    public void setPbx_line(String pbx_line) {
        this.pbx_line = pbx_line;
    }

    public String getChat_channel() {
        return chat_channel;
    }

    public void setChat_channel(String chat_channel) {
        this.chat_channel = chat_channel;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatus_msg() {
        return status_msg;
    }

    public void setStatus_msg(String status_msg) {
        this.status_msg = status_msg;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public Integer getOwned() {
        return owned;
    }

    public void setOwned(Integer owned) {
        this.owned = owned;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSpeeddial() {
        return speeddial;
    }

    public void setSpeeddial(Integer speeddial) {
        this.speeddial = speeddial;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }
    @Ignore
    public  String formatPhoneNumber(String number) {
        if (!isNullOrEmpty(number)) {
            Pattern pattern = Pattern.compile("(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})");
            Matcher matcher = pattern.matcher(number);
            if (matcher.matches()) {
                return String.format("%s (%s) %s-%s%s",
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(3),
                        matcher.group(4),
                        matcher.group(5));
            }
        }
        return number;
    }
    @Ignore
    public String setType(String type, Context context){
        if (!isNullOrEmpty(type)) {
            switch (type) {
                case "corporate":
                    return context.getResources().getString(R.string.company);
                case "personal":
                    return context.getResources().getString(R.string.teleconsole);
                case "mobile":
                    return context.getResources().getString(R.string.mobile);
                default:
                    return "";
            }
        }
        return "";
    }
    @Ignore
    public  boolean haveSameCharacters(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        }

        Map<Character, Integer> charCount = new HashMap<>();

        // Populate map with characters from str1
        for (char c : str1.toCharArray()) {
            charCount.put(c, charCount.getOrDefault(c, 0) + 1);
        }

        // Subtract counts using characters from str2
        for (char c : str2.toCharArray()) {
            if (!charCount.containsKey(c) || charCount.get(c) == 0) {
                return false;
            }
            charCount.put(c, charCount.get(c) - 1);
        }

        return true;
    }

    @Override
    public String getWholeName() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public List<PhoneNumber> getTelephoneLines() {
        return null;
    }

    @Override
    public List<String> getEmailAddresses() {
        return null;
    }
}
