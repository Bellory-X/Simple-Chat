package org.example;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "command")
public final class Command {

    @JacksonXmlProperty(isAttribute = true)
    private String commandName = "";

    @JacksonXmlProperty(localName = "name")
    private String username = "";

    @JacksonXmlProperty(localName = "password")
    private String password = "";

    @JacksonXmlProperty(localName = "message")
    private String message = "";

    public Command() {}

    public Command(String commandName, String username, String password, String message) {
        this.commandName = commandName;
        this.username = username;
        this.password = password;
        this.message = message;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Command[" +
                "commandName=" + commandName + ", " +
                "username=" + username + ", " +
                "password=" + password + ", " +
                "message=" + message + ']';
    }
}
