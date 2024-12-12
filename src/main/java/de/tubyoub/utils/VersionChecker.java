package de.tubyoub.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for checking if a new version of the plugin is available.
 */
public class VersionChecker {
    public VersionChecker(){}

    /**
     * Checks if a new version of the plugin is available.
     * @param version The current version of the plugin.
     * @return A boolean indicating whether a new version is available.
     */
    public static VersionInfo isNewVersionAvailable(String version, String project) {
        try {
            URL url = new URL("https://api.modrinth.com/v2/project/" + project + "/version");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "BTPluginz/GravePlugin/"+ version + " (github@tubyoub.de)");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String jsonResponse = reader.lines().collect(Collectors.joining("\n"));

                    List<Map<String, Object>> versions = parseJsonArray(jsonResponse);
                    if (!versions.isEmpty()) {
                        Map<String, Object> latestVersion = versions.get(0);
                        String latestVersionNumber = (String) latestVersion.get("version_number");
                        String changelog = (String) latestVersion.get("changelog");
                        String versionType = (String) latestVersion.get("version_type");

                        if (!latestVersionNumber.equals(version)) {
                            UpdateUrgency urgency = determineUrgency(changelog, versionType);
                            return new VersionInfo(true, latestVersionNumber, changelog, urgency);
                        } else {
                            return new VersionInfo(false, version, null, UpdateUrgency.NONE);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new VersionInfo(false, version, null, UpdateUrgency.NONE);
    }

    private static UpdateUrgency determineUrgency(String changelog, String versionType) {
        if (versionType.equals("release")) {
            if (changelog.toLowerCase().contains("security") || changelog.toLowerCase().contains("critical")) {
                return UpdateUrgency.CRITICAL;
            } else if (changelog.toLowerCase().contains("important") || changelog.toLowerCase().contains("major")) {
                return UpdateUrgency.HIGH;
            } else {
                return UpdateUrgency.NORMAL;
            }
        } else if (versionType.equals("beta")) {
            return UpdateUrgency.LOW;
        } else {
            return UpdateUrgency.NONE;
        }
    }

    public static class VersionInfo {
        public final boolean isNewVersionAvailable;
        public final String latestVersion;
        public final String changelog;
        public final UpdateUrgency urgency;

        public VersionInfo(boolean isNewVersionAvailable, String latestVersion, String changelog, UpdateUrgency urgency) {
            this.isNewVersionAvailable = isNewVersionAvailable;
            this.latestVersion = latestVersion;
            this.changelog = changelog;
            this.urgency = urgency;
        }
    }

    public enum UpdateUrgency {
        CRITICAL,
        HIGH,
        NORMAL,
        LOW,
        NONE
    }

    /**
     * Parses a JSON array into a list of maps.
     * @param jsonArray The JSON array to be parsed.
     * @return A list of maps representing the JSON array.
     */
    private static List<Map<String, Object>> parseJsonArray(String jsonArray) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>(){});
        } catch (IOException e) {
            e.printStackTrace();
            return Arrays.asList(); // Handle parsing exception
        }
    }
}