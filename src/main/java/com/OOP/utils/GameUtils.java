package com.OOP.utils;

import java.util.UUID;

public class GameUtils {
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
