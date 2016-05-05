package com.yalin.googleio.explore;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yalin.googleio.R;
import com.yalin.googleio.explore.data.ItemGroup;
import com.yalin.googleio.explore.data.LiveStreamData;
import com.yalin.googleio.explore.data.MessageData;
import com.yalin.googleio.explore.data.SessionData;
import com.yalin.googleio.explore.data.ThemeGroup;
import com.yalin.googleio.explore.data.TopicGroup;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.framework.UpdatableView;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.settings.ConfMessageCardUtils;
import com.yalin.googleio.ui.widget.CollectionView;
import com.yalin.googleio.ui.widget.CollectionViewCallbacks;
import com.yalin.googleio.ui.widget.DrawShadowFrameLayout;
import com.yalin.googleio.util.ImageLoader;
import com.yalin.googleio.util.SettingsUtils;
import com.yalin.googleio.util.ThrottledContentObserver;
import com.yalin.googleio.util.UIUtils;
import com.yalin.googleio.util.WiFiUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.yalin.googleio.settings.ConfMessageCardUtils.ConferencePrefChangeListener;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ExploreIOFragment extends Fragment implements UpdatableView<ExploreModel>, CollectionViewCallbacks {
    private static final String TAG = makeLogTag(ExploreIOFragment.class);

    private static final int GROUP_ID_KEYNOTE_STREAM_CARD = 10;

    private static final int GROUP_ID_LIVE_STREAM_CARD = 15;

    private static final int GROUP_ID_MESSAGE_CARDS = 20;

    private static final int GROUP_ID_TOPIC_CARDS = 30;

    private static final int GROUP_ID_THEME_CARDS = 40;

    private CollectionView mCollectionView;
    private View mEmptyView;

    private ImageLoader mImageLoader;

    private List<UserActionListener> mListeners = new ArrayList<>();

    private ThrottledContentObserver mSessionsObserver, mTagsObserver;

    private ConferencePrefChangeListener mConfMessageAnswerChangeListener = new ConferencePrefChangeListener() {
        @Override
        protected void onPrefChanged(String key, boolean value) {
            fireReloadEvent();
        }
    };

    private OnSharedPreferenceChangeListener mSettingsChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (SettingsUtils.PREF_DECLINED_WIFI_SETUP.equals(key)) {
                fireReloadEvent();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore_io, container, false);
        findView(root);
        getActivity().overridePendingTransition(0, 0);
        return root;
    }

    private void findView(View root) {
        mCollectionView = (CollectionView) root.findViewById(R.id.explore_collection_view);
        mEmptyView = root.findViewById(android.R.id.empty);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageLoader = new ImageLoader(getActivity(), R.drawable.io_logo);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
        DrawShadowFrameLayout drawShadowFrameLayout = (DrawShadowFrameLayout) getActivity().findViewById(R.id.main_content);
        if (drawShadowFrameLayout != null) {
            drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
        }
        setContentTopClearance(actionBarSize);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ConfMessageCardUtils.registerPreferencesChangeListener(getContext(),
                mConfMessageAnswerChangeListener);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.registerOnSharedPreferenceChangeListener(mSettingsChangeListener);

        mSessionsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                fireReloadEvent();
                fireReloadTagsEvent();
            }
        });

        mTagsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                fireReloadTagsEvent();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mConfMessageAnswerChangeListener != null) {
            ConfMessageCardUtils.unregisterPreferencesChangeListener(getContext(),
                    mConfMessageAnswerChangeListener);
        }

        if (mSettingsChangeListener != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            sp.unregisterOnSharedPreferenceChangeListener(mSettingsChangeListener);
        }
        getActivity().getContentResolver().unregisterContentObserver(mSessionsObserver);
        getActivity().getContentResolver().unregisterContentObserver(mTagsObserver);
    }

    private void setContentTopClearance(int clearance) {
        if (mCollectionView != null) {
            mCollectionView.setContentTopClearance(clearance);
        }
    }

    private void fireReloadEvent() {

    }

    private void fireReloadTagsEvent() {

    }

    @Override
    public void displayData(ExploreModel model, QueryEnum query) {
        updateCollectionView(model);
    }

    @Override
    public void displayErrorMessage(QueryEnum query) {

    }

    @Override
    public Uri getDataUri(QueryEnum query) {
        if (query == ExploreModel.ExploreQueryEnum.SESSIONS) {
            return ScheduleContract.Sessions.CONTENT_URI;
        }
        return Uri.EMPTY;
    }

    @Override
    public void addListener(UserActionListener listener) {
        mListeners.add(listener);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public View newCollectionHeaderView(Context context, int groupId, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.explore_io_card_header_with_button, parent, false);
    }

    @Override
    public void bindCollectionHeaderView(Context context, View view, int groupId, String headerLabel, Object headerTag) {

    }

    @Override
    public View newCollectionItemView(Context context, int groupId, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        int containerLayoutId;
        switch (groupId) {
            case GROUP_ID_TOPIC_CARDS:
            case GROUP_ID_THEME_CARDS:
            case GROUP_ID_LIVE_STREAM_CARD:
                containerLayoutId = R.layout.explore_io_topic_theme_livestream_card_container;
                break;
            default:
                containerLayoutId = R.layout.explore_io_card_container;
                break;
        }
        ViewGroup containerView = (ViewGroup) inflater.inflate(containerLayoutId, parent, false);
        UIUtils.setAccessibilityIgnore(containerView);
        ViewGroup containerContents = (ViewGroup) containerView.findViewById(R.id.explore_io_card_container_contents);
        int headerLayoutId = -1;
        switch (groupId) {
            case GROUP_ID_THEME_CARDS:
            case GROUP_ID_TOPIC_CARDS:
            case GROUP_ID_LIVE_STREAM_CARD:
                headerLayoutId = R.layout.explore_io_card_header_with_button;
                break;
        }
        if (headerLayoutId > -1) {
            inflater.inflate(headerLayoutId, containerContents, true);
        }
        int itemLayoutId = -1;
        int numItems = -1;
        switch (groupId) {
            case GROUP_ID_KEYNOTE_STREAM_CARD:
                itemLayoutId = R.layout.explore_io_keynote_stream_item;
                numItems = 1;
                break;
            case GROUP_ID_THEME_CARDS:
                itemLayoutId = R.layout.explore_io_topic_theme_livestream_item;
                numItems = ExploreModel.getThemeSessionLimit(getContext());
                break;
            case GROUP_ID_TOPIC_CARDS:
                itemLayoutId = R.layout.explore_io_topic_theme_livestream_item;
                numItems = ExploreModel.getTopicSessionLimit(getContext());
                break;
            case GROUP_ID_LIVE_STREAM_CARD:
                itemLayoutId = R.layout.explore_io_topic_theme_livestream_item;
                numItems = 3;
                break;
            case GROUP_ID_MESSAGE_CARDS:
                itemLayoutId = R.layout.explore_io_message_card_item;
                numItems = 1;
                break;
        }
        if (itemLayoutId > -1) {
            for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
                inflater.inflate(itemLayoutId, containerContents, true);
            }
        }
        return containerView;
    }

    @Override
    public void bindCollectionItemView(Context context, View view, int groupId, int indexInGroup, int dataIndex, Object tag) {
        if (GROUP_ID_KEYNOTE_STREAM_CARD == groupId || GROUP_ID_MESSAGE_CARDS == groupId) {
            populateSubItemInfo(context, view, groupId, tag);

            View clickableView = view.findViewById(R.id.explore_io_clickable_item);
            if (clickableView != null) {
                clickableView.setTag(tag);
            }
        } else {
            ViewGroup viewWithChildrenSubItems = (ViewGroup) view.findViewById(R.id.explore_io_card_container_contents);
            ItemGroup itemGroup = (ItemGroup) tag;
            viewWithChildrenSubItems.getChildAt(0).setTag(tag);

            TextView titleTextView = (TextView) view.findViewById(android.R.id.title);
            View headerView = view.findViewById(R.id.explore_io_card_header_layout);
            if (headerView != null) {
                headerView.setContentDescription(
                        getString(R.string.more_item_button_desc_with_label_ally,
                                itemGroup.getTitle()));
            }

            View moreButton = view.findViewById(android.R.id.button1);
            if (moreButton != null) {
                moreButton.setTag(tag);
            }
            if (titleTextView != null) {
                titleTextView.setText(itemGroup.getTitle());
            }

            for (int viewChildIndex = 1; viewChildIndex < viewWithChildrenSubItems.getChildCount(); viewChildIndex++) {
                View childView = viewWithChildrenSubItems.getChildAt(viewChildIndex);
                int sessionIndex = viewChildIndex - 1;
                int sessionSize = itemGroup.getSessions().size();
                if (childView != null && sessionIndex < sessionSize) {
                    childView.setVisibility(View.VISIBLE);
                    SessionData sessionData = itemGroup.getSessions().get(sessionIndex);
                    childView.setTag(sessionData);
                    populateSubItemInfo(context, childView, groupId, sessionData);
                } else if (childView != null) {
                    childView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void populateSubItemInfo(Context context, View view, int groupId, Object tag) {
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView descriptionView = (TextView) view.findViewById(R.id.description);
        Button startButton = (Button) view.findViewById(R.id.buttonStart);
        Button endButton = (Button) view.findViewById(R.id.buttonEnd);
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);

        if (tag instanceof SessionData) {
            SessionData sessionData = (SessionData) tag;
            titleView.setText(sessionData.getSessionName());
            if (TextUtils.isEmpty(sessionData.getImageUrl())) {
                ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
                mImageLoader.loadImage(sessionData.getImageUrl(), imageView);
            }
            ImageView inScheduleIndicator =
                    (ImageView) view.findViewById(R.id.indicator_in_schedule);
            if (inScheduleIndicator != null) {
                inScheduleIndicator.setVisibility(
                        sessionData.isInSchedule() ? View.VISIBLE : View.GONE);
            }
            if (!TextUtils.isEmpty(sessionData.getDetails())) {
                descriptionView.setText(sessionData.getDetails());
            }
        }

        if (GROUP_ID_MESSAGE_CARDS == groupId) {
            MessageData messageData = (MessageData) tag;
            descriptionView.setText(messageData.getMessageString(context));
            if (messageData.getEndButtonStringResourceId() != -1) {
                endButton.setText(messageData.getEndButtonStringResourceId());
            } else {
                endButton.setVisibility(View.GONE);
            }
            if (messageData.getStartButtonStringResourceId() != -1) {
                startButton.setText(messageData.getStartButtonStringResourceId());
            } else {
                startButton.setVisibility(View.GONE);
            }
            if (messageData.getIconDrawableId() > 0) {
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageResource(messageData.getIconDrawableId());
            } else {
                iconView.setVisibility(View.GONE);
            }
            if (messageData.getStartButtonClickListener() != null) {
                startButton.setOnClickListener(messageData.getStartButtonClickListener());
            }
            if (messageData.getEndButtonClickListener() != null) {
                endButton.setOnClickListener(messageData.getEndButtonClickListener());
            }
        }
    }

    private void updateCollectionView(ExploreModel model) {
        CollectionView.Inventory inventory = new CollectionView.Inventory();
        CollectionView.InventoryGroup inventoryGroup;

        if (SettingsUtils.isAttendeeAtVenue(getContext())) {
            if (!ConfMessageCardUtils.hasAnsweredConfMessageCardsPrompt(getContext())) {
                inventoryGroup = new CollectionView.InventoryGroup(GROUP_ID_MESSAGE_CARDS);
                MessageData conferenceMessageOptIn = MessageCardHelper.getConferenceOptInMessageData(getContext());
                inventoryGroup.addItemWithTag(conferenceMessageOptIn);
                inventoryGroup.setDisplayCols(1);
                inventory.addGroup(inventoryGroup);
            } else if (ConfMessageCardUtils.isConfMessageCardsEnabled(getContext())) {
                ConfMessageCardUtils.enableActiveCards(getContext());

            }

            if (WiFiUtils.shouldOfferToSetupWifi(getContext(), true)) {
                inventoryGroup = new CollectionView.InventoryGroup(GROUP_ID_MESSAGE_CARDS);
                MessageData conferenceMessageOptIn = MessageCardHelper.getWifiSetupMessageData(getContext());
                inventoryGroup.addItemWithTag(conferenceMessageOptIn);
                inventoryGroup.setDisplayCols(1);
                inventory.addGroup(inventoryGroup);
            }
        }

        SessionData keynoteData = model.getKeynoteData();
        if (keynoteData != null) {
            LOGD(TAG, "Keynote Live stream data found: " + keynoteData);
            inventoryGroup = new CollectionView.InventoryGroup(GROUP_ID_KEYNOTE_STREAM_CARD);
            inventoryGroup.addItemWithTag(keynoteData);
            inventory.addGroup(inventoryGroup);
        }

        LiveStreamData liveStreamData = model.getLiveStreamData();
        if (liveStreamData != null && liveStreamData.getSessions().size() > 0) {
            LOGD(TAG, "Live session data found: " + liveStreamData);
            inventoryGroup = new CollectionView.InventoryGroup(GROUP_ID_LIVE_STREAM_CARD);
            liveStreamData.setTitle(getResources().getString(R.string.live_now));
            inventoryGroup.addItemWithTag(liveStreamData);
            inventory.addGroup(inventoryGroup);
        }

        LOGD(TAG, "Inventory item count:" + inventory.getGroupCount() + " " + inventory.getTotalItemCount());

        ArrayList<CollectionView.InventoryGroup> themeGroups = new ArrayList<>();
        ArrayList<CollectionView.InventoryGroup> topicGroups = new ArrayList<>();

        for (TopicGroup topic : model.getTopics()) {
            LOGD(TAG, topic.getTitle() + ": " + topic.getSessions().size());
            if (topic.getSessions().size() > 0) {
                inventoryGroup = new CollectionView.InventoryGroup(GROUP_ID_TOPIC_CARDS);
                inventoryGroup.addItemWithTag(topic);
                topic.setTitle(getTranslatedTitle(topic.getTitle(), model));
                topicGroups.add(inventoryGroup);
            }
        }

        for (ThemeGroup theme : model.getThemes()) {
            LOGD(TAG, theme.getTitle() + ": " + theme.getSessions().size());
            if (theme.getSessions().size() > 0) {
                inventoryGroup = new CollectionView.InventoryGroup(GROUP_ID_THEME_CARDS);
                inventoryGroup.addItemWithTag(theme);
                theme.setTitle(getTranslatedTitle(theme.getTitle(), model));
                themeGroups.add(inventoryGroup);
            }
        }

        int topicsPerTheme = topicGroups.size();
        if (themeGroups.size() > 0) {
            topicsPerTheme = topicGroups.size() / themeGroups.size();
        }
        Iterator<CollectionView.InventoryGroup> themeIterator = themeGroups.iterator();
        int currentTopicNum = 0;
        for (CollectionView.InventoryGroup topicGroup : topicGroups) {
            inventory.addGroup(topicGroup);
            currentTopicNum++;
            if (currentTopicNum == topicsPerTheme) {
                if (themeIterator.hasNext()) {
                    inventory.addGroup(themeIterator.next());
                }
                currentTopicNum = 0;
            }
        }

        while (themeIterator.hasNext()) {
            inventory.addGroup(themeIterator.next());
        }

        Parcelable state = mCollectionView.onSaveInstanceState();
        mCollectionView.setCollectionAdapter(this);
        mCollectionView.updateInventory(inventory, false);
        if (state != null) {
            mCollectionView.onRestoreInstanceState(state);
        }

        mEmptyView.setVisibility(inventory.getGroupCount() < 1 ? View.VISIBLE : View.GONE);
    }

    private String getTranslatedTitle(String title, ExploreModel model) {
        if (model.getTagTitles().get(title) != null) {
            return model.getTagTitles().get(title);
        } else {
            return title;
        }
    }
}
