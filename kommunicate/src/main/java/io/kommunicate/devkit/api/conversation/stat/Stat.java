package io.kommunicate.devkit.api.conversation.stat;

import io.kommunicate.devkit.api.conversation.Message;

import java.io.Serializable;

/**
 * Created by devashish on 26/8/14.
 */
public class Stat implements Serializable {
    private Short sourceId = Message.Source.DEVICE_NATIVE_APP.getValue();
    private Short smsTypeId;
    private int normalSmsCount;
    private int selfDestructSmsCount;
    private static final String smsTypeId_stat = "stat{" + "smsTypeId=";

    public Short getSourceId() {
        return sourceId;
    }

    public void setSourceId(Short sourceId) {
        this.sourceId = sourceId;
    }

    public Short getSmsTypeId() {
        return smsTypeId;
    }

    public void setSmsTypeId(Short smsTypeId) {
        this.smsTypeId = smsTypeId;
    }

    public int getNormalSmsCount() {
        return normalSmsCount;
    }

    public void setNormalSmsCount(int normalSmsCount) {
        this.normalSmsCount = normalSmsCount;
    }

    public int getSelfDestructSmsCount() {
        return selfDestructSmsCount;
    }

    public void setSelfDestructSmsCount(int selfDestructSmsCount) {
        this.selfDestructSmsCount = selfDestructSmsCount;
    }

    @Override
    public String toString() {
        return smsTypeId_stat + smsTypeId + ", normalSmsCount=" + normalSmsCount + ", selfDestructSmsCount=" + selfDestructSmsCount + "}";
    }

}
