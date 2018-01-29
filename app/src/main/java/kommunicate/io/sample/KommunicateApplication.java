package kommunicate.io.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStartConvCallback;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMCreateChatCallback;

/**
 * Created by ashish on 23/01/18.
 */

public class KommunicateApplication extends MultiDexApplication implements KmStartConvCallback {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onStartNew(Context context) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Creating conversation, please wait...");
        dialog.setCancelable(false);
        dialog.show();

        Kommunicate.startNewConversation(this, "reytum_agent", "bot", new KMCreateChatCallback() {
            @Override
            public void onSuccess(Channel channel, Context context) {
                dialog.dismiss();
                Kommunicate.openParticularConversation(context, channel.getKey());
            }

            @Override
            public void onFailure(ChannelFeedApiResponse channelFeedApiResponse, Context context) {
                dialog.dismiss();
                Toast.makeText(context, "Unable to create : " + channelFeedApiResponse, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
