package com.applozic.mobicommons.personalization;

import com.applozic.mobicommons.people.contact.Contact;

public class PersonalizedMessage {

    private static String[] expressions = new String[]{
            "[fullname]", "[name]", "[fname]", "[mname]", "[lname]"
    };

    public static boolean isPersonalized(String message) {
        for (String expression : expressions) {
            if (message.contains(expression)) {
                return true;
            }
        }
        return false;
    }

    public static String prepareMessageFromTemplate(String body, Contact contact) {
        body = body.replace(expressions[0], contact.getFullName());
        body = body.replace(expressions[1], contact.getFullName());
        body = body.replace(expressions[2], contact.getFirstName());
        body = body.replace(expressions[3], contact.getMiddleName());
        body = body.replace(expressions[4], contact.getLastName());
        return body;
    }

    public static boolean isFromTemplate(String template, String message) {
        for (String expression : expressions) {
            template.replace(expression, "[$$]");
        }
        String[] templateParts = template.split("[$$]");
        for (String templatePart : templateParts) {
            if (!message.contains(templatePart)) {
                return false;
            }
        }
        return true;
    }
}
