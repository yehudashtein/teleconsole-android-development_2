package com.telebroad.teleconsole.viewmodels;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.model.Fax;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.repositories.FaxRepository;

import static com.google.common.base.Strings.nullToEmpty;

public class FaxViewModel extends MessageViewModel<Fax> {

    @Override
    public PhoneNumber findOtherNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getDirection() == Message.Direction.IN ? getItem().getCallerid() : getItem().getCalled());
    }

    public boolean isIncoming(){
        return getItem().getDirection() == Message.Direction.IN;
    }


    @Override
    public PhoneNumber findMyNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getMailbox());
    }

    @Override
    public boolean isNew() {
        return getItem().getDirection() == Message.Direction.IN && nullToEmpty(getItem().getRead_by()).isEmpty();
    }

    public String generateFileName(){
        String name = getOtherNumber().getNameString();
        String dateTime = android.text.format.DateFormat.format("MM_dd_yyyy__HH:mm a", getItem().getTimeStamp() * 1000).toString();

        return name + " " + dateTime;
    }
    @Override
    public int getIconResource() {
        return R.drawable.ic_fax;
    }

    @Override
    public String getInfo(){
//        android.util.Log.d("FaxDLR", getItem().getDlr_error() + " " + getTime().getValue());
        if (getItem().getDirection() == Message.Direction.IN){
            return "Fax Received";
        }
        if (getItem().getDlr_status() == null){
            return "Fax Submitted";
        }
        try {
            int status = Integer.parseInt(getItem().getDlr_status());
            if (status == 0){
                return "Fax Sent Successfully";
            }
            if (status < 0){
                return "Fax Sending";
            } else{
                return "Failed to send Fax";
            }
        }catch (NumberFormatException numberFormatException){
          //  android.util.Log.d("DLR_NFE", "status = " + getItem().getDlr_status()  + " message = " + getItem().getDlr_error());
            return "Fax Submitted";
        }
//        return getItem().getDirection() == Message.Direction.IN ? "Fax Received" : "Fax Sent";
    }

    @Override
    public int getStatusColor() {
        if (getItem().getDirection() == Message.Direction.IN) {
            return super.getStatusColor();
        }
        try{
            int status = Integer.parseInt(getItem().getDlr_status());
            if (status == 0){
                return R.color.fax_successful;
            }
            if (status < 0){
                return super.getStatusColor();
            } else{
                return R.color.fax_failed;
            }
        }catch (NumberFormatException numberFormatException){
            return super.getStatusColor();
        }
    }

    @Override
    public void deleteItem() {
        FaxRepository.getInstance().deleteFax(getItem().getMailbox(), getFileName(), getDir(), getID());
    }

    @Override
    public int getIconBackgroundResource() {
        return R.drawable.bg_fax_icon;
    }

    public String getFileName(){
        return getItem().getName();
    }

    public String getDownloadFilename(){
        if (getFileName() == null){
            return null;
        }
        return getFileName().replace(".pdf", "");

    }

    public String getDir(){
        return getItem().getDirection() == Message.Direction.IN ? "INBOX" : "SENT";
    }

    public void checkIfNeedToLoadMore(){
        FaxRepository.getInstance().checkIfNeedToLoadMore(getItem().getTimestamp());
    }

    public String getFormattedMailbox(){
        return getMyNumber().formatted();
    }


}
