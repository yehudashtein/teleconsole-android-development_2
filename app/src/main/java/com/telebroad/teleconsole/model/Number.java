package com.telebroad.teleconsole.model;

import androidx.room.Ignore;

public class Number {
    private String customer;
    private String description;
    private String ingroup;
    private String _class;
    private String stype;
    private String snumber;
    private String sname;
    private String snameAction;
    private String dtype;
    private String dnumber;
    private String callerid;
    private String routing;
    private String updateRouting;
    private String owner;
    private String shortcut;
    private String alias;
    private String recordgroup;
    private String language;
    private String playMessage;
    private String music;
    private String maximumSeconds;
    private String shared;
    private String directory;
    private String emergencyRegister;
    private String ported;
    private String screen;
    private String direct;
    private String faxDetect;
    private String faxDtype;
    private String faxDnumber;
    private String panel;

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngroup() {
        return ingroup;
    }

    public void setIngroup(String ingroup) {
        this.ingroup = ingroup;
    }

    public String getClass_() {
        return _class;
    }

    public void setClass_(String _class) {
        this._class = _class;
    }

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    public String getSnumber() {
        return snumber;
    }

    public void setSnumber(String snumber) {
        this.snumber = snumber;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getSnameAction() {
        return snameAction;
    }

    public void setSnameAction(String snameAction) {
        this.snameAction = snameAction;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getDnumber() {
        return dnumber;
    }

    public void setDnumber(String dnumber) {
        this.dnumber = dnumber;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getUpdateRouting() {
        return updateRouting;
    }

    public void setUpdateRouting(String updateRouting) {
        this.updateRouting = updateRouting;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getRecordgroup() {
        return recordgroup;
    }

    public void setRecordgroup(String recordgroup) {
        this.recordgroup = recordgroup;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPlayMessage() {
        return playMessage;
    }

    public void setPlayMessage(String playMessage) {
        this.playMessage = playMessage;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getMaximumSeconds() {
        return maximumSeconds;
    }

    public void setMaximumSeconds(String maximumSeconds) {
        this.maximumSeconds = maximumSeconds;
    }

    public String getShared() {
        return shared;
    }

    public void setShared(String shared) {
        this.shared = shared;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getEmergencyRegister() {
        return emergencyRegister;
    }

    public void setEmergencyRegister(String emergencyRegister) {
        this.emergencyRegister = emergencyRegister;
    }

    public String getPorted() {
        return ported;
    }

    public void setPorted(String ported) {
        this.ported = ported;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getDirect() {
        return direct;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public String getFaxDetect() {
        return faxDetect;
    }

    public void setFaxDetect(String faxDetect) {
        this.faxDetect = faxDetect;
    }

    public String getFaxDtype() {
        return faxDtype;
    }

    public void setFaxDtype(String faxDtype) {
        this.faxDtype = faxDtype;
    }

    public String getFaxDnumber() {
        return faxDnumber;
    }

    public void setFaxDnumber(String faxDnumber) {
        this.faxDnumber = faxDnumber;
    }

    public String getPanel() {
        return panel;
    }

    public void setPanel(String panel) {
        this.panel = panel;
    }

    @Ignore
    public PhoneNumber toPhoneNumber(){
        return PhoneNumber.getPhoneNumber(getSnumber());
    }
    @Ignore
    public String toFormattedPhoneNumber(){
        return PhoneNumber.format(getSnumber());
    }
}
