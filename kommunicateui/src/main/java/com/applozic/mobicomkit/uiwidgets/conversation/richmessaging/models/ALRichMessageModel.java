package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models;

import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;
import java.util.Map;

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
    private String requestType;
    private String messagePreview;

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

    public String getMessagePreview() {
        return messagePreview;
    }

    public void setMessagePreview(String messagePreview) {
        this.messagePreview = messagePreview;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public static class AlElementModel<T> extends JsonMarker {
        private String title;
        private String description;
        private T articleId;
        private String source;
        private String imgSrc;
        private AlAction action;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public T getArticleId() {
            return articleId;
        }

        public void setArticleId(T articleId) {
            this.articleId = articleId;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getImgSrc() {
            return imgSrc;
        }

        public void setImgSrc(String imgSrc) {
            this.imgSrc = imgSrc;
        }

        public AlAction getAction() {
            return action;
        }

        public void setAction(AlAction action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return "AlElementModel{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", articleId=" + articleId +
                    ", source='" + source + '\'' +
                    ", imgSrc='" + imgSrc + '\'' +
                    ", action=" + action +
                    '}';
        }
    }

    public static class ALPayloadModel extends JsonMarker {
        private String title;
        private String type;
        private String url;
        private String text;
        private String name;
        private String handlerId;
        private String formAction;
        private String message;
        private String headerText;
        private String headerImgSrc;
        private String headerImageUrl;
        private String subtitle;
        private String description;
        private String caption;
        private String titleExt;
        private String replyText;
        private AlAction action;
        private String updateLanguage;
        private AlHeaderModel header;
        private List<AlElementModel> elements;
        private List<AlButtonModel> actions;
        private String rating;
        private String overlayText;
        private String buttonLabel;
        private String requestType;
        private Map<String, Object> replyMetadata;
        private List<AlButtonModel> buttons;
        private AlFormDataModel formData;
        private boolean isDeepLink;

        public String getUpdateLanguage() {
            return updateLanguage;
        }

        public void setUpdateLanguage(String updateLanguage) {
            this.updateLanguage = updateLanguage;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

        public String getHandlerId() {
            return handlerId;
        }

        public String getFormAction() {
            return formAction;
        }

        public AlFormDataModel getFormData() {
            return formData;
        }

        public String getMessage() {
            return message;
        }

        public String getHeaderImageUrl() {
            return headerImageUrl;
        }

        public Map<String, Object> getReplyMetadata() {
            return replyMetadata;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getDescription() {
            return description;
        }

        public List<AlButtonModel> getActions() {
            return actions;
        }

        public String getRating() {
            return rating;
        }

        public String getOverlayText() {
            return overlayText;
        }

        public String getButtonLabel() {
            return buttonLabel;
        }

        public List<AlButtonModel> getButtons() {
            return buttons;
        }

        public String getHeaderText() {
            return headerText;
        }

        public String getHeaderImgSrc() {
            return headerImgSrc;
        }

        public List<AlElementModel> getElements() {
            return elements;
        }

        public String getCaption() {
            return caption;
        }

        public String getTitleExt() {
            return titleExt;
        }

        public AlHeaderModel getHeader() {
            return header;
        }

        public AlAction getAction() {
            return action;
        }

        public String getReplyText() {
            return replyText;
        }

        public void setReplyText(String replyText) {
            this.replyText = replyText;
        }

        public void setAction(AlAction action) {
            this.action = action;
        }

        public String getRequestType() {
            return requestType;
        }

        public boolean isDeepLink() {
            return isDeepLink;
        }

        public void setDeepLink(boolean deepLink) {
            isDeepLink = deepLink;
        }

        @Override
        public String toString() {
            return "ALPayloadModel{" +
                    "title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", text='" + text + '\'' +
                    ", name='" + name + '\'' +
                    ", handlerId='" + handlerId + '\'' +
                    ", formAction='" + formAction + '\'' +
                    ", message='" + message + '\'' +
                    ", headerText='" + headerText + '\'' +
                    ", headerImgSrc='" + headerImgSrc + '\'' +
                    ", headerImageUrl='" + headerImageUrl + '\'' +
                    ", subtitle='" + subtitle + '\'' +
                    ", description='" + description + '\'' +
                    ", caption='" + caption + '\'' +
                    ", titleExt='" + titleExt + '\'' +
                    ", header=" + header +
                    ", elements=" + elements +
                    ", actions=" + actions +
                    ", rating='" + rating + '\'' +
                    ", overlayText='" + overlayText + '\'' +
                    ", buttonLabel='" + buttonLabel + '\'' +
                    ", requestType='" + requestType + '\'' +
                    ", replyMetadata=" + replyMetadata +
                    ", buttons=" + buttons +
                    ", formData=" + formData +
                    '}';
        }
    }

    public static class AlFormDataModel extends JsonMarker {
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
        private String discription;

        public String getKey() {
            return key;
        }

        public String getTxnid() {
            return txnid;
        }

        public String getAmount() {
            return amount;
        }

        public String getProductinfo() {
            return productinfo;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getFurl() {
            return furl;
        }

        public String getSurl() {
            return surl;
        }

        public String getHASH() {
            return HASH;
        }

        public String getDiscription() {
            return discription;
        }

        @Override
        public String toString() {
            return "AlFormDataModel{" +
                    "key='" + key + '\'' +
                    ", txnid='" + txnid + '\'' +
                    ", amount='" + amount + '\'' +
                    ", productinfo='" + productinfo + '\'' +
                    ", firstname='" + firstname + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", furl='" + furl + '\'' +
                    ", surl='" + surl + '\'' +
                    ", HASH='" + HASH + '\'' +
                    ", discription='" + discription + '\'' +
                    '}';
        }
    }

    public static class AlButtonModel extends JsonMarker {
        private AlAction action;
        private String name;
        private String type;

        public AlAction getAction() {
            return action;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "AlButtonModel{" +
                    "action=" + action +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public static class AlAction extends JsonMarker {
        private String url;
        private String type;
        private String text;
        private String title;
        private String name;
        private String handlerId;
        private String formAction;
        private String message;
        private String headerText;
        private String headerImgSrc;
        private String headerImageUrl;
        private String subtitle;
        private String description;
        private String caption;
        private String titleExt;
        private AlHeaderModel header;
        private List<AlElementModel> elements;
        private List<AlButtonModel> actions;
        private String rating;
        private String replyText;
        private String overlayText;
        private String buttonLabel;
        private String requestType;
        private String updateLanguage;
        private Map<String, Object> replyMetadata;
        private List<AlButtonModel> buttons;
        private AlFormDataModel formData;
        private ALPayloadModel payload;
        private boolean isDeepLink;

        public String getUpdateLanguage() {
            return updateLanguage;
        }

        public void setUpdateLanguage(String updateLanguage) {
            this.updateLanguage = updateLanguage;
        }

        public String getUrl() {
            return url;
        }

        public String getText() {
            return text;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public ALPayloadModel getPayload() {
            return payload;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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

        public String getReplyText() {
            return replyText;
        }

        public void setReplyText(String replyText) {
            this.replyText = replyText;
        }

        public String getFormAction() {
            return formAction;
        }

        public void setFormAction(String formAction) {
            this.formAction = formAction;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getHeaderText() {
            return headerText;
        }

        public void setHeaderText(String headerText) {
            this.headerText = headerText;
        }

        public String getHeaderImgSrc() {
            return headerImgSrc;
        }

        public void setHeaderImgSrc(String headerImgSrc) {
            this.headerImgSrc = headerImgSrc;
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

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getTitleExt() {
            return titleExt;
        }

        public void setTitleExt(String titleExt) {
            this.titleExt = titleExt;
        }

        public AlHeaderModel getHeader() {
            return header;
        }

        public void setHeader(AlHeaderModel header) {
            this.header = header;
        }

        public List<AlElementModel> getElements() {
            return elements;
        }

        public void setElements(List<AlElementModel> elements) {
            this.elements = elements;
        }

        public List<AlButtonModel> getActions() {
            return actions;
        }

        public void setActions(List<AlButtonModel> actions) {
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

        public String getButtonLabel() {
            return buttonLabel;
        }

        public void setButtonLabel(String buttonLabel) {
            this.buttonLabel = buttonLabel;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public Map<String, Object> getReplyMetadata() {
            return replyMetadata;
        }

        public void setReplyMetadata(Map<String, Object> replyMetadata) {
            this.replyMetadata = replyMetadata;
        }

        public List<AlButtonModel> getButtons() {
            return buttons;
        }

        public void setButtons(List<AlButtonModel> buttons) {
            this.buttons = buttons;
        }

        public AlFormDataModel getFormData() {
            return formData;
        }

        public void setFormData(AlFormDataModel formData) {
            this.formData = formData;
        }

        public void setPayload(ALPayloadModel payload) {
            this.payload = payload;
        }

        public boolean isQuickReply() {
            return AlRichMessage.QUICK_REPLY.equals(type);
        }

        public boolean isSubmitButton() {
            return AlRichMessage.SUBMIT_BUTTON.equals(type);
        }

        public boolean isWebLink() {
            return AlRichMessage.WEB_LINK.equals(type);
        }

        public boolean isDeepLink() {
            return isDeepLink;
        }

        public void setDeepLink(boolean deepLink) {
            isDeepLink = deepLink;
        }

        @Override
        public String toString() {
            return "AlAction{" +
                    "url='" + url + '\'' +
                    ", type='" + type + '\'' +
                    ", payload=" + payload +
                    '}';
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
                ", headerText='" + headerText + '\'' +
                ", messagePreview='" + messagePreview + '\'' +
                '}';
    }

    public static class AlHeaderModel extends JsonMarker {
        private String overlayText;
        private String imgSrc;

        public String getOverlayText() {
            return overlayText;
        }

        public String getImgSrc() {
            return imgSrc;
        }

        @Override
        public String toString() {
            return "AlHeaderModel{" +
                    "overlayText='" + overlayText + '\'' +
                    ", imgSrc='" + imgSrc + '\'' +
                    '}';
        }
    }
}
