package com.telebroad.teleconsole.model;

import android.os.Build;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.model.repositories.ContactRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public interface Contact extends Comparable<Contact>{
     String getWholeName();
    String getType();
    String getID();
    List<PhoneNumber> getTelephoneLines();
    List<String> getEmailAddresses();

    default List<PhoneNumber> getFullLines(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getTelephoneLines().stream().filter(PhoneNumber::isFull).collect(Collectors.toList());
        }else{
            List<PhoneNumber> fullLines = new ArrayList<>();
            for (PhoneNumber pn: getTelephoneLines()) {
                if (pn.isFull()) {
                    fullLines.add(pn);
                }
            }
            return fullLines;
        }
    }

    default boolean hasFullLines(){
        return !getFullLines().isEmpty();
    }

    default List<PhoneNumber> getAllLines(){
        return getTelephoneLines();
    }

    @Override
    default int compareTo(@NonNull Contact o){
        String myName = Strings.nullToEmpty(this.getWholeName()).replaceAll("[^a-z0-9A-Z]", "");
        String otherName = Strings.nullToEmpty(o.getWholeName()).replaceAll("[^a-z0-9A-Z]", "");
        int n1 = myName.length();
        int n2 = otherName.length();
        int min = Math.min(n1, n2);
        for (int i = 0; i < min; i++) {
            char c1 = myName.charAt(i);
            char c2 = otherName.charAt(i);
            if (Character.isDigit(c1)) {
                c1 += 100;
            }
            if (Character.isDigit(c2)) {
                c2 += 100;
            }
            if (c1 != c2) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
                if (c1 != c2) {
                    c1 = Character.toLowerCase(c1);
                    c2 = Character.toLowerCase(c2);
                    if (c1 != c2) {
                        // No overflow because of numeric promotion
                        return c1 - c2;
                    }
                }
            }
        }
        return n1 - n2;
        // return getWholeName().toLowerCase().compareTo(o.getWholeName().toLowerCase());
    }


    static void loadContacts(Consumer<TeleConsoleError> completionHandler){
        ContactRepository.getInstance(AppController.getInstance()).loadContactsFromServer();
    }
}
