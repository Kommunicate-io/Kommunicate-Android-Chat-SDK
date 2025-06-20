package io.kommunicate.devkit.api.conversation.stat;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by devashish on 26/8/14.
 */
public class MessageStat implements Serializable {

    private static final String messageStat_listStat = "MessageStat{" + "statList=";
    @SerializedName("stat")
    private List<Stat> statList;

    public List<Stat> getStatList() {
        return statList;
    }

    public void setStat(List<Stat> statList) {
        this.statList = statList;
    }

    @Override
    public String toString() {
        return messageStat_listStat + statList + "}";
    }
}
