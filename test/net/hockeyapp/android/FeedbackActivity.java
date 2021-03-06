package net.hockeyapp.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.hockeyapp.android.adapters.MessagesAdapter;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.objects.FeedbackUserDataElement;
import net.hockeyapp.android.tasks.LoginTask;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.views.AttachmentListView;
import net.hockeyapp.android.views.AttachmentView;

public class FeedbackActivity extends Activity implements OnClickListener {
    private static final int ATTACH_FILE = 2;
    private static final int ATTACH_PICTURE = 1;
    private static final int DIALOG_ERROR_ID = 0;
    public static final String EXTRA_INITIAL_ATTACHMENTS = "initialAttachments";
    public static final String EXTRA_INITIAL_USER_EMAIL = "initialUserEmail";
    public static final String EXTRA_INITIAL_USER_NAME = "initialUserName";
    public static final String EXTRA_URL = "url";
    private static final int MAX_ATTACHMENTS_PER_MSG = 3;
    private static final int PAINT_IMAGE = 3;
    private String initialUserEmail;
    private String initialUserName;
    private Button mAddAttachmentButton;
    private Button mAddResponseButton;
    private Context mContext;
    private EditText mEmailInput;
    private ErrorObject mError;
    private Handler mFeedbackHandler;
    private ArrayList<FeedbackMessage> mFeedbackMessages;
    private ScrollView mFeedbackScrollview;
    private boolean mFeedbackViewInitialized;
    private boolean mInSendFeedback;
    private List<Uri> mInitialAttachments;
    private TextView mLastUpdatedTextView;
    private MessagesAdapter mMessagesAdapter;
    private ListView mMessagesListView;
    private EditText mNameInput;
    private Handler mParseFeedbackHandler;
    private ParseFeedbackTask mParseFeedbackTask;
    private Button mRefreshButton;
    private Button mSendFeedbackButton;
    private SendFeedbackTask mSendFeedbackTask;
    private EditText mSubjectInput;
    private EditText mTextInput;
    private String mToken;
    private String mUrl;
    private LinearLayout mWrapperLayoutFeedbackAndMessages;

    /* renamed from: net.hockeyapp.android.FeedbackActivity.1 */
    class C03771 implements DialogInterface.OnClickListener {
        C03771() {
        }

        public void onClick(DialogInterface dialog, int id) {
            FeedbackActivity.this.mError = null;
            dialog.cancel();
        }
    }

    /* renamed from: net.hockeyapp.android.FeedbackActivity.2 */
    class C03782 implements Runnable {
        final /* synthetic */ FeedbackResponse val$feedbackResponse;

        C03782(FeedbackResponse feedbackResponse) {
            this.val$feedbackResponse = feedbackResponse;
        }

        public void run() {
            FeedbackActivity.this.configureFeedbackView(true);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat formatNew = new SimpleDateFormat("d MMM h:mm a");
            if (this.val$feedbackResponse != null && this.val$feedbackResponse.getFeedback() != null && this.val$feedbackResponse.getFeedback().getMessages() != null && this.val$feedbackResponse.getFeedback().getMessages().size() > 0) {
                FeedbackActivity.this.mFeedbackMessages = this.val$feedbackResponse.getFeedback().getMessages();
                Collections.reverse(FeedbackActivity.this.mFeedbackMessages);
                try {
                    Date date = format.parse(((FeedbackMessage) FeedbackActivity.this.mFeedbackMessages.get(FeedbackActivity.DIALOG_ERROR_ID)).getCreatedAt());
                    TextView access$200 = FeedbackActivity.this.mLastUpdatedTextView;
                    FeedbackActivity feedbackActivity = FeedbackActivity.this;
                    int i = C0388R.string.hockeyapp_feedback_last_updated_text;
                    Object[] objArr = new Object[FeedbackActivity.ATTACH_PICTURE];
                    objArr[FeedbackActivity.DIALOG_ERROR_ID] = formatNew.format(date);
                    access$200.setText(feedbackActivity.getString(i, objArr));
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                if (FeedbackActivity.this.mMessagesAdapter == null) {
                    FeedbackActivity.this.mMessagesAdapter = new MessagesAdapter(FeedbackActivity.this.mContext, FeedbackActivity.this.mFeedbackMessages);
                } else {
                    FeedbackActivity.this.mMessagesAdapter.clear();
                    Iterator it = FeedbackActivity.this.mFeedbackMessages.iterator();
                    while (it.hasNext()) {
                        FeedbackActivity.this.mMessagesAdapter.add((FeedbackMessage) it.next());
                    }
                    FeedbackActivity.this.mMessagesAdapter.notifyDataSetChanged();
                }
                FeedbackActivity.this.mMessagesListView.setAdapter(FeedbackActivity.this.mMessagesAdapter);
            }
        }
    }

    /* renamed from: net.hockeyapp.android.FeedbackActivity.3 */
    class C03793 implements Runnable {
        C03793() {
        }

        public void run() {
            PrefsUtil.getInstance().saveFeedbackTokenToPrefs(FeedbackActivity.this, null);
            FeedbackActivity.this.getSharedPreferences(ParseFeedbackTask.PREFERENCES_NAME, FeedbackActivity.DIALOG_ERROR_ID).edit().remove(ParseFeedbackTask.ID_LAST_MESSAGE_SEND).remove(ParseFeedbackTask.ID_LAST_MESSAGE_PROCESSED).apply();
            FeedbackActivity.this.configureFeedbackView(false);
        }
    }

    private static class FeedbackHandler extends Handler {
        private final WeakReference<FeedbackActivity> mWeakFeedbackActivity;

        /* renamed from: net.hockeyapp.android.FeedbackActivity.FeedbackHandler.1 */
        class C03801 implements Runnable {
            final /* synthetic */ FeedbackActivity val$feedbackActivity;

            C03801(FeedbackActivity feedbackActivity) {
                this.val$feedbackActivity = feedbackActivity;
            }

            public void run() {
                this.val$feedbackActivity.enableDisableSendFeedbackButton(true);
                this.val$feedbackActivity.showDialog(FeedbackActivity.DIALOG_ERROR_ID);
            }
        }

        public FeedbackHandler(FeedbackActivity feedbackActivity) {
            this.mWeakFeedbackActivity = new WeakReference(feedbackActivity);
        }

        public void handleMessage(Message msg) {
            boolean success = false;
            ErrorObject error = new ErrorObject();
            FeedbackActivity feedbackActivity = (FeedbackActivity) this.mWeakFeedbackActivity.get();
            if (feedbackActivity != null) {
                if (msg == null || msg.getData() == null) {
                    error.setMessage(feedbackActivity.getString(C0388R.string.hockeyapp_feedback_send_generic_error));
                } else {
                    Bundle bundle = msg.getData();
                    String responseString = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_RESPONSE);
                    String statusCode = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_STATUS);
                    String requestType = bundle.getString(SendFeedbackTask.BUNDLE_REQUEST_TYPE);
                    if (requestType.equals("send") && (responseString == null || Integer.parseInt(statusCode) != 201)) {
                        error.setMessage(feedbackActivity.getString(C0388R.string.hockeyapp_feedback_send_generic_error));
                    } else if (requestType.equals("fetch") && statusCode != null && (Integer.parseInt(statusCode) == 404 || Integer.parseInt(statusCode) == 422)) {
                        feedbackActivity.resetFeedbackView();
                        success = true;
                    } else if (responseString != null) {
                        feedbackActivity.startParseFeedbackTask(responseString, requestType);
                        success = true;
                    } else {
                        error.setMessage(feedbackActivity.getString(C0388R.string.hockeyapp_feedback_send_network_error));
                    }
                }
                feedbackActivity.mError = error;
                if (!success) {
                    feedbackActivity.runOnUiThread(new C03801(feedbackActivity));
                }
                feedbackActivity.onSendFeedbackResult(success);
            }
        }
    }

    private static class ParseFeedbackHandler extends Handler {
        private final WeakReference<FeedbackActivity> mWeakFeedbackActivity;

        /* renamed from: net.hockeyapp.android.FeedbackActivity.ParseFeedbackHandler.1 */
        class C03811 implements Runnable {
            final /* synthetic */ FeedbackActivity val$feedbackActivity;

            C03811(FeedbackActivity feedbackActivity) {
                this.val$feedbackActivity = feedbackActivity;
            }

            public void run() {
                this.val$feedbackActivity.showDialog(FeedbackActivity.DIALOG_ERROR_ID);
            }
        }

        public ParseFeedbackHandler(FeedbackActivity feedbackActivity) {
            this.mWeakFeedbackActivity = new WeakReference(feedbackActivity);
        }

        public void handleMessage(Message msg) {
            boolean success = false;
            FeedbackActivity feedbackActivity = (FeedbackActivity) this.mWeakFeedbackActivity.get();
            if (feedbackActivity != null) {
                feedbackActivity.mError = new ErrorObject();
                if (!(msg == null || msg.getData() == null)) {
                    FeedbackResponse feedbackResponse = (FeedbackResponse) msg.getData().getSerializable(ParseFeedbackTask.BUNDLE_PARSE_FEEDBACK_RESPONSE);
                    if (feedbackResponse != null) {
                        if (feedbackResponse.getStatus().equalsIgnoreCase(LoginTask.BUNDLE_SUCCESS)) {
                            success = true;
                            if (feedbackResponse.getToken() != null) {
                                PrefsUtil.getInstance().saveFeedbackTokenToPrefs(feedbackActivity, feedbackResponse.getToken());
                                feedbackActivity.loadFeedbackMessages(feedbackResponse);
                                feedbackActivity.mInSendFeedback = false;
                            }
                        } else {
                            success = false;
                        }
                    }
                }
                if (!success) {
                    feedbackActivity.runOnUiThread(new C03811(feedbackActivity));
                }
                feedbackActivity.enableDisableSendFeedbackButton(true);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutView());
        setTitle(getString(C0388R.string.hockeyapp_feedback_title));
        this.mContext = this;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mUrl = extras.getString(EXTRA_URL);
            this.initialUserName = extras.getString(EXTRA_INITIAL_USER_NAME);
            this.initialUserEmail = extras.getString(EXTRA_INITIAL_USER_EMAIL);
            Parcelable[] initialAttachmentsArray = extras.getParcelableArray(EXTRA_INITIAL_ATTACHMENTS);
            if (initialAttachmentsArray != null) {
                this.mInitialAttachments = new ArrayList();
                int length = initialAttachmentsArray.length;
                for (int i = DIALOG_ERROR_ID; i < length; i += ATTACH_PICTURE) {
                    this.mInitialAttachments.add((Uri) initialAttachmentsArray[i]);
                }
            }
        }
        if (savedInstanceState != null) {
            this.mFeedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
            this.mInSendFeedback = savedInstanceState.getBoolean("inSendFeedback");
        } else {
            this.mInSendFeedback = false;
            this.mFeedbackViewInitialized = false;
        }
        ((NotificationManager) getSystemService("notification")).cancel(ATTACH_FILE);
        initFeedbackHandler();
        initParseFeedbackHandler();
        configureAppropriateView();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ViewGroup attachmentList = (ViewGroup) findViewById(C0388R.id.wrapper_attachments);
            Iterator it = savedInstanceState.getParcelableArrayList("attachments").iterator();
            while (it.hasNext()) {
                Uri attachmentUri = (Uri) it.next();
                if (!this.mInitialAttachments.contains(attachmentUri)) {
                    attachmentList.addView(new AttachmentView((Context) this, attachmentList, attachmentUri, true));
                }
            }
            this.mFeedbackViewInitialized = savedInstanceState.getBoolean("feedbackViewInitialized");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("attachments", ((AttachmentListView) findViewById(C0388R.id.wrapper_attachments)).getAttachments());
        outState.putBoolean("feedbackViewInitialized", this.mFeedbackViewInitialized);
        outState.putBoolean("inSendFeedback", this.mInSendFeedback);
        super.onSaveInstanceState(outState);
    }

    protected void onStop() {
        super.onStop();
        if (this.mSendFeedbackTask != null) {
            this.mSendFeedbackTask.detach();
        }
    }

    public Object onRetainNonConfigurationInstance() {
        if (this.mSendFeedbackTask != null) {
            this.mSendFeedbackTask.detach();
        }
        return this.mSendFeedbackTask;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        if (this.mInSendFeedback) {
            this.mInSendFeedback = false;
            configureAppropriateView();
        } else {
            finish();
        }
        return true;
    }

    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == C0388R.id.button_send) {
            sendFeedback();
        } else if (viewId == C0388R.id.button_attachment) {
            if (((ViewGroup) findViewById(C0388R.id.wrapper_attachments)).getChildCount() >= PAINT_IMAGE) {
                Toast.makeText(this, String.valueOf(PAINT_IMAGE), DIALOG_ERROR_ID).show();
            } else {
                openContextMenu(v);
            }
        } else if (viewId == C0388R.id.button_add_response) {
            configureFeedbackView(false);
            this.mInSendFeedback = true;
        } else if (viewId == C0388R.id.button_refresh) {
            sendFetchFeedback(this.mUrl, null, null, null, null, null, PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this.mContext), this.mFeedbackHandler, true);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(DIALOG_ERROR_ID, ATTACH_FILE, DIALOG_ERROR_ID, getString(C0388R.string.hockeyapp_feedback_attach_file));
        menu.add(DIALOG_ERROR_ID, ATTACH_PICTURE, DIALOG_ERROR_ID, getString(C0388R.string.hockeyapp_feedback_attach_picture));
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ATTACH_PICTURE /*1*/:
            case ATTACH_FILE /*2*/:
                return addAttachment(item.getItemId());
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ERROR_ID /*0*/:
                return new Builder(this).setMessage(getString(C0388R.string.hockeyapp_dialog_error_message)).setCancelable(false).setTitle(getString(C0388R.string.hockeyapp_dialog_error_title)).setIcon(17301543).setPositiveButton(getString(C0388R.string.hockeyapp_dialog_positive_button), new C03771()).create();
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_ERROR_ID /*0*/:
                AlertDialog messageDialogError = (AlertDialog) dialog;
                if (this.mError != null) {
                    messageDialogError.setMessage(this.mError.getMessage());
                } else {
                    messageDialogError.setMessage(getString(C0388R.string.hockeyapp_feedback_generic_error));
                }
            default:
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            Uri uri;
            ViewGroup attachments;
            if (requestCode == ATTACH_FILE) {
                uri = data.getData();
                if (uri != null) {
                    attachments = (ViewGroup) findViewById(C0388R.id.wrapper_attachments);
                    attachments.addView(new AttachmentView((Context) this, attachments, uri, true));
                }
            } else if (requestCode == ATTACH_PICTURE) {
                uri = data.getData();
                if (uri != null) {
                    try {
                        Intent intent = new Intent(this, PaintActivity.class);
                        intent.putExtra(PaintActivity.EXTRA_IMAGE_URI, uri);
                        startActivityForResult(intent, PAINT_IMAGE);
                    } catch (ActivityNotFoundException e) {
                        HockeyLog.error(Util.LOG_IDENTIFIER, "Paint activity not declared!", e);
                    }
                }
            } else if (requestCode == PAINT_IMAGE) {
                uri = (Uri) data.getParcelableExtra(PaintActivity.EXTRA_IMAGE_URI);
                if (uri != null) {
                    attachments = (ViewGroup) findViewById(C0388R.id.wrapper_attachments);
                    attachments.addView(new AttachmentView((Context) this, attachments, uri, true));
                }
            }
        }
    }

    @SuppressLint({"InflateParams"})
    public View getLayoutView() {
        return getLayoutInflater().inflate(C0388R.layout.hockeyapp_activity_feedback, null);
    }

    public void enableDisableSendFeedbackButton(boolean isEnable) {
        if (this.mSendFeedbackButton != null) {
            this.mSendFeedbackButton.setEnabled(isEnable);
        }
    }

    protected void configureFeedbackView(boolean haveToken) {
        this.mFeedbackScrollview = (ScrollView) findViewById(C0388R.id.wrapper_feedback_scroll);
        this.mWrapperLayoutFeedbackAndMessages = (LinearLayout) findViewById(C0388R.id.wrapper_messages);
        this.mMessagesListView = (ListView) findViewById(C0388R.id.list_feedback_messages);
        if (haveToken) {
            this.mWrapperLayoutFeedbackAndMessages.setVisibility(DIALOG_ERROR_ID);
            this.mFeedbackScrollview.setVisibility(8);
            this.mLastUpdatedTextView = (TextView) findViewById(C0388R.id.label_last_updated);
            this.mAddResponseButton = (Button) findViewById(C0388R.id.button_add_response);
            this.mAddResponseButton.setOnClickListener(this);
            this.mRefreshButton = (Button) findViewById(C0388R.id.button_refresh);
            this.mRefreshButton.setOnClickListener(this);
            return;
        }
        this.mWrapperLayoutFeedbackAndMessages.setVisibility(8);
        this.mFeedbackScrollview.setVisibility(DIALOG_ERROR_ID);
        this.mNameInput = (EditText) findViewById(C0388R.id.input_name);
        this.mEmailInput = (EditText) findViewById(C0388R.id.input_email);
        this.mSubjectInput = (EditText) findViewById(C0388R.id.input_subject);
        this.mTextInput = (EditText) findViewById(C0388R.id.input_message);
        if (!this.mFeedbackViewInitialized) {
            String nameEmailSubject = PrefsUtil.getInstance().getNameEmailFromPrefs(this.mContext);
            if (nameEmailSubject != null) {
                String[] nameEmailSubjectArray = nameEmailSubject.split("\\|");
                if (nameEmailSubjectArray != null && nameEmailSubjectArray.length >= ATTACH_FILE) {
                    this.mNameInput.setText(nameEmailSubjectArray[DIALOG_ERROR_ID]);
                    this.mEmailInput.setText(nameEmailSubjectArray[ATTACH_PICTURE]);
                    if (nameEmailSubjectArray.length >= PAINT_IMAGE) {
                        this.mSubjectInput.setText(nameEmailSubjectArray[ATTACH_FILE]);
                        this.mTextInput.requestFocus();
                    } else {
                        this.mSubjectInput.requestFocus();
                    }
                }
            } else {
                this.mNameInput.setText(this.initialUserName);
                this.mEmailInput.setText(this.initialUserEmail);
                this.mSubjectInput.setText(TtmlNode.ANONYMOUS_REGION_ID);
                if (TextUtils.isEmpty(this.initialUserName)) {
                    this.mNameInput.requestFocus();
                } else if (TextUtils.isEmpty(this.initialUserEmail)) {
                    this.mEmailInput.requestFocus();
                } else {
                    this.mSubjectInput.requestFocus();
                }
            }
            this.mFeedbackViewInitialized = true;
        }
        this.mTextInput.setText(TtmlNode.ANONYMOUS_REGION_ID);
        if (PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this.mContext) != null) {
            this.mSubjectInput.setVisibility(8);
        } else {
            this.mSubjectInput.setVisibility(DIALOG_ERROR_ID);
        }
        ViewGroup attachmentListView = (ViewGroup) findViewById(C0388R.id.wrapper_attachments);
        attachmentListView.removeAllViews();
        if (this.mInitialAttachments != null) {
            for (Uri attachmentUri : this.mInitialAttachments) {
                attachmentListView.addView(new AttachmentView((Context) this, attachmentListView, attachmentUri, true));
            }
        }
        this.mAddAttachmentButton = (Button) findViewById(C0388R.id.button_attachment);
        this.mAddAttachmentButton.setOnClickListener(this);
        registerForContextMenu(this.mAddAttachmentButton);
        this.mSendFeedbackButton = (Button) findViewById(C0388R.id.button_send);
        this.mSendFeedbackButton.setOnClickListener(this);
    }

    protected void onSendFeedbackResult(boolean success) {
    }

    private boolean addAttachment(int request) {
        Intent intent;
        if (request == ATTACH_FILE) {
            intent = new Intent();
            intent.setType("*/*");
            intent.setAction("android.intent.action.GET_CONTENT");
            startActivityForResult(Intent.createChooser(intent, getString(C0388R.string.hockeyapp_feedback_select_file)), ATTACH_FILE);
            return true;
        } else if (request != ATTACH_PICTURE) {
            return false;
        } else {
            intent = new Intent();
            intent.setType("image/*");
            intent.setAction("android.intent.action.GET_CONTENT");
            startActivityForResult(Intent.createChooser(intent, getString(C0388R.string.hockeyapp_feedback_select_picture)), ATTACH_PICTURE);
            return true;
        }
    }

    private void configureAppropriateView() {
        this.mToken = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this);
        if (this.mToken == null || this.mInSendFeedback) {
            configureFeedbackView(false);
            return;
        }
        configureFeedbackView(true);
        sendFetchFeedback(this.mUrl, null, null, null, null, null, this.mToken, this.mFeedbackHandler, true);
    }

    private void createParseFeedbackTask(String feedbackResponseString, String requestType) {
        this.mParseFeedbackTask = new ParseFeedbackTask(this, feedbackResponseString, this.mParseFeedbackHandler, requestType);
    }

    private void hideKeyboard() {
        if (this.mTextInput != null) {
            ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.mTextInput.getWindowToken(), DIALOG_ERROR_ID);
        }
    }

    private void initFeedbackHandler() {
        this.mFeedbackHandler = new FeedbackHandler(this);
    }

    private void initParseFeedbackHandler() {
        this.mParseFeedbackHandler = new ParseFeedbackHandler(this);
    }

    @SuppressLint({"SimpleDateFormat"})
    private void loadFeedbackMessages(FeedbackResponse feedbackResponse) {
        runOnUiThread(new C03782(feedbackResponse));
    }

    private void resetFeedbackView() {
        runOnUiThread(new C03793());
    }

    private void sendFeedback() {
        if (Util.isConnectedToNetwork(this)) {
            enableDisableSendFeedbackButton(false);
            hideKeyboard();
            String token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(this.mContext);
            String name = this.mNameInput.getText().toString().trim();
            String email = this.mEmailInput.getText().toString().trim();
            String subject = this.mSubjectInput.getText().toString().trim();
            String text = this.mTextInput.getText().toString().trim();
            if (TextUtils.isEmpty(subject)) {
                this.mSubjectInput.setVisibility(DIALOG_ERROR_ID);
                setError(this.mSubjectInput, C0388R.string.hockeyapp_feedback_validate_subject_error);
                return;
            } else if (FeedbackManager.getRequireUserName() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(name)) {
                setError(this.mNameInput, C0388R.string.hockeyapp_feedback_validate_name_error);
                return;
            } else if (FeedbackManager.getRequireUserEmail() == FeedbackUserDataElement.REQUIRED && TextUtils.isEmpty(email)) {
                setError(this.mEmailInput, C0388R.string.hockeyapp_feedback_validate_email_empty);
                return;
            } else if (TextUtils.isEmpty(text)) {
                setError(this.mTextInput, C0388R.string.hockeyapp_feedback_validate_text_error);
                return;
            } else if (FeedbackManager.getRequireUserEmail() != FeedbackUserDataElement.REQUIRED || Util.isValidEmail(email)) {
                PrefsUtil.getInstance().saveNameEmailSubjectToPrefs(this.mContext, name, email, subject);
                sendFetchFeedback(this.mUrl, name, email, subject, text, ((AttachmentListView) findViewById(C0388R.id.wrapper_attachments)).getAttachments(), token, this.mFeedbackHandler, false);
                return;
            } else {
                setError(this.mEmailInput, C0388R.string.hockeyapp_feedback_validate_email_error);
                return;
            }
        }
        Toast.makeText(this, C0388R.string.hockeyapp_error_no_network_message, ATTACH_PICTURE).show();
    }

    private void setError(EditText inputField, int feedbackStringId) {
        inputField.setError(getString(feedbackStringId));
        enableDisableSendFeedbackButton(true);
    }

    private void sendFetchFeedback(String url, String name, String email, String subject, String text, List<Uri> attachmentUris, String token, Handler feedbackHandler, boolean isFetchMessages) {
        this.mSendFeedbackTask = new SendFeedbackTask(this.mContext, url, name, email, subject, text, attachmentUris, token, feedbackHandler, isFetchMessages);
        AsyncTaskUtils.execute(this.mSendFeedbackTask);
    }

    private void startParseFeedbackTask(String feedbackResponseString, String requestType) {
        createParseFeedbackTask(feedbackResponseString, requestType);
        AsyncTaskUtils.execute(this.mParseFeedbackTask);
    }
}
