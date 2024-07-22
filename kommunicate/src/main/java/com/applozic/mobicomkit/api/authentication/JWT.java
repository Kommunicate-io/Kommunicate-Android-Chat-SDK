package com.applozic.mobicomkit.api.authentication;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWT {

    private Map<String, String> header;
    private JWTPayload payload;
    private String signature;
    private final String token;

    public JWT(@NonNull String token) {
        this.token = token;
        decode(token);
    }

    public boolean isExpired(long leeway) {
        if (leeway < 0) {
            throw new IllegalArgumentException("The leeway must be a positive value.");
        }
        long todayTime = (long) (Math.floor(new Date().getTime() / 1000) * 1000); //truncate millis
        Date futureToday = new Date(todayTime + leeway * 1000);
        Date pastToday = new Date(todayTime - leeway * 1000);
        boolean expValid = payload.exp == null || !pastToday.after(payload.exp);
        boolean iatValid = payload.iat == null || !futureToday.before(payload.iat);
        return !expValid || !iatValid;
    }

    private void decode(String token) {
        final String[] parts = splitToken(token);
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        header = parseJson(base64Decode(parts[0]), mapType);
        payload = parseJson(base64Decode(parts[1]), JWTPayload.class);
        signature = parts[2];
    }

    /**
     * Get the Header values from this JWT as a Map of Strings.
     *
     * @return the Header values of the JWT.
     */
    @NonNull
    public Map<String, String> getHeader() {
        return header;
    }

    /**
     * Get the Signature from this JWT as a Base64 encoded String.
     *
     * @return the Signature of the JWT.
     */
    @NonNull
    public String getSignature() {
        return signature;
    }

    /**
     * Get the value of the "iss" claim, or null if it's not available.
     *
     * @return the Issuer value or null.
     */
    @Nullable
    public String getIssuer() {
        return payload.iss;
    }

    /**
     * Get the value of the "sub" claim, or null if it's not available.
     *
     * @return the Subject value or null.
     */
    @Nullable
    public String getSubject() {
        return payload.sub;
    }

    /**
     * Get the value of the "aud" claim, or an empty list if it's not available.
     *
     * @return the Audience value or an empty list.
     */
    @Nullable
    public List<String> getAudience() {
        return payload.aud;
    }

    /**
     * Get the value of the "exp" claim, or null if it's not available.
     *
     * @return the Expiration Time value or null.
     */
    @Nullable
    public Date getExpiresAt() {
        return payload.exp;
    }

    /**
     * Get the value of the "nbf" claim, or null if it's not available.
     *
     * @return the Not Before value or null.
     */
    @Nullable
    public Date getNotBefore() {
        return payload.nbf;
    }

    /**
     * Get the value of the "iat" claim, or null if it's not available.
     *
     * @return the Issued At value or null.
     */
    @Nullable
    public Date getIssuedAt() {
        return payload.iat;
    }

    /**
     * Get the value of the "jti" claim, or null if it's not available.
     *
     * @return the JWT ID value or null.
     */
    @Nullable
    public String getId() {
        return payload.jti;
    }

    /**
     * Get a Claim given it's name. If the Claim wasn't specified in the JWT payload, a BaseClaim will be returned.
     *
     * @param name the name of the Claim to retrieve.
     * @return a valid Claim.
     */
    @NonNull
    public Claim getClaim(@NonNull String name) {
        return payload.claimForName(name);
    }

    /**
     * Get all the Claims.
     *
     * @return a valid Map of Claims.
     */
    @NonNull
    public Map<String, Claim> getClaims() {
        return payload.tree;
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length == 2 && token.endsWith(".")) {
            //Tokens with alg='none' have empty String as Signature.
            parts = new String[]{parts[0], parts[1], ""};
        }
        if (parts.length != 3) {
            throw new DecodeException(String.format("The token was expected to have 3 parts, but got %s.", parts.length));
        }
        return parts;
    }

    @Nullable
    private String base64Decode(String string) {
        String decoded;
        try {
            byte[] bytes = Base64.decode(string, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            decoded = new String(bytes, Charset.defaultCharset());
        } catch (IllegalArgumentException e) {
            throw new DecodeException("Received bytes didn't correspond to a valid Base64 encoded string.", e);
        }
        return decoded;
    }

    private <T> T parseJson(String json, Type typeOfT) {
        T payload;
        try {
            payload = getGson().fromJson(json, typeOfT);
        } catch (Exception e) {
            throw new DecodeException("The token's payload had an invalid JSON format.", e);
        }
        return payload;
    }

    static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(JWTPayload.class, new JsonDeserializer<JWTPayload>() {
                    @Override
                    public JWTPayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        if (json.isJsonNull() || !json.isJsonObject()) {
                            throw new DecodeException("The token's payload had an invalid JSON format.");
                        }

                        JsonObject object = json.getAsJsonObject();

                        //Public Claims
                        String iss = getString(object, "iss");
                        String sub = getString(object, "sub");
                        Date exp = getDate(object, "exp");
                        Date nbf = getDate(object, "nbf");
                        Date iat = getDate(object, "iat");
                        String jti = getString(object, "jti");
                        List<String> aud = getStringOrArray(object, "aud");

                        //Private Claims
                        Map<String, Claim> extra = new HashMap<>();
                        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
                            extra.put(e.getKey(), new ClaimImpl(e.getValue()));
                        }

                        return new JWTPayload(iss, sub, exp, nbf, iat, jti, aud, extra);
                    }

                    @SuppressWarnings("SameParameterValue")
                    private List<String> getStringOrArray(JsonObject obj, String claimName) {
                        List<String> list = Collections.emptyList();
                        if (obj.has(claimName)) {
                            JsonElement arrElement = obj.get(claimName);
                            if (arrElement.isJsonArray()) {
                                JsonArray jsonArr = arrElement.getAsJsonArray();
                                list = new ArrayList<>(jsonArr.size());
                                for (int i = 0; i < jsonArr.size(); i++) {
                                    list.add(jsonArr.get(i).getAsString());
                                }
                            } else {
                                list = Collections.singletonList(arrElement.getAsString());
                            }
                        }
                        return list;
                    }

                    private Date getDate(JsonObject obj, String claimName) {
                        if (!obj.has(claimName)) {
                            return null;
                        }
                        long ms = obj.get(claimName).getAsLong() * 1000;
                        return new Date(ms);
                    }

                    private String getString(JsonObject obj, String claimName) {
                        if (!obj.has(claimName)) {
                            return null;
                        }
                        return obj.get(claimName).getAsString();
                    }
                }).create();
    }

    public static void parseToken(Context context, String token) {
        JWT jwtService = new JWT(token);
        MobiComUserPreference.getInstance(context)
                .setUserAuthToken(token)
                .setTokenCreatedAtTime(jwtService.getClaim("createdAtTime").asLong())
                .setTokenValidUptoMins(jwtService.getClaim("validUpto").asInt());
        HttpRequestUtils.isRefreshTokenInProgress = false;
    }
}
