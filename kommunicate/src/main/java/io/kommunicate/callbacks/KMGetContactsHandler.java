package io.kommunicate.callbacks;

import java.util.List;

import io.kommunicate.users.KmContact;
import io.kommunicate.models.feed.ErrorResponseFeed;

/**
 * Created by ashish on 30/01/18.
 */

public interface KMGetContactsHandler {

    void onSuccess(List<KmContact> contactList);

    void onFailure(List<ErrorResponseFeed> errorResponseFeeds, Exception exception);
}
