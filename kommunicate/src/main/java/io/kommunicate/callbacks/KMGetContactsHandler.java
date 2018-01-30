package io.kommunicate.callbacks;

import com.applozic.mobicomkit.feed.ErrorResponseFeed;

import java.util.List;

import io.kommunicate.KmContact;

/**
 * Created by ashish on 30/01/18.
 */

public interface KMGetContactsHandler {

    void onSuccess(List<KmContact> contactList);

    void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception);
}
