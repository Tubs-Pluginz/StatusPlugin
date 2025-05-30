package de.tubyoub.statusplugin.model;

import java.util.List;

public class GroupConfig {
        private final String status;
        private final List<String> permissions;

        public GroupConfig(String status, List<String> permissions) {
            this.status = status;
            this.permissions = permissions;
        }

        public String getStatus() {
            return status;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }