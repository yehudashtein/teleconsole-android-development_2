package com.telebroad.teleconsole.helpers;

// class for static intent and extras values
public class IntentHelper {
    private static final String BASE_INTENT = "com.telebroad.teleconsole.intent.";
    public static final String INCOMING_CALL = BASE_INTENT + "incoming.call";
    public static final String CALL_HUNGUP = BASE_INTENT + ".call.hungup";
    public static final String CALL_CHANGED = BASE_INTENT + ".call.changed";

    private static final String BASE_EXTRA = "com.telebroad.teleconsole.extra.";
    public static final String CALL_ID = BASE_EXTRA + "call.id";

    public static final String MESSAGE_ID = BASE_EXTRA + "message.id";
    public static final String MESSAGE_TIME = BASE_EXTRA + "message.time";

    public static final String NUMBER_TO_CALL = BASE_EXTRA + "number.to.call";
    public static final String TAB_TO_OPEN = BASE_EXTRA + "tab.to.open";
    public static final String NOTIFICATION_ID = BASE_EXTRA + "notification.id";
}
