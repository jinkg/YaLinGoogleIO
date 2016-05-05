package com.yalin.googleio.explore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.yalin.googleio.R;
import com.yalin.googleio.explore.ExploreModel.ExploreUserActionEnum;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.ui.BaseActivity;
import com.yalin.googleio.ui.widget.CollectionView;
import com.yalin.googleio.ui.widget.DrawShadowFrameLayout;

import static com.yalin.googleio.util.LogUtils.makeLogTag;

public class ExploreIOActivity extends BaseActivity {
    private static final String TAG = makeLogTag(ExploreIOActivity.class);

    public static Intent getOpenIntent(Activity activity) {
        Intent intent = new Intent(activity, ExploreIOActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_io);
        addPresenterFragment(R.id.explore_library_frag,
                new ExploreModel(getApplicationContext()),
                new QueryEnum[]{ExploreModel.ExploreQueryEnum.SESSIONS,
                        ExploreModel.ExploreQueryEnum.TAGS},
                new ExploreUserActionEnum[]{ExploreUserActionEnum.RELOAD});
        registerHideableHeaderView(findViewById(R.id.header_bar));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        enableActionBarAuthHide((CollectionView) findViewById(R.id.explore_collection_view));
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        DrawShadowFrameLayout frame = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        frame.setShadowVisible(shown, shown);
    }

    public void cardHeaderClicked(View view) {
    }
}
