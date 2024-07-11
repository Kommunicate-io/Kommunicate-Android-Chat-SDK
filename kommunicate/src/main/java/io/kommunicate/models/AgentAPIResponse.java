package io.kommunicate.models;

import java.util.ArrayList;

//model for api response of kommunicate api calls
public class AgentAPIResponse<T> extends KmApiResponse {
    private ArrayList<T> response;

    public ArrayList<T> getResponse() {
        return response;
    }

    public void setResponse(ArrayList<T> response) {
        this.response = response;
    }
}