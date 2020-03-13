package com.example.android.flyanywhere.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.adapters.TagAdapter;
import com.example.android.flyanywhere.models.Tag;
import com.example.android.flyanywhere.utils.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TagActivity extends AppCompatActivity implements TagAdapter.OnListItemClickListener {

    @BindView(R.id.rv_tag)
    RecyclerView tagRecyclerView;
    @BindView(R.id.sv_tag)
    MaterialSearchView searchView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.pb_tag_progress_Bar)
    ProgressBar mProgressBar;
    @BindView(R.id.cl_tag_root_layout)
    CoordinatorLayout mCoordinatorLayout;

    TagAdapter mTagAdapter;
    ArrayList<Tag> tagList = new ArrayList<>();
    private Bundle mSavedInstanceState;
    private FirebaseUser currentUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private DatabaseReference mTagReference;
    private ValueEventListener mTagEventListener;

    private static final String STATE_KEY_RECYCLER_STATE = "recyclerState";
    private static final String STATE_KEY_SELECTED_TAGS = "selectedTags";
    private static final String TAG_KEY_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        ButterKnife.bind(this);
        getCurrentUser();
        setUpActionBar();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (savedInstanceState != null) {
            mSavedInstanceState = savedInstanceState;
        }
        mTagAdapter = new TagAdapter(this);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false);
        tagRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        tagRecyclerView.setHasFixedSize(false);
        tagRecyclerView.setLayoutManager(linearLayoutManager);
        tagRecyclerView.setAdapter(mTagAdapter);
        getTagsFromDatabase();

        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                selectTag(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setEnabled(false);
                getCurrentUser();
                subscribeToTags();
            }
        });

    }

    private String[] getSuggestionList() {
        String[] suggestionList = new String[tagList.size()];
        for (int i = 0; i < tagList.size(); i++) {
            suggestionList[i] = tagList.get(i).getTagName();
        }
        return suggestionList;
    }

    public void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_bar_title_tag_activity);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void getUserTags() {
        mTagReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/subscribedTags/");

        mTagEventListener = mTagReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot subscribedTags : dataSnapshot.getChildren()) {
                        if (mSavedInstanceState == null) {
                            ArrayList<Integer> selectedTags = new ArrayList<>();
                            if (subscribedTags.getValue() != null) {
                                selectedTags.add(
                                        Integer.parseInt(subscribedTags.getValue().toString()));
                            }
                            selectTags(selectedTags);
                        }
                    }
                }
                if (mSavedInstanceState != null) {
                    selectTags(mSavedInstanceState.getIntegerArrayList(STATE_KEY_SELECTED_TAGS));
                }
                mTagAdapter.setTagList(tagList);
                if (mSavedInstanceState != null) {
                    tagRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedInstanceState
                            .getParcelable(STATE_KEY_RECYCLER_STATE));
                }
                searchView.setSuggestions(getSuggestionList());
                hideProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_toast_not_logged_in, Toast.LENGTH_SHORT).show();
            Intent goToMainActivity = new Intent(TagActivity.this, MainActivity.class);
            startActivity(goToMainActivity);
            TagActivity.this.finish();
        }
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        tagRecyclerView.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        tagRecyclerView.setVisibility(View.VISIBLE);
    }

    private void subscribeToTags() {
        if (NetworkUtils.isConnectedToTheInternet(this)) {
            final DatabaseReference mTagReference = mFirebaseDatabase.getReference()
                    .child("users/" + currentUser.getUid() + "/subscribedTags");
            mTagReference.setValue(getSelectedTags(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null){
                        logSelectedTags(getSelectedTags());
                        Toast.makeText(
                                TagActivity.this,
                                R.string.toast_tags_updated,
                                Toast.LENGTH_SHORT).show();
                        TagActivity.this.finish();
                    } else {
                        fab.setEnabled(true);
                    }
                }
            });
        } else {
            Snackbar.make(
                    mCoordinatorLayout,
                    R.string.error_snackbar_no_internet,
                    Snackbar.LENGTH_LONG).show();
            fab.setEnabled(true);
        }
    }

    public void getTagsFromDatabase() {
        tagList.clear();
        showProgressBar();
        final DatabaseReference mTagReference = mFirebaseDatabase.getReference().child("tags");
        mTagReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tag : dataSnapshot.getChildren()) {
                    Integer tagId = Integer.parseInt(tag.getKey());
                    String tagName = tag.child(TAG_KEY_NAME).getValue().toString();
                    tagList.add(new Tag(tagId, tagName, false));
                }
                getUserTags();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selectTag(String tag) {
        Boolean tagFound = false;
        for (int i = 0; i < tagList.size(); i++) {
            Tag currentTag = tagList.get(i);
            if (currentTag.getTagName().equals(tag)) {
                tagFound = true;
                if (currentTag.getTagIsSelected()) {
                    tagList.get(i).setTagIsSelected(false);
                    Snackbar.make(
                            mCoordinatorLayout,
                            tag + getString(R.string.snackbar_tag_removed),
                            Snackbar.LENGTH_LONG).show();
                } else {
                    tagList.get(i).setTagIsSelected(true);
                    Snackbar.make(
                            mCoordinatorLayout,
                            tag + getString(R.string.snackbar_tag_added),
                            Snackbar.LENGTH_LONG).show();
                }
                mTagAdapter.notifyDataSetChanged();
            }
        }
        if (!tagFound) {
            Snackbar.make(
                    mCoordinatorLayout,
                    R.string.snacbkar_no_such_tag,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void selectTags(ArrayList<Integer> selectedTagIds) {
        for (int i = 0; i < selectedTagIds.size(); i++) {
            for (int j = 0; j < tagList.size(); j++) {
                if (tagList.get(j).getTagId().equals(selectedTagIds.get(i))) {
                    tagList.get(j).setTagIsSelected(true);
                }
            }
        }
    }

    @Override
    public void onListItemClicked(int position) {
        if (tagList.get(position).getTagIsSelected()) {
            tagList.get(position).setTagIsSelected(false);
        } else {
            tagList.get(position).setTagIsSelected(true);
        }
        tagRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    private ArrayList<Integer> getSelectedTags() {
        ArrayList<Integer> selectedTags = new ArrayList<>();
        for (int i = 0; i < tagList.size(); i++) {
            if (tagList.get(i).getTagIsSelected()) {
                selectedTags.add(tagList.get(i).getTagId());
            }
        }
        return selectedTags;
    }

    private void logSelectedTags(ArrayList<Integer> selectedTags) {
        for (Integer tagId : selectedTags) {
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.params_tag));
            params.putString(FirebaseAnalytics.Param.ITEM_ID, tagId.toString());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(
                STATE_KEY_SELECTED_TAGS,
                getSelectedTags());
        outState.putParcelable(
                STATE_KEY_RECYCLER_STATE,
                tagRecyclerView.getLayoutManager().onSaveInstanceState());
        getSelectedTags();
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_up_btn:
                TagActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTagReference.removeEventListener(mTagEventListener);
    }
}
