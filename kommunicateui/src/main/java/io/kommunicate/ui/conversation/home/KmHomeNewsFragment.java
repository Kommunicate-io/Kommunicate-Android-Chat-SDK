package io.kommunicate.ui.conversation.home;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;

/**
 * News tab of the home screen. Shows the cards of "whatsNewList" in the settings file; a card with
 * a valid link opens it when tapped.
 */
public class KmHomeNewsFragment extends KmHomeBaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_fragment_home_news, container, false);
        CustomizationSettings settings = host != null ? host.getHomeCustomizationSettings() : null;
        List<KmWhatsNewItem> items = settings != null ? settings.getWhatsNewList() : Collections.emptyList();

        RecyclerView list = view.findViewById(R.id.kmHomeNewsList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(new NewsAdapter(items, host != null ? host.getHomeAccentColor() : 0));
        view.findViewById(R.id.kmHomeNewsEmpty).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        return view;
    }

    private static void bindText(TextView textView, String text) {
        textView.setText(text);
        textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    private static class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
        private final List<KmWhatsNewItem> items;
        private final int linkColor;

        NewsAdapter(List<KmWhatsNewItem> items, int linkColor) {
            this.items = items;
            this.linkColor = linkColor;
        }

        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NewsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.km_home_news_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
            KmWhatsNewItem item = items.get(position);
            bindText(holder.title, item.getTitle());
            bindText(holder.meta, item.getMeta());
            bindText(holder.description, item.getDescription());

            boolean hasLink = KmHomeLinks.isOpenable(item.getLink());
            holder.link.setVisibility(hasLink ? View.VISIBLE : View.GONE);
            if (hasLink) {
                holder.link.setText(TextUtils.isEmpty(item.getLinkText())
                        ? holder.itemView.getContext().getString(R.string.km_home_news_learn_more)
                        : item.getLinkText());
                if (linkColor != 0) {
                    holder.link.setTextColor(linkColor);
                }
            }
            holder.itemView.setOnClickListener(hasLink ? v -> KmHomeLinks.open(v.getContext(), Uri.parse(item.getLink())) : null);
            holder.itemView.setClickable(hasLink);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class NewsViewHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView meta;
            final TextView description;
            final TextView link;

            NewsViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.kmHomeNewsTitle);
                meta = itemView.findViewById(R.id.kmHomeNewsMeta);
                description = itemView.findViewById(R.id.kmHomeNewsDescription);
                link = itemView.findViewById(R.id.kmHomeNewsLink);
            }
        }
    }
}
