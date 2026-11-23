package com.mdmuntakim.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Guards the promise of the plugin: a normal player must see and be able to run
 * every command without being an operator.
 */
class PluginDescriptorTest {

    @SuppressWarnings("unchecked")
    private static Map<String, Object> descriptor() throws Exception {
        try (InputStream stream = PluginDescriptorTest.class.getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(stream, "plugin.yml must be packaged with the plugin");
            Map<String, Object> root = new Yaml().load(stream);
            assertNotNull(root, "plugin.yml must not be empty");
            return root;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void commandsDoNotRequireAPermission() throws Exception {
        Object commands = descriptor().get("commands");
        assertTrue(commands instanceof Map, "plugin.yml must declare commands");
        Map<String, Object> declared = (Map<String, Object>) commands;
        assertFalse(declared.isEmpty(), "plugin.yml must declare commands");
        for (Map.Entry<String, Object> command : declared.entrySet()) {
            assertFalse(((Map<String, Object>) command.getValue()).containsKey("permission"),
                    "/" + command.getKey() + " must not be hidden behind a command permission");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyPermissionDefaultsToEveryone() throws Exception {
        Object permissions = descriptor().get("permissions");
        assertTrue(permissions instanceof Map, "plugin.yml must declare the permission nodes");
        Map<String, Object> declared = (Map<String, Object>) permissions;
        assertFalse(declared.isEmpty(), "plugin.yml must declare the permission nodes");
        for (Map.Entry<String, Object> permission : declared.entrySet()) {
            Object node = permission.getValue();
            assertTrue(node instanceof Map, permission.getKey() + " must be a permission node");
            assertEquals(Boolean.TRUE, ((Map<String, Object>) node).get("default"),
                    permission.getKey() + " must default to true so normal players can use it");
        }
    }
}
