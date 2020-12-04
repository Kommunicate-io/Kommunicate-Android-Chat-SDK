package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashish on 05/03/18.
 */

public class KmGuestCountModel extends JsonMarker {

    private String NoOfAdults = "1";
    private String NoOfChild = "0";
    private List<Integer> ChildAge = new ArrayList<>();

    public String getNoOfAdults() {
        return NoOfAdults;
    }

    public void setNoOfAdults(String noOfAdults) {
        NoOfAdults = noOfAdults;
    }

    public String getNoOfChild() {
        return NoOfChild;
    }

    public void setNoOfChild(String noOfChild) {
        NoOfChild = noOfChild;
    }

    public List<Integer> getChildAge() {
        return ChildAge;
    }

    public void setChildAge(List<Integer> childAge) {
        ChildAge = childAge;
    }

    @Override
    public String toString() {
        return "ALGuestCountModel{" +
                "NoOfAdults='" + NoOfAdults + '\'' +
                ", NoOfChild='" + NoOfChild + '\'' +
                ", ChildAge=" + ChildAge +
                '}';
    }
}
