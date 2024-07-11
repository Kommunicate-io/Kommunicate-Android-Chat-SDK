package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by ashish on 28/02/18.
 */

public class KmHotelBookingModel extends JsonMarker {
    private String HotelName;
    private String HotelCode;
    private float StarRating;
    private String HotelAddress;
    private String HotelPicture;
    private String HotelDescription;
    private String HotelContactNo;
    private String TraceId;
    private String sessionId;
    private int ResultIndex;
    private Short NoOfGuest;
    private int RoomIndex;
    private Short ChildCount;
    private int HotelResultIndex;
    private int NoOfNights;
    private Short NoOfRooms;
    private boolean RequireAllPaxDetails;
    private String RoomTypeCode;
    private String RoomTypeName;
    private String RatePlanCode;
    private String CancellationPolicy;
    private AlHotelPriceModel Price;

    public String getHotelName() {
        return HotelName;
    }

    public void setHotelName(String hotelName) {
        HotelName = hotelName;
    }

    public String getHotelCode() {
        return HotelCode;
    }

    public void setHotelCode(String hotelCode) {
        HotelCode = hotelCode;
    }

    public float getStarRating() {
        return StarRating;
    }

    public void setStarRating(float starRating) {
        StarRating = starRating;
    }

    public String getHotelAddress() {
        return HotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        HotelAddress = hotelAddress;
    }

    public String getHotelPicture() {
        return HotelPicture;
    }

    public void setHotelPicture(String hotelPicture) {
        HotelPicture = hotelPicture;
    }

    public String getHotelDescription() {
        return HotelDescription;
    }

    public void setHotelDescription(String hotelDescription) {
        HotelDescription = hotelDescription;
    }

    public String getHotelContactNo() {
        return HotelContactNo;
    }

    public void setHotelContactNo(String hotelContactNo) {
        HotelContactNo = hotelContactNo;
    }

    public String getTraceId() {
        return TraceId;
    }

    public void setTraceId(String traceId) {
        TraceId = traceId;
    }

    public int getResultIndex() {
        return ResultIndex;
    }

    public void setResultIndex(int resultIndex) {
        ResultIndex = resultIndex;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Short getNoOfGuest() {
        return NoOfGuest;
    }

    public void setNoOfGuest(Short noOfGuest) {
        NoOfGuest = noOfGuest;
    }

    public Short getChildCount() {
        return ChildCount;
    }

    public void setChildCount(Short childCount) {
        ChildCount = childCount;
    }

    public int getHotelResultIndex() {
        return HotelResultIndex;
    }

    public void setHotelResultIndex(int hotelResultIndex) {
        HotelResultIndex = hotelResultIndex;
    }

    public int getNoOfNights() {
        return NoOfNights;
    }

    public void setNoOfNights(int noOfNights) {
        NoOfNights = noOfNights;
    }

    public Short getNoOfRooms() {
        return NoOfRooms;
    }

    public void setNoOfRooms(Short noOfRooms) {
        NoOfRooms = noOfRooms;
    }

    public boolean isRequireAllPaxDetails() {
        return RequireAllPaxDetails;
    }

    public void setRequireAllPaxDetails(boolean requireAllPaxDetails) {
        RequireAllPaxDetails = requireAllPaxDetails;
    }

    public String getRoomTypeCode() {
        return RoomTypeCode;
    }

    public void setRoomTypeCode(String roomTypeCode) {
        RoomTypeCode = roomTypeCode;
    }

    public String getRoomTypeName() {
        return RoomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        RoomTypeName = roomTypeName;
    }

    public String getRatePlanCode() {
        return RatePlanCode;
    }

    public void setRatePlanCode(String ratePlanCode) {
        RatePlanCode = ratePlanCode;
    }

    public String getCancellationPolicy() {
        return CancellationPolicy;
    }

    public void setCancellationPolicy(String cancellationPolicy) {
        CancellationPolicy = cancellationPolicy;
    }

    public int getRoomIndex() {
        return RoomIndex;
    }

    public void setRoomIndex(int roomIndex) {
        RoomIndex = roomIndex;
    }

    public AlHotelPriceModel getPrice() {
        return Price;
    }

    public void setPrice(AlHotelPriceModel price) {
        Price = price;
    }

    public class AlHotelPriceModel extends JsonMarker {
        private String CurrencyCode;
        private float RoomPrice;
        private float Tax;
        private float ExtraGuestCharge;
        private float ChildCharge;
        private float OtherCharges;
        private float Discount;
        private float PublishedPrice;
        private float PublishedPriceRoundedOff;
        private float OfferedPrice;
        private float OfferedPriceRoundedOff;
        private float ServiceTax;
        private float TDS;
        private float ServiceCharge;
        private float TotalGSTAmount;

        public String getCurrencyCode() {
            return CurrencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            CurrencyCode = currencyCode;
        }

        public float getRoomPrice() {
            return RoomPrice;
        }

        public void setRoomPrice(float roomPrice) {
            RoomPrice = roomPrice;
        }

        public float getTax() {
            return Tax;
        }

        public void setTax(float tax) {
            Tax = tax;
        }

        public float getExtraGuestCharge() {
            return ExtraGuestCharge;
        }

        public void setExtraGuestCharge(float extraGuestCharge) {
            ExtraGuestCharge = extraGuestCharge;
        }

        public float getChildCharge() {
            return ChildCharge;
        }

        public void setChildCharge(float childCharge) {
            ChildCharge = childCharge;
        }

        public float getOtherCharges() {
            return OtherCharges;
        }

        public void setOtherCharges(float otherCharges) {
            OtherCharges = otherCharges;
        }

        public float getDiscount() {
            return Discount;
        }

        public void setDiscount(float discount) {
            Discount = discount;
        }

        public float getPublishedPrice() {
            return PublishedPrice;
        }

        public void setPublishedPrice(float publishedPrice) {
            PublishedPrice = publishedPrice;
        }

        public float getPublishedPriceRoundedOff() {
            return PublishedPriceRoundedOff;
        }

        public void setPublishedPriceRoundedOff(float publishedPriceRoundedOff) {
            PublishedPriceRoundedOff = publishedPriceRoundedOff;
        }

        public float getOfferedPrice() {
            return OfferedPrice;
        }

        public void setOfferedPrice(float offeredPrice) {
            OfferedPrice = offeredPrice;
        }

        public float getOfferedPriceRoundedOff() {
            return OfferedPriceRoundedOff;
        }

        public void setOfferedPriceRoundedOff(float offeredPriceRoundedOff) {
            OfferedPriceRoundedOff = offeredPriceRoundedOff;
        }

        public float getServiceTax() {
            return ServiceTax;
        }

        public void setServiceTax(float serviceTax) {
            ServiceTax = serviceTax;
        }

        public float getTDS() {
            return TDS;
        }

        public void setTDS(float TDS) {
            this.TDS = TDS;
        }

        public float getServiceCharge() {
            return ServiceCharge;
        }

        public void setServiceCharge(float serviceCharge) {
            ServiceCharge = serviceCharge;
        }

        public float getTotalGSTAmount() {
            return TotalGSTAmount;
        }

        public void setTotalGSTAmount(float totalGSTAmount) {
            TotalGSTAmount = totalGSTAmount;
        }
    }

    public class RoomDetailModel extends JsonMarker{
        private String HotelCode;
        private List<KmHotelBookingModel> HotelRoomsDetails;

        public String getHotelCode() {
            return HotelCode;
        }

        public void setHotelCode(String hotelCode) {
            HotelCode = hotelCode;
        }

        public List<KmHotelBookingModel> getHotelRoomsDetails() {
            return HotelRoomsDetails;
        }

        public void setHotelRoomsDetails(List<KmHotelBookingModel> hotelRoomsDetails) {
            HotelRoomsDetails = hotelRoomsDetails;
        }
    }
}
