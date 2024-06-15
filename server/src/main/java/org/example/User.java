package org.example;

import org.apache.commons.codec.digest.DigestUtils;

public record User(String username, String password) {
    public User(String username, String password) {
        this.username = username;
        this.password = DigestUtils.md5Hex(password);
    }
}
