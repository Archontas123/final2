package com.tavuc.networking.models;

public class ErrorMessage extends BaseMessage {
    public String errorMessageContent;

    public ErrorMessage() {
    }

    public ErrorMessage(String errorMessageContent) {
        this.type = "ERROR_MESSAGE";
        this.errorMessageContent = errorMessageContent;
    }
}
