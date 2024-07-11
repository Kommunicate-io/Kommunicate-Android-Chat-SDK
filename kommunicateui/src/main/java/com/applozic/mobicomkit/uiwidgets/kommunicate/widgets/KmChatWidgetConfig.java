package com.applozic.mobicomkit.uiwidgets.kommunicate.widgets;


public class KmChatWidgetConfig {
    public enum GRAVITY {
        LEFT_CENTER, LEFT_TOP, TOP_CENTER, TOP_RIGHT, RIGHT_CENTER, RIGHT_BOTTOM, BOTTOM_CENTER, LEFT_BOTTOM, CENTER
    }

    int paddingLeft, paddingTop, paddingRight, paddingBottom;
    int displayWidth, displayHeight;
    int launcherSize;
    Boolean movable;
    GRAVITY gravity;

    private KmChatWidgetConfig(Builder builder) {
        this.paddingLeft = builder.paddingLeft;
        this.paddingTop = builder.paddingTop;
        this.paddingRight = builder.paddingRight;
        this.paddingBottom = builder.paddingBottom;

        this.displayWidth = builder.displayWidth;
        this.displayHeight = builder.displayHeight;
        this.launcherSize = builder.launcherSize;
        this.gravity = builder.gravity;
        this.movable = builder.movable;
    }


    public static class Builder {
        int paddingLeft, paddingTop, paddingRight, paddingBottom;
        int displayWidth = Integer.MAX_VALUE, displayHeight = Integer.MAX_VALUE;
        int launcherSize = 0;
        GRAVITY gravity = GRAVITY.RIGHT_CENTER;
        Boolean movable = true;

        /**
         * @param launcherSize unit is DP
         */
        public Builder setLauncherSize(int launcherSize) {
            this.launcherSize = launcherSize;
            return this;
        }

        /**
         * @param paddingLeft unit is DP
         */
        public Builder setPaddingLeft(int paddingLeft) {
            this.paddingLeft = paddingLeft;
            return this;
        }

        /**
         * @param paddingTop unit is DP
         */
        public Builder setPaddingTop(int paddingTop) {
            this.paddingTop = paddingTop;
            return this;
        }

        /**
         * @param paddingRight unit is DP
         */
        public Builder setPaddingRight(int paddingRight) {
            this.paddingRight = paddingRight;
            return this;
        }

        /**
         * @param paddingBottom unit is DP
         */
        public Builder setPaddingBottom(int paddingBottom) {
            this.paddingBottom = paddingBottom;
            return this;
        }

        /**
         * Set the width of area where Chat Widget is to show.
         * default: width of screen
         *
         * @param displayWidth
         */
        public Builder setDisplayWidth(int displayWidth) {
            this.displayWidth = displayWidth;
            return this;
        }

        /**
         * Set the height of area where Chat Widget is to show.
         * default: height of screen - height of status bar
         *
         * @param displayHeight
         */
        public Builder setDisplayHeight(int displayHeight) {
            this.displayHeight = displayHeight;
            return this;
        }

        /**
         * Set the position to display the FloatingView.
         *
         * @param gravity
         */
        public Builder setGravity(GRAVITY gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setMovable(Boolean movable) {
            this.movable = movable;
            return this;
        }

        public KmChatWidgetConfig build() {
            return new KmChatWidgetConfig(this);
        }
    }
}
