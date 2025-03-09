package io.kommunicate.callbacks;

import annotations.CleanUpRequired;

@Deprecated
@CleanUpRequired(reason = "Migrated to task listener.")
public interface KmRemoveMemberCallback {
    void onSuccess(String response, int index);

    void onFailure(String response, Exception e);
}
