package com.utc2.appreborn.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        System.out.println("HASH_RESULT=" + new BCryptPasswordEncoder().encode("123456789"));
    }
}
