
package com.telebroad.teleconsole.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity(tableName = "contact" , indices = {@Index("id")})
public class TeleConsoleContact implements Contact {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private Integer id;
    private String title;
    private String fname;
    private String lname;
    private String organization;
    private String username;
    private String email;
    private String pbxLine;
    private String chatChannel;
    private String extension;
    private String home;
    private String work;
    private String mobile;
    private String fax;
    private String website;
    private String photo;
    private Integer status;
    private String statusMsg;
    private String contactType;
    private Integer _public = 0;
    private Integer owned;

    @Ignore
    private ArrayList<PhoneNumber> telephoneLines;

    @Ignore
    private ArrayList<String> emailAddresses;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getPbxLine() {
        return pbxLine;
    }

    public void setPbxLine(String pbxLine) {
        this.pbxLine = pbxLine;
    }

    public String getChatChannel() {
        return chatChannel;
    }

    public void setChatChannel(String chatChannel) {
        this.chatChannel = chatChannel;
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

    public String
    getPhoto() {
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

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public Integer getPublic() {
        return _public;
    }

    public void setPublic(Integer _public) {
        this._public = _public;
    }

    public Integer getOwned() {
        return owned;
    }

    public void setOwned(Integer owned) {
        this.owned = owned;
    }

    @Override
    public String getWholeName() {
        String wholeName = Strings.nullToEmpty(getFname()).trim() + " " + Strings.nullToEmpty(getLname()).trim();
        if (wholeName.trim().isEmpty()) {
            if (!getAllLines().isEmpty()) {
                return getAllLines().get(0).formatted();
            } else {
                return "I shouldn't be here";
            }
        }
        return wholeName;

    }

    @Override
    public String getType() {
        return getContactType();
    }

    @Override
    public String getID() {
        return id.toString();
    }

    @Override
    public List<PhoneNumber> getTelephoneLines() {
        if (telephoneLines == null) {
            telephoneLines = new ArrayList<>();
            telephoneLines.addAll(splitTelephoneString(getHome(), PhoneNumber.PhoneType.HOME));
            telephoneLines.addAll(splitTelephoneString(getWork(), PhoneNumber.PhoneType.WORK));
            telephoneLines.addAll(splitTelephoneString(getMobile(), PhoneNumber.PhoneType.MOBILE));
            telephoneLines.addAll(splitTelephoneString(getFax(), PhoneNumber.PhoneType.FAX));

            // If it is a corporate contact, we want to get his extensions and pbx lines as well
           // android.util.Log.d("BottomSheet", "has Extension " + (contactType != null && contactType.equals("corporate")));
            if (contactType != null && contactType.equals("corporate")) {
                //android.util.Log.d("BottomSheet", "adding Extension");
                if (extension != null && !extension.isEmpty()) {
                    telephoneLines.add(PhoneNumber.getPhoneNumber(extension, PhoneNumber.PhoneType.EXTENSION));
                }else if (pbxLine != null && !pbxLine.isEmpty()) {
                    telephoneLines.add(PhoneNumber.getPhoneNumber(pbxLine, PhoneNumber.PhoneType.EXTENSION));
                }
            }
        }
        return telephoneLines;
    }

//    @Override
//    public List<PhoneNumber> getFaxLines() {
//        return splitTelephoneString(fax, PhoneNumber.PhoneType.FAX);
//    }

    @Ignore
    @Override
    public List<String> getEmailAddresses() {
        if (emailAddresses == null) {
            if (email == null || email.isEmpty()) {
                emailAddresses = new ArrayList<>();
            }else {
                emailAddresses = new ArrayList<>(Arrays.asList(email.split(",")));
//                android.util.Log.d("Contact13", "email was not empty size = " + emailAddresses.size() + " email " + email + " email char size " + email.length());
            }
        }
//        android.util.Log.d("Contact13", "size = " + emailAddresses.size());
        return emailAddresses;
    }

    private List<PhoneNumber> splitTelephoneString(String telephoneString, PhoneNumber.PhoneType type) {
        ArrayList<PhoneNumber> results = new ArrayList<>();
        if (telephoneString == null || telephoneString.trim().isEmpty()) {
            return results;
        } else {
            for (String number : telephoneString.split(",")) {
                results.add(new PhoneNumber(number, type));
            }
            return results;
        }
    }

    @Override
    public String toString() {
        return "TeleConsoleContact{" +
                "\n id=" + id +
                "\n title=" + title +
                "\n fname=" + fname +
                "\n lname=" + lname +
                "\n organization=" + organization +
                "\n username=" + username +
                "\n email=" + email +
                "\n pbxLine=" + pbxLine +
                "\n chatChannel=" + chatChannel +
                "\n extension=" + extension +
                "\n home=" + home +
                "\n work=" + work +
                "\n mobile=" + mobile +
                "\n fax=" + fax +
                "\n website=" + website +
                "\n photo=" + photo +
                "\n status=" + status +
                "\n statusMsg=" + statusMsg +
                "\n contactType=" + contactType +
                "\n _public=" + _public +
                "\n owned=" + owned +
                '}';
    }
}
