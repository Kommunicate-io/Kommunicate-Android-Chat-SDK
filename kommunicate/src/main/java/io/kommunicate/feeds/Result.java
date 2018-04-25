
package io.kommunicate.feeds;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("apzToken")
    @Expose
    private String apzToken;
    @SerializedName("authorization")
    @Expose
    private String authorization;
    @SerializedName("accessToken")
    @Expose
    private String accessToken;
    @SerializedName("customerId")
    @Expose
    private Integer customerId;
    @SerializedName("role")
    @Expose
    private Object role;
    @SerializedName("contactNo")
    @Expose
    private Object contactNo;
    @SerializedName("industry")
    @Expose
    private Object industry;
    @SerializedName("companyName")
    @Expose
    private Object companyName;
    @SerializedName("companySize")
    @Expose
    private Object companySize;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("userKey")
    @Expose
    private String userKey;
    @SerializedName("availability_status")
    @Expose
    private Integer availabilityStatus;
    @SerializedName("allConversations")
    @Expose
    private Integer allConversations;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("deleted_at")
    @Expose
    private Object deletedAt;
    @SerializedName("isAdmin")
    @Expose
    private Boolean isAdmin;
    @SerializedName("adminUserName")
    @Expose
    private String adminUserName;
    @SerializedName("adminDisplayName")
    @Expose
    private String adminDisplayName;
    @SerializedName("applozicUser")
    @Expose
    private RegistrationResponse applozicUser;
    @SerializedName("application")
    @Expose
    private Application application;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApzToken() {
        return apzToken;
    }

    public void setApzToken(String apzToken) {
        this.apzToken = apzToken;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Object getRole() {
        return role;
    }

    public void setRole(Object role) {
        this.role = role;
    }

    public Object getContactNo() {
        return contactNo;
    }

    public void setContactNo(Object contactNo) {
        this.contactNo = contactNo;
    }

    public Object getIndustry() {
        return industry;
    }

    public void setIndustry(Object industry) {
        this.industry = industry;
    }

    public Object getCompanyName() {
        return companyName;
    }

    public void setCompanyName(Object companyName) {
        this.companyName = companyName;
    }

    public Object getCompanySize() {
        return companySize;
    }

    public void setCompanySize(Object companySize) {
        this.companySize = companySize;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public Integer getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(Integer availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Integer getAllConversations() {
        return allConversations;
    }

    public void setAllConversations(Integer allConversations) {
        this.allConversations = allConversations;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Object getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Object deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminDisplayName() {
        return adminDisplayName;
    }

    public void setAdminDisplayName(String adminDisplayName) {
        this.adminDisplayName = adminDisplayName;
    }

    public RegistrationResponse getApplozicUser() {
        return applozicUser;
    }

    public void setApplozicUser(RegistrationResponse applozicUser) {
        this.applozicUser = applozicUser;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

}
