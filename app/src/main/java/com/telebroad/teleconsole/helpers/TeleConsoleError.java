package com.telebroad.teleconsole.helpers;

import androidx.annotation.StringRes;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;

/**
 * Created by yser on 3/16/2018.
 */
public interface TeleConsoleError{

    int getCode();
    String getErrorMessage();

    default String getFullErrorMessage(){
        return "Error code: " + getCode() + "\n" + getErrorMessage();
    }

    class CustomError implements TeleConsoleError{

        private Integer code;
        private String message;

        public CustomError(){

        }

        @Override
        public String toString() {
            return getFullErrorMessage();
        }

        public CustomError(int code, String message){
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getErrorMessage() {
            return message;
        }
    }


    enum ServerError implements TeleConsoleError{
        LOGIN_ERROR(401, AppController.getInstance().getString(R.string.login_error)),
        BLOCKED_ERROR(439, AppController.getInstance().getString(R.string.account_blocked_message)),
        // 7xx errors are my defined error codes
        MALFORMED_ERROR(701, R.string.malformed_error),
        NO_PHONES_ERROR(702, AppController.getInstance().getString(R.string.no_phones_error)),
        DISABLED_USER_ERROR(703, AppController.getInstance().getString(R.string.disabled_error_msg)),
        INVALID_EMAIL_ERROR (704, R.string.invalid_email_error);


        ServerError(int code, String errorMessage){
            this.setCode(code);
            this.setErrorMessage(errorMessage);
        }

        ServerError(int code, @StringRes int stringRes){
            this(code, AppController.getInstance().getString(stringRes));
        }
        private int code;
        private String errorMessage;

        @Override
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}


