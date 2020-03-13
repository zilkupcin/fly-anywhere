package com.example.android.flyanywhere.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;


public class PostDealFragment extends Fragment {

    @BindView(R.id.sv_post)
    NestedScrollView mScrollView;
    @BindView(R.id.et_post_title)
    EditText postTitleEditText;
    @BindView(R.id.et_post_link)
    EditText postLinkEditText;
    @BindView(R.id.et_post_description)
    EditText postDescriptionEditText;
    @BindView(R.id.btn_submit_deal)
    Button submitDealButton;

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser currentUser;

    private static final String STATE_KEY_TITLE = "title";
    private static final String STATE_KEY_LINK = "link";
    private static final String STATE_KEY_DESCRIPTION = "description";

    public PostDealFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_deal, container, false);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            postTitleEditText.setText(savedInstanceState.getString(STATE_KEY_TITLE));
            postLinkEditText.setText(savedInstanceState.getString(STATE_KEY_LINK));
            postDescriptionEditText.setText(savedInstanceState.getString(STATE_KEY_DESCRIPTION));
        }
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        submitDealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtils.isConnectedToTheInternet(getContext())) {
                    submitDeal();
                } else {
                    Snackbar.make(
                            mScrollView,
                            getString(R.string.error_internet_required_message),
                            Snackbar.LENGTH_LONG).show();
                    submitDealButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        getCurrentUser();
        super.onResume();
    }

    private void submitDeal() {
        if (currentUser != null) {

            String postTitle = postTitleEditText.getText().toString();
            String postLink = postLinkEditText.getText().toString();
            String postDescription = postDescriptionEditText.getText().toString();

            if (postTitle.isEmpty()) {
                showErrorDialog(getString(R.string.error_title_cannot_be_empty));
            } else if (postDescription.isEmpty()) {
                showErrorDialog(getString(R.string.error_description_cannot_be_empty));
            } else if (postDescription.length() < 100) {
                showErrorDialog(getString(R.string.error_description_too_short));
            } else {
                submitDealButton.setEnabled(false);
                DealSubmission dealSubmission = new DealSubmission(currentUser.getUid(),
                        postTitle,
                        postLink,
                        postDescription);

                final DatabaseReference mSubmittedDealsReference = mFirebaseDatabase.getReference()
                        .child("submittedDeals/");
                mSubmittedDealsReference.push()
                        .setValue(dealSubmission, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Snackbar.make(
                                            mScrollView,
                                            getString(R.string.success_submission_received),
                                            Snackbar.LENGTH_LONG).show();
                                    enableSubmitButtonAfterDelay(5000);
                                } else {
                                    enableSubmitButtonAfterDelay(1000);
                                }
                            }
                        });
            }
        } else {
            Snackbar.make(
                    mScrollView,
                    getString(R.string.error_only_for_logged_in_users),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    public void enableSubmitButtonAfterDelay(Integer delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (submitDealButton != null) {
                    submitDealButton.setEnabled(true);
                }
            }
        }, delay);
    }

    public void getCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (postTitleEditText != null) {
            outState.putString(STATE_KEY_TITLE, postTitleEditText.getText().toString());
            outState.putString(STATE_KEY_LINK, postLinkEditText.getText().toString());
            outState.putString(STATE_KEY_DESCRIPTION, postDescriptionEditText.getText().toString());
            super.onSaveInstanceState(outState);
        }
    }

    private void showErrorDialog(String error) {
        final AlertDialog dialog =
                new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle(R.string.error_error)
                        .setMessage(error)
                        .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
    }

    @IgnoreExtraProperties
    public class DealSubmission {

        private String author;
        private String title;
        private String link;
        private String description;

        public DealSubmission() {
        }

        public DealSubmission(String author, String title, String link, String description) {
            this.author = author;
            this.title = title;
            this.link = link;
            this.description = description;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}


