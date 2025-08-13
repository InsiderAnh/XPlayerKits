package io.github.InsiderAnh.xPlayerKits.utils;

import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class LanguageUtils {

    public String replacePlaceholders(String message, Placeholder... placeholders) {
        StringBuilder messageBuilder = new StringBuilder(message);

        for (Placeholder placeholder : placeholders) {
            String placeholderStr = placeholder.getPlaceholder();
            String value = placeholder.getValue();

            int index;
            while ((index = messageBuilder.indexOf(placeholderStr)) != -1) {
                messageBuilder.replace(index, index + placeholderStr.length(), value);
            }
        }
        return messageBuilder.toString();
    }

    public List<String> replacePlaceholders(List<String> messages, Placeholder... placeholders) {
        if (messages == null || messages.isEmpty() || placeholders.length == 0) {
            return new ArrayList<>(messages != null ? messages : Collections.emptyList());
        }

        Map<String, String> placeholderMap = new HashMap<>();
        for (Placeholder placeholder : placeholders) {
            String key = placeholder.getPlaceholder();
            String value = placeholder.getValue();
            if (key != null && value != null) {
                placeholderMap.put(key, value);
            }
        }

        List<String> result = new ArrayList<>(messages.size());

        for (String message : messages) {
            result.add(replacePlaceholdersWithMap(message, placeholderMap));
        }

        return result;
    }

    private String replacePlaceholdersWithMap(String message, Map<String, String> placeholderMap) {
        if (message == null || message.isEmpty() || placeholderMap.isEmpty()) {
            return message;
        }

        StringBuilder messageBuilder = new StringBuilder(message);

        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            String placeholderStr = entry.getKey();
            String value = entry.getValue();

            int index = 0;
            while ((index = messageBuilder.indexOf(placeholderStr, index)) != -1) {
                messageBuilder.replace(index, index + placeholderStr.length(), value);
                index += value.length();
            }
        }

        return messageBuilder.toString();
    }

}