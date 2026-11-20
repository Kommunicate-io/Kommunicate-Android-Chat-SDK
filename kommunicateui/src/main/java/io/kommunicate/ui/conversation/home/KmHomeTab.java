package io.kommunicate.ui.conversation.home;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.ConversationUIService;

/**
 * Tabs of the home screen bottom navigation. The fragment tag doubles as the tab identity, so the
 * selected tab can always be derived from the fragment currently shown.
 */
public enum KmHomeTab {
    WELCOME("KmHomeWelcomeFragment", R.drawable.km_home_tab_welcome, R.string.km_home_tab_welcome),
    CONVERSATIONS(ConversationUIService.QUICK_CONVERSATION_FRAGMENT, R.drawable.km_home_tab_conversations, R.string.conversations),
    FAQ("KmHomeFaqFragment", R.drawable.km_home_tab_faq, R.string.faq_option),
    NEWS("KmHomeNewsFragment", R.drawable.km_home_tab_news, R.string.km_home_tab_news);

    private final String fragmentTag;
    private final int iconRes;
    private final int labelRes;

    KmHomeTab(String fragmentTag, @DrawableRes int iconRes, @StringRes int labelRes) {
        this.fragmentTag = fragmentTag;
        this.iconRes = iconRes;
        this.labelRes = labelRes;
    }

    public String getFragmentTag() {
        return fragmentTag;
    }

    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    public static KmHomeTab fromFragmentTag(String fragmentTag) {
        for (KmHomeTab tab : values()) {
            if (tab.fragmentTag.equals(fragmentTag)) {
                return tab;
            }
        }
        return null;
    }
}
