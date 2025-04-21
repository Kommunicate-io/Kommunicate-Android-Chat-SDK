package io.kommunicate.devkit.api.account.user;

import android.content.Context;

import io.kommunicate.devkit.exception.KommunicateException;
import annotations.CleanUpRequired;
import io.kommunicate.commons.people.contact.Contact;
import io.kommunicate.commons.task.CoreAsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

@Deprecated
@CleanUpRequired(reason = "Not used anywhere")
public class UserSearchTask extends CoreAsyncTask<Void, List<Contact>> {

    private WeakReference<Context> context;
    private String searchString;
    private Exception exception;
    private UserService userService;
    private UserSearchHandler listener;
    private static final String EMPTY_SEARCH = "Empty search string";

    public UserSearchTask(Context context, String searchString, UserSearchHandler listener) {
        this.context = new WeakReference<>(context);
        this.searchString = searchString;
        this.listener = listener;
        userService = UserService.getInstance(context);
    }

    @Override
    protected List<Contact> doInBackground() {
        if (searchString == null) {
            exception = new KommunicateException(EMPTY_SEARCH);
            return null;
        }

        try {
            return userService.getUserListBySearch(searchString);
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Contact> contactList) {
        super.onPostExecute(contactList);

        if (listener != null) {
            if (contactList != null) {
                listener.onSuccess(contactList, context.get());
            } else {
                listener.onFailure(exception, context.get());
            }
        }
    }

    public interface UserSearchHandler {
        void onSuccess(List<Contact> contacts, Context context);

        void onFailure(Exception e, Context context);
    }
}
