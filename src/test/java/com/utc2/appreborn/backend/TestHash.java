package com.utc2.appreborn.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC";
        boolean match = encoder.matches("123456789", hash);
        System.out.println("MATCH: " + match);
    }
}
