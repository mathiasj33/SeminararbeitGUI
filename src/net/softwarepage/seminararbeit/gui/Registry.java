package net.softwarepage.seminararbeit.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class Registry {
    private final static Preferences PREFS = Preferences.userRoot().node("/net/softwarepage/gametheoryapp");
    
    public static void saveToRegistry(String name) {
        moveKeysFurther(name);
        PREFS.put("game0", name);
    }

    public static String getFromRegistry(String key) {
        return PREFS.get(key, null);
    }
    
    private static void remove(String key) {
        PREFS.remove(key);
    }
    
    private static void moveKeysFurther(String newName) {
        final String name = "game";
        Map<String, String> regMap = new HashMap<>();
        for(int i = 0; i < 10; i++) {
            String finalName = name + (i + 1);
            String lastKey = getFromRegistry("game" + i);
            if(lastKey == null) {
                continue;
            }
            remove(lastKey);
            if(lastKey.equals(newName)) {
                break;
            }
            regMap.put(finalName, lastKey);
        }
        regMap.keySet().forEach((key) -> PREFS.put(key, regMap.get(key)));
    }
}
