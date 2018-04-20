package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by ashish on 28/02/18.
 */

public class ALRichMessageModel extends JsonMarker {
    private Short contentType;
    private String hotelList;
    private String payload;
    private String sessionId;
    private Short templateId;
    private boolean skipBot;
    private String hotelRoomDetail;
    private String price;
    private String formAction;
    private String formData;
    private String headerText;

    public Short getContentType() {
        return contentType;
    }

    public void setContentType(Short contentType) {
        this.contentType = contentType;
    }

    public Short getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Short templateId) {
        this.templateId = templateId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getHotelList() {
        return hotelList;
    }

    public void setHotelList(String hotelList) {
        this.hotelList = hotelList;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSkipBot() {
        return skipBot;
    }

    public void setSkipBot(boolean skipBot) {
        this.skipBot = skipBot;
    }

    public String getHotelRoomDetail() {
        return hotelRoomDetail;
    }

    public void setHotelRoomDetail(String hotelRoomDetail) {
        this.hotelRoomDetail = hotelRoomDetail;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFormAction() {
        return formAction;
    }

    public void setFormAction(String formAction) {
        this.formAction = formAction;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public class ALPayloadModel extends JsonMarker {
        private String title;
        private String type;
        private String url;
        private String name;
        private String handlerId;
        private String formAction;
        private String message;
        private String headerImageUrl;
        private String subtitle;
        private String description;
        private List<AlActionModel> actions;
        private String rating;
        private String overlayText;
        private AlFormDataModel formData;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHandlerId() {
            return handlerId;
        }

        public void setHandlerId(String handlerId) {
            this.handlerId = handlerId;
        }

        public String getFormAction() {
            return formAction;
        }

        public void setFormAction(String formAction) {
            this.formAction = formAction;
        }

        public AlFormDataModel getFormData() {
            return formData;
        }

        public void setFormData(AlFormDataModel formData) {
            this.formData = formData;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getHeaderImageUrl() {
            return headerImageUrl;
        }

        public void setHeaderImageUrl(String headerImageUrl) {
            this.headerImageUrl = headerImageUrl;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<AlActionModel> getActions() {
            return actions;
        }

        public void setActions(List<AlActionModel> actions) {
            this.actions = actions;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getOverlayText() {
            return overlayText;
        }

        public void setOverlayText(String overlayText) {
            this.overlayText = overlayText;
        }
    }

    public class AlFormDataModel extends JsonMarker {
        private String key;
        private String txnid;
        private String amount;
        private String productinfo;
        private String firstname;
        private String email;
        private String phone;
        private String furl;
        private String surl;
        private String HASH;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTxnid() {
            return txnid;
        }

        public void setTxnid(String txnid) {
            this.txnid = txnid;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getProductinfo() {
            return productinfo;
        }

        public void setProductinfo(String productinfo) {
            this.productinfo = productinfo;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getFurl() {
            return furl;
        }

        public void setFurl(String furl) {
            this.furl = furl;
        }

        public String getSurl() {
            return surl;
        }

        public void setSurl(String surl) {
            this.surl = surl;
        }

        public String getHASH() {
            return HASH;
        }

        public void setHASH(String HASH) {
            this.HASH = HASH;
        }
    }

    public class AlActionModel extends JsonMarker{
        private String action;
        private String name;
        private String data;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Override
    public String toString() {
        return "ALRichMessageModel{" +
                "contentType=" + contentType +
                ", hotelList='" + hotelList + '\'' +
                ", payload='" + payload + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", templateId=" + templateId +
                ", skipBot=" + skipBot +
                ", hotelRoomDetail='" + hotelRoomDetail + '\'' +
                ", price='" + price + '\'' +
                ", formAction='" + formAction + '\'' +
                ", formData='" + formData + '\'' +
                '}';
    }
}
