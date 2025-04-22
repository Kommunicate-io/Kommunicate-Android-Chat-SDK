package io.kommunicate.ui.conversation.richmessaging.models;

import io.kommunicate.commons.json.JsonMarker;

/**
 * Created by ashish on 06/03/18.
 */

public class KmBookingDetailsModel extends JsonMarker {
    private String sessionId;
    private BookingDetailsModel personInfo;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public BookingDetailsModel getPersonInfo() {
        if (personInfo == null) {
            personInfo = new BookingDetailsModel();
        }
        return personInfo;
    }

    public void setPersonInfo(BookingDetailsModel personInfo) {
        this.personInfo = personInfo;
    }

    public class BookingDetailsModel extends JsonMarker {
        private String Title;
        private String Age;
        private String FirstName;
        private String MiddleName;
        private String LastName;
        private String EmailId;
        private String PhoneNo;

        public String getTitle() {
            return Title;
        }

        public void setTitle(String title) {
            Title = title.trim();
        }

        public String getAge() {
            return Age;
        }

        public void setAge(String age) {
            Age = age.trim();
        }

        public String getFirstName() {
            return FirstName;
        }

        public void setFirstName(String firstName) {
            FirstName = firstName.trim();
        }

        public String getMiddleName() {
            return MiddleName;
        }

        public void setMiddleName(String middleName) {
            MiddleName = middleName.trim();
        }

        public String getLastName() {
            return LastName;
        }

        public void setLastName(String lastName) {
            LastName = lastName.trim();
        }

        public String getEmailId() {
            return EmailId;
        }

        public void setEmailId(String emailId) {
            EmailId = emailId.trim();
        }

        public String getPhoneNo() {
            return PhoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            PhoneNo = phoneNo.trim();
        }
    }
}
