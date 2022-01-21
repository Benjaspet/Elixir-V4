package dev.benpetrillo.elixir.utilities;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Cooldown {

    private static Map<String, Long> cooldown = new ConcurrentHashMap<>();

    public static void add(String userId) {
        cooldown.put(userId, new Date().toInstant().toEpochMilli());
    }

    public static void remove(String userId) {
        cooldown.remove(userId);
    }

    public static boolean isInCooldown(String userId, Long now) {
        if (cooldown.containsKey(userId)) {
            if (cooldown.get(userId) - now > 1000 * 3.5) {
                Cooldown.remove(userId);
                return false;
            } else return true;
        }
        return false;
    }
}