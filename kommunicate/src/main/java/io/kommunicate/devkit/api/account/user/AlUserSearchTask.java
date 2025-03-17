package io.kommunicate.devkit.api.account.user;

import android.content.Context;

import io.kommunicate.devkit.exception.ApplozicException;
import io.kommunicate.commons.people.contact.Contact;
import io.kommunicate.commons.task.AlAsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

public class AlUserSearchTask extends AlAsyncTask<Void, List<Contact>> {

    private WeakReference<Context> context;
    private String searchString;
    private Exception exception;
    private UserService userService;
    private AlUserSearchHandler listener;
    private static final String EMPTY_SEARCH = "Empty search string";

    public AlUserSearchTask(Context context, String searchString, AlUserSearchHandler listener) {
        this.context = new WeakReference<>(context);
        this.searchString = searchString;
        this.listener = listener;
        userService = UserService.getInstance(context);
    }

    @Override
    protected List<Contact> doInBackground() {
        if (searchString == null) {
            exception = new ApplozicException(EMPTY_SEARCH);
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

    public interface AlUserSearchHandler {
        void onSuccess(List<Contact> contacts, Context context);

        void onFailure(Exception e, Context context);
    }
}
