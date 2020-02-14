package com.applozic.mobicomkit.uiwidgets.conversation;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;

public class KmConversationStatus {
    private static final String OPEN = getString(R.string.km_status_open);
    private static final String RESOLVED = getString(R.string.km_status_resolved);
    private static final String NOT_RESPONDED = getString(R.string.km_status_not_responded);
    private static final String SPAM = getString(R.string.km_status_spam);
    private static final String DUPLICATE = getString(R.string.km_status_duplicate);
    public static final String MARK_AS_DUPLICATE = getString(R.string.km_status_mark_as_duplicate);
    public static final String MARK_AS_SPAM = getString(R.string.km_status_mark_as_spam);

    public static String getStatus(int status) {
        switch (status) {
            case 2:
                return RESOLVED;
            case 3:
                return SPAM;
            case 4:
                return DUPLICATE;
            case 6:
                return NOT_RESPONDED;
            default:
                return OPEN;
        }
    }

    public static int getIconId(int status) {
        switch (status) {
            case 2:
                return R.drawable.ic_resolved;
            case 3:
                return R.drawable.ic_spam;
            case 4:
                return R.drawable.ic_duplicate;
            default:
                return R.drawable.ic_open_conversation;
        }
    }

    public static int getColorId(int status) {
        switch (status) {
            case 2:
                return R.color.km_resolved_status_color;
            case 3:
                return R.color.km_spam_status_color;
            case 4:
                return R.color.km_duplicate_status_color;
            default:
                return R.color.km_open_status_color;
        }
    }

    public static int getStatusFromName(String name) {
        if (RESOLVED.equals(name)) {
            return 2;
        } else if (SPAM.equals(name) || MARK_AS_SPAM.equals(name)) {
            return 3;
        } else if (DUPLICATE.equals(name) || MARK_AS_DUPLICATE.equals(name)) {
            return 4;
        } else if (NOT_RESPONDED.equals(name)) {
            return 6;
        }
        return 0;
    }

    private static String getString(int resId) {
        return Utils.getString(ApplozicService.getAppContext(), resId);
    }
}
