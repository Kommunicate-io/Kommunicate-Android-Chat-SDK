package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.Message;

import io.kommunicate.utils.KmAppSettingPreferences;

public class KmBotTypingDelayManager {

    private int botMessageDelayInterval;
    private MessageDispatcher messageDispatcher;

    public KmBotTypingDelayManager(Context context, MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
        botMessageDelayInterval = KmAppSettingPreferences.getInstance().getKmBotMessageDelayInterval();
    }

    public void addMessage(Message message) {
        new BotDelayAsyncTask(message, messageDispatcher, botMessageDelayInterval).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public interface MessageDispatcher {
        void onMessageQueued(Message message);

        void onMessageDispatched(Message message);
    }

    public static class BotDelayAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final Message message;
        private final MessageDispatcher messageDispatcher;
        private final int botMessageDelay;

        public BotDelayAsyncTask(Message message, MessageDispatcher messageDispatcher, int botMessageDelay) {
            this.message = message;
            this.messageDispatcher = messageDispatcher;
            this.botMessageDelay = botMessageDelay;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (messageDispatcher != null) {
                messageDispatcher.onMessageQueued(message);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                publishProgress(0);
                Thread.sleep(botMessageDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (messageDispatcher != null) {
                messageDispatcher.onMessageDispatched(message);
            }
        }
    }
}
