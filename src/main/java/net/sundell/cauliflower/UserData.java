package net.sundell.cauliflower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UserData {

    private Properties properties = new Properties();
    private boolean dirty = false;

    public UserData() {
    }

    public boolean isDirty() {
        return dirty;
    }

    protected UserData(Properties properties) {
        this.properties = properties;
    }

    public void store(File file) throws IOException {
        // XXX Does this blow away the old file contents?
        properties.store(new OutputStreamWriter(new FileOutputStream(file),
                                                StandardCharsets.UTF_8), null);
    }

    static Properties loadProperties(File file) throws IOException {
        if (file == null || !file.exists()) {
            return new Properties();
        }
        Properties p = new Properties();
        p.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        return p;
    }

    /**
     * Store a {@link UserDataComponent} in the user's data store, using
     * the specified key.
     * @param udcKey
     * @param data
     */
    protected void store(String udcKey, UserDataComponent data) {
        for (Map.Entry<String, String> e : data.getValues().entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            properties.setProperty(key(udcKey, e.getKey()), e.getValue());
        }
        dirty = true;
    }

    /**
     * Retrieve the {@link UserDataComponent} data associated with the
     * specified key from the user's data store.
     * @param udcKey
     * @return
     */
    protected Map<String, String> fetch(String udcKey) {
        Map<String, String> m = new HashMap<String, String>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            String key = (String)e.getKey();
            String p = prefix(udcKey);
            if (key.startsWith(p) && key.length() > p.length()) {
                m.put(key.substring(p.length()), 
                      (String)e.getValue());
            }
        }
        return m;
    }

    private String key(String p, String rest) {
        return prefix(p) + rest;
    }

    private String prefix(String p) {
        return p + '.';
    }
}
