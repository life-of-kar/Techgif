package org.telegram.messenger;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.exoplayer.MediaCodecAudioTrackRenderer;
import org.telegram.messenger.exoplayer.MediaCodecVideoTrackRenderer;
import org.telegram.messenger.query.BotQuery;
import org.telegram.messenger.query.MessagesQuery;
import org.telegram.messenger.query.SharedMediaQuery;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_chatChannelParticipant;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatInviteEmpty;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipants;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_inputMessageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported_old;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_dialogs;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettingsEmpty;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceTooLong;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WallPaper;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.messages_Dialogs;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.tgnet.TLRPC.photos_Photos;
import org.telegram.ui.Components.VideoPlayer;

public class MessagesStorage {
    private static volatile MessagesStorage Instance;
    public static int lastDateValue;
    public static int lastPtsValue;
    public static int lastQtsValue;
    public static int lastSecretVersion;
    public static int lastSeqValue;
    public static int secretG;
    public static byte[] secretPBytes;
    private File cacheFile;
    private SQLiteDatabase database;
    private int lastSavedDate;
    private int lastSavedPts;
    private int lastSavedQts;
    private int lastSavedSeq;
    private AtomicLong lastTaskId;
    private DispatchQueue storageQueue;

    /* renamed from: org.telegram.messenger.MessagesStorage.11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ ArrayList val$wallPapers;

        AnonymousClass11(ArrayList arrayList) {
            this.val$wallPapers = arrayList;
        }

        public void run() {
            int num = 0;
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM wallpapers WHERE 1").stepThis().dispose();
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO wallpapers VALUES(?, ?)");
                Iterator i$ = this.val$wallPapers.iterator();
                while (i$.hasNext()) {
                    WallPaper wallPaper = (WallPaper) i$.next();
                    state.requery();
                    NativeByteBuffer data = new NativeByteBuffer(wallPaper.getObjectSize());
                    wallPaper.serializeToStream(data);
                    state.bindInteger(1, num);
                    state.bindByteBuffer(2, data);
                    state.step();
                    num++;
                    data.reuse();
                }
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.12 */
    class AnonymousClass12 implements Runnable {
        final /* synthetic */ int val$type;

        /* renamed from: org.telegram.messenger.MessagesStorage.12.1 */
        class C06391 implements Runnable {
            final /* synthetic */ ArrayList val$arrayList;

            C06391(ArrayList arrayList) {
                this.val$arrayList = arrayList;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.recentImagesDidLoaded, Integer.valueOf(AnonymousClass12.this.val$type), this.val$arrayList);
            }
        }

        AnonymousClass12(int i) {
            this.val$type = i;
        }

        public void run() {
            try {
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT id, image_url, thumb_url, local_url, width, height, size, date, document FROM web_recent_v3 WHERE type = " + this.val$type + " ORDER BY date DESC", new Object[0]);
                ArrayList<SearchImage> arrayList = new ArrayList();
                while (cursor.next()) {
                    SearchImage searchImage = new SearchImage();
                    searchImage.id = cursor.stringValue(0);
                    searchImage.imageUrl = cursor.stringValue(1);
                    searchImage.thumbUrl = cursor.stringValue(2);
                    searchImage.localUrl = cursor.stringValue(3);
                    searchImage.width = cursor.intValue(4);
                    searchImage.height = cursor.intValue(5);
                    searchImage.size = cursor.intValue(6);
                    searchImage.date = cursor.intValue(7);
                    if (!cursor.isNull(8)) {
                        NativeByteBuffer data = cursor.byteBufferValue(8);
                        if (data != null) {
                            searchImage.document = Document.TLdeserialize(data, data.readInt32(false), false);
                            data.reuse();
                        }
                    }
                    searchImage.type = this.val$type;
                    arrayList.add(searchImage);
                }
                cursor.dispose();
                AndroidUtilities.runOnUIThread(new C06391(arrayList));
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.13 */
    class AnonymousClass13 implements Runnable {
        final /* synthetic */ Document val$document;
        final /* synthetic */ String val$imageUrl;
        final /* synthetic */ String val$localUrl;

        AnonymousClass13(Document document, String str, String str2) {
            this.val$document = document;
            this.val$imageUrl = str;
            this.val$localUrl = str2;
        }

        public void run() {
            try {
                SQLitePreparedStatement state;
                if (this.val$document != null) {
                    state = MessagesStorage.this.database.executeFast("UPDATE web_recent_v3 SET document = ? WHERE image_url = ?");
                    state.requery();
                    NativeByteBuffer data = new NativeByteBuffer(this.val$document.getObjectSize());
                    this.val$document.serializeToStream(data);
                    state.bindByteBuffer(1, data);
                    state.bindString(2, this.val$imageUrl);
                    state.step();
                    state.dispose();
                    data.reuse();
                    return;
                }
                state = MessagesStorage.this.database.executeFast("UPDATE web_recent_v3 SET local_url = ? WHERE image_url = ?");
                state.requery();
                state.bindString(1, this.val$localUrl);
                state.bindString(2, this.val$imageUrl);
                state.step();
                state.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.14 */
    class AnonymousClass14 implements Runnable {
        final /* synthetic */ SearchImage val$searchImage;

        AnonymousClass14(SearchImage searchImage) {
            this.val$searchImage = searchImage;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE id = '" + this.val$searchImage.id + "'").stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.15 */
    class AnonymousClass15 implements Runnable {
        final /* synthetic */ int val$type;

        AnonymousClass15(int i) {
            this.val$type = i;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE type = " + this.val$type).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.16 */
    class AnonymousClass16 implements Runnable {
        final /* synthetic */ ArrayList val$arrayList;

        AnonymousClass16(ArrayList arrayList) {
            this.val$arrayList = arrayList;
        }

        public void run() {
            try {
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO web_recent_v3 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                int a = 0;
                while (a < this.val$arrayList.size() && a != Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                    SearchImage searchImage = (SearchImage) this.val$arrayList.get(a);
                    state.requery();
                    state.bindString(1, searchImage.id);
                    state.bindInteger(2, searchImage.type);
                    state.bindString(3, searchImage.imageUrl != null ? searchImage.imageUrl : TtmlNode.ANONYMOUS_REGION_ID);
                    state.bindString(4, searchImage.thumbUrl != null ? searchImage.thumbUrl : TtmlNode.ANONYMOUS_REGION_ID);
                    state.bindString(5, searchImage.localUrl != null ? searchImage.localUrl : TtmlNode.ANONYMOUS_REGION_ID);
                    state.bindInteger(6, searchImage.width);
                    state.bindInteger(7, searchImage.height);
                    state.bindInteger(8, searchImage.size);
                    state.bindInteger(9, searchImage.date);
                    NativeByteBuffer data = null;
                    if (searchImage.document != null) {
                        data = new NativeByteBuffer(searchImage.document.getObjectSize());
                        searchImage.document.serializeToStream(data);
                        state.bindByteBuffer(10, data);
                    } else {
                        state.bindNull(10);
                    }
                    state.step();
                    if (data != null) {
                        data.reuse();
                    }
                    a++;
                }
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
                if (this.val$arrayList.size() >= Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                    MessagesStorage.this.database.beginTransaction();
                    for (a = Callback.DEFAULT_DRAG_ANIMATION_DURATION; a < this.val$arrayList.size(); a++) {
                        MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE id = '" + ((SearchImage) this.val$arrayList.get(a)).id + "'").stepThis().dispose();
                    }
                    MessagesStorage.this.database.commitTransaction();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.19 */
    class AnonymousClass19 implements Runnable {
        final /* synthetic */ int val$id;

        AnonymousClass19(int i) {
            this.val$id = i;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM blocked_users WHERE uid = " + this.val$id).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.1 */
    class C06411 implements Runnable {
        final /* synthetic */ int val$currentVersion;

        /* renamed from: org.telegram.messenger.MessagesStorage.1.1 */
        class C06371 implements Runnable {
            C06371() {
            }

            public void run() {
                Iterator i$;
                ArrayList<Integer> ids = new ArrayList();
                for (Entry<String, ?> entry : ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getAll().entrySet()) {
                    String key = (String) entry.getKey();
                    if (key.startsWith("notify2_") && ((Integer) entry.getValue()).intValue() == 2) {
                        try {
                            ids.add(Integer.valueOf(Integer.parseInt(key.replace("notify2_", TtmlNode.ANONYMOUS_REGION_ID))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO dialog_settings VALUES(?, ?)");
                    i$ = ids.iterator();
                    while (i$.hasNext()) {
                        Integer id = (Integer) i$.next();
                        state.requery();
                        state.bindLong(1, (long) id.intValue());
                        state.bindInteger(2, 1);
                        state.step();
                    }
                    state.dispose();
                    MessagesStorage.this.database.commitTransaction();
                } catch (Throwable e2) {
                    FileLog.m13e("tmessages", e2);
                }
            }
        }

        C06411(int i) {
            this.val$currentVersion = i;
        }

        public void run() {
            try {
                SQLiteCursor cursor;
                SQLitePreparedStatement state;
                NativeByteBuffer data;
                int version = this.val$currentVersion;
                if (version < 4) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_photos(uid INTEGER, id INTEGER, data BLOB, PRIMARY KEY (uid, id))").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS read_state_out_idx_messages;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS ttl_idx_messages;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS date_idx_messages;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_out_idx_messages ON messages(mid, out);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS task_idx_messages ON messages(uid, out, read_state, ttl, date, send_state);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_idx_messages ON messages(uid, date, mid);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_contacts_v6(uid INTEGER PRIMARY KEY, fname TEXT, sname TEXT)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_phones_v6(uid INTEGER, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (uid, phone))").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v6(sphone, deleted);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_idx_randoms ON randoms(mid);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS sent_files_v2(uid TEXT, type INTEGER, data BLOB, PRIMARY KEY (uid, type))").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS blocked_users(uid INTEGER PRIMARY KEY)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS download_queue(uid INTEGER, type INTEGER, date INTEGER, data BLOB, PRIMARY KEY (uid, type));").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS type_date_idx_download_queue ON download_queue(type, date);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS dialog_settings(did INTEGER PRIMARY KEY, flags INTEGER);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS send_state_idx_messages ON messages(mid, send_state, date) WHERE mid < 0 AND send_state = 1;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_idx_dialogs ON dialogs(unread_count);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("UPDATE messages SET send_state = 2 WHERE mid < 0 AND send_state = 1").stepThis().dispose();
                    MessagesStorage.this.storageQueue.postRunnable(new C06371());
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 4").stepThis().dispose();
                    version = 4;
                }
                if (version == 4) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS enc_tasks_v2(mid INTEGER PRIMARY KEY, date INTEGER)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_enc_tasks_v2 ON enc_tasks_v2(date);").stepThis().dispose();
                    MessagesStorage.this.database.beginTransaction();
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT date, data FROM enc_tasks WHERE 1", new Object[0]);
                    state = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
                    if (cursor.next()) {
                        int date = cursor.intValue(0);
                        data = cursor.byteBufferValue(1);
                        if (data != null) {
                            int length = data.limit();
                            for (int a = 0; a < length / 4; a++) {
                                state.requery();
                                state.bindInteger(1, data.readInt32(false));
                                state.bindInteger(2, date);
                                state.step();
                            }
                            data.reuse();
                        }
                    }
                    state.dispose();
                    cursor.dispose();
                    MessagesStorage.this.database.commitTransaction();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS date_idx_enc_tasks;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS enc_tasks;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN media INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 6").stepThis().dispose();
                    version = 6;
                }
                if (version == 6) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_seq(mid INTEGER PRIMARY KEY, seq_in INTEGER, seq_out INTEGER);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS seq_idx_messages_seq ON messages_seq(seq_in, seq_out);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN layer INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN seq_in INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN seq_out INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 7").stepThis().dispose();
                    version = 7;
                }
                if (version == 7 || version == 8 || version == 9) {
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN use_count INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN exchange_id INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN key_date INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN fprint INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN fauthkey BLOB default NULL").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN khash BLOB default NULL").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 10").stepThis().dispose();
                    version = 10;
                }
                if (version == 10) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS web_recent_v3(id TEXT, type INTEGER, image_url TEXT, thumb_url TEXT, local_url TEXT, width INTEGER, height INTEGER, size INTEGER, date INTEGER, PRIMARY KEY (id, type));").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 11").stepThis().dispose();
                    version = 11;
                }
                if (version == 11) {
                    version = 12;
                }
                if (version == 12) {
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_mid_idx_media;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS mid_idx_media;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_date_mid_idx_media;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS media;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS media_counts;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_v2(mid INTEGER PRIMARY KEY, uid INTEGER, date INTEGER, type INTEGER, data BLOB)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_counts_v2(uid INTEGER, type INTEGER, count INTEGER, PRIMARY KEY(uid, type))").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_type_date_idx_media ON media_v2(uid, mid, type, date);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS keyvalue(id TEXT PRIMARY KEY, value TEXT)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 13").stepThis().dispose();
                    version = 13;
                }
                if (version == 13) {
                    MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN replydata BLOB default NULL").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 14").stepThis().dispose();
                    version = 14;
                }
                if (version == 14) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS hashtag_recent_v2(id TEXT PRIMARY KEY, date INTEGER);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 15").stepThis().dispose();
                    version = 15;
                }
                if (version == 15) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS webpage_pending(id INTEGER, mid INTEGER, PRIMARY KEY (id, mid));").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 16").stepThis().dispose();
                    version = 16;
                }
                if (version == 16) {
                    MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN inbox_max INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN outbox_max INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 17").stepThis().dispose();
                    version = 17;
                }
                if (version == 17) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE bot_info(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 18").stepThis().dispose();
                    version = 18;
                }
                if (version == 18) {
                    MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS stickers;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS stickers_v2(id INTEGER PRIMARY KEY, data BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 19").stepThis().dispose();
                    version = 19;
                }
                if (version == 19) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS bot_keyboard(uid INTEGER PRIMARY KEY, mid INTEGER, info BLOB)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS bot_keyboard_idx_mid ON bot_keyboard(mid);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 20").stepThis().dispose();
                    version = 20;
                }
                if (version == 20) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE search_recent(did INTEGER PRIMARY KEY, date INTEGER);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 21").stepThis().dispose();
                    version = 21;
                }
                if (version == 21) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_settings_v2(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT uid, participants FROM chat_settings WHERE uid < 0", new Object[0]);
                    state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?)");
                    while (cursor.next()) {
                        int chat_id = cursor.intValue(0);
                        data = cursor.byteBufferValue(1);
                        if (data != null) {
                            ChatParticipants participants = ChatParticipants.TLdeserialize(data, data.readInt32(false), false);
                            data.reuse();
                            if (participants != null) {
                                TL_chatFull chatFull = new TL_chatFull();
                                chatFull.id = chat_id;
                                chatFull.chat_photo = new TL_photoEmpty();
                                chatFull.notify_settings = new TL_peerNotifySettingsEmpty();
                                chatFull.exported_invite = new TL_chatInviteEmpty();
                                chatFull.participants = participants;
                                NativeByteBuffer data2 = new NativeByteBuffer(chatFull.getObjectSize());
                                chatFull.serializeToStream(data2);
                                state.requery();
                                state.bindInteger(1, chat_id);
                                state.bindByteBuffer(2, data2);
                                state.step();
                                data2.reuse();
                            }
                        }
                    }
                    state.dispose();
                    cursor.dispose();
                    MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS chat_settings;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN last_mid_i INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN unread_count_i INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN pts INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN date_i INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_i_idx_dialogs ON dialogs(last_mid_i);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_i_idx_dialogs ON dialogs(unread_count_i);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN imp INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_holes ON messages_holes(uid, end);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 22").stepThis().dispose();
                    version = 22;
                }
                if (version == 22) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_holes_v2(uid INTEGER, type INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, type, start));").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_media_holes_v2 ON media_holes_v2(uid, type, end);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 23").stepThis().dispose();
                    version = 23;
                }
                if (version == 24) {
                    MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid != 0 AND type >= 0 AND start IN (0, 1)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 25").stepThis().dispose();
                    version = 25;
                }
                if (version == 25 || version == 26) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS channel_users_v2(did INTEGER, uid INTEGER, date INTEGER, data BLOB, PRIMARY KEY(did, uid))").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 27").stepThis().dispose();
                    version = 27;
                }
                if (version == 27) {
                    MessagesStorage.this.database.executeFast("ALTER TABLE web_recent_v3 ADD COLUMN document BLOB default NULL").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 28").stepThis().dispose();
                    version = 28;
                }
                if (version == 28) {
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 29").stepThis().dispose();
                    version = 29;
                }
                if (version == 29) {
                    MessagesStorage.this.database.executeFast("DELETE FROM sent_files_v2 WHERE 1").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM download_queue WHERE 1").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 30").stepThis().dispose();
                    version = 30;
                }
                if (version == 30) {
                    MessagesStorage.this.database.executeFast("ALTER TABLE chat_settings_v2 ADD COLUMN pinned INTEGER default 0").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_settings_pinned_idx ON chat_settings_v2(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_pinned(uid INTEGER PRIMARY KEY, pinned INTEGER, data BLOB)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_pinned_mid_idx ON chat_pinned(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS users_data(uid INTEGER PRIMARY KEY, about TEXT)").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 31").stepThis().dispose();
                    version = 31;
                }
                if (version == 31) {
                    MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS bot_recent;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_hints(did INTEGER, type INTEGER, rating REAL, date INTEGER, PRIMARY KEY(did, type))").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_hints_rating_idx ON chat_hints(rating);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 32").stepThis().dispose();
                    version = 32;
                }
                if (version == 32) {
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_mid_idx_imp_messages;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_date_mid_imp_idx_messages;").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 33").stepThis().dispose();
                    version = 33;
                }
                if (version == 33) {
                    MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS pending_tasks(id INTEGER PRIMARY KEY, data BLOB);").stepThis().dispose();
                    MessagesStorage.this.database.executeFast("PRAGMA user_version = 34").stepThis().dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.20 */
    class AnonymousClass20 implements Runnable {
        final /* synthetic */ ArrayList val$ids;
        final /* synthetic */ boolean val$replace;

        AnonymousClass20(boolean z, ArrayList arrayList) {
            this.val$replace = z;
            this.val$ids = arrayList;
        }

        public void run() {
            try {
                if (this.val$replace) {
                    MessagesStorage.this.database.executeFast("DELETE FROM blocked_users WHERE 1").stepThis().dispose();
                }
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO blocked_users VALUES(?)");
                Iterator i$ = this.val$ids.iterator();
                while (i$.hasNext()) {
                    Integer id = (Integer) i$.next();
                    state.requery();
                    state.bindInteger(1, id.intValue());
                    state.step();
                }
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.21 */
    class AnonymousClass21 implements Runnable {
        final /* synthetic */ int val$channelId;
        final /* synthetic */ int val$uid;

        /* renamed from: org.telegram.messenger.MessagesStorage.21.1 */
        class C06431 implements Runnable {
            final /* synthetic */ ArrayList val$mids;

            C06431(ArrayList arrayList) {
                this.val$mids = arrayList;
            }

            public void run() {
                MessagesController.getInstance().markChannelDialogMessageAsDeleted(this.val$mids, AnonymousClass21.this.val$channelId);
            }
        }

        /* renamed from: org.telegram.messenger.MessagesStorage.21.2 */
        class C06442 implements Runnable {
            final /* synthetic */ ArrayList val$mids;

            C06442(ArrayList arrayList) {
                this.val$mids = arrayList;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesDeleted, this.val$mids, Integer.valueOf(AnonymousClass21.this.val$channelId));
            }
        }

        AnonymousClass21(int i, int i2) {
            this.val$channelId = i;
            this.val$uid = i2;
        }

        public void run() {
            try {
                long did = (long) (-this.val$channelId);
                ArrayList<Integer> mids = new ArrayList();
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT data FROM messages WHERE uid = " + did, new Object[0]);
                ArrayList<File> filesToDelete = new ArrayList();
                while (cursor.next()) {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (!(message == null || message.from_id != this.val$uid || message.id == 1)) {
                            mids.add(Integer.valueOf(message.id));
                            File file;
                            if (message.media instanceof TL_messageMediaPhoto) {
                                Iterator i$ = message.media.photo.sizes.iterator();
                                while (i$.hasNext()) {
                                    file = FileLoader.getPathToAttach((PhotoSize) i$.next());
                                    if (file != null && file.toString().length() > 0) {
                                        filesToDelete.add(file);
                                    }
                                }
                            } else {
                                try {
                                    if (message.media instanceof TL_messageMediaDocument) {
                                        file = FileLoader.getPathToAttach(message.media.document);
                                        if (file != null && file.toString().length() > 0) {
                                            filesToDelete.add(file);
                                        }
                                        file = FileLoader.getPathToAttach(message.media.document.thumb);
                                        if (file != null && file.toString().length() > 0) {
                                            filesToDelete.add(file);
                                        }
                                    }
                                } catch (Throwable e) {
                                    FileLog.m13e("tmessages", e);
                                }
                            }
                        }
                    }
                }
                cursor.dispose();
                AndroidUtilities.runOnUIThread(new C06431(mids));
                MessagesStorage.this.markMessagesAsDeletedInternal(mids, this.val$channelId);
                MessagesStorage.this.updateDialogsWithDeletedMessagesInternal(mids, this.val$channelId);
                FileLoader.getInstance().deleteFiles(filesToDelete, 0);
                if (!mids.isEmpty()) {
                    AndroidUtilities.runOnUIThread(new C06442(mids));
                }
            } catch (Throwable e2) {
                FileLog.m13e("tmessages", e2);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.22 */
    class AnonymousClass22 implements Runnable {
        final /* synthetic */ long val$did;
        final /* synthetic */ int val$messagesOnly;

        /* renamed from: org.telegram.messenger.MessagesStorage.22.1 */
        class C06451 implements Runnable {
            C06451() {
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
            }
        }

        AnonymousClass22(int i, long j) {
            this.val$messagesOnly = i;
            this.val$did = j;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r30 = this;
            r0 = r30;
            r0 = r0.val$messagesOnly;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r26 = 3;
            r0 = r25;
            r1 = r26;
            if (r0 != r1) goto L_0x0056;
        L_0x000e:
            r14 = -1;
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "SELECT last_mid FROM dialogs WHERE did = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r27 = 0;
            r0 = r27;
            r0 = new java.lang.Object[r0];	 Catch:{ Exception -> 0x040b }
            r27 = r0;
            r6 = r25.queryFinalized(r26, r27);	 Catch:{ Exception -> 0x040b }
            r25 = r6.next();	 Catch:{ Exception -> 0x040b }
            if (r25 == 0) goto L_0x0050;
        L_0x0048:
            r25 = 0;
            r0 = r25;
            r14 = r6.intValue(r0);	 Catch:{ Exception -> 0x040b }
        L_0x0050:
            r6.dispose();	 Catch:{ Exception -> 0x040b }
            if (r14 == 0) goto L_0x0056;
        L_0x0055:
            return;
        L_0x0056:
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r0 = r26;
            r0 = (int) r0;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            if (r25 == 0) goto L_0x0071;
        L_0x0063:
            r0 = r30;
            r0 = r0.val$messagesOnly;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r26 = 2;
            r0 = r25;
            r1 = r26;
            if (r0 != r1) goto L_0x0138;
        L_0x0071:
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "SELECT data FROM messages WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r27 = 0;
            r0 = r27;
            r0 = new java.lang.Object[r0];	 Catch:{ Exception -> 0x040b }
            r27 = r0;
            r6 = r25.queryFinalized(r26, r27);	 Catch:{ Exception -> 0x040b }
            r11 = new java.util.ArrayList;	 Catch:{ Exception -> 0x040b }
            r11.<init>();	 Catch:{ Exception -> 0x040b }
        L_0x00a9:
            r25 = r6.next();	 Catch:{ Exception -> 0x011c }
            if (r25 == 0) goto L_0x0124;
        L_0x00af:
            r25 = 0;
            r0 = r25;
            r8 = r6.byteBufferValue(r0);	 Catch:{ Exception -> 0x011c }
            if (r8 == 0) goto L_0x00a9;
        L_0x00b9:
            r25 = 0;
            r0 = r25;
            r25 = r8.readInt32(r0);	 Catch:{ Exception -> 0x011c }
            r26 = 0;
            r0 = r25;
            r1 = r26;
            r20 = org.telegram.tgnet.TLRPC.Message.TLdeserialize(r8, r0, r1);	 Catch:{ Exception -> 0x011c }
            r8.reuse();	 Catch:{ Exception -> 0x011c }
            if (r20 == 0) goto L_0x00a9;
        L_0x00d0:
            r0 = r20;
            r0 = r0.media;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            if (r25 == 0) goto L_0x00a9;
        L_0x00d8:
            r0 = r20;
            r0 = r0.media;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            if (r25 == 0) goto L_0x0415;
        L_0x00e6:
            r0 = r20;
            r0 = r0.media;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0.photo;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0.sizes;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r13 = r25.iterator();	 Catch:{ Exception -> 0x011c }
        L_0x00fc:
            r25 = r13.hasNext();	 Catch:{ Exception -> 0x011c }
            if (r25 == 0) goto L_0x00a9;
        L_0x0102:
            r22 = r13.next();	 Catch:{ Exception -> 0x011c }
            r22 = (org.telegram.tgnet.TLRPC.PhotoSize) r22;	 Catch:{ Exception -> 0x011c }
            r10 = org.telegram.messenger.FileLoader.getPathToAttach(r22);	 Catch:{ Exception -> 0x011c }
            if (r10 == 0) goto L_0x00fc;
        L_0x010e:
            r25 = r10.toString();	 Catch:{ Exception -> 0x011c }
            r25 = r25.length();	 Catch:{ Exception -> 0x011c }
            if (r25 <= 0) goto L_0x00fc;
        L_0x0118:
            r11.add(r10);	 Catch:{ Exception -> 0x011c }
            goto L_0x00fc;
        L_0x011c:
            r9 = move-exception;
            r25 = "tmessages";
            r0 = r25;
            org.telegram.messenger.FileLog.m13e(r0, r9);	 Catch:{ Exception -> 0x040b }
        L_0x0124:
            r6.dispose();	 Catch:{ Exception -> 0x040b }
            r25 = org.telegram.messenger.FileLoader.getInstance();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$messagesOnly;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r0 = r25;
            r1 = r26;
            r0.deleteFiles(r11, r1);	 Catch:{ Exception -> 0x040b }
        L_0x0138:
            r0 = r30;
            r0 = r0.val$messagesOnly;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            if (r25 == 0) goto L_0x014e;
        L_0x0140:
            r0 = r30;
            r0 = r0.val$messagesOnly;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r26 = 3;
            r0 = r25;
            r1 = r26;
            if (r0 != r1) goto L_0x0499;
        L_0x014e:
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM dialogs WHERE did = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM chat_settings_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM chat_pinned WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM channel_users_v2 WHERE did = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM search_recent WHERE did = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r0 = r26;
            r15 = (int) r0;	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r25 = 32;
            r26 = r26 >> r25;
            r0 = r26;
            r12 = (int) r0;	 Catch:{ Exception -> 0x040b }
            if (r15 == 0) goto L_0x046d;
        L_0x0260:
            r25 = 1;
            r0 = r25;
            if (r12 != r0) goto L_0x0469;
        L_0x0266:
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM chats WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r26;
            r26 = r0.append(r15);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
        L_0x0290:
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "UPDATE dialogs SET unread_count = 0, unread_count_i = 0 WHERE did = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM messages WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM bot_keyboard WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM media_counts_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM media_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM messages_holes WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM media_holes_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r25 = 0;
            r0 = r26;
            r2 = r25;
            org.telegram.messenger.query.BotQuery.clearBotKeyboard(r0, r2);	 Catch:{ Exception -> 0x040b }
            r25 = new org.telegram.messenger.MessagesStorage$22$1;	 Catch:{ Exception -> 0x040b }
            r0 = r25;
            r1 = r30;
            r0.<init>();	 Catch:{ Exception -> 0x040b }
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r25);	 Catch:{ Exception -> 0x040b }
            goto L_0x0055;
        L_0x040b:
            r9 = move-exception;
            r25 = "tmessages";
            r0 = r25;
            org.telegram.messenger.FileLog.m13e(r0, r9);
            goto L_0x0055;
        L_0x0415:
            r0 = r20;
            r0 = r0.media;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaDocument;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            if (r25 == 0) goto L_0x00a9;
        L_0x0423:
            r0 = r20;
            r0 = r0.media;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0.document;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r10 = org.telegram.messenger.FileLoader.getPathToAttach(r25);	 Catch:{ Exception -> 0x011c }
            if (r10 == 0) goto L_0x0442;
        L_0x0435:
            r25 = r10.toString();	 Catch:{ Exception -> 0x011c }
            r25 = r25.length();	 Catch:{ Exception -> 0x011c }
            if (r25 <= 0) goto L_0x0442;
        L_0x043f:
            r11.add(r10);	 Catch:{ Exception -> 0x011c }
        L_0x0442:
            r0 = r20;
            r0 = r0.media;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0.document;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r0 = r25;
            r0 = r0.thumb;	 Catch:{ Exception -> 0x011c }
            r25 = r0;
            r10 = org.telegram.messenger.FileLoader.getPathToAttach(r25);	 Catch:{ Exception -> 0x011c }
            if (r10 == 0) goto L_0x00a9;
        L_0x045a:
            r25 = r10.toString();	 Catch:{ Exception -> 0x011c }
            r25 = r25.length();	 Catch:{ Exception -> 0x011c }
            if (r25 <= 0) goto L_0x00a9;
        L_0x0464:
            r11.add(r10);	 Catch:{ Exception -> 0x011c }
            goto L_0x00a9;
        L_0x0469:
            if (r15 >= 0) goto L_0x0290;
        L_0x046b:
            goto L_0x0290;
        L_0x046d:
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM enc_chats WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r26;
            r26 = r0.append(r12);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            goto L_0x0290;
        L_0x0499:
            r0 = r30;
            r0 = r0.val$messagesOnly;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r26 = 2;
            r0 = r25;
            r1 = r26;
            if (r0 != r1) goto L_0x0290;
        L_0x04a7:
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "SELECT last_mid_i, last_mid FROM dialogs WHERE did = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r27 = 0;
            r0 = r27;
            r0 = new java.lang.Object[r0];	 Catch:{ Exception -> 0x040b }
            r27 = r0;
            r6 = r25.queryFinalized(r26, r27);	 Catch:{ Exception -> 0x040b }
            r21 = -1;
            r25 = r6.next();	 Catch:{ Exception -> 0x040b }
            if (r25 == 0) goto L_0x0716;
        L_0x04e2:
            r25 = 0;
            r0 = r25;
            r18 = r6.longValue(r0);	 Catch:{ Exception -> 0x040b }
            r25 = 1;
            r0 = r25;
            r16 = r6.longValue(r0);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "SELECT data FROM messages WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r27 = " AND mid IN (";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r26;
            r1 = r18;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r27 = ",";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r26;
            r1 = r16;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r27 = ")";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r27 = 0;
            r0 = r27;
            r0 = new java.lang.Object[r0];	 Catch:{ Exception -> 0x040b }
            r27 = r0;
            r7 = r25.queryFinalized(r26, r27);	 Catch:{ Exception -> 0x040b }
        L_0x0547:
            r25 = r7.next();	 Catch:{ Exception -> 0x0575 }
            if (r25 == 0) goto L_0x057d;
        L_0x054d:
            r25 = 0;
            r0 = r25;
            r8 = r7.byteBufferValue(r0);	 Catch:{ Exception -> 0x0575 }
            if (r8 == 0) goto L_0x0547;
        L_0x0557:
            r25 = 0;
            r0 = r25;
            r25 = r8.readInt32(r0);	 Catch:{ Exception -> 0x0575 }
            r26 = 0;
            r0 = r25;
            r1 = r26;
            r20 = org.telegram.tgnet.TLRPC.Message.TLdeserialize(r8, r0, r1);	 Catch:{ Exception -> 0x0575 }
            r8.reuse();	 Catch:{ Exception -> 0x0575 }
            if (r20 == 0) goto L_0x0547;
        L_0x056e:
            r0 = r20;
            r0 = r0.id;	 Catch:{ Exception -> 0x0575 }
            r21 = r0;
            goto L_0x0547;
        L_0x0575:
            r9 = move-exception;
            r25 = "tmessages";
            r0 = r25;
            org.telegram.messenger.FileLog.m13e(r0, r9);	 Catch:{ Exception -> 0x040b }
        L_0x057d:
            r7.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM messages WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r27 = " AND mid != ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r26;
            r1 = r18;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r27 = " AND mid != ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r26;
            r1 = r16;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM messages_holes WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM bot_keyboard WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM media_counts_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM media_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x040b }
            r26.<init>();	 Catch:{ Exception -> 0x040b }
            r27 = "DELETE FROM media_holes_v2 WHERE uid = ";
            r26 = r26.append(r27);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r28 = r0;
            r0 = r26;
            r1 = r28;
            r26 = r0.append(r1);	 Catch:{ Exception -> 0x040b }
            r26 = r26.toString();	 Catch:{ Exception -> 0x040b }
            r25 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = r25.stepThis();	 Catch:{ Exception -> 0x040b }
            r25.dispose();	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r25 = 0;
            r0 = r26;
            r2 = r25;
            org.telegram.messenger.query.BotQuery.clearBotKeyboard(r0, r2);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = "REPLACE INTO messages_holes VALUES(?, ?, ?)";
            r23 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r0 = r30;
            r0 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x040b }
            r25 = r0;
            r25 = r25.database;	 Catch:{ Exception -> 0x040b }
            r26 = "REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)";
            r24 = r25.executeFast(r26);	 Catch:{ Exception -> 0x040b }
            r25 = -1;
            r0 = r21;
            r1 = r25;
            if (r0 == r1) goto L_0x0710;
        L_0x06ff:
            r0 = r30;
            r0 = r0.val$did;	 Catch:{ Exception -> 0x040b }
            r26 = r0;
            r0 = r26;
            r2 = r23;
            r3 = r24;
            r4 = r21;
            org.telegram.messenger.MessagesStorage.createFirstHoles(r0, r2, r3, r4);	 Catch:{ Exception -> 0x040b }
        L_0x0710:
            r23.dispose();	 Catch:{ Exception -> 0x040b }
            r24.dispose();	 Catch:{ Exception -> 0x040b }
        L_0x0716:
            r6.dispose();	 Catch:{ Exception -> 0x040b }
            goto L_0x0055;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.22.run():void");
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.23 */
    class AnonymousClass23 implements Runnable {
        final /* synthetic */ int val$classGuid;
        final /* synthetic */ int val$count;
        final /* synthetic */ int val$did;
        final /* synthetic */ long val$max_id;
        final /* synthetic */ int val$offset;

        /* renamed from: org.telegram.messenger.MessagesStorage.23.1 */
        class C06461 implements Runnable {
            final /* synthetic */ photos_Photos val$res;

            C06461(photos_Photos org_telegram_tgnet_TLRPC_photos_Photos) {
                this.val$res = org_telegram_tgnet_TLRPC_photos_Photos;
            }

            public void run() {
                MessagesController.getInstance().processLoadedUserPhotos(this.val$res, AnonymousClass23.this.val$did, AnonymousClass23.this.val$offset, AnonymousClass23.this.val$count, AnonymousClass23.this.val$max_id, true, AnonymousClass23.this.val$classGuid);
            }
        }

        AnonymousClass23(long j, int i, int i2, int i3, int i4) {
            this.val$max_id = j;
            this.val$did = i;
            this.val$count = i2;
            this.val$offset = i3;
            this.val$classGuid = i4;
        }

        public void run() {
            try {
                SQLiteCursor cursor;
                if (this.val$max_id != 0) {
                    cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM user_photos WHERE uid = %d AND id < %d ORDER BY id DESC LIMIT %d", new Object[]{Integer.valueOf(this.val$did), Long.valueOf(this.val$max_id), Integer.valueOf(this.val$count)}), new Object[0]);
                } else {
                    cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM user_photos WHERE uid = %d ORDER BY id DESC LIMIT %d,%d", new Object[]{Integer.valueOf(this.val$did), Integer.valueOf(this.val$offset), Integer.valueOf(this.val$count)}), new Object[0]);
                }
                photos_Photos res = new photos_Photos();
                while (cursor.next()) {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        Photo photo = Photo.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        res.photos.add(photo);
                    }
                }
                cursor.dispose();
                Utilities.stageQueue.postRunnable(new C06461(res));
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.24 */
    class AnonymousClass24 implements Runnable {
        final /* synthetic */ int val$uid;

        AnonymousClass24(int i) {
            this.val$uid = i;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM user_photos WHERE uid = " + this.val$uid).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.25 */
    class AnonymousClass25 implements Runnable {
        final /* synthetic */ long val$pid;
        final /* synthetic */ int val$uid;

        AnonymousClass25(int i, long j) {
            this.val$uid = i;
            this.val$pid = j;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM user_photos WHERE uid = " + this.val$uid + " AND id = " + this.val$pid).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.26 */
    class AnonymousClass26 implements Runnable {
        final /* synthetic */ int val$did;
        final /* synthetic */ photos_Photos val$photos;

        AnonymousClass26(photos_Photos org_telegram_tgnet_TLRPC_photos_Photos, int i) {
            this.val$photos = org_telegram_tgnet_TLRPC_photos_Photos;
            this.val$did = i;
        }

        public void run() {
            try {
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO user_photos VALUES(?, ?, ?)");
                Iterator i$ = this.val$photos.photos.iterator();
                while (i$.hasNext()) {
                    Photo photo = (Photo) i$.next();
                    if (!(photo instanceof TL_photoEmpty)) {
                        state.requery();
                        NativeByteBuffer data = new NativeByteBuffer(photo.getObjectSize());
                        photo.serializeToStream(data);
                        state.bindInteger(1, this.val$did);
                        state.bindLong(2, photo.id);
                        state.bindByteBuffer(3, data);
                        state.step();
                        data.reuse();
                    }
                }
                state.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.27 */
    class AnonymousClass27 implements Runnable {
        final /* synthetic */ ArrayList val$oldTask;

        AnonymousClass27(ArrayList arrayList) {
            this.val$oldTask = arrayList;
        }

        public void run() {
            try {
                if (this.val$oldTask != null) {
                    String ids = TextUtils.join(",", this.val$oldTask);
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM enc_tasks_v2 WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
                }
                int date = 0;
                ArrayList<Integer> arr = null;
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT mid, date FROM enc_tasks_v2 WHERE date = (SELECT min(date) FROM enc_tasks_v2)", new Object[0]);
                while (cursor.next()) {
                    Integer mid = Integer.valueOf(cursor.intValue(0));
                    date = cursor.intValue(1);
                    if (arr == null) {
                        arr = new ArrayList();
                    }
                    arr.add(mid);
                }
                cursor.dispose();
                MessagesController.getInstance().processLoadedDeleteTask(date, arr);
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.28 */
    class AnonymousClass28 implements Runnable {
        final /* synthetic */ int val$chat_id;
        final /* synthetic */ int val$isOut;
        final /* synthetic */ ArrayList val$random_ids;
        final /* synthetic */ int val$readTime;
        final /* synthetic */ int val$time;

        /* renamed from: org.telegram.messenger.MessagesStorage.28.1 */
        class C06471 implements Runnable {
            final /* synthetic */ ArrayList val$midsArray;

            C06471(ArrayList arrayList) {
                this.val$midsArray = arrayList;
            }

            public void run() {
                MessagesStorage.getInstance().markMessagesContentAsRead(this.val$midsArray);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesReadContent, this.val$midsArray);
            }
        }

        AnonymousClass28(ArrayList arrayList, int i, int i2, int i3, int i4) {
            this.val$random_ids = arrayList;
            this.val$chat_id = i;
            this.val$isOut = i2;
            this.val$time = i3;
            this.val$readTime = i4;
        }

        public void run() {
            int minDate = ConnectionsManager.DEFAULT_DATACENTER_ID;
            try {
                SQLiteCursor cursor;
                ArrayList<Integer> arr;
                SparseArray<ArrayList<Integer>> messages = new SparseArray();
                ArrayList<Long> midsArray = new ArrayList();
                StringBuilder mids = new StringBuilder();
                if (this.val$random_ids == null) {
                    SQLiteDatabase access$000 = MessagesStorage.this.database;
                    r20 = new Object[3];
                    r20[0] = Long.valueOf(((long) this.val$chat_id) << 32);
                    r20[1] = Integer.valueOf(this.val$isOut);
                    r20[2] = Integer.valueOf(this.val$time);
                    cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT mid, ttl FROM messages WHERE uid = %d AND out = %d AND read_state != 0 AND ttl > 0 AND date <= %d AND send_state = 0 AND media != 1", r20), new Object[0]);
                } else {
                    String ids = TextUtils.join(",", this.val$random_ids);
                    Object[] objArr = new Object[0];
                    cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.mid, m.ttl FROM messages as m INNER JOIN randoms as r ON m.mid = r.mid WHERE r.random_id IN (%s)", new Object[]{ids}), r0);
                }
                while (cursor.next()) {
                    int ttl = cursor.intValue(1);
                    int mid = cursor.intValue(0);
                    if (this.val$random_ids != null) {
                        midsArray.add(Long.valueOf((long) mid));
                    }
                    if (ttl > 0) {
                        int date = Math.min(this.val$readTime, this.val$time) + ttl;
                        minDate = Math.min(minDate, date);
                        arr = (ArrayList) messages.get(date);
                        if (arr == null) {
                            arr = new ArrayList();
                            messages.put(date, arr);
                        }
                        if (mids.length() != 0) {
                            mids.append(",");
                        }
                        mids.append(mid);
                        arr.add(Integer.valueOf(mid));
                    }
                }
                cursor.dispose();
                if (this.val$random_ids != null) {
                    AndroidUtilities.runOnUIThread(new C06471(midsArray));
                }
                if (messages.size() != 0) {
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
                    for (int a = 0; a < messages.size(); a++) {
                        int key = messages.keyAt(a);
                        arr = (ArrayList) messages.get(key);
                        for (int b = 0; b < arr.size(); b++) {
                            state.requery();
                            state.bindInteger(1, ((Integer) arr.get(b)).intValue());
                            state.bindInteger(2, key);
                            state.step();
                        }
                    }
                    state.dispose();
                    MessagesStorage.this.database.commitTransaction();
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET ttl = 0 WHERE mid IN(%s)", new Object[]{mids.toString()})).stepThis().dispose();
                    MessagesController.getInstance().didAddedNewTask(minDate, messages);
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.29 */
    class AnonymousClass29 implements Runnable {
        final /* synthetic */ SparseArray val$inbox;
        final /* synthetic */ SparseArray val$outbox;

        AnonymousClass29(SparseArray sparseArray, SparseArray sparseArray2) {
            this.val$inbox = sparseArray;
            this.val$outbox = sparseArray2;
        }

        public void run() {
            MessagesStorage.this.updateDialogsWithReadMessagesInternal(null, this.val$inbox, this.val$outbox);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.2 */
    class C06482 implements Runnable {
        final /* synthetic */ boolean val$isLogin;

        /* renamed from: org.telegram.messenger.MessagesStorage.2.1 */
        class C06421 implements Runnable {
            C06421() {
            }

            public void run() {
                MessagesController.getInstance().getDifference();
            }
        }

        C06482(boolean z) {
            this.val$isLogin = z;
        }

        public void run() {
            MessagesStorage.lastDateValue = 0;
            MessagesStorage.lastSeqValue = 0;
            MessagesStorage.lastPtsValue = 0;
            MessagesStorage.lastQtsValue = 0;
            MessagesStorage.lastSecretVersion = 0;
            MessagesStorage.this.lastSavedSeq = 0;
            MessagesStorage.this.lastSavedPts = 0;
            MessagesStorage.this.lastSavedDate = 0;
            MessagesStorage.this.lastSavedQts = 0;
            MessagesStorage.secretPBytes = null;
            MessagesStorage.secretG = 0;
            if (MessagesStorage.this.database != null) {
                MessagesStorage.this.database.close();
                MessagesStorage.this.database = null;
            }
            if (MessagesStorage.this.cacheFile != null) {
                MessagesStorage.this.cacheFile.delete();
                MessagesStorage.this.cacheFile = null;
            }
            MessagesStorage.this.openDatabase();
            if (this.val$isLogin) {
                Utilities.stageQueue.postRunnable(new C06421());
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.30 */
    class AnonymousClass30 implements Runnable {
        final /* synthetic */ ChatParticipants val$participants;

        /* renamed from: org.telegram.messenger.MessagesStorage.30.1 */
        class C06491 implements Runnable {
            final /* synthetic */ ChatFull val$finalInfo;

            C06491(ChatFull chatFull) {
                this.val$finalInfo = chatFull;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, this.val$finalInfo, Integer.valueOf(0), Boolean.valueOf(false), null);
            }
        }

        AnonymousClass30(ChatParticipants chatParticipants) {
            this.val$participants = chatParticipants;
        }

        public void run() {
            try {
                NativeByteBuffer data;
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + this.val$participants.chat_id, new Object[0]);
                ChatFull info = null;
                ArrayList<User> loadedUsers = new ArrayList();
                if (cursor.next()) {
                    data = cursor.byteBufferValue(0);
                    if (data != null) {
                        info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        info.pinned_msg_id = cursor.intValue(1);
                    }
                }
                cursor.dispose();
                if (info instanceof TL_chatFull) {
                    info.participants = this.val$participants;
                    AndroidUtilities.runOnUIThread(new C06491(info));
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
                    data = new NativeByteBuffer(info.getObjectSize());
                    info.serializeToStream(data);
                    state.bindInteger(1, info.id);
                    state.bindByteBuffer(2, data);
                    state.bindInteger(3, info.pinned_msg_id);
                    state.step();
                    state.dispose();
                    data.reuse();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.31 */
    class AnonymousClass31 implements Runnable {
        final /* synthetic */ int val$channel_id;
        final /* synthetic */ ArrayList val$participants;

        AnonymousClass31(int i, ArrayList arrayList) {
            this.val$channel_id = i;
            this.val$participants = arrayList;
        }

        public void run() {
            try {
                long did = (long) (-this.val$channel_id);
                MessagesStorage.this.database.executeFast("DELETE FROM channel_users_v2 WHERE did = " + did).stepThis().dispose();
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO channel_users_v2 VALUES(?, ?, ?, ?)");
                int date = (int) (System.currentTimeMillis() / 1000);
                for (int a = 0; a < this.val$participants.size(); a++) {
                    ChannelParticipant participant = (ChannelParticipant) this.val$participants.get(a);
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, participant.user_id);
                    state.bindInteger(3, date);
                    NativeByteBuffer data = new NativeByteBuffer(participant.getObjectSize());
                    participant.serializeToStream(data);
                    state.bindByteBuffer(4, data);
                    data.reuse();
                    state.step();
                    date--;
                }
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
                MessagesStorage.this.loadChatInfo(this.val$channel_id, null, false, true);
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.32 */
    class AnonymousClass32 implements Runnable {
        final /* synthetic */ boolean val$ifExist;
        final /* synthetic */ ChatFull val$info;

        AnonymousClass32(boolean z, ChatFull chatFull) {
            this.val$ifExist = z;
            this.val$info = chatFull;
        }

        public void run() {
            try {
                int i;
                SQLiteCursor cursor;
                if (this.val$ifExist) {
                    i = this.val$info.id;
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT uid FROM chat_settings_v2 WHERE uid = " + r0, new Object[0]);
                    boolean exist = cursor.next();
                    cursor.dispose();
                    if (!exist) {
                        return;
                    }
                }
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
                NativeByteBuffer data = new NativeByteBuffer(this.val$info.getObjectSize());
                this.val$info.serializeToStream(data);
                state.bindInteger(1, this.val$info.id);
                state.bindByteBuffer(2, data);
                state.bindInteger(3, this.val$info.pinned_msg_id);
                state.step();
                state.dispose();
                data.reuse();
                if (this.val$info instanceof TL_channelFull) {
                    i = -this.val$info.id;
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT date, pts, last_mid, inbox_max, outbox_max FROM dialogs WHERE did = " + r0, new Object[0]);
                    if (cursor.next()) {
                        int inbox_max = cursor.intValue(3);
                        if (inbox_max <= this.val$info.read_inbox_max_id) {
                            int inbox_diff = this.val$info.read_inbox_max_id - inbox_max;
                            if (inbox_diff < this.val$info.unread_count) {
                                this.val$info.unread_count = inbox_diff;
                            }
                            int dialog_date = cursor.intValue(0);
                            int pts = cursor.intValue(1);
                            long last_mid = cursor.longValue(2);
                            int outbox_max = cursor.intValue(4);
                            state = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            state.bindLong(1, (long) (-this.val$info.id));
                            state.bindInteger(2, dialog_date);
                            state.bindInteger(3, this.val$info.unread_count);
                            state.bindLong(4, last_mid);
                            state.bindInteger(5, this.val$info.read_inbox_max_id);
                            state.bindInteger(6, Math.max(outbox_max, this.val$info.read_outbox_max_id));
                            state.bindLong(7, 0);
                            state.bindInteger(8, 0);
                            state.bindInteger(9, pts);
                            state.bindInteger(10, 0);
                            state.step();
                            state.dispose();
                        }
                    }
                    cursor.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.33 */
    class AnonymousClass33 implements Runnable {
        final /* synthetic */ int val$channelId;
        final /* synthetic */ int val$messageId;

        /* renamed from: org.telegram.messenger.MessagesStorage.33.1 */
        class C06501 implements Runnable {
            final /* synthetic */ ChatFull val$finalInfo;

            C06501(ChatFull chatFull) {
                this.val$finalInfo = chatFull;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, this.val$finalInfo, Integer.valueOf(0), Boolean.valueOf(false), null);
            }
        }

        AnonymousClass33(int i, int i2) {
            this.val$channelId = i;
            this.val$messageId = i2;
        }

        public void run() {
            try {
                NativeByteBuffer data;
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + this.val$channelId, new Object[0]);
                ChatFull info = null;
                ArrayList<User> loadedUsers = new ArrayList();
                if (cursor.next()) {
                    data = cursor.byteBufferValue(0);
                    if (data != null) {
                        info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        info.pinned_msg_id = cursor.intValue(1);
                    }
                }
                cursor.dispose();
                if (info instanceof TL_channelFull) {
                    info.pinned_msg_id = this.val$messageId;
                    info.flags |= 32;
                    AndroidUtilities.runOnUIThread(new C06501(info));
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
                    data = new NativeByteBuffer(info.getObjectSize());
                    info.serializeToStream(data);
                    state.bindInteger(1, this.val$channelId);
                    state.bindByteBuffer(2, data);
                    state.bindInteger(3, info.pinned_msg_id);
                    state.step();
                    state.dispose();
                    data.reuse();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.34 */
    class AnonymousClass34 implements Runnable {
        final /* synthetic */ int val$chat_id;
        final /* synthetic */ int val$invited_id;
        final /* synthetic */ int val$user_id;
        final /* synthetic */ int val$version;
        final /* synthetic */ int val$what;

        /* renamed from: org.telegram.messenger.MessagesStorage.34.1 */
        class C06511 implements Runnable {
            final /* synthetic */ ChatFull val$finalInfo;

            C06511(ChatFull chatFull) {
                this.val$finalInfo = chatFull;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, this.val$finalInfo, Integer.valueOf(0), Boolean.valueOf(false), null);
            }
        }

        AnonymousClass34(int i, int i2, int i3, int i4, int i5) {
            this.val$chat_id = i;
            this.val$what = i2;
            this.val$user_id = i3;
            this.val$invited_id = i4;
            this.val$version = i5;
        }

        public void run() {
            try {
                NativeByteBuffer data;
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + this.val$chat_id, new Object[0]);
                ChatFull info = null;
                ArrayList<User> loadedUsers = new ArrayList();
                if (cursor.next()) {
                    data = cursor.byteBufferValue(0);
                    if (data != null) {
                        info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        info.pinned_msg_id = cursor.intValue(1);
                    }
                }
                cursor.dispose();
                if (info instanceof TL_chatFull) {
                    int a;
                    if (this.val$what == 1) {
                        for (a = 0; a < info.participants.participants.size(); a++) {
                            if (((ChatParticipant) info.participants.participants.get(a)).user_id == this.val$user_id) {
                                info.participants.participants.remove(a);
                                break;
                            }
                        }
                    } else if (this.val$what == 0) {
                        Iterator i$ = info.participants.participants.iterator();
                        while (i$.hasNext()) {
                            if (((ChatParticipant) i$.next()).user_id == this.val$user_id) {
                                return;
                            }
                        }
                        TL_chatParticipant participant = new TL_chatParticipant();
                        participant.user_id = this.val$user_id;
                        participant.inviter_id = this.val$invited_id;
                        participant.date = ConnectionsManager.getInstance().getCurrentTime();
                        info.participants.participants.add(participant);
                    } else if (this.val$what == 2) {
                        a = 0;
                        while (a < info.participants.participants.size()) {
                            ChatParticipant participant2 = (ChatParticipant) info.participants.participants.get(a);
                            if (participant2.user_id == this.val$user_id) {
                                ChatParticipant newParticipant;
                                if (this.val$invited_id == 1) {
                                    newParticipant = new TL_chatParticipantAdmin();
                                    newParticipant.user_id = participant2.user_id;
                                    newParticipant.date = participant2.date;
                                    newParticipant.inviter_id = participant2.inviter_id;
                                } else {
                                    newParticipant = new TL_chatParticipant();
                                    newParticipant.user_id = participant2.user_id;
                                    newParticipant.date = participant2.date;
                                    newParticipant.inviter_id = participant2.inviter_id;
                                }
                                info.participants.participants.set(a, newParticipant);
                            } else {
                                a++;
                            }
                        }
                    }
                    info.participants.version = this.val$version;
                    AndroidUtilities.runOnUIThread(new C06511(info));
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
                    data = new NativeByteBuffer(info.getObjectSize());
                    info.serializeToStream(data);
                    state.bindInteger(1, this.val$chat_id);
                    state.bindByteBuffer(2, data);
                    state.bindInteger(3, info.pinned_msg_id);
                    state.step();
                    state.dispose();
                    data.reuse();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.35 */
    class AnonymousClass35 implements Runnable {
        final /* synthetic */ int val$chat_id;
        final /* synthetic */ boolean[] val$result;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass35(int i, boolean[] zArr, Semaphore semaphore) {
            this.val$chat_id = i;
            this.val$result = zArr;
            this.val$semaphore = semaphore;
        }

        public void run() {
            boolean z = false;
            try {
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info FROM chat_settings_v2 WHERE uid = " + this.val$chat_id, new Object[0]);
                ChatFull info = null;
                ArrayList<User> loadedUsers = new ArrayList();
                if (cursor.next()) {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                    }
                }
                cursor.dispose();
                boolean[] zArr = this.val$result;
                if ((info instanceof TL_channelFull) && info.migrated_from_chat_id != 0) {
                    z = true;
                }
                zArr[0] = z;
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            } catch (Throwable th) {
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.36 */
    class AnonymousClass36 implements Runnable {
        final /* synthetic */ boolean val$byChannelUsers;
        final /* synthetic */ int val$chat_id;
        final /* synthetic */ boolean val$force;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass36(int i, Semaphore semaphore, boolean z, boolean z2) {
            this.val$chat_id = i;
            this.val$semaphore = semaphore;
            this.val$force = z;
            this.val$byChannelUsers = z2;
        }

        public void run() {
            MessageObject pinnedMessageObject = null;
            ChatFull info = null;
            ArrayList<User> loadedUsers = new ArrayList();
            try {
                NativeByteBuffer data;
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + this.val$chat_id, new Object[0]);
                if (cursor.next()) {
                    data = cursor.byteBufferValue(0);
                    if (data != null) {
                        info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        info.pinned_msg_id = cursor.intValue(1);
                    }
                }
                cursor.dispose();
                StringBuilder usersToLoad;
                int a;
                if (info instanceof TL_chatFull) {
                    usersToLoad = new StringBuilder();
                    for (a = 0; a < info.participants.participants.size(); a++) {
                        ChatParticipant c = (ChatParticipant) info.participants.participants.get(a);
                        if (usersToLoad.length() != 0) {
                            usersToLoad.append(",");
                        }
                        usersToLoad.append(c.user_id);
                    }
                    if (usersToLoad.length() != 0) {
                        MessagesStorage.this.getUsersInternal(usersToLoad.toString(), loadedUsers);
                    }
                } else if (info instanceof TL_channelFull) {
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT us.data, us.status, cu.data, cu.date FROM channel_users_v2 as cu LEFT JOIN users as us ON us.uid = cu.uid WHERE cu.did = " + (-this.val$chat_id) + " ORDER BY cu.date DESC", new Object[0]);
                    info.participants = new TL_chatParticipants();
                    while (cursor.next()) {
                        User user = null;
                        ChannelParticipant participant = null;
                        try {
                            data = cursor.byteBufferValue(0);
                            if (data != null) {
                                user = User.TLdeserialize(data, data.readInt32(false), false);
                                data.reuse();
                            }
                            data = cursor.byteBufferValue(2);
                            if (data != null) {
                                participant = ChannelParticipant.TLdeserialize(data, data.readInt32(false), false);
                                data.reuse();
                            }
                            if (!(user == null || participant == null)) {
                                if (user.status != null) {
                                    user.status.expires = cursor.intValue(1);
                                }
                                loadedUsers.add(user);
                                participant.date = cursor.intValue(3);
                                TL_chatChannelParticipant chatChannelParticipant = new TL_chatChannelParticipant();
                                chatChannelParticipant.user_id = participant.user_id;
                                chatChannelParticipant.date = participant.date;
                                chatChannelParticipant.inviter_id = participant.inviter_id;
                                chatChannelParticipant.channelParticipant = participant;
                                info.participants.participants.add(chatChannelParticipant);
                            }
                        } catch (Throwable e) {
                            FileLog.m13e("tmessages", e);
                        }
                    }
                    cursor.dispose();
                    usersToLoad = new StringBuilder();
                    for (a = 0; a < info.bot_info.size(); a++) {
                        BotInfo botInfo = (BotInfo) info.bot_info.get(a);
                        if (usersToLoad.length() != 0) {
                            usersToLoad.append(",");
                        }
                        usersToLoad.append(botInfo.user_id);
                    }
                    if (usersToLoad.length() != 0) {
                        MessagesStorage.this.getUsersInternal(usersToLoad.toString(), loadedUsers);
                    }
                }
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
                if ((info instanceof TL_channelFull) && info.pinned_msg_id != 0) {
                    pinnedMessageObject = MessagesQuery.loadPinnedMessage(this.val$chat_id, info.pinned_msg_id, false);
                }
                MessagesController.getInstance().processChatInfo(this.val$chat_id, info, loadedUsers, true, this.val$force, this.val$byChannelUsers, pinnedMessageObject);
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            } catch (Throwable e2) {
                FileLog.m13e("tmessages", e2);
                MessagesController.getInstance().processChatInfo(this.val$chat_id, info, loadedUsers, true, this.val$force, this.val$byChannelUsers, null);
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                MessagesController.getInstance().processChatInfo(this.val$chat_id, info, loadedUsers, true, this.val$force, this.val$byChannelUsers, null);
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.37 */
    class AnonymousClass37 implements Runnable {
        final /* synthetic */ long val$dialog_id;
        final /* synthetic */ int val$max_date;
        final /* synthetic */ long val$max_id;

        AnonymousClass37(long j, long j2, int i) {
            this.val$dialog_id = j;
            this.val$max_id = j2;
            this.val$max_date = i;
        }

        public void run() {
            try {
                SQLitePreparedStatement state;
                MessagesStorage.this.database.beginTransaction();
                if (((int) this.val$dialog_id) != 0) {
                    state = MessagesStorage.this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND mid <= ? AND read_state IN(0,2) AND out = 0");
                    state.requery();
                    state.bindLong(1, this.val$dialog_id);
                    state.bindLong(2, this.val$max_id);
                    state.step();
                    state.dispose();
                } else {
                    state = MessagesStorage.this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND date <= ? AND read_state IN(0,2) AND out = 0");
                    state.requery();
                    state.bindLong(1, this.val$dialog_id);
                    state.bindInteger(2, this.val$max_date);
                    state.step();
                    state.dispose();
                }
                int currentMaxId = 0;
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + this.val$dialog_id, new Object[0]);
                if (cursor.next()) {
                    currentMaxId = cursor.intValue(0);
                }
                cursor.dispose();
                currentMaxId = Math.max(currentMaxId, (int) this.val$max_id);
                state = MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0, inbox_max = ? WHERE did = ?");
                state.requery();
                state.bindInteger(1, currentMaxId);
                state.bindLong(2, this.val$dialog_id);
                state.step();
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.38 */
    class AnonymousClass38 implements Runnable {
        final /* synthetic */ ArrayList val$contactsCopy;
        final /* synthetic */ boolean val$deleteAll;

        AnonymousClass38(boolean z, ArrayList arrayList) {
            this.val$deleteAll = z;
            this.val$contactsCopy = arrayList;
        }

        public void run() {
            try {
                if (this.val$deleteAll) {
                    MessagesStorage.this.database.executeFast("DELETE FROM contacts WHERE 1").stepThis().dispose();
                }
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO contacts VALUES(?, ?)");
                for (int a = 0; a < this.val$contactsCopy.size(); a++) {
                    TL_contact contact = (TL_contact) this.val$contactsCopy.get(a);
                    state.requery();
                    state.bindInteger(1, contact.user_id);
                    state.bindInteger(2, contact.mutual ? 1 : 0);
                    state.step();
                }
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.39 */
    class AnonymousClass39 implements Runnable {
        final /* synthetic */ ArrayList val$uids;

        AnonymousClass39(ArrayList arrayList) {
            this.val$uids = arrayList;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM contacts WHERE uid IN(" + TextUtils.join(",", this.val$uids) + ")").stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.3 */
    class C06523 implements Runnable {
        final /* synthetic */ int val$lsv;
        final /* synthetic */ byte[] val$pbytes;
        final /* synthetic */ int val$sg;

        C06523(int i, int i2, byte[] bArr) {
            this.val$lsv = i;
            this.val$sg = i2;
            this.val$pbytes = bArr;
        }

        public void run() {
            int i = 1;
            try {
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE params SET lsv = ?, sg = ?, pbytes = ? WHERE id = 1");
                state.bindInteger(1, this.val$lsv);
                state.bindInteger(2, this.val$sg);
                if (this.val$pbytes != null) {
                    i = this.val$pbytes.length;
                }
                NativeByteBuffer data = new NativeByteBuffer(i);
                if (this.val$pbytes != null) {
                    data.writeBytes(this.val$pbytes);
                }
                state.bindByteBuffer(3, data);
                state.step();
                state.dispose();
                data.reuse();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.40 */
    class AnonymousClass40 implements Runnable {
        final /* synthetic */ String val$adds;
        final /* synthetic */ String val$deletes;

        AnonymousClass40(String str, String str2) {
            this.val$adds = str;
            this.val$deletes = str2;
        }

        public void run() {
            try {
                if (this.val$adds.length() != 0) {
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE user_phones_v6 SET deleted = 0 WHERE sphone IN(%s)", new Object[]{this.val$adds})).stepThis().dispose();
                }
                if (this.val$deletes.length() != 0) {
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE user_phones_v6 SET deleted = 1 WHERE sphone IN(%s)", new Object[]{this.val$deletes})).stepThis().dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.41 */
    class AnonymousClass41 implements Runnable {
        final /* synthetic */ HashMap val$contactHashMap;

        AnonymousClass41(HashMap hashMap) {
            this.val$contactHashMap = hashMap;
        }

        public void run() {
            try {
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO user_contacts_v6 VALUES(?, ?, ?)");
                SQLitePreparedStatement state2 = MessagesStorage.this.database.executeFast("REPLACE INTO user_phones_v6 VALUES(?, ?, ?, ?)");
                for (Entry<Integer, Contact> entry : this.val$contactHashMap.entrySet()) {
                    Contact contact = (Contact) entry.getValue();
                    if (!(contact.phones.isEmpty() || contact.shortPhones.isEmpty())) {
                        state.requery();
                        state.bindInteger(1, contact.id);
                        state.bindString(2, contact.first_name);
                        state.bindString(3, contact.last_name);
                        state.step();
                        for (int a = 0; a < contact.phones.size(); a++) {
                            state2.requery();
                            state2.bindInteger(1, contact.id);
                            state2.bindString(2, (String) contact.phones.get(a));
                            state2.bindString(3, (String) contact.shortPhones.get(a));
                            state2.bindInteger(4, ((Integer) contact.phoneDeleted.get(a)).intValue());
                            state2.step();
                        }
                    }
                }
                state.dispose();
                state2.dispose();
                MessagesStorage.this.database.commitTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.44 */
    class AnonymousClass44 implements Runnable {
        final /* synthetic */ int val$count;

        AnonymousClass44(int i) {
            this.val$count = i;
        }

        public void run() {
            try {
                HashMap<Integer, Message> messageHashMap = new HashMap();
                ArrayList<Message> messages = new ArrayList();
                ArrayList<User> users = new ArrayList();
                ArrayList<Chat> chats = new ArrayList();
                ArrayList<EncryptedChat> encryptedChats = new ArrayList();
                ArrayList<Integer> usersToLoad = new ArrayList();
                ArrayList<Integer> chatsToLoad = new ArrayList();
                ArrayList<Integer> broadcastIds = new ArrayList();
                ArrayList<Integer> encryptedChatIds = new ArrayList();
                Object[] objArr = new Object[0];
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.uid, s.seq_in, s.seq_out, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid LEFT JOIN messages_seq as s ON m.mid = s.mid WHERE m.mid < 0 AND m.send_state = 1 ORDER BY m.mid DESC LIMIT " + this.val$count, r0);
                while (cursor.next()) {
                    NativeByteBuffer data = cursor.byteBufferValue(1);
                    if (data != null) {
                        Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (messageHashMap.containsKey(Integer.valueOf(message.id))) {
                            continue;
                        } else {
                            MessageObject.setUnreadFlags(message, cursor.intValue(0));
                            message.id = cursor.intValue(3);
                            message.date = cursor.intValue(4);
                            if (!cursor.isNull(5)) {
                                message.random_id = cursor.longValue(5);
                            }
                            message.dialog_id = cursor.longValue(6);
                            message.seq_in = cursor.intValue(7);
                            message.seq_out = cursor.intValue(8);
                            message.ttl = cursor.intValue(9);
                            messages.add(message);
                            messageHashMap.put(Integer.valueOf(message.id), message);
                            int lower_id = (int) message.dialog_id;
                            int high_id = (int) (message.dialog_id >> 32);
                            if (lower_id == 0) {
                                if (!encryptedChatIds.contains(Integer.valueOf(high_id))) {
                                    encryptedChatIds.add(Integer.valueOf(high_id));
                                }
                            } else if (high_id == 1) {
                                if (!broadcastIds.contains(Integer.valueOf(lower_id))) {
                                    broadcastIds.add(Integer.valueOf(lower_id));
                                }
                            } else if (lower_id < 0) {
                                if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                                    chatsToLoad.add(Integer.valueOf(-lower_id));
                                }
                            } else if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                                usersToLoad.add(Integer.valueOf(lower_id));
                            }
                            MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                            message.send_state = cursor.intValue(2);
                            if (!(message.to_id.channel_id != 0 || MessageObject.isUnread(message) || lower_id == 0) || message.id > 0) {
                                message.send_state = 0;
                            }
                            if (lower_id == 0 && !cursor.isNull(5)) {
                                message.random_id = cursor.longValue(5);
                            }
                        }
                    }
                }
                cursor.dispose();
                if (!encryptedChatIds.isEmpty()) {
                    MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", encryptedChatIds), encryptedChats, usersToLoad);
                }
                if (!usersToLoad.isEmpty()) {
                    MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), users);
                }
                if (!(chatsToLoad.isEmpty() && broadcastIds.isEmpty())) {
                    int a;
                    Integer cid;
                    StringBuilder stringToLoad = new StringBuilder();
                    for (a = 0; a < chatsToLoad.size(); a++) {
                        cid = (Integer) chatsToLoad.get(a);
                        if (stringToLoad.length() != 0) {
                            stringToLoad.append(",");
                        }
                        stringToLoad.append(cid);
                    }
                    for (a = 0; a < broadcastIds.size(); a++) {
                        cid = (Integer) broadcastIds.get(a);
                        if (stringToLoad.length() != 0) {
                            stringToLoad.append(",");
                        }
                        stringToLoad.append(-cid.intValue());
                    }
                    MessagesStorage.this.getChatsInternal(stringToLoad.toString(), chats);
                }
                SendMessagesHelper.getInstance().processUnsentMessages(messages, users, chats, encryptedChats);
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.45 */
    class AnonymousClass45 implements Runnable {
        final /* synthetic */ long val$dialog_id;
        final /* synthetic */ int val$mid;
        final /* synthetic */ boolean[] val$result;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass45(long j, int i, boolean[] zArr, Semaphore semaphore) {
            this.val$dialog_id = j;
            this.val$mid = i;
            this.val$result = zArr;
            this.val$semaphore = semaphore;
        }

        public void run() {
            SQLiteCursor cursor = null;
            try {
                cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = %d AND mid = %d", new Object[]{Long.valueOf(this.val$dialog_id), Integer.valueOf(this.val$mid)}), new Object[0]);
                if (cursor.next()) {
                    this.val$result[0] = true;
                }
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            this.val$semaphore.release();
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.46 */
    class AnonymousClass46 implements Runnable {
        final /* synthetic */ int val$classGuid;
        final /* synthetic */ int val$count;
        final /* synthetic */ long val$dialog_id;
        final /* synthetic */ boolean val$isChannel;
        final /* synthetic */ int val$loadIndex;
        final /* synthetic */ int val$load_type;
        final /* synthetic */ int val$max_id;
        final /* synthetic */ int val$minDate;

        /* renamed from: org.telegram.messenger.MessagesStorage.46.1 */
        class C06531 implements Comparator<Message> {
            C06531() {
            }

            public int compare(Message lhs, Message rhs) {
                if (lhs.id <= 0 || rhs.id <= 0) {
                    if (lhs.id >= 0 || rhs.id >= 0) {
                        if (lhs.date > rhs.date) {
                            return -1;
                        }
                        if (lhs.date < rhs.date) {
                            return 1;
                        }
                    } else if (lhs.id < rhs.id) {
                        return -1;
                    } else {
                        if (lhs.id > rhs.id) {
                            return 1;
                        }
                    }
                } else if (lhs.id > rhs.id) {
                    return -1;
                } else {
                    if (lhs.id < rhs.id) {
                        return 1;
                    }
                }
                return 0;
            }
        }

        AnonymousClass46(int i, int i2, boolean z, long j, int i3, int i4, int i5, int i6) {
            this.val$count = i;
            this.val$max_id = i2;
            this.val$isChannel = z;
            this.val$dialog_id = j;
            this.val$load_type = i3;
            this.val$minDate = i4;
            this.val$classGuid = i5;
            this.val$loadIndex = i6;
        }

        public void run() {
            TL_messages_messages res = new TL_messages_messages();
            int count_unread = 0;
            int count_query = this.val$count;
            int offset_query = 0;
            int min_unread_id = 0;
            int last_message_id = 0;
            boolean queryFromServer = false;
            int max_unread_date = 0;
            long messageMaxId = (long) this.val$max_id;
            int max_id_query = this.val$max_id;
            int channelId = 0;
            if (this.val$isChannel) {
                channelId = -((int) this.val$dialog_id);
            }
            if (!(messageMaxId == 0 || channelId == 0)) {
                messageMaxId |= ((long) channelId) << 32;
            }
            boolean isEnd = false;
            int num = this.val$dialog_id == 777000 ? 4 : 1;
            try {
                SQLiteCursor cursor;
                AbstractSerializedData data;
                Message message;
                ArrayList<Integer> usersToLoad = new ArrayList();
                ArrayList<Integer> chatsToLoad = new ArrayList();
                ArrayList<Long> replyMessages = new ArrayList();
                HashMap<Integer, ArrayList<Message>> replyMessageOwners = new HashMap();
                HashMap<Long, ArrayList<Message>> replyMessageRandomOwners = new HashMap();
                int lower_id = (int) this.val$dialog_id;
                SQLiteDatabase access$000;
                Object[] objArr;
                if (lower_id != 0) {
                    boolean containMessage;
                    if (!(this.val$load_type == 1 || this.val$load_type == 3 || this.val$minDate != 0)) {
                        if (this.val$load_type == 2) {
                            cursor = MessagesStorage.this.database.queryFinalized("SELECT inbox_max, unread_count, date FROM dialogs WHERE did = " + this.val$dialog_id, new Object[0]);
                            if (cursor.next()) {
                                min_unread_id = cursor.intValue(0);
                                max_id_query = min_unread_id;
                                messageMaxId = (long) min_unread_id;
                                count_unread = cursor.intValue(1);
                                max_unread_date = cursor.intValue(2);
                                queryFromServer = true;
                                if (!(messageMaxId == 0 || channelId == 0)) {
                                    messageMaxId |= ((long) channelId) << 32;
                                }
                            }
                            cursor.dispose();
                            if (!queryFromServer) {
                                access$000 = MessagesStorage.this.database;
                                objArr = new Object[1];
                                objArr[0] = Long.valueOf(this.val$dialog_id);
                                cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT min(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > 0", objArr), new Object[0]);
                                if (cursor.next()) {
                                    min_unread_id = cursor.intValue(0);
                                    max_unread_date = cursor.intValue(1);
                                }
                                cursor.dispose();
                                if (min_unread_id != 0) {
                                    access$000 = MessagesStorage.this.database;
                                    objArr = new Object[2];
                                    objArr[0] = Long.valueOf(this.val$dialog_id);
                                    objArr[1] = Integer.valueOf(min_unread_id);
                                    cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid >= %d AND out = 0 AND read_state IN(0,2)", objArr), new Object[0]);
                                    if (cursor.next()) {
                                        count_unread = cursor.intValue(0);
                                    }
                                    cursor.dispose();
                                }
                            } else if (max_id_query == 0) {
                                int existingUnreadCount = 0;
                                access$000 = MessagesStorage.this.database;
                                objArr = new Object[1];
                                objArr[0] = Long.valueOf(this.val$dialog_id);
                                cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid > 0 AND out = 0 AND read_state IN(0,2)", objArr), new Object[0]);
                                if (cursor.next()) {
                                    existingUnreadCount = cursor.intValue(0);
                                }
                                cursor.dispose();
                                if (existingUnreadCount == count_unread) {
                                    access$000 = MessagesStorage.this.database;
                                    objArr = new Object[1];
                                    objArr[0] = Long.valueOf(this.val$dialog_id);
                                    cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > 0", objArr), new Object[0]);
                                    if (cursor.next()) {
                                        min_unread_id = cursor.intValue(0);
                                        max_id_query = min_unread_id;
                                        messageMaxId = (long) min_unread_id;
                                        if (!(messageMaxId == 0 || channelId == 0)) {
                                            messageMaxId |= ((long) channelId) << 32;
                                        }
                                    }
                                    cursor.dispose();
                                }
                            } else {
                                access$000 = MessagesStorage.this.database;
                                objArr = new Object[3];
                                objArr[0] = Long.valueOf(this.val$dialog_id);
                                objArr[1] = Integer.valueOf(max_id_query);
                                objArr[2] = Integer.valueOf(max_id_query);
                                cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT start, end FROM messages_holes WHERE uid = %d AND start < %d AND end > %d", objArr), new Object[0]);
                                containMessage = !cursor.next();
                                cursor.dispose();
                                if (containMessage) {
                                    access$000 = MessagesStorage.this.database;
                                    objArr = new Object[2];
                                    objArr[0] = Long.valueOf(this.val$dialog_id);
                                    objArr[1] = Integer.valueOf(max_id_query);
                                    cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > %d", objArr), new Object[0]);
                                    if (cursor.next()) {
                                        max_id_query = cursor.intValue(0);
                                        messageMaxId = (long) max_id_query;
                                        if (!(messageMaxId == 0 || channelId == 0)) {
                                            messageMaxId |= ((long) channelId) << 32;
                                        }
                                    }
                                    cursor.dispose();
                                }
                            }
                        }
                        if (count_query > count_unread || count_unread < num) {
                            count_query = Math.max(count_query, count_unread + 10);
                            if (count_unread < num) {
                                count_unread = 0;
                                min_unread_id = 0;
                                messageMaxId = 0;
                                last_message_id = 0;
                                queryFromServer = false;
                            }
                        } else {
                            offset_query = count_unread - count_query;
                            count_query += 10;
                        }
                    }
                    access$000 = MessagesStorage.this.database;
                    objArr = new Object[1];
                    objArr[0] = Long.valueOf(this.val$dialog_id);
                    cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start IN (0, 1)", objArr), new Object[0]);
                    if (cursor.next()) {
                        isEnd = cursor.intValue(0) == 1;
                        cursor.dispose();
                    } else {
                        cursor.dispose();
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[1];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND mid > 0", objArr), new Object[0]);
                        if (cursor.next()) {
                            int mid = cursor.intValue(0);
                            if (mid != 0) {
                                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
                                state.requery();
                                state.bindLong(1, this.val$dialog_id);
                                state.bindInteger(2, 0);
                                state.bindInteger(3, mid);
                                state.step();
                                state.dispose();
                            }
                        }
                        cursor.dispose();
                    }
                    if (this.val$load_type == 3 || (queryFromServer && this.val$load_type == 2)) {
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[1];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT max(mid) FROM messages WHERE uid = %d AND mid > 0", objArr), new Object[0]);
                        if (cursor.next()) {
                            last_message_id = cursor.intValue(0);
                        }
                        cursor.dispose();
                        containMessage = max_id_query != 0;
                        if (containMessage) {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[3];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(max_id_query);
                            objArr[2] = Integer.valueOf(max_id_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start < %d AND end > %d", objArr), new Object[0]);
                            if (cursor.next()) {
                                containMessage = false;
                            }
                            cursor.dispose();
                        }
                        if (containMessage) {
                            long holeMessageMaxId = 0;
                            long holeMessageMinId = 1;
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[2];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(max_id_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start >= %d ORDER BY start ASC LIMIT 1", objArr), new Object[0]);
                            if (cursor.next()) {
                                holeMessageMaxId = (long) cursor.intValue(0);
                                if (channelId != 0) {
                                    holeMessageMaxId |= ((long) channelId) << 32;
                                }
                            }
                            cursor.dispose();
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[2];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(max_id_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT end FROM messages_holes WHERE uid = %d AND end <= %d ORDER BY end DESC LIMIT 1", objArr), new Object[0]);
                            if (cursor.next()) {
                                holeMessageMinId = (long) cursor.intValue(0);
                                if (channelId != 0) {
                                    holeMessageMinId |= ((long) channelId) << 32;
                                }
                            }
                            cursor.dispose();
                            if (holeMessageMaxId == 0 && holeMessageMinId == 1) {
                                access$000 = MessagesStorage.this.database;
                                objArr = new Object[6];
                                objArr[0] = Long.valueOf(this.val$dialog_id);
                                objArr[1] = Long.valueOf(messageMaxId);
                                objArr[2] = Integer.valueOf(count_query / 2);
                                objArr[3] = Long.valueOf(this.val$dialog_id);
                                objArr[4] = Long.valueOf(messageMaxId);
                                objArr[5] = Integer.valueOf(count_query / 2);
                                cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.date ASC, m.mid ASC LIMIT %d)", objArr), new Object[0]);
                            } else {
                                if (holeMessageMaxId == 0) {
                                    holeMessageMaxId = 1000000000;
                                    if (channelId != 0) {
                                        holeMessageMaxId = 1000000000 | (((long) channelId) << 32);
                                    }
                                }
                                access$000 = MessagesStorage.this.database;
                                objArr = new Object[8];
                                objArr[0] = Long.valueOf(this.val$dialog_id);
                                objArr[1] = Long.valueOf(messageMaxId);
                                objArr[2] = Long.valueOf(holeMessageMinId);
                                objArr[3] = Integer.valueOf(count_query / 2);
                                objArr[4] = Long.valueOf(this.val$dialog_id);
                                objArr[5] = Long.valueOf(messageMaxId);
                                objArr[6] = Long.valueOf(holeMessageMaxId);
                                objArr[7] = Integer.valueOf(count_query / 2);
                                cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d AND m.mid >= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d AND m.mid <= %d ORDER BY m.date ASC, m.mid ASC LIMIT %d)", objArr), new Object[0]);
                            }
                        } else {
                            cursor = null;
                        }
                    } else if (this.val$load_type == 1) {
                        holeMessageId = 0;
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[2];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(this.val$max_id);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT start, end FROM messages_holes WHERE uid = %d AND start >= %d AND start != 1 AND end != 1 ORDER BY start ASC LIMIT 1", objArr), new Object[0]);
                        if (cursor.next()) {
                            holeMessageId = (long) cursor.intValue(0);
                            if (channelId != 0) {
                                holeMessageId |= ((long) channelId) << 32;
                            }
                        }
                        cursor.dispose();
                        if (holeMessageId != 0) {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[5];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(this.val$minDate);
                            objArr[2] = Long.valueOf(messageMaxId);
                            objArr[3] = Long.valueOf(holeMessageId);
                            objArr[4] = Integer.valueOf(count_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date >= %d AND m.mid > %d AND m.mid <= %d ORDER BY m.date ASC, m.mid ASC LIMIT %d", objArr), new Object[0]);
                        } else {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[4];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(this.val$minDate);
                            objArr[2] = Long.valueOf(messageMaxId);
                            objArr[3] = Integer.valueOf(count_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date >= %d AND m.mid > %d ORDER BY m.date ASC, m.mid ASC LIMIT %d", objArr), new Object[0]);
                        }
                    } else if (this.val$minDate == 0) {
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[1];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT max(mid) FROM messages WHERE uid = %d AND mid > 0", objArr), new Object[0]);
                        if (cursor.next()) {
                            last_message_id = cursor.intValue(0);
                        }
                        cursor.dispose();
                        holeMessageId = 0;
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[1];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT max(end) FROM messages_holes WHERE uid = %d", objArr), new Object[0]);
                        if (cursor.next()) {
                            holeMessageId = (long) cursor.intValue(0);
                            if (channelId != 0) {
                                holeMessageId |= ((long) channelId) << 32;
                            }
                        }
                        cursor.dispose();
                        if (holeMessageId != 0) {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[4];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Long.valueOf(holeMessageId);
                            objArr[2] = Integer.valueOf(offset_query);
                            objArr[3] = Integer.valueOf(count_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND (m.mid >= %d OR m.mid < 0) ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d", objArr), new Object[0]);
                        } else {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[3];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(offset_query);
                            objArr[2] = Integer.valueOf(count_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d", objArr), new Object[0]);
                        }
                    } else if (messageMaxId != 0) {
                        holeMessageId = 0;
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[2];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(this.val$max_id);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT end FROM messages_holes WHERE uid = %d AND end <= %d ORDER BY end DESC LIMIT 1", objArr), new Object[0]);
                        if (cursor.next()) {
                            holeMessageId = (long) cursor.intValue(0);
                            if (channelId != 0) {
                                holeMessageId |= ((long) channelId) << 32;
                            }
                        }
                        cursor.dispose();
                        if (holeMessageId != 0) {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[5];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(this.val$minDate);
                            objArr[2] = Long.valueOf(messageMaxId);
                            objArr[3] = Long.valueOf(holeMessageId);
                            objArr[4] = Integer.valueOf(count_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d AND m.mid < %d AND (m.mid >= %d OR m.mid < 0) ORDER BY m.date DESC, m.mid DESC LIMIT %d", objArr), new Object[0]);
                        } else {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[4];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            objArr[1] = Integer.valueOf(this.val$minDate);
                            objArr[2] = Long.valueOf(messageMaxId);
                            objArr[3] = Integer.valueOf(count_query);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d AND m.mid < %d ORDER BY m.date DESC, m.mid DESC LIMIT %d", objArr), new Object[0]);
                        }
                    } else {
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[4];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(this.val$minDate);
                        objArr[2] = Integer.valueOf(offset_query);
                        objArr[3] = Integer.valueOf(count_query);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d", objArr), new Object[0]);
                    }
                } else {
                    isEnd = true;
                    if (this.val$load_type == 1) {
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[3];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(this.val$max_id);
                        objArr[2] = Integer.valueOf(count_query);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid < %d ORDER BY m.mid DESC LIMIT %d", objArr), new Object[0]);
                    } else if (this.val$minDate == 0) {
                        if (this.val$load_type == 2) {
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[1];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND mid < 0", objArr), new Object[0]);
                            if (cursor.next()) {
                                last_message_id = cursor.intValue(0);
                            }
                            cursor.dispose();
                            access$000 = MessagesStorage.this.database;
                            objArr = new Object[1];
                            objArr[0] = Long.valueOf(this.val$dialog_id);
                            cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT max(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid < 0", objArr), new Object[0]);
                            if (cursor.next()) {
                                min_unread_id = cursor.intValue(0);
                                max_unread_date = cursor.intValue(1);
                            }
                            cursor.dispose();
                            if (min_unread_id != 0) {
                                access$000 = MessagesStorage.this.database;
                                objArr = new Object[2];
                                objArr[0] = Long.valueOf(this.val$dialog_id);
                                objArr[1] = Integer.valueOf(min_unread_id);
                                cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid <= %d AND out = 0 AND read_state IN(0,2)", objArr), new Object[0]);
                                if (cursor.next()) {
                                    count_unread = cursor.intValue(0);
                                }
                                cursor.dispose();
                            }
                        }
                        if (count_query > count_unread || count_unread < num) {
                            count_query = Math.max(count_query, count_unread + 10);
                            if (count_unread < num) {
                                count_unread = 0;
                                min_unread_id = 0;
                                last_message_id = 0;
                            }
                        } else {
                            offset_query = count_unread - count_query;
                            count_query += 10;
                        }
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[3];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(offset_query);
                        objArr[2] = Integer.valueOf(count_query);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d ORDER BY m.mid ASC LIMIT %d,%d", objArr), new Object[0]);
                    } else if (this.val$max_id != 0) {
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[3];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(this.val$max_id);
                        objArr[2] = Integer.valueOf(count_query);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.mid ASC LIMIT %d", objArr), new Object[0]);
                    } else {
                        access$000 = MessagesStorage.this.database;
                        objArr = new Object[4];
                        objArr[0] = Long.valueOf(this.val$dialog_id);
                        objArr[1] = Integer.valueOf(this.val$minDate);
                        objArr[2] = Integer.valueOf(0);
                        objArr[3] = Integer.valueOf(count_query);
                        cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d ORDER BY m.mid ASC LIMIT %d,%d", objArr), new Object[0]);
                    }
                }
                if (cursor != null) {
                    while (cursor.next()) {
                        data = cursor.byteBufferValue(1);
                        if (data != null) {
                            message = Message.TLdeserialize(data, data.readInt32(false), false);
                            data.reuse();
                            MessageObject.setUnreadFlags(message, cursor.intValue(0));
                            message.id = cursor.intValue(3);
                            message.date = cursor.intValue(4);
                            message.dialog_id = this.val$dialog_id;
                            if ((message.flags & MessagesController.UPDATE_MASK_PHONE) != 0) {
                                message.views = cursor.intValue(7);
                            }
                            if (lower_id != 0) {
                                message.ttl = cursor.intValue(8);
                            }
                            res.messages.add(message);
                            MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                            if (!(message.reply_to_msg_id == 0 && message.reply_to_random_id == 0)) {
                                if (!cursor.isNull(6)) {
                                    data = cursor.byteBufferValue(6);
                                    if (data != null) {
                                        message.replyMessage = Message.TLdeserialize(data, data.readInt32(false), false);
                                        data.reuse();
                                        if (message.replyMessage != null) {
                                            MessagesStorage.addUsersAndChatsFromMessage(message.replyMessage, usersToLoad, chatsToLoad);
                                        }
                                    }
                                }
                                if (message.replyMessage == null) {
                                    ArrayList<Message> messages;
                                    if (message.reply_to_msg_id != 0) {
                                        long messageId = (long) message.reply_to_msg_id;
                                        if (message.to_id.channel_id != 0) {
                                            messageId |= ((long) message.to_id.channel_id) << 32;
                                        }
                                        if (!replyMessages.contains(Long.valueOf(messageId))) {
                                            replyMessages.add(Long.valueOf(messageId));
                                        }
                                        messages = (ArrayList) replyMessageOwners.get(Integer.valueOf(message.reply_to_msg_id));
                                        if (messages == null) {
                                            messages = new ArrayList();
                                            replyMessageOwners.put(Integer.valueOf(message.reply_to_msg_id), messages);
                                        }
                                        messages.add(message);
                                    } else {
                                        if (!replyMessages.contains(Long.valueOf(message.reply_to_random_id))) {
                                            replyMessages.add(Long.valueOf(message.reply_to_random_id));
                                        }
                                        messages = (ArrayList) replyMessageRandomOwners.get(Long.valueOf(message.reply_to_random_id));
                                        if (messages == null) {
                                            messages = new ArrayList();
                                            replyMessageRandomOwners.put(Long.valueOf(message.reply_to_random_id), messages);
                                        }
                                        messages.add(message);
                                    }
                                }
                            }
                            message.send_state = cursor.intValue(2);
                            if (message.id > 0 && message.send_state != 0) {
                                message.send_state = 0;
                            }
                            if (lower_id == 0 && !cursor.isNull(5)) {
                                message.random_id = cursor.longValue(5);
                            }
                            if (!(((int) this.val$dialog_id) != 0 || message.media == null || message.media.photo == null)) {
                                try {
                                    SQLiteCursor cursor2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT date FROM enc_tasks_v2 WHERE mid = %d", new Object[]{Integer.valueOf(message.id)}), new Object[0]);
                                    if (cursor2.next()) {
                                        message.destroyTime = cursor2.intValue(0);
                                    }
                                    cursor2.dispose();
                                } catch (Throwable e) {
                                    FileLog.m13e("tmessages", e);
                                }
                            }
                        }
                    }
                    cursor.dispose();
                }
                Collections.sort(res.messages, new C06531());
                if ((this.val$load_type == 3 || (this.val$load_type == 2 && queryFromServer)) && !res.messages.isEmpty()) {
                    int minId = ((Message) res.messages.get(res.messages.size() - 1)).id;
                    int maxId = ((Message) res.messages.get(0)).id;
                    if (minId > max_id_query || maxId < max_id_query) {
                        replyMessages.clear();
                        usersToLoad.clear();
                        chatsToLoad.clear();
                        res.messages.clear();
                    }
                }
                if (this.val$load_type == 3 && res.messages.size() == 1) {
                    res.messages.clear();
                }
                if (!replyMessages.isEmpty()) {
                    ArrayList<Message> arrayList;
                    int a;
                    if (replyMessageOwners.isEmpty()) {
                        cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.data, m.mid, m.date, r.random_id FROM randoms as r INNER JOIN messages as m ON r.mid = m.mid WHERE r.random_id IN(%s)", new Object[]{TextUtils.join(",", replyMessages)}), new Object[0]);
                    } else {
                        cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data, mid, date FROM messages WHERE mid IN(%s)", new Object[]{TextUtils.join(",", replyMessages)}), new Object[0]);
                    }
                    while (cursor.next()) {
                        data = cursor.byteBufferValue(0);
                        if (data != null) {
                            message = Message.TLdeserialize(data, data.readInt32(false), false);
                            data.reuse();
                            message.id = cursor.intValue(1);
                            message.date = cursor.intValue(2);
                            message.dialog_id = this.val$dialog_id;
                            MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                            if (replyMessageOwners.isEmpty()) {
                                arrayList = (ArrayList) replyMessageRandomOwners.remove(Long.valueOf(cursor.longValue(3)));
                                if (arrayList != null) {
                                    for (a = 0; a < arrayList.size(); a++) {
                                        Message object = (Message) arrayList.get(a);
                                        object.replyMessage = message;
                                        object.reply_to_msg_id = message.id;
                                    }
                                }
                            } else {
                                arrayList = (ArrayList) replyMessageOwners.get(Integer.valueOf(message.id));
                                if (arrayList != null) {
                                    for (a = 0; a < arrayList.size(); a++) {
                                        ((Message) arrayList.get(a)).replyMessage = message;
                                    }
                                }
                            }
                        }
                    }
                    cursor.dispose();
                    if (!replyMessageRandomOwners.isEmpty()) {
                        for (Entry<Long, ArrayList<Message>> entry : replyMessageRandomOwners.entrySet()) {
                            arrayList = (ArrayList) entry.getValue();
                            for (a = 0; a < arrayList.size(); a++) {
                                ((Message) arrayList.get(a)).reply_to_random_id = 0;
                            }
                        }
                    }
                }
                if (!usersToLoad.isEmpty()) {
                    MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), res.users);
                }
                if (!chatsToLoad.isEmpty()) {
                    MessagesStorage.this.getChatsInternal(TextUtils.join(",", chatsToLoad), res.chats);
                }
                MessagesController.getInstance().processLoadedMessages(res, this.val$dialog_id, count_query, this.val$max_id, true, this.val$classGuid, min_unread_id, last_message_id, count_unread, max_unread_date, this.val$load_type, this.val$isChannel, isEnd, this.val$loadIndex, queryFromServer);
            } catch (Throwable e2) {
                res.messages.clear();
                res.chats.clear();
                res.users.clear();
                FileLog.m13e("tmessages", e2);
                MessagesController.getInstance().processLoadedMessages(res, this.val$dialog_id, count_query, this.val$max_id, true, this.val$classGuid, min_unread_id, last_message_id, count_unread, max_unread_date, this.val$load_type, this.val$isChannel, isEnd, this.val$loadIndex, queryFromServer);
            } catch (Throwable th) {
                Throwable th2 = th;
                MessagesController.getInstance().processLoadedMessages(res, this.val$dialog_id, count_query, this.val$max_id, true, this.val$classGuid, min_unread_id, last_message_id, count_unread, max_unread_date, this.val$load_type, this.val$isChannel, isEnd, this.val$loadIndex, queryFromServer);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.49 */
    class AnonymousClass49 implements Runnable {
        final /* synthetic */ String val$path;
        final /* synthetic */ ArrayList val$result;
        final /* synthetic */ Semaphore val$semaphore;
        final /* synthetic */ int val$type;

        AnonymousClass49(String str, int i, ArrayList arrayList, Semaphore semaphore) {
            this.val$path = str;
            this.val$type = i;
            this.val$result = arrayList;
            this.val$semaphore = semaphore;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r11 = this;
            r5 = r11.val$path;	 Catch:{ Exception -> 0x006a }
            r4 = org.telegram.messenger.Utilities.MD5(r5);	 Catch:{ Exception -> 0x006a }
            if (r4 == 0) goto L_0x0056;
        L_0x0008:
            r5 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x006a }
            r5 = r5.database;	 Catch:{ Exception -> 0x006a }
            r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x006a }
            r7 = "SELECT data FROM sent_files_v2 WHERE uid = '%s' AND type = %d";
            r8 = 2;
            r8 = new java.lang.Object[r8];	 Catch:{ Exception -> 0x006a }
            r9 = 0;
            r8[r9] = r4;	 Catch:{ Exception -> 0x006a }
            r9 = 1;
            r10 = r11.val$type;	 Catch:{ Exception -> 0x006a }
            r10 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x006a }
            r8[r9] = r10;	 Catch:{ Exception -> 0x006a }
            r6 = java.lang.String.format(r6, r7, r8);	 Catch:{ Exception -> 0x006a }
            r7 = 0;
            r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x006a }
            r0 = r5.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x006a }
            r5 = r0.next();	 Catch:{ Exception -> 0x006a }
            if (r5 == 0) goto L_0x0053;
        L_0x0032:
            r5 = 0;
            r1 = r0.byteBufferValue(r5);	 Catch:{ Exception -> 0x006a }
            if (r1 == 0) goto L_0x0053;
        L_0x0039:
            r5 = 0;
            r5 = r1.readInt32(r5);	 Catch:{ Exception -> 0x006a }
            r6 = 0;
            r3 = org.telegram.tgnet.TLRPC.MessageMedia.TLdeserialize(r1, r5, r6);	 Catch:{ Exception -> 0x006a }
            r1.reuse();	 Catch:{ Exception -> 0x006a }
            r5 = r3 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaDocument;	 Catch:{ Exception -> 0x006a }
            if (r5 == 0) goto L_0x005c;
        L_0x004a:
            r5 = r11.val$result;	 Catch:{ Exception -> 0x006a }
            r3 = (org.telegram.tgnet.TLRPC.TL_messageMediaDocument) r3;	 Catch:{ Exception -> 0x006a }
            r6 = r3.document;	 Catch:{ Exception -> 0x006a }
            r5.add(r6);	 Catch:{ Exception -> 0x006a }
        L_0x0053:
            r0.dispose();	 Catch:{ Exception -> 0x006a }
        L_0x0056:
            r5 = r11.val$semaphore;
            r5.release();
        L_0x005b:
            return;
        L_0x005c:
            r5 = r3 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;	 Catch:{ Exception -> 0x006a }
            if (r5 == 0) goto L_0x0053;
        L_0x0060:
            r5 = r11.val$result;	 Catch:{ Exception -> 0x006a }
            r3 = (org.telegram.tgnet.TLRPC.TL_messageMediaPhoto) r3;	 Catch:{ Exception -> 0x006a }
            r6 = r3.photo;	 Catch:{ Exception -> 0x006a }
            r5.add(r6);	 Catch:{ Exception -> 0x006a }
            goto L_0x0053;
        L_0x006a:
            r2 = move-exception;
            r5 = "tmessages";
            org.telegram.messenger.FileLog.m13e(r5, r2);	 Catch:{ all -> 0x0076 }
            r5 = r11.val$semaphore;
            r5.release();
            goto L_0x005b;
        L_0x0076:
            r5 = move-exception;
            r6 = r11.val$semaphore;
            r6.release();
            throw r5;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.49.run():void");
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.4 */
    class C06544 implements Runnable {
        final /* synthetic */ NativeByteBuffer val$data;
        final /* synthetic */ long val$id;

        C06544(long j, NativeByteBuffer nativeByteBuffer) {
            this.val$id = j;
            this.val$data = nativeByteBuffer;
        }

        public void run() {
            try {
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO pending_tasks VALUES(?, ?)");
                state.bindLong(1, this.val$id);
                state.bindByteBuffer(2, this.val$data);
                state.step();
                state.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            } finally {
                this.val$data.reuse();
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.50 */
    class AnonymousClass50 implements Runnable {
        final /* synthetic */ TLObject val$file;
        final /* synthetic */ String val$path;
        final /* synthetic */ int val$type;

        AnonymousClass50(String str, TLObject tLObject, int i) {
            this.val$path = str;
            this.val$file = tLObject;
            this.val$type = i;
        }

        public void run() {
            SQLitePreparedStatement state = null;
            try {
                String id = Utilities.MD5(this.val$path);
                if (id != null) {
                    MessageMedia messageMedia = null;
                    if (this.val$file instanceof Photo) {
                        messageMedia = new TL_messageMediaPhoto();
                        messageMedia.caption = TtmlNode.ANONYMOUS_REGION_ID;
                        messageMedia.photo = (Photo) this.val$file;
                    } else if (this.val$file instanceof Document) {
                        messageMedia = new TL_messageMediaDocument();
                        messageMedia.caption = TtmlNode.ANONYMOUS_REGION_ID;
                        messageMedia.document = (Document) this.val$file;
                    }
                    if (messageMedia != null) {
                        state = MessagesStorage.this.database.executeFast("REPLACE INTO sent_files_v2 VALUES(?, ?, ?)");
                        state.requery();
                        NativeByteBuffer data = new NativeByteBuffer(messageMedia.getObjectSize());
                        messageMedia.serializeToStream(data);
                        state.bindString(1, id);
                        state.bindInteger(2, this.val$type);
                        state.bindByteBuffer(3, data);
                        state.step();
                        data.reuse();
                    } else if (state != null) {
                        state.dispose();
                        return;
                    } else {
                        return;
                    }
                }
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th) {
                if (state != null) {
                    state.dispose();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.51 */
    class AnonymousClass51 implements Runnable {
        final /* synthetic */ EncryptedChat val$chat;

        AnonymousClass51(EncryptedChat encryptedChat) {
            this.val$chat = encryptedChat;
        }

        public void run() {
            SQLitePreparedStatement state = null;
            try {
                state = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET seq_in = ?, seq_out = ?, use_count = ? WHERE uid = ?");
                state.bindInteger(1, this.val$chat.seq_in);
                state.bindInteger(2, this.val$chat.seq_out);
                state.bindInteger(3, (this.val$chat.key_use_count_in << 16) | this.val$chat.key_use_count_out);
                state.bindInteger(4, this.val$chat.id);
                state.step();
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th) {
                if (state != null) {
                    state.dispose();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.52 */
    class AnonymousClass52 implements Runnable {
        final /* synthetic */ EncryptedChat val$chat;

        AnonymousClass52(EncryptedChat encryptedChat) {
            this.val$chat = encryptedChat;
        }

        public void run() {
            SQLitePreparedStatement state = null;
            try {
                state = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET ttl = ? WHERE uid = ?");
                state.bindInteger(1, this.val$chat.ttl);
                state.bindInteger(2, this.val$chat.id);
                state.step();
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th) {
                if (state != null) {
                    state.dispose();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.53 */
    class AnonymousClass53 implements Runnable {
        final /* synthetic */ EncryptedChat val$chat;

        AnonymousClass53(EncryptedChat encryptedChat) {
            this.val$chat = encryptedChat;
        }

        public void run() {
            SQLitePreparedStatement state = null;
            try {
                state = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET layer = ? WHERE uid = ?");
                state.bindInteger(1, this.val$chat.layer);
                state.bindInteger(2, this.val$chat.id);
                state.step();
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th) {
                if (state != null) {
                    state.dispose();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.54 */
    class AnonymousClass54 implements Runnable {
        final /* synthetic */ EncryptedChat val$chat;

        AnonymousClass54(EncryptedChat encryptedChat) {
            this.val$chat = encryptedChat;
        }

        public void run() {
            int i = 1;
            SQLitePreparedStatement state = null;
            try {
                int length;
                if ((this.val$chat.key_hash == null || this.val$chat.key_hash.length < 16) && this.val$chat.auth_key != null) {
                    this.val$chat.key_hash = AndroidUtilities.calcAuthKeyHash(this.val$chat.auth_key);
                }
                state = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET data = ?, g = ?, authkey = ?, ttl = ?, layer = ?, seq_in = ?, seq_out = ?, use_count = ?, exchange_id = ?, key_date = ?, fprint = ?, fauthkey = ?, khash = ? WHERE uid = ?");
                NativeByteBuffer data = new NativeByteBuffer(this.val$chat.getObjectSize());
                if (this.val$chat.a_or_b != null) {
                    length = this.val$chat.a_or_b.length;
                } else {
                    length = 1;
                }
                NativeByteBuffer data2 = new NativeByteBuffer(length);
                if (this.val$chat.auth_key != null) {
                    length = this.val$chat.auth_key.length;
                } else {
                    length = 1;
                }
                NativeByteBuffer data3 = new NativeByteBuffer(length);
                if (this.val$chat.future_auth_key != null) {
                    length = this.val$chat.future_auth_key.length;
                } else {
                    length = 1;
                }
                NativeByteBuffer data4 = new NativeByteBuffer(length);
                if (this.val$chat.key_hash != null) {
                    i = this.val$chat.key_hash.length;
                }
                NativeByteBuffer data5 = new NativeByteBuffer(i);
                this.val$chat.serializeToStream(data);
                state.bindByteBuffer(1, data);
                if (this.val$chat.a_or_b != null) {
                    data2.writeBytes(this.val$chat.a_or_b);
                }
                if (this.val$chat.auth_key != null) {
                    data3.writeBytes(this.val$chat.auth_key);
                }
                if (this.val$chat.future_auth_key != null) {
                    data4.writeBytes(this.val$chat.future_auth_key);
                }
                if (this.val$chat.key_hash != null) {
                    data5.writeBytes(this.val$chat.key_hash);
                }
                state.bindByteBuffer(2, data2);
                state.bindByteBuffer(3, data3);
                state.bindInteger(4, this.val$chat.ttl);
                state.bindInteger(5, this.val$chat.layer);
                state.bindInteger(6, this.val$chat.seq_in);
                state.bindInteger(7, this.val$chat.seq_out);
                state.bindInteger(8, (this.val$chat.key_use_count_in << 16) | this.val$chat.key_use_count_out);
                state.bindLong(9, this.val$chat.exchange_id);
                state.bindInteger(10, this.val$chat.key_create_date);
                state.bindLong(11, this.val$chat.future_key_fingerprint);
                state.bindByteBuffer(12, data4);
                state.bindByteBuffer(13, data5);
                state.bindInteger(14, this.val$chat.id);
                state.step();
                data.reuse();
                data2.reuse();
                data3.reuse();
                data4.reuse();
                data5.reuse();
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th) {
                if (state != null) {
                    state.dispose();
                }
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.55 */
    class AnonymousClass55 implements Runnable {
        final /* synthetic */ long val$did;
        final /* synthetic */ boolean[] val$result;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass55(long j, boolean[] zArr, Semaphore semaphore) {
            this.val$did = j;
            this.val$result = zArr;
            this.val$semaphore = semaphore;
        }

        public void run() {
            try {
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = %d LIMIT 1", new Object[]{Long.valueOf(this.val$did)}), new Object[0]);
                this.val$result[0] = cursor.next();
                cursor.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            } finally {
                this.val$semaphore.release();
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.56 */
    class AnonymousClass56 implements Runnable {
        final /* synthetic */ int val$date;
        final /* synthetic */ boolean[] val$result;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass56(int i, boolean[] zArr, Semaphore semaphore) {
            this.val$date = i;
            this.val$result = zArr;
            this.val$semaphore = semaphore;
        }

        public void run() {
            try {
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = 777000 AND date = %d AND mid < 0 LIMIT 1", new Object[]{Integer.valueOf(this.val$date)}), new Object[0]);
                this.val$result[0] = cursor.next();
                cursor.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            } finally {
                this.val$semaphore.release();
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.57 */
    class AnonymousClass57 implements Runnable {
        final /* synthetic */ int val$chat_id;
        final /* synthetic */ ArrayList val$result;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass57(int i, ArrayList arrayList, Semaphore semaphore) {
            this.val$chat_id = i;
            this.val$result = arrayList;
            this.val$semaphore = semaphore;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r7 = this;
            r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0060 }
            r3.<init>();	 Catch:{ Exception -> 0x0060 }
            r1 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0060 }
            r1.<init>();	 Catch:{ Exception -> 0x0060 }
            r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0060 }
            r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0060 }
            r5.<init>();	 Catch:{ Exception -> 0x0060 }
            r6 = "";
            r5 = r5.append(r6);	 Catch:{ Exception -> 0x0060 }
            r6 = r7.val$chat_id;	 Catch:{ Exception -> 0x0060 }
            r5 = r5.append(r6);	 Catch:{ Exception -> 0x0060 }
            r5 = r5.toString();	 Catch:{ Exception -> 0x0060 }
            r4.getEncryptedChatsInternal(r5, r1, r3);	 Catch:{ Exception -> 0x0060 }
            r4 = r1.isEmpty();	 Catch:{ Exception -> 0x0060 }
            if (r4 != 0) goto L_0x005a;
        L_0x002a:
            r4 = r3.isEmpty();	 Catch:{ Exception -> 0x0060 }
            if (r4 != 0) goto L_0x005a;
        L_0x0030:
            r2 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0060 }
            r2.<init>();	 Catch:{ Exception -> 0x0060 }
            r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0060 }
            r5 = ",";
            r5 = android.text.TextUtils.join(r5, r3);	 Catch:{ Exception -> 0x0060 }
            r4.getUsersInternal(r5, r2);	 Catch:{ Exception -> 0x0060 }
            r4 = r2.isEmpty();	 Catch:{ Exception -> 0x0060 }
            if (r4 != 0) goto L_0x005a;
        L_0x0046:
            r4 = r7.val$result;	 Catch:{ Exception -> 0x0060 }
            r5 = 0;
            r5 = r1.get(r5);	 Catch:{ Exception -> 0x0060 }
            r4.add(r5);	 Catch:{ Exception -> 0x0060 }
            r4 = r7.val$result;	 Catch:{ Exception -> 0x0060 }
            r5 = 0;
            r5 = r2.get(r5);	 Catch:{ Exception -> 0x0060 }
            r4.add(r5);	 Catch:{ Exception -> 0x0060 }
        L_0x005a:
            r4 = r7.val$semaphore;
            r4.release();
        L_0x005f:
            return;
        L_0x0060:
            r0 = move-exception;
            r4 = "tmessages";
            org.telegram.messenger.FileLog.m13e(r4, r0);	 Catch:{ all -> 0x006c }
            r4 = r7.val$semaphore;
            r4.release();
            goto L_0x005f;
        L_0x006c:
            r4 = move-exception;
            r5 = r7.val$semaphore;
            r5.release();
            throw r4;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.57.run():void");
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.58 */
    class AnonymousClass58 implements Runnable {
        final /* synthetic */ EncryptedChat val$chat;
        final /* synthetic */ TL_dialog val$dialog;
        final /* synthetic */ User val$user;

        AnonymousClass58(EncryptedChat encryptedChat, User user, TL_dialog tL_dialog) {
            this.val$chat = encryptedChat;
            this.val$user = user;
            this.val$dialog = tL_dialog;
        }

        public void run() {
            int i = 1;
            try {
                int length;
                if ((this.val$chat.key_hash == null || this.val$chat.key_hash.length < 16) && this.val$chat.auth_key != null) {
                    this.val$chat.key_hash = AndroidUtilities.calcAuthKeyHash(this.val$chat.auth_key);
                }
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO enc_chats VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                NativeByteBuffer data = new NativeByteBuffer(this.val$chat.getObjectSize());
                if (this.val$chat.a_or_b != null) {
                    length = this.val$chat.a_or_b.length;
                } else {
                    length = 1;
                }
                NativeByteBuffer data2 = new NativeByteBuffer(length);
                if (this.val$chat.auth_key != null) {
                    length = this.val$chat.auth_key.length;
                } else {
                    length = 1;
                }
                NativeByteBuffer data3 = new NativeByteBuffer(length);
                if (this.val$chat.future_auth_key != null) {
                    length = this.val$chat.future_auth_key.length;
                } else {
                    length = 1;
                }
                NativeByteBuffer data4 = new NativeByteBuffer(length);
                if (this.val$chat.key_hash != null) {
                    i = this.val$chat.key_hash.length;
                }
                NativeByteBuffer data5 = new NativeByteBuffer(i);
                this.val$chat.serializeToStream(data);
                state.bindInteger(1, this.val$chat.id);
                state.bindInteger(2, this.val$user.id);
                state.bindString(3, MessagesStorage.this.formatUserSearchName(this.val$user));
                state.bindByteBuffer(4, data);
                if (this.val$chat.a_or_b != null) {
                    data2.writeBytes(this.val$chat.a_or_b);
                }
                if (this.val$chat.auth_key != null) {
                    data3.writeBytes(this.val$chat.auth_key);
                }
                if (this.val$chat.future_auth_key != null) {
                    data4.writeBytes(this.val$chat.future_auth_key);
                }
                if (this.val$chat.key_hash != null) {
                    data5.writeBytes(this.val$chat.key_hash);
                }
                state.bindByteBuffer(5, data2);
                state.bindByteBuffer(6, data3);
                state.bindInteger(7, this.val$chat.ttl);
                state.bindInteger(8, this.val$chat.layer);
                state.bindInteger(9, this.val$chat.seq_in);
                state.bindInteger(10, this.val$chat.seq_out);
                state.bindInteger(11, (this.val$chat.key_use_count_in << 16) | this.val$chat.key_use_count_out);
                state.bindLong(12, this.val$chat.exchange_id);
                state.bindInteger(13, this.val$chat.key_create_date);
                state.bindLong(14, this.val$chat.future_key_fingerprint);
                state.bindByteBuffer(15, data4);
                state.bindByteBuffer(16, data5);
                state.step();
                state.dispose();
                data.reuse();
                data2.reuse();
                data3.reuse();
                data4.reuse();
                data5.reuse();
                if (this.val$dialog != null) {
                    state = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    state.bindLong(1, this.val$dialog.id);
                    state.bindInteger(2, this.val$dialog.last_message_date);
                    state.bindInteger(3, this.val$dialog.unread_count);
                    state.bindInteger(4, this.val$dialog.top_message);
                    state.bindInteger(5, this.val$dialog.read_inbox_max_id);
                    state.bindInteger(6, this.val$dialog.read_outbox_max_id);
                    state.bindInteger(7, 0);
                    state.bindInteger(8, 0);
                    state.bindInteger(9, this.val$dialog.pts);
                    state.bindInteger(10, 0);
                    state.step();
                    state.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.59 */
    class AnonymousClass59 implements Runnable {
        final /* synthetic */ ArrayList val$chats;
        final /* synthetic */ ArrayList val$users;
        final /* synthetic */ boolean val$withTransaction;

        AnonymousClass59(ArrayList arrayList, ArrayList arrayList2, boolean z) {
            this.val$users = arrayList;
            this.val$chats = arrayList2;
            this.val$withTransaction = z;
        }

        public void run() {
            MessagesStorage.this.putUsersAndChatsInternal(this.val$users, this.val$chats, this.val$withTransaction);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.5 */
    class C06555 implements Runnable {
        final /* synthetic */ long val$id;

        C06555(long j) {
            this.val$id = j;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast("DELETE FROM pending_tasks WHERE id = " + this.val$id).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.60 */
    class AnonymousClass60 implements Runnable {
        final /* synthetic */ long val$id;
        final /* synthetic */ boolean val$move;
        final /* synthetic */ int val$type;

        AnonymousClass60(boolean z, int i, long j) {
            this.val$move = z;
            this.val$type = i;
            this.val$id = j;
        }

        public void run() {
            try {
                if (this.val$move) {
                    int minDate = -1;
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(date) FROM download_queue WHERE type = %d", new Object[]{Integer.valueOf(this.val$type)}), new Object[0]);
                    if (cursor.next()) {
                        minDate = cursor.intValue(0);
                    }
                    cursor.dispose();
                    if (minDate != -1) {
                        MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE download_queue SET date = %d WHERE uid = %d AND type = %d", new Object[]{Integer.valueOf(minDate - 1), Long.valueOf(this.val$id), Integer.valueOf(this.val$type)})).stepThis().dispose();
                        return;
                    }
                    return;
                }
                MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM download_queue WHERE uid = %d AND type = %d", new Object[]{Long.valueOf(this.val$id), Integer.valueOf(this.val$type)})).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.61 */
    class AnonymousClass61 implements Runnable {
        final /* synthetic */ int val$type;

        AnonymousClass61(int i) {
            this.val$type = i;
        }

        public void run() {
            try {
                if (this.val$type == 0) {
                    MessagesStorage.this.database.executeFast("DELETE FROM download_queue WHERE 1").stepThis().dispose();
                    return;
                }
                MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM download_queue WHERE type = %d", new Object[]{Integer.valueOf(this.val$type)})).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.62 */
    class AnonymousClass62 implements Runnable {
        final /* synthetic */ int val$type;

        /* renamed from: org.telegram.messenger.MessagesStorage.62.1 */
        class C06591 implements Runnable {
            final /* synthetic */ ArrayList val$objects;

            C06591(ArrayList arrayList) {
                this.val$objects = arrayList;
            }

            public void run() {
                MediaController.getInstance().processDownloadObjects(AnonymousClass62.this.val$type, this.val$objects);
            }
        }

        AnonymousClass62(int i) {
            this.val$type = i;
        }

        public void run() {
            try {
                ArrayList<DownloadObject> objects = new ArrayList();
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT uid, type, data FROM download_queue WHERE type = %d ORDER BY date DESC LIMIT 3", new Object[]{Integer.valueOf(this.val$type)}), new Object[0]);
                while (cursor.next()) {
                    DownloadObject downloadObject = new DownloadObject();
                    downloadObject.type = cursor.intValue(1);
                    downloadObject.id = cursor.longValue(0);
                    NativeByteBuffer data = cursor.byteBufferValue(2);
                    if (data != null) {
                        MessageMedia messageMedia = MessageMedia.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (messageMedia.document != null) {
                            downloadObject.object = messageMedia.document;
                        } else if (messageMedia.photo != null) {
                            downloadObject.object = FileLoader.getClosestPhotoSizeWithSize(messageMedia.photo.sizes, AndroidUtilities.getPhotoSize());
                        }
                    }
                    objects.add(downloadObject);
                }
                cursor.dispose();
                AndroidUtilities.runOnUIThread(new C06591(objects));
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.63 */
    class AnonymousClass63 implements Runnable {
        final /* synthetic */ HashMap val$webPages;

        /* renamed from: org.telegram.messenger.MessagesStorage.63.1 */
        class C06601 implements Runnable {
            final /* synthetic */ ArrayList val$messages;

            C06601(ArrayList arrayList) {
                this.val$messages = arrayList;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReceivedWebpages, this.val$messages);
            }
        }

        AnonymousClass63(HashMap hashMap) {
            this.val$webPages = hashMap;
        }

        public void run() {
            try {
                String ids = TextUtils.join(",", this.val$webPages.keySet());
                Object[] objArr = new Object[0];
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM webpage_pending WHERE id IN (%s)", new Object[]{ids}), r0);
                ArrayList<Long> mids = new ArrayList();
                while (cursor.next()) {
                    mids.add(Long.valueOf(cursor.longValue(0)));
                }
                cursor.dispose();
                if (!mids.isEmpty()) {
                    NativeByteBuffer data;
                    Message message;
                    ArrayList<Message> messages = new ArrayList();
                    SQLiteDatabase access$000 = MessagesStorage.this.database;
                    Object[] objArr2 = new Object[1];
                    objArr2[0] = TextUtils.join(",", mids);
                    cursor = access$000.queryFinalized(String.format(Locale.US, "SELECT mid, data FROM messages WHERE mid IN (%s)", objArr2), new Object[0]);
                    while (cursor.next()) {
                        int mid = cursor.intValue(0);
                        data = cursor.byteBufferValue(1);
                        if (data != null) {
                            message = Message.TLdeserialize(data, data.readInt32(false), false);
                            data.reuse();
                            if (message.media instanceof TL_messageMediaWebPage) {
                                message.id = mid;
                                message.media.webpage = (WebPage) this.val$webPages.get(Long.valueOf(message.media.webpage.id));
                                messages.add(message);
                            }
                        }
                    }
                    cursor.dispose();
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM webpage_pending WHERE id IN (%s)", new Object[]{ids})).stepThis().dispose();
                    if (!messages.isEmpty()) {
                        MessagesStorage.this.database.beginTransaction();
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE messages SET data = ? WHERE mid = ?");
                        SQLitePreparedStatement state2 = MessagesStorage.this.database.executeFast("UPDATE media_v2 SET data = ? WHERE mid = ?");
                        for (int a = 0; a < messages.size(); a++) {
                            message = (Message) messages.get(a);
                            data = new NativeByteBuffer(message.getObjectSize());
                            message.serializeToStream(data);
                            long messageId = (long) message.id;
                            if (message.to_id.channel_id != 0) {
                                messageId |= ((long) message.to_id.channel_id) << 32;
                            }
                            state.requery();
                            state.bindByteBuffer(1, data);
                            state.bindLong(2, messageId);
                            state.step();
                            state2.requery();
                            state2.bindByteBuffer(1, data);
                            state2.bindLong(2, messageId);
                            state2.step();
                            data.reuse();
                        }
                        state.dispose();
                        state2.dispose();
                        MessagesStorage.this.database.commitTransaction();
                        AndroidUtilities.runOnUIThread(new C06601(messages));
                    }
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.64 */
    class AnonymousClass64 implements Runnable {
        final /* synthetic */ int val$channel_id;
        final /* synthetic */ TL_updates_channelDifferenceTooLong val$difference;
        final /* synthetic */ int val$newDialogType;

        /* renamed from: org.telegram.messenger.MessagesStorage.64.1 */
        class C06611 implements Runnable {
            final /* synthetic */ long val$did;

            C06611(long j) {
                this.val$did = j;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.removeAllMessagesFromDialog, Long.valueOf(this.val$did), Boolean.valueOf(true));
            }
        }

        AnonymousClass64(int i, int i2, TL_updates_channelDifferenceTooLong tL_updates_channelDifferenceTooLong) {
            this.val$channel_id = i;
            this.val$newDialogType = i2;
            this.val$difference = tL_updates_channelDifferenceTooLong;
        }

        public void run() {
            boolean checkInvite = false;
            try {
                long did = (long) (-this.val$channel_id);
                if (this.val$newDialogType != 0) {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT pts FROM dialogs WHERE did = " + did, new Object[0]);
                    if (!cursor.next()) {
                        checkInvite = true;
                    }
                    cursor.dispose();
                }
                MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + did).stepThis().dispose();
                MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + did).stepThis().dispose();
                MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + did).stepThis().dispose();
                MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + did).stepThis().dispose();
                MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + did).stepThis().dispose();
                MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + did).stepThis().dispose();
                BotQuery.clearBotKeyboard(did, null);
                TL_messages_dialogs dialogs = new TL_messages_dialogs();
                dialogs.chats.addAll(this.val$difference.chats);
                dialogs.users.addAll(this.val$difference.users);
                dialogs.messages.addAll(this.val$difference.messages);
                TL_dialog dialog = new TL_dialog();
                dialog.id = did;
                dialog.flags = 1;
                dialog.peer = new TL_peerChannel();
                dialog.peer.channel_id = this.val$channel_id;
                dialog.top_message = this.val$difference.top_message;
                dialog.read_inbox_max_id = this.val$difference.read_inbox_max_id;
                dialog.read_outbox_max_id = this.val$difference.read_outbox_max_id;
                dialog.unread_count = this.val$difference.unread_count;
                dialog.notify_settings = null;
                dialog.pts = this.val$difference.pts;
                dialogs.dialogs.add(dialog);
                MessagesStorage.this.putDialogsInternal(dialogs);
                MessagesStorage.getInstance().updateDialogsWithDeletedMessages(new ArrayList(), false, this.val$channel_id);
                AndroidUtilities.runOnUIThread(new C06611(did));
                if (!checkInvite) {
                    return;
                }
                if (this.val$newDialogType == 1) {
                    MessagesController.getInstance().checkChannelInviter(this.val$channel_id);
                } else {
                    MessagesController.getInstance().generateJoinMessage(this.val$channel_id, false);
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.65 */
    class AnonymousClass65 implements Runnable {
        final /* synthetic */ SparseArray val$channelViews;
        final /* synthetic */ boolean val$isChannel;

        AnonymousClass65(SparseArray sparseArray, boolean z) {
            this.val$channelViews = sparseArray;
            this.val$isChannel = z;
        }

        public void run() {
            try {
                MessagesStorage.this.database.beginTransaction();
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE messages SET media = max((SELECT media FROM messages WHERE mid = ?), ?) WHERE mid = ?");
                for (int a = 0; a < this.val$channelViews.size(); a++) {
                    int peer = this.val$channelViews.keyAt(a);
                    SparseIntArray messages = (SparseIntArray) this.val$channelViews.get(peer);
                    for (int b = 0; b < messages.size(); b++) {
                        int views = messages.get(messages.keyAt(b));
                        long messageId = (long) messages.keyAt(b);
                        if (this.val$isChannel) {
                            messageId |= ((long) (-peer)) << 32;
                        }
                        state.requery();
                        state.bindLong(1, messageId);
                        state.bindInteger(2, views);
                        state.bindLong(3, messageId);
                        state.step();
                    }
                }
                state.dispose();
                MessagesStorage.this.database.commitTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.66 */
    class AnonymousClass66 implements Runnable {
        final /* synthetic */ int val$downloadMediaMaskFinal;

        AnonymousClass66(int i) {
            this.val$downloadMediaMaskFinal = i;
        }

        public void run() {
            MediaController.getInstance().newDownloadObjectsAvailable(this.val$downloadMediaMaskFinal);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.67 */
    class AnonymousClass67 implements Runnable {
        final /* synthetic */ boolean val$doNotUpdateDialogDate;
        final /* synthetic */ int val$downloadMask;
        final /* synthetic */ boolean val$ifNoLastMessage;
        final /* synthetic */ ArrayList val$messages;
        final /* synthetic */ boolean val$withTransaction;

        AnonymousClass67(ArrayList arrayList, boolean z, boolean z2, int i, boolean z3) {
            this.val$messages = arrayList;
            this.val$withTransaction = z;
            this.val$doNotUpdateDialogDate = z2;
            this.val$downloadMask = i;
            this.val$ifNoLastMessage = z3;
        }

        public void run() {
            MessagesStorage.this.putMessagesInternal(this.val$messages, this.val$withTransaction, this.val$doNotUpdateDialogDate, this.val$downloadMask, this.val$ifNoLastMessage);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.68 */
    class AnonymousClass68 implements Runnable {
        final /* synthetic */ Message val$message;

        AnonymousClass68(Message message) {
            this.val$message = message;
        }

        public void run() {
            try {
                long messageId = (long) this.val$message.id;
                if (this.val$message.to_id.channel_id != 0) {
                    messageId |= ((long) this.val$message.to_id.channel_id) << 32;
                }
                MessagesStorage.this.database.executeFast("UPDATE messages SET send_state = 2 WHERE mid = " + messageId).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.69 */
    class AnonymousClass69 implements Runnable {
        final /* synthetic */ int val$mid;
        final /* synthetic */ int val$seq_in;
        final /* synthetic */ int val$seq_out;

        AnonymousClass69(int i, int i2, int i3) {
            this.val$mid = i;
            this.val$seq_in = i2;
            this.val$seq_out = i3;
        }

        public void run() {
            try {
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO messages_seq VALUES(?, ?, ?)");
                state.requery();
                state.bindInteger(1, this.val$mid);
                state.bindInteger(2, this.val$seq_in);
                state.bindInteger(3, this.val$seq_out);
                state.step();
                state.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.6 */
    class C06626 implements Runnable {

        /* renamed from: org.telegram.messenger.MessagesStorage.6.1 */
        class C06561 implements Runnable {
            final /* synthetic */ Chat val$chat;
            final /* synthetic */ long val$taskId;

            C06561(Chat chat, long j) {
                this.val$chat = chat;
                this.val$taskId = j;
            }

            public void run() {
                MessagesController.getInstance().loadUnknownChannel(this.val$chat, this.val$taskId);
            }
        }

        /* renamed from: org.telegram.messenger.MessagesStorage.6.2 */
        class C06572 implements Runnable {
            final /* synthetic */ int val$channelId;
            final /* synthetic */ int val$newDialogType;
            final /* synthetic */ long val$taskId;

            C06572(int i, int i2, long j) {
                this.val$channelId = i;
                this.val$newDialogType = i2;
                this.val$taskId = j;
            }

            public void run() {
                MessagesController.getInstance().getChannelDifference(this.val$channelId, this.val$newDialogType, this.val$taskId);
            }
        }

        /* renamed from: org.telegram.messenger.MessagesStorage.6.3 */
        class C06583 implements Runnable {
            final /* synthetic */ TL_dialog val$dialog;
            final /* synthetic */ InputPeer val$peer;
            final /* synthetic */ long val$taskId;

            C06583(TL_dialog tL_dialog, InputPeer inputPeer, long j) {
                this.val$dialog = tL_dialog;
                this.val$peer = inputPeer;
                this.val$taskId = j;
            }

            public void run() {
                MessagesController.getInstance().checkLastDialogMessage(this.val$dialog, this.val$peer, this.val$taskId);
            }
        }

        C06626() {
        }

        public void run() {
            try {
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT id, data FROM pending_tasks WHERE 1", new Object[0]);
                while (cursor.next()) {
                    long taskId = cursor.longValue(0);
                    AbstractSerializedData data = cursor.byteBufferValue(1);
                    if (data != null) {
                        switch (data.readInt32(false)) {
                            case VideoPlayer.TRACK_DEFAULT /*0*/:
                                Chat chat = Chat.TLdeserialize(data, data.readInt32(false), false);
                                if (chat != null) {
                                    Utilities.stageQueue.postRunnable(new C06561(chat, taskId));
                                    break;
                                }
                                break;
                            case MediaCodecVideoTrackRenderer.MSG_SET_SURFACE /*1*/:
                                Utilities.stageQueue.postRunnable(new C06572(data.readInt32(false), data.readInt32(false), taskId));
                                break;
                            case MediaCodecAudioTrackRenderer.MSG_SET_PLAYBACK_PARAMS /*2*/:
                                TL_dialog dialog = new TL_dialog();
                                dialog.id = data.readInt64(false);
                                dialog.top_message = data.readInt32(false);
                                dialog.read_inbox_max_id = data.readInt32(false);
                                dialog.read_outbox_max_id = data.readInt32(false);
                                dialog.unread_count = data.readInt32(false);
                                dialog.last_message_date = data.readInt32(false);
                                dialog.pts = data.readInt32(false);
                                dialog.flags = data.readInt32(false);
                                AndroidUtilities.runOnUIThread(new C06583(dialog, InputPeer.TLdeserialize(data, data.readInt32(false), false), taskId));
                                break;
                        }
                        data.reuse();
                    }
                }
                cursor.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.70 */
    class AnonymousClass70 implements Runnable {
        final /* synthetic */ Integer val$_oldId;
        final /* synthetic */ int val$channelId;
        final /* synthetic */ int val$date;
        final /* synthetic */ int val$newId;
        final /* synthetic */ long val$random_id;

        AnonymousClass70(long j, Integer num, int i, int i2, int i3) {
            this.val$random_id = j;
            this.val$_oldId = num;
            this.val$newId = i;
            this.val$date = i2;
            this.val$channelId = i3;
        }

        public void run() {
            MessagesStorage.this.updateMessageStateAndIdInternal(this.val$random_id, this.val$_oldId, this.val$newId, this.val$date, this.val$channelId);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.71 */
    class AnonymousClass71 implements Runnable {
        final /* synthetic */ boolean val$onlyStatus;
        final /* synthetic */ ArrayList val$users;
        final /* synthetic */ boolean val$withTransaction;

        AnonymousClass71(ArrayList arrayList, boolean z, boolean z2) {
            this.val$users = arrayList;
            this.val$onlyStatus = z;
            this.val$withTransaction = z2;
        }

        public void run() {
            MessagesStorage.this.updateUsersInternal(this.val$users, this.val$onlyStatus, this.val$withTransaction);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.72 */
    class AnonymousClass72 implements Runnable {
        final /* synthetic */ ArrayList val$mids;

        AnonymousClass72(ArrayList arrayList) {
            this.val$mids = arrayList;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 2 WHERE mid IN (%s)", new Object[]{TextUtils.join(",", this.val$mids)})).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.73 */
    class AnonymousClass73 implements Runnable {
        final /* synthetic */ HashMap val$encryptedMessages;
        final /* synthetic */ SparseArray val$inbox;
        final /* synthetic */ SparseArray val$outbox;

        AnonymousClass73(SparseArray sparseArray, SparseArray sparseArray2, HashMap hashMap) {
            this.val$inbox = sparseArray;
            this.val$outbox = sparseArray2;
            this.val$encryptedMessages = hashMap;
        }

        public void run() {
            MessagesStorage.this.markMessagesAsReadInternal(this.val$inbox, this.val$outbox, this.val$encryptedMessages);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.74 */
    class AnonymousClass74 implements Runnable {
        final /* synthetic */ ArrayList val$messages;

        /* renamed from: org.telegram.messenger.MessagesStorage.74.1 */
        class C06631 implements Runnable {
            final /* synthetic */ ArrayList val$mids;

            C06631(ArrayList arrayList) {
                this.val$mids = arrayList;
            }

            public void run() {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesDeleted, this.val$mids, Integer.valueOf(0));
            }
        }

        AnonymousClass74(ArrayList arrayList) {
            this.val$messages = arrayList;
        }

        public void run() {
            try {
                String ids = TextUtils.join(",", this.val$messages);
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM randoms WHERE random_id IN(%s)", new Object[]{ids}), new Object[0]);
                ArrayList<Integer> mids = new ArrayList();
                while (cursor.next()) {
                    mids.add(Integer.valueOf(cursor.intValue(0)));
                }
                cursor.dispose();
                if (!mids.isEmpty()) {
                    AndroidUtilities.runOnUIThread(new C06631(mids));
                    MessagesStorage.getInstance().updateDialogsWithReadMessagesInternal(mids, null, null);
                    MessagesStorage.getInstance().markMessagesAsDeletedInternal(mids, 0);
                    MessagesStorage.getInstance().updateDialogsWithDeletedMessagesInternal(mids, 0);
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.75 */
    class AnonymousClass75 implements Runnable {
        final /* synthetic */ int val$channelId;
        final /* synthetic */ ArrayList val$messages;

        AnonymousClass75(ArrayList arrayList, int i) {
            this.val$messages = arrayList;
            this.val$channelId = i;
        }

        public void run() {
            MessagesStorage.this.updateDialogsWithDeletedMessagesInternal(this.val$messages, this.val$channelId);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.76 */
    class AnonymousClass76 implements Runnable {
        final /* synthetic */ int val$channelId;
        final /* synthetic */ ArrayList val$messages;

        AnonymousClass76(ArrayList arrayList, int i) {
            this.val$messages = arrayList;
            this.val$channelId = i;
        }

        public void run() {
            MessagesStorage.this.markMessagesAsDeletedInternal(this.val$messages, this.val$channelId);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.77 */
    class AnonymousClass77 implements Runnable {
        final /* synthetic */ boolean val$createDialog;
        final /* synthetic */ long val$dialog_id;
        final /* synthetic */ int val$load_type;
        final /* synthetic */ int val$max_id;
        final /* synthetic */ messages_Messages val$messages;

        AnonymousClass77(messages_Messages org_telegram_tgnet_TLRPC_messages_Messages, int i, long j, int i2, boolean z) {
            this.val$messages = org_telegram_tgnet_TLRPC_messages_Messages;
            this.val$load_type = i;
            this.val$dialog_id = j;
            this.val$max_id = i2;
            this.val$createDialog = z;
        }

        public void run() {
            try {
                if (!this.val$messages.messages.isEmpty()) {
                    MessagesStorage.this.database.beginTransaction();
                    int minId;
                    if (this.val$load_type == 0) {
                        minId = ((Message) this.val$messages.messages.get(this.val$messages.messages.size() - 1)).id;
                        MessagesStorage.this.closeHolesInTable("messages_holes", this.val$dialog_id, minId, this.val$max_id);
                        MessagesStorage.this.closeHolesInMedia(this.val$dialog_id, minId, this.val$max_id, -1);
                    } else if (this.val$load_type == 1) {
                        maxId = ((Message) this.val$messages.messages.get(0)).id;
                        MessagesStorage.this.closeHolesInTable("messages_holes", this.val$dialog_id, this.val$max_id, maxId);
                        MessagesStorage.this.closeHolesInMedia(this.val$dialog_id, this.val$max_id, maxId, -1);
                    } else if (this.val$load_type == 3 || this.val$load_type == 2) {
                        if (this.val$max_id == 0) {
                            maxId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        } else {
                            maxId = ((Message) this.val$messages.messages.get(0)).id;
                        }
                        minId = ((Message) this.val$messages.messages.get(this.val$messages.messages.size() - 1)).id;
                        MessagesStorage.this.closeHolesInTable("messages_holes", this.val$dialog_id, minId, maxId);
                        MessagesStorage.this.closeHolesInMedia(this.val$dialog_id, minId, maxId, -1);
                    }
                    int count = this.val$messages.messages.size();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)");
                    SQLitePreparedStatement state2 = MessagesStorage.this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                    SQLitePreparedStatement state5 = null;
                    Message botKeyboard = null;
                    int channelId = 0;
                    for (int a = 0; a < count; a++) {
                        Message message = (Message) this.val$messages.messages.get(a);
                        long messageId = (long) message.id;
                        if (channelId == 0) {
                            channelId = message.to_id.channel_id;
                        }
                        if (message.to_id.channel_id != 0) {
                            messageId |= ((long) channelId) << 32;
                        }
                        if (this.val$load_type == -2) {
                            SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, data FROM messages WHERE mid = %d", new Object[]{Long.valueOf(messageId)}), new Object[0]);
                            boolean exist = cursor.next();
                            if (exist) {
                                AbstractSerializedData data = cursor.byteBufferValue(1);
                                if (data != null) {
                                    Message oldMessage = Message.TLdeserialize(data, data.readInt32(false), false);
                                    data.reuse();
                                    if (oldMessage != null) {
                                        message.attachPath = oldMessage.attachPath;
                                    }
                                }
                            }
                            cursor.dispose();
                            if (!exist) {
                            }
                        }
                        if (a == 0 && this.val$createDialog) {
                            SQLitePreparedStatement state3 = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            state3.bindLong(1, this.val$dialog_id);
                            state3.bindInteger(2, message.date);
                            state3.bindInteger(3, 0);
                            state3.bindLong(4, messageId);
                            state3.bindInteger(5, message.id);
                            state3.bindInteger(6, 0);
                            state3.bindLong(7, messageId);
                            state3.bindInteger(8, message.ttl);
                            state3.bindInteger(9, this.val$messages.pts);
                            state3.bindInteger(10, message.date);
                            state3.step();
                            state3.dispose();
                        }
                        MessagesStorage.this.fixUnsupportedMedia(message);
                        state.requery();
                        AbstractSerializedData nativeByteBuffer = new NativeByteBuffer(message.getObjectSize());
                        message.serializeToStream(nativeByteBuffer);
                        state.bindLong(1, messageId);
                        state.bindLong(2, this.val$dialog_id);
                        state.bindInteger(3, MessageObject.getUnreadFlags(message));
                        state.bindInteger(4, message.send_state);
                        state.bindInteger(5, message.date);
                        state.bindByteBuffer(6, (NativeByteBuffer) nativeByteBuffer);
                        state.bindInteger(7, MessageObject.isOut(message) ? 1 : 0);
                        state.bindInteger(8, 0);
                        if ((message.flags & MessagesController.UPDATE_MASK_PHONE) != 0) {
                            state.bindInteger(9, message.views);
                        } else {
                            state.bindInteger(9, 0);
                        }
                        state.bindInteger(10, 0);
                        state.step();
                        if (SharedMediaQuery.canAddMessageToMedia(message)) {
                            state2.requery();
                            state2.bindLong(1, messageId);
                            state2.bindLong(2, this.val$dialog_id);
                            state2.bindInteger(3, message.date);
                            state2.bindInteger(4, SharedMediaQuery.getMediaType(message));
                            state2.bindByteBuffer(5, (NativeByteBuffer) nativeByteBuffer);
                            state2.step();
                        }
                        nativeByteBuffer.reuse();
                        if ((message.media instanceof TL_messageMediaWebPage) && (message.media.webpage instanceof TL_webPagePending)) {
                            if (state5 == null) {
                                state5 = MessagesStorage.this.database.executeFast("REPLACE INTO webpage_pending VALUES(?, ?)");
                            }
                            state5.requery();
                            state5.bindLong(1, message.media.webpage.id);
                            state5.bindLong(2, messageId);
                            state5.step();
                        }
                        if (this.val$load_type == 0 && MessagesStorage.this.isValidKeyboardToSave(message) && (botKeyboard == null || botKeyboard.id < message.id)) {
                            botKeyboard = message;
                        }
                    }
                    state.dispose();
                    state2.dispose();
                    if (state5 != null) {
                        state5.dispose();
                    }
                    if (botKeyboard != null) {
                        BotQuery.putBotKeyboard(this.val$dialog_id, botKeyboard);
                    }
                    MessagesStorage.this.putUsersInternal(this.val$messages.users);
                    MessagesStorage.this.putChatsInternal(this.val$messages.chats);
                    MessagesStorage.this.database.commitTransaction();
                    if (this.val$createDialog) {
                        MessagesStorage.getInstance().updateDialogsWithDeletedMessages(new ArrayList(), false, channelId);
                    }
                } else if (this.val$load_type == 0) {
                    MessagesStorage.this.doneHolesInTable("messages_holes", this.val$dialog_id, this.val$max_id);
                    MessagesStorage.this.doneHolesInMedia(this.val$dialog_id, this.val$max_id, -1);
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.78 */
    class AnonymousClass78 implements Runnable {
        final /* synthetic */ int val$count;
        final /* synthetic */ int val$offset;

        AnonymousClass78(int i, int i2) {
            this.val$offset = i;
            this.val$count = i2;
        }

        public void run() {
            messages_Dialogs dialogs = new messages_Dialogs();
            ArrayList<EncryptedChat> encryptedChats = new ArrayList();
            ArrayList<Integer> usersToLoad = new ArrayList();
            usersToLoad.add(Integer.valueOf(UserConfig.getClientUserId()));
            ArrayList<Integer> chatsToLoad = new ArrayList();
            ArrayList<Integer> encryptedToLoad = new ArrayList();
            ArrayList<Long> replyMessages = new ArrayList();
            HashMap<Long, Message> replyMessageOwners = new HashMap();
            SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT d.did, d.last_mid, d.unread_count, d.date, m.data, m.read_state, m.mid, m.send_state, s.flags, m.date, d.pts, d.inbox_max, d.outbox_max, m.replydata FROM dialogs as d LEFT JOIN messages as m ON d.last_mid = m.mid LEFT JOIN dialog_settings as s ON d.did = s.did ORDER BY d.date DESC LIMIT %d,%d", new Object[]{Integer.valueOf(this.val$offset), Integer.valueOf(this.val$count)}), new Object[0]);
            while (cursor.next()) {
                Message message;
                TL_dialog dialog = new TL_dialog();
                dialog.id = cursor.longValue(0);
                dialog.top_message = cursor.intValue(1);
                dialog.unread_count = cursor.intValue(2);
                dialog.last_message_date = cursor.intValue(3);
                dialog.pts = cursor.intValue(10);
                int i = (dialog.pts == 0 || ((int) dialog.id) > 0) ? 0 : 1;
                dialog.flags = i;
                dialog.read_inbox_max_id = cursor.intValue(11);
                dialog.read_outbox_max_id = cursor.intValue(12);
                long flags = cursor.longValue(8);
                int low_flags = (int) flags;
                dialog.notify_settings = new TL_peerNotifySettings();
                if ((low_flags & 1) != 0) {
                    dialog.notify_settings.mute_until = (int) (flags >> 32);
                    if (dialog.notify_settings.mute_until == 0) {
                        dialog.notify_settings.mute_until = ConnectionsManager.DEFAULT_DATACENTER_ID;
                    }
                }
                dialogs.dialogs.add(dialog);
                NativeByteBuffer data = cursor.byteBufferValue(4);
                if (data != null) {
                    message = Message.TLdeserialize(data, data.readInt32(false), false);
                    data.reuse();
                    if (message != null) {
                        MessageObject.setUnreadFlags(message, cursor.intValue(5));
                        message.id = cursor.intValue(6);
                        int date = cursor.intValue(9);
                        if (date != 0) {
                            dialog.last_message_date = date;
                        }
                        message.send_state = cursor.intValue(7);
                        message.dialog_id = dialog.id;
                        dialogs.messages.add(message);
                        MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                        try {
                            if (message.reply_to_msg_id != 0 && (message.action instanceof TL_messageActionPinMessage)) {
                                if (!cursor.isNull(13)) {
                                    data = cursor.byteBufferValue(13);
                                    if (data != null) {
                                        message.replyMessage = Message.TLdeserialize(data, data.readInt32(false), false);
                                        data.reuse();
                                        if (message.replyMessage != null) {
                                            MessagesStorage.addUsersAndChatsFromMessage(message.replyMessage, usersToLoad, chatsToLoad);
                                        }
                                    }
                                }
                                if (message.replyMessage == null) {
                                    long messageId = (long) message.reply_to_msg_id;
                                    if (message.to_id.channel_id != 0) {
                                        messageId |= ((long) message.to_id.channel_id) << 32;
                                    }
                                    if (!replyMessages.contains(Long.valueOf(messageId))) {
                                        replyMessages.add(Long.valueOf(messageId));
                                    }
                                    replyMessageOwners.put(Long.valueOf(dialog.id), message);
                                }
                            }
                        } catch (Throwable e) {
                            FileLog.m13e("tmessages", e);
                        }
                    }
                }
                try {
                    int lower_id = (int) dialog.id;
                    int high_id = (int) (dialog.id >> 32);
                    if (lower_id == 0) {
                        if (!encryptedToLoad.contains(Integer.valueOf(high_id))) {
                            encryptedToLoad.add(Integer.valueOf(high_id));
                        }
                    } else if (high_id == 1) {
                        if (!chatsToLoad.contains(Integer.valueOf(lower_id))) {
                            chatsToLoad.add(Integer.valueOf(lower_id));
                        }
                    } else if (lower_id > 0) {
                        if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                            usersToLoad.add(Integer.valueOf(lower_id));
                        }
                    } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                        chatsToLoad.add(Integer.valueOf(-lower_id));
                    }
                } catch (Throwable e2) {
                    dialogs.dialogs.clear();
                    dialogs.users.clear();
                    dialogs.chats.clear();
                    encryptedChats.clear();
                    FileLog.m13e("tmessages", e2);
                    MessagesController.getInstance().processLoadedDialogs(dialogs, encryptedChats, 0, 100, 1, true, false);
                    return;
                }
            }
            cursor.dispose();
            if (!replyMessages.isEmpty()) {
                cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data, mid, date, uid FROM messages WHERE mid IN(%s)", new Object[]{TextUtils.join(",", replyMessages)}), new Object[0]);
                while (cursor.next()) {
                    data = cursor.byteBufferValue(0);
                    if (data != null) {
                        message = Message.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        message.id = cursor.intValue(1);
                        message.date = cursor.intValue(2);
                        message.dialog_id = cursor.longValue(3);
                        MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                        Message owner = (Message) replyMessageOwners.get(Long.valueOf(message.dialog_id));
                        if (owner != null) {
                            owner.replyMessage = message;
                            message.dialog_id = owner.dialog_id;
                        }
                    }
                }
                cursor.dispose();
            }
            if (!encryptedToLoad.isEmpty()) {
                MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", encryptedToLoad), encryptedChats, usersToLoad);
            }
            if (!chatsToLoad.isEmpty()) {
                MessagesStorage.this.getChatsInternal(TextUtils.join(",", chatsToLoad), dialogs.chats);
            }
            if (!usersToLoad.isEmpty()) {
                MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), dialogs.users);
            }
            MessagesController.getInstance().processLoadedDialogs(dialogs, encryptedChats, this.val$offset, this.val$count, 1, false, false);
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.79 */
    class AnonymousClass79 implements Runnable {
        final /* synthetic */ messages_Dialogs val$dialogs;

        AnonymousClass79(messages_Dialogs org_telegram_tgnet_TLRPC_messages_Dialogs) {
            this.val$dialogs = org_telegram_tgnet_TLRPC_messages_Dialogs;
        }

        public void run() {
            MessagesStorage.this.putDialogsInternal(this.val$dialogs);
            MessagesStorage.this.loadUnreadMessages();
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.7 */
    class C06647 implements Runnable {
        final /* synthetic */ int val$channelId;
        final /* synthetic */ int val$pts;

        C06647(int i, int i2) {
            this.val$pts = i;
            this.val$channelId = i2;
        }

        public void run() {
            try {
                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE dialogs SET pts = ? WHERE did = ?");
                state.bindInteger(1, this.val$pts);
                state.bindInteger(2, -this.val$channelId);
                state.step();
                state.dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.80 */
    class AnonymousClass80 implements Runnable {
        final /* synthetic */ long val$dialog_id;
        final /* synthetic */ Integer[] val$max;
        final /* synthetic */ boolean val$outbox;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass80(boolean z, long j, Integer[] numArr, Semaphore semaphore) {
            this.val$outbox = z;
            this.val$dialog_id = j;
            this.val$max = numArr;
            this.val$semaphore = semaphore;
        }

        public void run() {
            SQLiteCursor cursor = null;
            try {
                if (this.val$outbox) {
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT outbox_max FROM dialogs WHERE did = " + this.val$dialog_id, new Object[0]);
                } else {
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + this.val$dialog_id, new Object[0]);
                }
                if (cursor.next()) {
                    this.val$max[0] = Integer.valueOf(cursor.intValue(0));
                }
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            this.val$semaphore.release();
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.81 */
    class AnonymousClass81 implements Runnable {
        final /* synthetic */ int val$channelId;
        final /* synthetic */ Integer[] val$pts;
        final /* synthetic */ Semaphore val$semaphore;

        AnonymousClass81(int i, Integer[] numArr, Semaphore semaphore) {
            this.val$channelId = i;
            this.val$pts = numArr;
            this.val$semaphore = semaphore;
        }

        public void run() {
            SQLiteCursor cursor = null;
            try {
                cursor = MessagesStorage.this.database.queryFinalized("SELECT pts FROM dialogs WHERE did = " + (-this.val$channelId), new Object[0]);
                if (cursor.next()) {
                    this.val$pts[0] = Integer.valueOf(cursor.intValue(0));
                }
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            try {
                if (this.val$semaphore != null) {
                    this.val$semaphore.release();
                }
            } catch (Throwable e2) {
                FileLog.m13e("tmessages", e2);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.82 */
    class AnonymousClass82 implements Runnable {
        final /* synthetic */ Semaphore val$semaphore;
        final /* synthetic */ User[] val$user;
        final /* synthetic */ int val$user_id;

        AnonymousClass82(User[] userArr, int i, Semaphore semaphore) {
            this.val$user = userArr;
            this.val$user_id = i;
            this.val$semaphore = semaphore;
        }

        public void run() {
            this.val$user[0] = MessagesStorage.this.getUser(this.val$user_id);
            this.val$semaphore.release();
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.83 */
    class AnonymousClass83 implements Runnable {
        final /* synthetic */ Chat[] val$chat;
        final /* synthetic */ Semaphore val$semaphore;
        final /* synthetic */ int val$user_id;

        AnonymousClass83(Chat[] chatArr, int i, Semaphore semaphore) {
            this.val$chat = chatArr;
            this.val$user_id = i;
            this.val$semaphore = semaphore;
        }

        public void run() {
            this.val$chat[0] = MessagesStorage.this.getChat(this.val$user_id);
            this.val$semaphore.release();
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.8 */
    class C06658 implements Runnable {
        final /* synthetic */ int val$date;
        final /* synthetic */ int val$pts;
        final /* synthetic */ int val$qts;
        final /* synthetic */ int val$seq;

        C06658(int i, int i2, int i3, int i4) {
            this.val$seq = i;
            this.val$pts = i2;
            this.val$date = i3;
            this.val$qts = i4;
        }

        public void run() {
            try {
                if (MessagesStorage.this.lastSavedSeq != this.val$seq || MessagesStorage.this.lastSavedPts != this.val$pts || MessagesStorage.this.lastSavedDate != this.val$date || MessagesStorage.lastQtsValue != this.val$qts) {
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE params SET seq = ?, pts = ?, date = ?, qts = ? WHERE id = 1");
                    state.bindInteger(1, this.val$seq);
                    state.bindInteger(2, this.val$pts);
                    state.bindInteger(3, this.val$date);
                    state.bindInteger(4, this.val$qts);
                    state.step();
                    state.dispose();
                    MessagesStorage.this.lastSavedSeq = this.val$seq;
                    MessagesStorage.this.lastSavedPts = this.val$pts;
                    MessagesStorage.this.lastSavedDate = this.val$date;
                    MessagesStorage.this.lastSavedQts = this.val$qts;
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    /* renamed from: org.telegram.messenger.MessagesStorage.9 */
    class C06669 implements Runnable {
        final /* synthetic */ long val$did;
        final /* synthetic */ long val$flags;

        C06669(long j, long j2) {
            this.val$did = j;
            this.val$flags = j2;
        }

        public void run() {
            try {
                MessagesStorage.this.database.executeFast(String.format(Locale.US, "REPLACE INTO dialog_settings VALUES(%d, %d)", new Object[]{Long.valueOf(this.val$did), Long.valueOf(this.val$flags)})).stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    private class Hole {
        public int end;
        public int start;
        public int type;

        public Hole(int s, int e) {
            this.start = s;
            this.end = e;
        }

        public Hole(int t, int s, int e) {
            this.type = t;
            this.start = s;
            this.end = e;
        }
    }

    static {
        lastDateValue = 0;
        lastPtsValue = 0;
        lastQtsValue = 0;
        lastSeqValue = 0;
        lastSecretVersion = 0;
        secretPBytes = null;
        secretG = 0;
        Instance = null;
    }

    public static MessagesStorage getInstance() {
        MessagesStorage localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesStorage.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        MessagesStorage localInstance2 = new MessagesStorage();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public MessagesStorage() {
        this.storageQueue = new DispatchQueue("storageQueue");
        this.lastTaskId = new AtomicLong(System.currentTimeMillis());
        this.lastSavedSeq = 0;
        this.lastSavedPts = 0;
        this.lastSavedDate = 0;
        this.lastSavedQts = 0;
        this.storageQueue.setPriority(10);
        openDatabase();
    }

    public SQLiteDatabase getDatabase() {
        return this.database;
    }

    public DispatchQueue getStorageQueue() {
        return this.storageQueue;
    }

    public void openDatabase() {
        this.cacheFile = new File(ApplicationLoader.getFilesDirFixed(), "cache4.db");
        boolean createTable = false;
        if (!this.cacheFile.exists()) {
            createTable = true;
        }
        try {
            this.database = new SQLiteDatabase(this.cacheFile.getPath());
            this.database.executeFast("PRAGMA secure_delete = ON").stepThis().dispose();
            this.database.executeFast("PRAGMA temp_store = 1").stepThis().dispose();
            if (createTable) {
                this.database.executeFast("CREATE TABLE messages_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_holes ON messages_holes(uid, end);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE media_holes_v2(uid INTEGER, type INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, type, start));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_media_holes_v2 ON media_holes_v2(uid, type, end);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE messages(mid INTEGER PRIMARY KEY, uid INTEGER, read_state INTEGER, send_state INTEGER, date INTEGER, data BLOB, out INTEGER, ttl INTEGER, media INTEGER, replydata BLOB, imp INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_idx_messages ON messages(uid, mid);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_idx_messages ON messages(uid, date, mid);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_out_idx_messages ON messages(mid, out);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS task_idx_messages ON messages(uid, out, read_state, ttl, date, send_state);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS send_state_idx_messages ON messages(mid, send_state, date) WHERE mid < 0 AND send_state = 1;").stepThis().dispose();
                this.database.executeFast("CREATE TABLE download_queue(uid INTEGER, type INTEGER, date INTEGER, data BLOB, PRIMARY KEY (uid, type));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS type_date_idx_download_queue ON download_queue(type, date);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE user_phones_v6(uid INTEGER, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (uid, phone))").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v6(sphone, deleted);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE dialogs(did INTEGER PRIMARY KEY, date INTEGER, unread_count INTEGER, last_mid INTEGER, inbox_max INTEGER, outbox_max INTEGER, last_mid_i INTEGER, unread_count_i INTEGER, pts INTEGER, date_i INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_dialogs ON dialogs(date);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_idx_dialogs ON dialogs(last_mid);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_idx_dialogs ON dialogs(unread_count);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_i_idx_dialogs ON dialogs(last_mid_i);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_i_idx_dialogs ON dialogs(unread_count_i);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE randoms(random_id INTEGER, mid INTEGER, PRIMARY KEY (random_id, mid))").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_idx_randoms ON randoms(mid);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE enc_tasks_v2(mid INTEGER PRIMARY KEY, date INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_enc_tasks_v2 ON enc_tasks_v2(date);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE messages_seq(mid INTEGER PRIMARY KEY, seq_in INTEGER, seq_out INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS seq_idx_messages_seq ON messages_seq(seq_in, seq_out);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE params(id INTEGER PRIMARY KEY, seq INTEGER, pts INTEGER, date INTEGER, qts INTEGER, lsv INTEGER, sg INTEGER, pbytes BLOB)").stepThis().dispose();
                this.database.executeFast("INSERT INTO params VALUES(1, 0, 0, 0, 0, 0, 0, NULL)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE media_v2(mid INTEGER PRIMARY KEY, uid INTEGER, date INTEGER, type INTEGER, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_type_date_idx_media ON media_v2(uid, mid, type, date);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE bot_keyboard(uid INTEGER PRIMARY KEY, mid INTEGER, info BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS bot_keyboard_idx_mid ON bot_keyboard(mid);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE chat_settings_v2(uid INTEGER PRIMARY KEY, info BLOB, pinned INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_settings_pinned_idx ON chat_settings_v2(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
                this.database.executeFast("CREATE TABLE chat_pinned(uid INTEGER PRIMARY KEY, pinned INTEGER, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_pinned_mid_idx ON chat_pinned(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
                this.database.executeFast("CREATE TABLE chat_hints(did INTEGER, type INTEGER, rating REAL, date INTEGER, PRIMARY KEY(did, type))").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_hints_rating_idx ON chat_hints(rating);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE users_data(uid INTEGER PRIMARY KEY, about TEXT)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE users(uid INTEGER PRIMARY KEY, name TEXT, status INTEGER, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE chats(uid INTEGER PRIMARY KEY, name TEXT, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE enc_chats(uid INTEGER PRIMARY KEY, user INTEGER, name TEXT, data BLOB, g BLOB, authkey BLOB, ttl INTEGER, layer INTEGER, seq_in INTEGER, seq_out INTEGER, use_count INTEGER, exchange_id INTEGER, key_date INTEGER, fprint INTEGER, fauthkey BLOB, khash BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE channel_users_v2(did INTEGER, uid INTEGER, date INTEGER, data BLOB, PRIMARY KEY(did, uid))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE contacts(uid INTEGER PRIMARY KEY, mutual INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE wallpapers(uid INTEGER PRIMARY KEY, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE user_photos(uid INTEGER, id INTEGER, data BLOB, PRIMARY KEY (uid, id))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE blocked_users(uid INTEGER PRIMARY KEY)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE dialog_settings(did INTEGER PRIMARY KEY, flags INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE web_recent_v3(id TEXT, type INTEGER, image_url TEXT, thumb_url TEXT, local_url TEXT, width INTEGER, height INTEGER, size INTEGER, date INTEGER, document BLOB, PRIMARY KEY (id, type));").stepThis().dispose();
                this.database.executeFast("CREATE TABLE stickers_v2(id INTEGER PRIMARY KEY, data BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE hashtag_recent_v2(id TEXT PRIMARY KEY, date INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE webpage_pending(id INTEGER, mid INTEGER, PRIMARY KEY (id, mid));").stepThis().dispose();
                this.database.executeFast("CREATE TABLE user_contacts_v6(uid INTEGER PRIMARY KEY, fname TEXT, sname TEXT)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE sent_files_v2(uid TEXT, type INTEGER, data BLOB, PRIMARY KEY (uid, type))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE search_recent(did INTEGER PRIMARY KEY, date INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE media_counts_v2(uid INTEGER, type INTEGER, count INTEGER, PRIMARY KEY(uid, type))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE keyvalue(id TEXT PRIMARY KEY, value TEXT)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE bot_info(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE pending_tasks(id INTEGER PRIMARY KEY, data BLOB);").stepThis().dispose();
                this.database.executeFast("PRAGMA user_version = 34").stepThis().dispose();
            } else {
                try {
                    SQLiteCursor cursor = this.database.queryFinalized("SELECT seq, pts, date, qts, lsv, sg, pbytes FROM params WHERE id = 1", new Object[0]);
                    if (cursor.next()) {
                        lastSeqValue = cursor.intValue(0);
                        lastPtsValue = cursor.intValue(1);
                        lastDateValue = cursor.intValue(2);
                        lastQtsValue = cursor.intValue(3);
                        lastSecretVersion = cursor.intValue(4);
                        secretG = cursor.intValue(5);
                        if (cursor.isNull(6)) {
                            secretPBytes = null;
                        } else {
                            secretPBytes = cursor.byteArrayValue(6);
                            if (secretPBytes != null && secretPBytes.length == 1) {
                                secretPBytes = null;
                            }
                        }
                    }
                    cursor.dispose();
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                    try {
                        this.database.executeFast("CREATE TABLE IF NOT EXISTS params(id INTEGER PRIMARY KEY, seq INTEGER, pts INTEGER, date INTEGER, qts INTEGER, lsv INTEGER, sg INTEGER, pbytes BLOB)").stepThis().dispose();
                        this.database.executeFast("INSERT INTO params VALUES(1, 0, 0, 0, 0, 0, 0, NULL)").stepThis().dispose();
                    } catch (Throwable e2) {
                        FileLog.m13e("tmessages", e2);
                    }
                }
                int version = this.database.executeInt("PRAGMA user_version", new Object[0]).intValue();
                if (version < 34) {
                    updateDbToLastVersion(version);
                }
            }
        } catch (Throwable e3) {
            FileLog.m13e("tmessages", e3);
        }
        loadUnreadMessages();
        loadPendingTasks();
    }

    public void updateDbToLastVersion(int currentVersion) {
        this.storageQueue.postRunnable(new C06411(currentVersion));
    }

    public void cleanup(boolean isLogin) {
        this.storageQueue.cleanupQueue();
        this.storageQueue.postRunnable(new C06482(isLogin));
    }

    public void saveSecretParams(int lsv, int sg, byte[] pbytes) {
        this.storageQueue.postRunnable(new C06523(lsv, sg, pbytes));
    }

    public long createPendingTask(NativeByteBuffer data) {
        if (data == null) {
            return 0;
        }
        long id = this.lastTaskId.getAndAdd(1);
        this.storageQueue.postRunnable(new C06544(id, data));
        return id;
    }

    public void removePendingTask(long id) {
        this.storageQueue.postRunnable(new C06555(id));
    }

    private void loadPendingTasks() {
        this.storageQueue.postRunnable(new C06626());
    }

    public void saveChannelPts(int channelId, int pts) {
        this.storageQueue.postRunnable(new C06647(pts, channelId));
    }

    public void saveDiffParams(int seq, int pts, int date, int qts) {
        this.storageQueue.postRunnable(new C06658(seq, pts, date, qts));
    }

    public void setDialogFlags(long did, long flags) {
        this.storageQueue.postRunnable(new C06669(did, flags));
    }

    public void loadUnreadMessages() {
        this.storageQueue.postRunnable(new Runnable() {

            /* renamed from: org.telegram.messenger.MessagesStorage.10.1 */
            class C06381 implements Runnable {
                final /* synthetic */ ArrayList val$chats;
                final /* synthetic */ ArrayList val$encryptedChats;
                final /* synthetic */ ArrayList val$messages;
                final /* synthetic */ HashMap val$pushDialogs;
                final /* synthetic */ ArrayList val$users;

                C06381(HashMap hashMap, ArrayList arrayList, ArrayList arrayList2, ArrayList arrayList3, ArrayList arrayList4) {
                    this.val$pushDialogs = hashMap;
                    this.val$messages = arrayList;
                    this.val$users = arrayList2;
                    this.val$chats = arrayList3;
                    this.val$encryptedChats = arrayList4;
                }

                public void run() {
                    NotificationsController.getInstance().processLoadedUnreadMessages(this.val$pushDialogs, this.val$messages, this.val$users, this.val$chats, this.val$encryptedChats);
                }
            }

            public void run() {
                try {
                    long did;
                    int lower_id;
                    ArrayList<Integer> usersToLoad = new ArrayList();
                    ArrayList<Integer> chatsToLoad = new ArrayList();
                    ArrayList<Integer> encryptedChatIds = new ArrayList();
                    HashMap<Long, Integer> pushDialogs = new HashMap();
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT d.did, d.unread_count, s.flags FROM dialogs as d LEFT JOIN dialog_settings as s ON d.did = s.did WHERE d.unread_count != 0", new Object[0]);
                    StringBuilder ids = new StringBuilder();
                    while (cursor.next()) {
                        if (cursor.isNull(2) || cursor.intValue(2) != 1) {
                            did = cursor.longValue(0);
                            pushDialogs.put(Long.valueOf(did), Integer.valueOf(cursor.intValue(1)));
                            if (ids.length() != 0) {
                                ids.append(",");
                            }
                            ids.append(did);
                            lower_id = (int) did;
                            int high_id = (int) (did >> 32);
                            if (lower_id == 0) {
                                if (!encryptedChatIds.contains(Integer.valueOf(high_id))) {
                                    encryptedChatIds.add(Integer.valueOf(high_id));
                                }
                            } else if (lower_id >= 0) {
                                if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                                    usersToLoad.add(Integer.valueOf(lower_id));
                                }
                            } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                                chatsToLoad.add(Integer.valueOf(-lower_id));
                            }
                        }
                    }
                    cursor.dispose();
                    ArrayList<Message> messages = new ArrayList();
                    ArrayList<User> users = new ArrayList();
                    ArrayList<Chat> chats = new ArrayList();
                    ArrayList<EncryptedChat> encryptedChats = new ArrayList();
                    if (ids.length() > 0) {
                        String stringBuilder = ids.toString();
                        stringBuilder = ") AND out = 0 AND read_state IN(0,2) ORDER BY date DESC LIMIT 50";
                        Object[] objArr = new Object[0];
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT read_state, data, send_state, mid, date, uid FROM messages WHERE uid IN (" + r27 + r27, objArr);
                        while (cursor.next()) {
                            AbstractSerializedData data = cursor.byteBufferValue(1);
                            if (data != null) {
                                Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                                data.reuse();
                                MessageObject.setUnreadFlags(message, cursor.intValue(0));
                                message.id = cursor.intValue(3);
                                message.date = cursor.intValue(4);
                                message.dialog_id = cursor.longValue(5);
                                messages.add(message);
                                lower_id = (int) message.dialog_id;
                                MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                                message.send_state = cursor.intValue(2);
                                if (!(message.to_id.channel_id != 0 || MessageObject.isUnread(message) || lower_id == 0) || message.id > 0) {
                                    message.send_state = 0;
                                }
                                if (lower_id == 0 && !cursor.isNull(5)) {
                                    message.random_id = cursor.longValue(5);
                                }
                            }
                        }
                        cursor.dispose();
                        if (!encryptedChatIds.isEmpty()) {
                            MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", encryptedChatIds), encryptedChats, usersToLoad);
                        }
                        if (!usersToLoad.isEmpty()) {
                            MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), users);
                        }
                        if (!chatsToLoad.isEmpty()) {
                            MessagesStorage.this.getChatsInternal(TextUtils.join(",", chatsToLoad), chats);
                            int a = 0;
                            while (a < chats.size()) {
                                Chat chat = (Chat) chats.get(a);
                                if (chat != null && (chat.left || chat.migrated_to != null)) {
                                    MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0 WHERE did = " + ((long) (-chat.id))).stepThis().dispose();
                                    stringBuilder = "UPDATE messages SET read_state = 3 WHERE uid = %d AND mid > 0 AND read_state IN(0,2) AND out = 0";
                                    MessagesStorage.this.database.executeFast(String.format(Locale.US, r27, new Object[]{Long.valueOf(did)})).stepThis().dispose();
                                    chats.remove(a);
                                    a--;
                                    pushDialogs.remove(Long.valueOf((long) (-chat.id)));
                                    int b = 0;
                                    while (b < messages.size()) {
                                        if (((Message) messages.get(b)).dialog_id == ((long) (-chat.id))) {
                                            messages.remove(b);
                                            b--;
                                        }
                                        b++;
                                    }
                                }
                                a++;
                            }
                        }
                    }
                    Collections.reverse(messages);
                    AndroidUtilities.runOnUIThread(new C06381(pushDialogs, messages, users, chats, encryptedChats));
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
            }
        });
    }

    public void putWallpapers(ArrayList<WallPaper> wallPapers) {
        this.storageQueue.postRunnable(new AnonymousClass11(wallPapers));
    }

    public void loadWebRecent(int type) {
        this.storageQueue.postRunnable(new AnonymousClass12(type));
    }

    public void addRecentLocalFile(String imageUrl, String localUrl, Document document) {
        if (imageUrl != null && imageUrl.length() != 0) {
            if ((localUrl != null && localUrl.length() != 0) || document != null) {
                this.storageQueue.postRunnable(new AnonymousClass13(document, imageUrl, localUrl));
            }
        }
    }

    public void removeWebRecent(SearchImage searchImage) {
        if (searchImage != null) {
            this.storageQueue.postRunnable(new AnonymousClass14(searchImage));
        }
    }

    public void clearWebRecent(int type) {
        this.storageQueue.postRunnable(new AnonymousClass15(type));
    }

    public void putWebRecent(ArrayList<SearchImage> arrayList) {
        this.storageQueue.postRunnable(new AnonymousClass16(arrayList));
    }

    public void getWallpapers() {
        this.storageQueue.postRunnable(new Runnable() {

            /* renamed from: org.telegram.messenger.MessagesStorage.17.1 */
            class C06401 implements Runnable {
                final /* synthetic */ ArrayList val$wallPapers;

                C06401(ArrayList arrayList) {
                    this.val$wallPapers = arrayList;
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.wallpapersDidLoaded, this.val$wallPapers);
                }
            }

            public void run() {
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT data FROM wallpapers WHERE 1", new Object[0]);
                    ArrayList<WallPaper> wallPapers = new ArrayList();
                    while (cursor.next()) {
                        NativeByteBuffer data = cursor.byteBufferValue(0);
                        if (data != null) {
                            WallPaper wallPaper = WallPaper.TLdeserialize(data, data.readInt32(false), false);
                            data.reuse();
                            wallPapers.add(wallPaper);
                        }
                    }
                    cursor.dispose();
                    AndroidUtilities.runOnUIThread(new C06401(wallPapers));
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
            }
        });
    }

    public void getBlockedUsers() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    ArrayList<Integer> ids = new ArrayList();
                    ArrayList<User> users = new ArrayList();
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT * FROM blocked_users WHERE 1", new Object[0]);
                    StringBuilder usersToLoad = new StringBuilder();
                    while (cursor.next()) {
                        int user_id = cursor.intValue(0);
                        ids.add(Integer.valueOf(user_id));
                        if (usersToLoad.length() != 0) {
                            usersToLoad.append(",");
                        }
                        usersToLoad.append(user_id);
                    }
                    cursor.dispose();
                    if (usersToLoad.length() != 0) {
                        MessagesStorage.this.getUsersInternal(usersToLoad.toString(), users);
                    }
                    MessagesController.getInstance().processLoadedBlockedUsers(ids, users, true);
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
            }
        });
    }

    public void deleteBlockedUser(int id) {
        this.storageQueue.postRunnable(new AnonymousClass19(id));
    }

    public void putBlockedUsers(ArrayList<Integer> ids, boolean replace) {
        if (ids != null && !ids.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass20(replace, ids));
        }
    }

    public void deleteUserChannelHistory(int channelId, int uid) {
        this.storageQueue.postRunnable(new AnonymousClass21(channelId, uid));
    }

    public void deleteDialog(long did, int messagesOnly) {
        this.storageQueue.postRunnable(new AnonymousClass22(messagesOnly, did));
    }

    public void getDialogPhotos(int did, int offset, int count, long max_id, int classGuid) {
        this.storageQueue.postRunnable(new AnonymousClass23(max_id, did, count, offset, classGuid));
    }

    public void clearUserPhotos(int uid) {
        this.storageQueue.postRunnable(new AnonymousClass24(uid));
    }

    public void clearUserPhoto(int uid, long pid) {
        this.storageQueue.postRunnable(new AnonymousClass25(uid, pid));
    }

    public void putDialogPhotos(int did, photos_Photos photos) {
        if (photos != null && !photos.photos.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass26(photos, did));
        }
    }

    public void getNewTask(ArrayList<Integer> oldTask) {
        this.storageQueue.postRunnable(new AnonymousClass27(oldTask));
    }

    public void createTaskForSecretChat(int chat_id, int time, int readTime, int isOut, ArrayList<Long> random_ids) {
        this.storageQueue.postRunnable(new AnonymousClass28(random_ids, chat_id, isOut, time, readTime));
    }

    private void updateDialogsWithReadMessagesInternal(ArrayList<Integer> messages, SparseArray<Long> inbox, SparseArray<Long> outbox) {
        try {
            SQLitePreparedStatement state;
            HashMap<Long, Integer> dialogsToUpdate = new HashMap();
            SQLiteCursor cursor;
            if (messages == null || messages.isEmpty()) {
                int b;
                int key;
                long messageId;
                if (!(inbox == null || inbox.size() == 0)) {
                    for (b = 0; b < inbox.size(); b++) {
                        key = inbox.keyAt(b);
                        messageId = ((Long) inbox.get(key)).longValue();
                        cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(mid) FROM messages WHERE uid = %d AND mid > %d AND read_state IN(0,2) AND out = 0", new Object[]{Integer.valueOf(key), Long.valueOf(messageId)}), new Object[0]);
                        if (cursor.next()) {
                            int count = cursor.intValue(0);
                            dialogsToUpdate.put(Long.valueOf((long) key), Integer.valueOf(count));
                        }
                        cursor.dispose();
                        state = this.database.executeFast("UPDATE dialogs SET inbox_max = max((SELECT inbox_max FROM dialogs WHERE did = ?), ?) WHERE did = ?");
                        state.requery();
                        state.bindLong(1, (long) key);
                        state.bindInteger(2, (int) messageId);
                        state.bindLong(3, (long) key);
                        state.step();
                        state.dispose();
                    }
                }
                if (!(outbox == null || outbox.size() == 0)) {
                    for (b = 0; b < outbox.size(); b++) {
                        key = outbox.keyAt(b);
                        messageId = ((Long) outbox.get(key)).longValue();
                        state = this.database.executeFast("UPDATE dialogs SET outbox_max = max((SELECT outbox_max FROM dialogs WHERE did = ?), ?) WHERE did = ?");
                        state.requery();
                        state.bindLong(1, (long) key);
                        state.bindInteger(2, (int) messageId);
                        state.bindLong(3, (long) key);
                        state.step();
                        state.dispose();
                    }
                }
            } else {
                String ids = TextUtils.join(",", messages);
                cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, read_state, out FROM messages WHERE mid IN(%s)", new Object[]{ids}), new Object[0]);
                while (cursor.next()) {
                    if (cursor.intValue(2) == 0 && cursor.intValue(1) == 0) {
                        long uid = cursor.longValue(0);
                        Integer currentCount = (Integer) dialogsToUpdate.get(Long.valueOf(uid));
                        if (currentCount == null) {
                            dialogsToUpdate.put(Long.valueOf(uid), Integer.valueOf(1));
                        } else {
                            dialogsToUpdate.put(Long.valueOf(uid), Integer.valueOf(currentCount.intValue() + 1));
                        }
                    }
                }
                cursor.dispose();
            }
            if (!dialogsToUpdate.isEmpty()) {
                this.database.beginTransaction();
                state = this.database.executeFast("UPDATE dialogs SET unread_count = ? WHERE did = ?");
                for (Entry<Long, Integer> entry : dialogsToUpdate.entrySet()) {
                    state.requery();
                    state.bindInteger(1, ((Integer) entry.getValue()).intValue());
                    state.bindLong(2, ((Long) entry.getKey()).longValue());
                    state.step();
                }
                state.dispose();
                this.database.commitTransaction();
            }
            if (!dialogsToUpdate.isEmpty()) {
                MessagesController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
            }
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
    }

    public void updateDialogsWithReadMessages(SparseArray<Long> inbox, SparseArray<Long> outbox, boolean useQueue) {
        if (inbox.size() != 0) {
            if (useQueue) {
                this.storageQueue.postRunnable(new AnonymousClass29(inbox, outbox));
            } else {
                updateDialogsWithReadMessagesInternal(null, inbox, outbox);
            }
        }
    }

    public void updateChatParticipants(ChatParticipants participants) {
        if (participants != null) {
            this.storageQueue.postRunnable(new AnonymousClass30(participants));
        }
    }

    public void updateChannelUsers(int channel_id, ArrayList<ChannelParticipant> participants) {
        this.storageQueue.postRunnable(new AnonymousClass31(channel_id, participants));
    }

    public void updateChatInfo(ChatFull info, boolean ifExist) {
        this.storageQueue.postRunnable(new AnonymousClass32(ifExist, info));
    }

    public void updateChannelPinnedMessage(int channelId, int messageId) {
        this.storageQueue.postRunnable(new AnonymousClass33(channelId, messageId));
    }

    public void updateChatInfo(int chat_id, int user_id, int what, int invited_id, int version) {
        this.storageQueue.postRunnable(new AnonymousClass34(chat_id, what, user_id, invited_id, version));
    }

    public boolean isMigratedChat(int chat_id) {
        Semaphore semaphore = new Semaphore(0);
        boolean[] result = new boolean[1];
        this.storageQueue.postRunnable(new AnonymousClass35(chat_id, result, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return result[0];
    }

    public void loadChatInfo(int chat_id, Semaphore semaphore, boolean force, boolean byChannelUsers) {
        this.storageQueue.postRunnable(new AnonymousClass36(chat_id, semaphore, force, byChannelUsers));
    }

    public void processPendingRead(long dialog_id, long max_id, int max_date) {
        this.storageQueue.postRunnable(new AnonymousClass37(dialog_id, max_id, max_date));
    }

    public void putContacts(ArrayList<TL_contact> contacts, boolean deleteAll) {
        if (!contacts.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass38(deleteAll, new ArrayList(contacts)));
        }
    }

    public void deleteContacts(ArrayList<Integer> uids) {
        if (uids != null && !uids.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass39(uids));
        }
    }

    public void applyPhoneBookUpdates(String adds, String deletes) {
        if (adds.length() != 0 || deletes.length() != 0) {
            this.storageQueue.postRunnable(new AnonymousClass40(adds, deletes));
        }
    }

    public void putCachedPhoneBook(HashMap<Integer, Contact> contactHashMap) {
        this.storageQueue.postRunnable(new AnonymousClass41(contactHashMap));
    }

    public void getCachedPhoneBook() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                HashMap<Integer, Contact> contactHashMap = new HashMap();
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT us.uid, us.fname, us.sname, up.phone, up.sphone, up.deleted FROM user_contacts_v6 as us LEFT JOIN user_phones_v6 as up ON us.uid = up.uid WHERE 1", new Object[0]);
                    while (cursor.next()) {
                        int uid = cursor.intValue(0);
                        Contact contact = (Contact) contactHashMap.get(Integer.valueOf(uid));
                        if (contact == null) {
                            contact = new Contact();
                            contact.first_name = cursor.stringValue(1);
                            contact.last_name = cursor.stringValue(2);
                            contact.id = uid;
                            contactHashMap.put(Integer.valueOf(uid), contact);
                        }
                        String phone = cursor.stringValue(3);
                        if (phone != null) {
                            contact.phones.add(phone);
                            String sphone = cursor.stringValue(4);
                            if (sphone != null) {
                                if (sphone.length() == 8 && phone.length() != 8) {
                                    sphone = PhoneFormat.stripExceptNumbers(phone);
                                }
                                contact.shortPhones.add(sphone);
                                contact.phoneDeleted.add(Integer.valueOf(cursor.intValue(5)));
                                contact.phoneTypes.add(TtmlNode.ANONYMOUS_REGION_ID);
                            }
                        }
                    }
                    cursor.dispose();
                } catch (Throwable e) {
                    contactHashMap.clear();
                    FileLog.m13e("tmessages", e);
                }
                ContactsController.getInstance().performSyncPhoneBook(contactHashMap, true, true, false, false);
            }
        });
    }

    public void getContacts() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                ArrayList<TL_contact> contacts = new ArrayList();
                ArrayList<User> users = new ArrayList();
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT * FROM contacts WHERE 1", new Object[0]);
                    StringBuilder uids = new StringBuilder();
                    while (cursor.next()) {
                        boolean z;
                        int user_id = cursor.intValue(0);
                        TL_contact contact = new TL_contact();
                        contact.user_id = user_id;
                        if (cursor.intValue(1) == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        contact.mutual = z;
                        if (uids.length() != 0) {
                            uids.append(",");
                        }
                        contacts.add(contact);
                        uids.append(contact.user_id);
                    }
                    cursor.dispose();
                    if (uids.length() != 0) {
                        MessagesStorage.this.getUsersInternal(uids.toString(), users);
                    }
                } catch (Throwable e) {
                    contacts.clear();
                    users.clear();
                    FileLog.m13e("tmessages", e);
                }
                ContactsController.getInstance().processLoadedContacts(contacts, users, 1);
            }
        });
    }

    public void getUnsentMessages(int count) {
        this.storageQueue.postRunnable(new AnonymousClass44(count));
    }

    public boolean checkMessageId(long dialog_id, int mid) {
        boolean[] result = new boolean[1];
        Semaphore semaphore = new Semaphore(0);
        this.storageQueue.postRunnable(new AnonymousClass45(dialog_id, mid, result, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return result[0];
    }

    public void getMessages(long dialog_id, int count, int max_id, int minDate, int classGuid, int load_type, boolean isChannel, int loadIndex) {
        this.storageQueue.postRunnable(new AnonymousClass46(count, max_id, isChannel, dialog_id, load_type, minDate, classGuid, loadIndex));
    }

    public void startTransaction(boolean useQueue) {
        if (useQueue) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.beginTransaction();
                    } catch (Throwable e) {
                        FileLog.m13e("tmessages", e);
                    }
                }
            });
            return;
        }
        try {
            this.database.beginTransaction();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
    }

    public void commitTransaction(boolean useQueue) {
        if (useQueue) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.commitTransaction();
                    } catch (Throwable e) {
                        FileLog.m13e("tmessages", e);
                    }
                }
            });
            return;
        }
        try {
            this.database.commitTransaction();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
    }

    public TLObject getSentFile(String path, int type) {
        if (path == null) {
            return null;
        }
        TLObject tLObject;
        Semaphore semaphore = new Semaphore(0);
        ArrayList<TLObject> result = new ArrayList();
        this.storageQueue.postRunnable(new AnonymousClass49(path, type, result, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        if (result.isEmpty()) {
            tLObject = null;
        } else {
            tLObject = (TLObject) result.get(0);
        }
        return tLObject;
    }

    public void putSentFile(String path, TLObject file, int type) {
        if (path != null && file != null) {
            this.storageQueue.postRunnable(new AnonymousClass50(path, file, type));
        }
    }

    public void updateEncryptedChatSeq(EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new AnonymousClass51(chat));
        }
    }

    public void updateEncryptedChatTTL(EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new AnonymousClass52(chat));
        }
    }

    public void updateEncryptedChatLayer(EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new AnonymousClass53(chat));
        }
    }

    public void updateEncryptedChat(EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new AnonymousClass54(chat));
        }
    }

    public boolean isDialogHasMessages(long did) {
        Semaphore semaphore = new Semaphore(0);
        boolean[] result = new boolean[1];
        this.storageQueue.postRunnable(new AnonymousClass55(did, result, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return result[0];
    }

    public boolean hasAuthMessage(int date) {
        Semaphore semaphore = new Semaphore(0);
        boolean[] result = new boolean[1];
        this.storageQueue.postRunnable(new AnonymousClass56(date, result, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return result[0];
    }

    public void getEncryptedChat(int chat_id, Semaphore semaphore, ArrayList<TLObject> result) {
        if (semaphore != null && result != null) {
            this.storageQueue.postRunnable(new AnonymousClass57(chat_id, result, semaphore));
        }
    }

    public void putEncryptedChat(EncryptedChat chat, User user, TL_dialog dialog) {
        if (chat != null) {
            this.storageQueue.postRunnable(new AnonymousClass58(chat, user, dialog));
        }
    }

    private String formatUserSearchName(User user) {
        StringBuilder str = new StringBuilder(TtmlNode.ANONYMOUS_REGION_ID);
        if (user.first_name != null && user.first_name.length() > 0) {
            str.append(user.first_name);
        }
        if (user.last_name != null && user.last_name.length() > 0) {
            if (str.length() > 0) {
                str.append(" ");
            }
            str.append(user.last_name);
        }
        str.append(";;;");
        if (user.username != null && user.username.length() > 0) {
            str.append(user.username);
        }
        return str.toString().toLowerCase();
    }

    private void putUsersInternal(ArrayList<User> users) throws Exception {
        if (users != null && !users.isEmpty()) {
            SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO users VALUES(?, ?, ?, ?)");
            for (int a = 0; a < users.size(); a++) {
                NativeByteBuffer data;
                User user = (User) users.get(a);
                if (user.min) {
                    SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM users WHERE uid = %d", new Object[]{Integer.valueOf(user.id)}), new Object[0]);
                    if (cursor.next()) {
                        try {
                            data = cursor.byteBufferValue(0);
                            if (data != null) {
                                User oldUser = User.TLdeserialize(data, data.readInt32(false), false);
                                data.reuse();
                                if (oldUser != null) {
                                    if (user.username != null) {
                                        oldUser.username = user.username;
                                        oldUser.flags |= 8;
                                    } else {
                                        oldUser.username = null;
                                        oldUser.flags &= -9;
                                    }
                                    if (user.photo != null) {
                                        oldUser.photo = user.photo;
                                        oldUser.flags |= 32;
                                    } else {
                                        oldUser.photo = null;
                                        oldUser.flags &= -33;
                                    }
                                    user = oldUser;
                                }
                            }
                        } catch (Throwable e) {
                            FileLog.m13e("tmessages", e);
                        }
                    }
                    cursor.dispose();
                }
                state.requery();
                data = new NativeByteBuffer(user.getObjectSize());
                user.serializeToStream(data);
                state.bindInteger(1, user.id);
                state.bindString(2, formatUserSearchName(user));
                if (user.status != null) {
                    if (user.status instanceof TL_userStatusRecently) {
                        user.status.expires = -100;
                    } else if (user.status instanceof TL_userStatusLastWeek) {
                        user.status.expires = -101;
                    } else if (user.status instanceof TL_userStatusLastMonth) {
                        user.status.expires = -102;
                    }
                    state.bindInteger(3, user.status.expires);
                } else {
                    state.bindInteger(3, 0);
                }
                state.bindByteBuffer(4, data);
                state.step();
                data.reuse();
            }
            state.dispose();
        }
    }

    private void putChatsInternal(ArrayList<Chat> chats) throws Exception {
        if (chats != null && !chats.isEmpty()) {
            SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO chats VALUES(?, ?, ?)");
            for (int a = 0; a < chats.size(); a++) {
                NativeByteBuffer data;
                Chat chat = (Chat) chats.get(a);
                if (chat.min) {
                    SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM chats WHERE uid = %d", new Object[]{Integer.valueOf(chat.id)}), new Object[0]);
                    if (cursor.next()) {
                        try {
                            data = cursor.byteBufferValue(0);
                            if (data != null) {
                                Chat oldChat = Chat.TLdeserialize(data, data.readInt32(false), false);
                                data.reuse();
                                if (oldChat != null) {
                                    oldChat.title = chat.title;
                                    oldChat.photo = chat.photo;
                                    oldChat.broadcast = chat.broadcast;
                                    oldChat.verified = chat.verified;
                                    oldChat.megagroup = chat.megagroup;
                                    oldChat.democracy = chat.democracy;
                                    if (chat.username != null) {
                                        oldChat.username = chat.username;
                                        oldChat.flags |= 64;
                                    } else {
                                        oldChat.username = null;
                                        oldChat.flags &= -65;
                                    }
                                    chat = oldChat;
                                }
                            }
                        } catch (Throwable e) {
                            FileLog.m13e("tmessages", e);
                        }
                    }
                    cursor.dispose();
                }
                state.requery();
                data = new NativeByteBuffer(chat.getObjectSize());
                chat.serializeToStream(data);
                state.bindInteger(1, chat.id);
                if (chat.title != null) {
                    state.bindString(2, chat.title.toLowerCase());
                } else {
                    state.bindString(2, TtmlNode.ANONYMOUS_REGION_ID);
                }
                state.bindByteBuffer(3, data);
                state.step();
                data.reuse();
            }
            state.dispose();
        }
    }

    public void getUsersInternal(String usersToLoad, ArrayList<User> result) throws Exception {
        if (usersToLoad != null && usersToLoad.length() != 0 && result != null) {
            SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data, status FROM users WHERE uid IN(%s)", new Object[]{usersToLoad}), new Object[0]);
            while (cursor.next()) {
                try {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        User user = User.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (user != null) {
                            if (user.status != null) {
                                user.status.expires = cursor.intValue(1);
                            }
                            result.add(user);
                        }
                    }
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
            }
            cursor.dispose();
        }
    }

    public void getChatsInternal(String chatsToLoad, ArrayList<Chat> result) throws Exception {
        if (chatsToLoad != null && chatsToLoad.length() != 0 && result != null) {
            SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM chats WHERE uid IN(%s)", new Object[]{chatsToLoad}), new Object[0]);
            while (cursor.next()) {
                try {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        Chat chat = Chat.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (chat != null) {
                            result.add(chat);
                        }
                    }
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
            }
            cursor.dispose();
        }
    }

    public void getEncryptedChatsInternal(String chatsToLoad, ArrayList<EncryptedChat> result, ArrayList<Integer> usersToLoad) throws Exception {
        if (chatsToLoad != null && chatsToLoad.length() != 0 && result != null) {
            SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data, user, g, authkey, ttl, layer, seq_in, seq_out, use_count, exchange_id, key_date, fprint, fauthkey, khash FROM enc_chats WHERE uid IN(%s)", new Object[]{chatsToLoad}), new Object[0]);
            while (cursor.next()) {
                try {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        EncryptedChat chat = EncryptedChat.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (chat != null) {
                            chat.user_id = cursor.intValue(1);
                            if (!(usersToLoad == null || usersToLoad.contains(Integer.valueOf(chat.user_id)))) {
                                usersToLoad.add(Integer.valueOf(chat.user_id));
                            }
                            chat.a_or_b = cursor.byteArrayValue(2);
                            chat.auth_key = cursor.byteArrayValue(3);
                            chat.ttl = cursor.intValue(4);
                            chat.layer = cursor.intValue(5);
                            chat.seq_in = cursor.intValue(6);
                            chat.seq_out = cursor.intValue(7);
                            int use_count = cursor.intValue(8);
                            chat.key_use_count_in = (short) (use_count >> 16);
                            chat.key_use_count_out = (short) use_count;
                            chat.exchange_id = cursor.longValue(9);
                            chat.key_create_date = cursor.intValue(10);
                            chat.future_key_fingerprint = cursor.longValue(11);
                            chat.future_auth_key = cursor.byteArrayValue(12);
                            chat.key_hash = cursor.byteArrayValue(13);
                            result.add(chat);
                        }
                    }
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
            }
            cursor.dispose();
        }
    }

    private void putUsersAndChatsInternal(ArrayList<User> users, ArrayList<Chat> chats, boolean withTransaction) {
        if (withTransaction) {
            try {
                this.database.beginTransaction();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                return;
            }
        }
        putUsersInternal(users);
        putChatsInternal(chats);
        if (withTransaction) {
            this.database.commitTransaction();
        }
    }

    public void putUsersAndChats(ArrayList<User> users, ArrayList<Chat> chats, boolean withTransaction, boolean useQueue) {
        if (users != null && users.isEmpty() && chats != null && chats.isEmpty()) {
            return;
        }
        if (useQueue) {
            this.storageQueue.postRunnable(new AnonymousClass59(users, chats, withTransaction));
        } else {
            putUsersAndChatsInternal(users, chats, withTransaction);
        }
    }

    public void removeFromDownloadQueue(long id, int type, boolean move) {
        this.storageQueue.postRunnable(new AnonymousClass60(move, type, id));
    }

    public void clearDownloadQueue(int type) {
        this.storageQueue.postRunnable(new AnonymousClass61(type));
    }

    public void getDownloadQueue(int type) {
        this.storageQueue.postRunnable(new AnonymousClass62(type));
    }

    private int getMessageMediaType(Message message) {
        if ((message instanceof TL_message_secret) && (((message.media instanceof TL_messageMediaPhoto) && message.ttl > 0 && message.ttl <= 60) || MessageObject.isVoiceMessage(message) || MessageObject.isVideoMessage(message))) {
            return 1;
        }
        if ((message.media instanceof TL_messageMediaPhoto) || MessageObject.isVideoMessage(message)) {
            return 0;
        }
        return -1;
    }

    public void putWebPages(HashMap<Long, WebPage> webPages) {
        if (webPages != null && !webPages.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass63(webPages));
        }
    }

    public void overwriteChannel(int channel_id, TL_updates_channelDifferenceTooLong difference, int newDialogType) {
        this.storageQueue.postRunnable(new AnonymousClass64(channel_id, newDialogType, difference));
    }

    public void putChannelViews(SparseArray<SparseIntArray> channelViews, boolean isChannel) {
        this.storageQueue.postRunnable(new AnonymousClass65(channelViews, isChannel));
    }

    private boolean isValidKeyboardToSave(Message message) {
        return (message.reply_markup == null || (message.reply_markup instanceof TL_replyInlineMarkup) || (message.reply_markup.selective && !message.mentioned)) ? false : true;
    }

    private void putMessagesInternal(ArrayList<Message> messages, boolean withTransaction, boolean doNotUpdateDialogDate, int downloadMask, boolean ifNoLastMessage) {
        Message lastMessage;
        SQLiteCursor cursor;
        int a;
        Integer type;
        Integer count;
        if (ifNoLastMessage) {
            try {
                lastMessage = (Message) messages.get(0);
                if (lastMessage.dialog_id == 0) {
                    if (lastMessage.to_id.user_id != 0) {
                        lastMessage.dialog_id = (long) lastMessage.to_id.user_id;
                    } else {
                        if (lastMessage.to_id.chat_id != 0) {
                            lastMessage.dialog_id = (long) (-lastMessage.to_id.chat_id);
                        } else {
                            lastMessage.dialog_id = (long) (-lastMessage.to_id.channel_id);
                        }
                    }
                }
                int lastMid = -1;
                cursor = this.database.queryFinalized("SELECT last_mid FROM dialogs WHERE did = " + lastMessage.dialog_id, new Object[0]);
                if (cursor.next()) {
                    lastMid = cursor.intValue(0);
                }
                cursor.dispose();
                if (lastMid != 0) {
                    return;
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                return;
            }
        }
        if (withTransaction) {
            this.database.beginTransaction();
        }
        HashMap<Long, Message> messagesMap = new HashMap();
        HashMap<Long, Integer> messagesCounts = new HashMap();
        HashMap<Integer, HashMap<Long, Integer>> mediaCounts = null;
        HashMap<Long, Message> botKeyboards = new HashMap();
        HashMap<Long, Long> messagesMediaIdsMap = null;
        StringBuilder messageMediaIds = null;
        HashMap<Long, Integer> mediaTypes = null;
        StringBuilder messageIds = new StringBuilder();
        HashMap<Long, Integer> dialogsReadMax = new HashMap();
        HashMap<Long, Long> messagesIdsMap = new HashMap();
        SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)");
        SQLitePreparedStatement state2 = null;
        SQLitePreparedStatement state3 = this.database.executeFast("REPLACE INTO randoms VALUES(?, ?)");
        SQLitePreparedStatement state4 = this.database.executeFast("REPLACE INTO download_queue VALUES(?, ?, ?, ?)");
        SQLitePreparedStatement state5 = this.database.executeFast("REPLACE INTO webpage_pending VALUES(?, ?)");
        for (a = 0; a < messages.size(); a++) {
            Message message = (Message) messages.get(a);
            long messageId = (long) message.id;
            if (message.dialog_id == 0) {
                if (message.to_id.user_id != 0) {
                    message.dialog_id = (long) message.to_id.user_id;
                } else {
                    if (message.to_id.chat_id != 0) {
                        message.dialog_id = (long) (-message.to_id.chat_id);
                    } else {
                        message.dialog_id = (long) (-message.to_id.channel_id);
                    }
                }
            }
            if (message.to_id.channel_id != 0) {
                messageId |= ((long) message.to_id.channel_id) << 32;
            }
            if (MessageObject.isUnread(message) && !MessageObject.isOut(message)) {
                Integer currentMaxId = (Integer) dialogsReadMax.get(Long.valueOf(message.dialog_id));
                if (currentMaxId == null) {
                    cursor = this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + message.dialog_id, new Object[0]);
                    if (cursor.next()) {
                        currentMaxId = Integer.valueOf(cursor.intValue(0));
                    } else {
                        currentMaxId = Integer.valueOf(0);
                    }
                    cursor.dispose();
                    dialogsReadMax.put(Long.valueOf(message.dialog_id), currentMaxId);
                }
                if (message.id < 0 || currentMaxId.intValue() < message.id) {
                    if (messageIds.length() > 0) {
                        messageIds.append(",");
                    }
                    messageIds.append(messageId);
                    messagesIdsMap.put(Long.valueOf(messageId), Long.valueOf(message.dialog_id));
                }
            }
            if (SharedMediaQuery.canAddMessageToMedia(message)) {
                if (messageMediaIds == null) {
                    messageMediaIds = new StringBuilder();
                    messagesMediaIdsMap = new HashMap();
                    mediaTypes = new HashMap();
                }
                if (messageMediaIds.length() > 0) {
                    messageMediaIds.append(",");
                }
                messageMediaIds.append(messageId);
                messagesMediaIdsMap.put(Long.valueOf(messageId), Long.valueOf(message.dialog_id));
                mediaTypes.put(Long.valueOf(messageId), Integer.valueOf(SharedMediaQuery.getMediaType(message)));
            }
            if (isValidKeyboardToSave(message)) {
                Message oldMessage = (Message) botKeyboards.get(Long.valueOf(message.dialog_id));
                if (oldMessage == null || oldMessage.id < message.id) {
                    botKeyboards.put(Long.valueOf(message.dialog_id), message);
                }
            }
        }
        for (Entry<Long, Message> entry : botKeyboards.entrySet()) {
            BotQuery.putBotKeyboard(((Long) entry.getKey()).longValue(), (Message) entry.getValue());
        }
        if (messageMediaIds != null) {
            cursor = this.database.queryFinalized("SELECT mid FROM media_v2 WHERE mid IN(" + messageMediaIds.toString() + ")", new Object[0]);
            while (cursor.next()) {
                messagesMediaIdsMap.remove(Long.valueOf(cursor.longValue(0)));
            }
            cursor.dispose();
            mediaCounts = new HashMap();
            for (Entry<Long, Long> entry2 : messagesMediaIdsMap.entrySet()) {
                type = (Integer) mediaTypes.get(entry2.getKey());
                HashMap<Long, Integer> counts = (HashMap) mediaCounts.get(type);
                if (counts == null) {
                    counts = new HashMap();
                    count = Integer.valueOf(0);
                    mediaCounts.put(type, counts);
                } else {
                    count = (Integer) counts.get(entry2.getValue());
                }
                if (count == null) {
                    count = Integer.valueOf(0);
                }
                counts.put(entry2.getValue(), Integer.valueOf(count.intValue() + 1));
            }
        }
        if (messageIds.length() > 0) {
            cursor = this.database.queryFinalized("SELECT mid FROM messages WHERE mid IN(" + messageIds.toString() + ")", new Object[0]);
            while (cursor.next()) {
                messagesIdsMap.remove(Long.valueOf(cursor.longValue(0)));
            }
            cursor.dispose();
            for (Long dialog_id : messagesIdsMap.values()) {
                count = (Integer) messagesCounts.get(dialog_id);
                if (count == null) {
                    count = Integer.valueOf(0);
                }
                messagesCounts.put(dialog_id, Integer.valueOf(count.intValue() + 1));
            }
        }
        int downloadMediaMask = 0;
        for (a = 0; a < messages.size(); a++) {
            message = (Message) messages.get(a);
            fixUnsupportedMedia(message);
            state.requery();
            messageId = (long) message.id;
            if (message.local_id != 0) {
                messageId = (long) message.local_id;
            }
            if (message.to_id.channel_id != 0) {
                messageId |= ((long) message.to_id.channel_id) << 32;
            }
            NativeByteBuffer data = new NativeByteBuffer(message.getObjectSize());
            message.serializeToStream(data);
            boolean updateDialog = true;
            if (message.action != null) {
                if (message.action instanceof TL_messageEncryptedAction) {
                    if (!(message.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL)) {
                        if (!(message.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages)) {
                            updateDialog = false;
                        }
                    }
                }
            }
            if (updateDialog) {
                lastMessage = (Message) messagesMap.get(Long.valueOf(message.dialog_id));
                if (lastMessage == null || message.date > lastMessage.date || ((message.id > 0 && lastMessage.id > 0 && message.id > lastMessage.id) || (message.id < 0 && lastMessage.id < 0 && message.id < lastMessage.id))) {
                    messagesMap.put(Long.valueOf(message.dialog_id), message);
                }
            }
            state.bindLong(1, messageId);
            state.bindLong(2, message.dialog_id);
            state.bindInteger(3, MessageObject.getUnreadFlags(message));
            state.bindInteger(4, message.send_state);
            state.bindInteger(5, message.date);
            state.bindByteBuffer(6, data);
            state.bindInteger(7, MessageObject.isOut(message) ? 1 : 0);
            state.bindInteger(8, message.ttl);
            if ((message.flags & MessagesController.UPDATE_MASK_PHONE) != 0) {
                state.bindInteger(9, message.views);
            } else {
                state.bindInteger(9, getMessageMediaType(message));
            }
            state.bindInteger(10, 0);
            state.step();
            if (message.random_id != 0) {
                state3.requery();
                state3.bindLong(1, message.random_id);
                state3.bindLong(2, messageId);
                state3.step();
            }
            if (SharedMediaQuery.canAddMessageToMedia(message)) {
                if (state2 == null) {
                    state2 = this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                }
                state2.requery();
                state2.bindLong(1, messageId);
                state2.bindLong(2, message.dialog_id);
                state2.bindInteger(3, message.date);
                state2.bindInteger(4, SharedMediaQuery.getMediaType(message));
                state2.bindByteBuffer(5, data);
                state2.step();
            }
            if (message.media instanceof TL_messageMediaWebPage) {
                if (message.media.webpage instanceof TL_webPagePending) {
                    state5.requery();
                    state5.bindLong(1, message.media.webpage.id);
                    state5.bindLong(2, messageId);
                    state5.step();
                }
            }
            data.reuse();
            if ((message.to_id.channel_id == 0 || message.post) && message.date >= ConnectionsManager.getInstance().getCurrentTime() - 3600 && downloadMask != 0) {
                if (!(message.media instanceof TL_messageMediaPhoto)) {
                    if (!(message.media instanceof TL_messageMediaDocument)) {
                    }
                }
                int type2 = 0;
                long id = 0;
                MessageMedia object = null;
                if (!MessageObject.isVoiceMessage(message)) {
                    if (message.media instanceof TL_messageMediaPhoto) {
                        if ((downloadMask & 1) != 0) {
                            if (FileLoader.getClosestPhotoSizeWithSize(message.media.photo.sizes, AndroidUtilities.getPhotoSize()) != null) {
                                id = message.media.photo.id;
                                type2 = 1;
                                object = new TL_messageMediaPhoto();
                                object.caption = TtmlNode.ANONYMOUS_REGION_ID;
                                object.photo = message.media.photo;
                            }
                        }
                    } else if (!MessageObject.isVideoMessage(message)) {
                        if ((message.media instanceof TL_messageMediaDocument) && !MessageObject.isMusicMessage(message)) {
                            if (!(MessageObject.isGifDocument(message.media.document) || (downloadMask & 8) == 0)) {
                                id = message.media.document.id;
                                type2 = 8;
                                object = new TL_messageMediaDocument();
                                object.caption = TtmlNode.ANONYMOUS_REGION_ID;
                                object.document = message.media.document;
                            }
                        }
                    } else if ((downloadMask & 4) != 0) {
                        id = message.media.document.id;
                        type2 = 4;
                        object = new TL_messageMediaDocument();
                        object.caption = TtmlNode.ANONYMOUS_REGION_ID;
                        object.document = message.media.document;
                    }
                } else if ((downloadMask & 2) != 0) {
                    int i = message.media.document.size;
                    if (r0 < 5242880) {
                        id = message.media.document.id;
                        type2 = 2;
                        object = new TL_messageMediaDocument();
                        object.caption = TtmlNode.ANONYMOUS_REGION_ID;
                        object.document = message.media.document;
                    }
                }
                if (object != null) {
                    downloadMediaMask |= type2;
                    state4.requery();
                    data = new NativeByteBuffer(object.getObjectSize());
                    object.serializeToStream(data);
                    state4.bindLong(1, id);
                    state4.bindInteger(2, type2);
                    state4.bindInteger(3, message.date);
                    state4.bindByteBuffer(4, data);
                    state4.step();
                    data.reuse();
                }
            }
        }
        state.dispose();
        if (state2 != null) {
            state2.dispose();
        }
        state3.dispose();
        state4.dispose();
        state5.dispose();
        state = this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        HashMap<Long, Message> dids = new HashMap();
        dids.putAll(messagesMap);
        for (Entry<Long, Message> pair : dids.entrySet()) {
            Long key = (Long) pair.getKey();
            if (key.longValue() != 0) {
                message = (Message) messagesMap.get(key);
                int channelId = 0;
                if (message != null) {
                    channelId = message.to_id.channel_id;
                }
                cursor = this.database.queryFinalized("SELECT date, unread_count, pts, last_mid, inbox_max, outbox_max FROM dialogs WHERE did = " + key, new Object[0]);
                int dialog_date = 0;
                int last_mid = 0;
                int old_unread_count = 0;
                int pts = channelId != 0 ? 1 : 0;
                int inbox_max = 0;
                int outbox_max = 0;
                if (cursor.next()) {
                    dialog_date = cursor.intValue(0);
                    old_unread_count = cursor.intValue(1);
                    pts = cursor.intValue(2);
                    last_mid = cursor.intValue(3);
                    inbox_max = cursor.intValue(4);
                    outbox_max = cursor.intValue(5);
                } else if (channelId != 0) {
                    MessagesController.getInstance().checkChannelInviter(channelId);
                }
                cursor.dispose();
                Integer unread_count = (Integer) messagesCounts.get(key);
                if (unread_count == null) {
                    unread_count = Integer.valueOf(0);
                } else {
                    messagesCounts.put(key, Integer.valueOf(unread_count.intValue() + old_unread_count));
                }
                if (message != null) {
                    messageId = (long) message.id;
                } else {
                    messageId = (long) last_mid;
                }
                if (!(message == null || message.local_id == 0)) {
                    messageId = (long) message.local_id;
                }
                if (channelId != 0) {
                    messageId |= ((long) channelId) << 32;
                }
                state.requery();
                state.bindLong(1, key.longValue());
                if (message == null || (doNotUpdateDialogDate && dialog_date != 0)) {
                    state.bindInteger(2, dialog_date);
                } else {
                    state.bindInteger(2, message.date);
                }
                state.bindInteger(3, unread_count.intValue() + old_unread_count);
                state.bindLong(4, messageId);
                state.bindInteger(5, inbox_max);
                state.bindInteger(6, outbox_max);
                state.bindLong(7, 0);
                state.bindInteger(8, 0);
                state.bindInteger(9, pts);
                state.bindInteger(10, 0);
                state.step();
            }
        }
        state.dispose();
        if (mediaCounts != null) {
            state3 = this.database.executeFast("REPLACE INTO media_counts_v2 VALUES(?, ?, ?)");
            for (Entry<Integer, HashMap<Long, Integer>> counts2 : mediaCounts.entrySet()) {
                type = (Integer) counts2.getKey();
                for (Entry<Long, Integer> pair2 : ((HashMap) counts2.getValue()).entrySet()) {
                    long uid = ((Long) pair2.getKey()).longValue();
                    int lower_part = (int) uid;
                    int count2 = -1;
                    cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT count FROM media_counts_v2 WHERE uid = %d AND type = %d LIMIT 1", new Object[]{Long.valueOf(uid), type}), new Object[0]);
                    if (cursor.next()) {
                        count2 = cursor.intValue(0);
                    }
                    cursor.dispose();
                    if (count2 != -1) {
                        state3.requery();
                        count2 += ((Integer) pair2.getValue()).intValue();
                        state3.bindLong(1, uid);
                        state3.bindInteger(2, type.intValue());
                        state3.bindInteger(3, count2);
                        state3.step();
                    }
                }
            }
            state3.dispose();
        }
        if (withTransaction) {
            this.database.commitTransaction();
        }
        MessagesController.getInstance().processDialogsUpdateRead(messagesCounts);
        if (downloadMediaMask != 0) {
            AndroidUtilities.runOnUIThread(new AnonymousClass66(downloadMediaMask));
        }
    }

    public void putMessages(ArrayList<Message> messages, boolean withTransaction, boolean useQueue, boolean doNotUpdateDialogDate, int downloadMask) {
        putMessages(messages, withTransaction, useQueue, doNotUpdateDialogDate, downloadMask, false);
    }

    public void putMessages(ArrayList<Message> messages, boolean withTransaction, boolean useQueue, boolean doNotUpdateDialogDate, int downloadMask, boolean ifNoLastMessage) {
        if (messages.size() != 0) {
            if (useQueue) {
                this.storageQueue.postRunnable(new AnonymousClass67(messages, withTransaction, doNotUpdateDialogDate, downloadMask, ifNoLastMessage));
            } else {
                putMessagesInternal(messages, withTransaction, doNotUpdateDialogDate, downloadMask, ifNoLastMessage);
            }
        }
    }

    public void markMessageAsSendError(Message message) {
        this.storageQueue.postRunnable(new AnonymousClass68(message));
    }

    public void setMessageSeq(int mid, int seq_in, int seq_out) {
        this.storageQueue.postRunnable(new AnonymousClass69(mid, seq_in, seq_out));
    }

    private long[] updateMessageStateAndIdInternal(long random_id, Integer _oldId, int newId, int date, int channelId) {
        SQLitePreparedStatement state;
        SQLiteCursor cursor = null;
        long newMessageId = (long) newId;
        if (_oldId == null) {
            try {
                cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM randoms WHERE random_id = %d LIMIT 1", new Object[]{Long.valueOf(random_id)}), new Object[0]);
                if (cursor.next()) {
                    _oldId = Integer.valueOf(cursor.intValue(0));
                }
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            if (_oldId == null) {
                return null;
            }
        }
        long oldMessageId = (long) _oldId.intValue();
        if (channelId != 0) {
            oldMessageId |= ((long) channelId) << 32;
            newMessageId |= ((long) channelId) << 32;
        }
        long did = 0;
        try {
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid FROM messages WHERE mid = %d LIMIT 1", new Object[]{Long.valueOf(oldMessageId)}), new Object[0]);
            if (cursor.next()) {
                did = cursor.longValue(0);
            }
            if (cursor != null) {
                cursor.dispose();
            }
        } catch (Throwable e2) {
            FileLog.m13e("tmessages", e2);
            if (cursor != null) {
                cursor.dispose();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.dispose();
            }
        }
        if (did == 0) {
            return null;
        }
        if (oldMessageId != newMessageId || date == 0) {
            state = null;
            try {
                state = this.database.executeFast("UPDATE messages SET mid = ?, send_state = 0 WHERE mid = ?");
                state.bindLong(1, newMessageId);
                state.bindLong(2, oldMessageId);
                state.step();
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            } catch (Exception e3) {
                try {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid = %d", new Object[]{Long.valueOf(oldMessageId)})).stepThis().dispose();
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM messages_seq WHERE mid = %d", new Object[]{Long.valueOf(oldMessageId)})).stepThis().dispose();
                } catch (Throwable e22) {
                    FileLog.m13e("tmessages", e22);
                } catch (Throwable th3) {
                    if (state != null) {
                        state.dispose();
                    }
                }
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            }
            try {
                state = this.database.executeFast("UPDATE media_v2 SET mid = ? WHERE mid = ?");
                state.bindLong(1, newMessageId);
                state.bindLong(2, oldMessageId);
                state.step();
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            } catch (Exception e4) {
                try {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE mid = %d", new Object[]{Long.valueOf(oldMessageId)})).stepThis().dispose();
                } catch (Throwable e222) {
                    FileLog.m13e("tmessages", e222);
                } catch (Throwable th4) {
                    if (state != null) {
                        state.dispose();
                    }
                }
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            }
            try {
                state = this.database.executeFast("UPDATE dialogs SET last_mid = ? WHERE last_mid = ?");
                state.bindLong(1, newMessageId);
                state.bindLong(2, oldMessageId);
                state.step();
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e23) {
                FileLog.m13e("tmessages", e23);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th5) {
                if (state != null) {
                    state.dispose();
                }
            }
            return new long[]{did, (long) _oldId.intValue()};
        }
        state = null;
        try {
            state = this.database.executeFast("UPDATE messages SET send_state = 0, date = ? WHERE mid = ?");
            state.bindInteger(1, date);
            state.bindLong(2, newMessageId);
            state.step();
            if (state != null) {
                state.dispose();
            }
        } catch (Throwable e232) {
            FileLog.m13e("tmessages", e232);
            if (state != null) {
                state.dispose();
            }
        } catch (Throwable th6) {
            if (state != null) {
                state.dispose();
            }
        }
        return new long[]{did, (long) newId};
    }

    public long[] updateMessageStateAndId(long random_id, Integer _oldId, int newId, int date, boolean useQueue, int channelId) {
        if (!useQueue) {
            return updateMessageStateAndIdInternal(random_id, _oldId, newId, date, channelId);
        }
        this.storageQueue.postRunnable(new AnonymousClass70(random_id, _oldId, newId, date, channelId));
        return null;
    }

    private void updateUsersInternal(ArrayList<User> users, boolean onlyStatus, boolean withTransaction) {
        if (Thread.currentThread().getId() != this.storageQueue.getId()) {
            throw new RuntimeException("wrong db thread");
        } else if (onlyStatus) {
            if (withTransaction) {
                try {
                    this.database.beginTransaction();
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                    return;
                }
            }
            SQLitePreparedStatement state = this.database.executeFast("UPDATE users SET status = ? WHERE uid = ?");
            i$ = users.iterator();
            while (i$.hasNext()) {
                user = (User) i$.next();
                state.requery();
                if (user.status != null) {
                    state.bindInteger(1, user.status.expires);
                } else {
                    state.bindInteger(1, 0);
                }
                state.bindInteger(2, user.id);
                state.step();
            }
            state.dispose();
            if (withTransaction) {
                this.database.commitTransaction();
            }
        } else {
            StringBuilder ids = new StringBuilder();
            HashMap<Integer, User> usersDict = new HashMap();
            i$ = users.iterator();
            while (i$.hasNext()) {
                user = (User) i$.next();
                if (ids.length() != 0) {
                    ids.append(",");
                }
                ids.append(user.id);
                usersDict.put(Integer.valueOf(user.id), user);
            }
            ArrayList<User> loadedUsers = new ArrayList();
            getUsersInternal(ids.toString(), loadedUsers);
            i$ = loadedUsers.iterator();
            while (i$.hasNext()) {
                user = (User) i$.next();
                User updateUser = (User) usersDict.get(Integer.valueOf(user.id));
                if (updateUser != null) {
                    if (updateUser.first_name != null && updateUser.last_name != null) {
                        if (!UserObject.isContact(user)) {
                            user.first_name = updateUser.first_name;
                            user.last_name = updateUser.last_name;
                        }
                        user.username = updateUser.username;
                    } else if (updateUser.photo != null) {
                        user.photo = updateUser.photo;
                    } else if (updateUser.phone != null) {
                        user.phone = updateUser.phone;
                    }
                }
            }
            if (!loadedUsers.isEmpty()) {
                if (withTransaction) {
                    this.database.beginTransaction();
                }
                putUsersInternal(loadedUsers);
                if (withTransaction) {
                    this.database.commitTransaction();
                }
            }
        }
    }

    public void updateUsers(ArrayList<User> users, boolean onlyStatus, boolean withTransaction, boolean useQueue) {
        if (!users.isEmpty()) {
            if (useQueue) {
                this.storageQueue.postRunnable(new AnonymousClass71(users, onlyStatus, withTransaction));
            } else {
                updateUsersInternal(users, onlyStatus, withTransaction);
            }
        }
    }

    private void markMessagesAsReadInternal(SparseArray<Long> inbox, SparseArray<Long> outbox, HashMap<Integer, Integer> encryptedMessages) {
        int b;
        if (inbox != null) {
            b = 0;
            while (b < inbox.size()) {
                try {
                    long messageId = ((Long) inbox.get(inbox.keyAt(b))).longValue();
                    this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 1 WHERE uid = %d AND mid > 0 AND mid <= %d AND read_state IN(0,2) AND out = 0", new Object[]{Integer.valueOf(key), Long.valueOf(messageId)})).stepThis().dispose();
                    b++;
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                    return;
                }
            }
        }
        if (outbox != null) {
            for (b = 0; b < outbox.size(); b++) {
                messageId = ((Long) outbox.get(outbox.keyAt(b))).longValue();
                this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 1 WHERE uid = %d AND mid > 0 AND mid <= %d AND read_state IN(0,2) AND out = 1", new Object[]{Integer.valueOf(key), Long.valueOf(messageId)})).stepThis().dispose();
            }
        }
        if (encryptedMessages != null && !encryptedMessages.isEmpty()) {
            for (Entry<Integer, Integer> entry : encryptedMessages.entrySet()) {
                long dialog_id = ((long) ((Integer) entry.getKey()).intValue()) << 32;
                int max_date = ((Integer) entry.getValue()).intValue();
                SQLitePreparedStatement state = this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND date <= ? AND read_state IN(0,2) AND out = 1");
                state.requery();
                state.bindLong(1, dialog_id);
                state.bindInteger(2, max_date);
                state.step();
                state.dispose();
            }
        }
    }

    public void markMessagesContentAsRead(ArrayList<Long> mids) {
        if (mids != null && !mids.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass72(mids));
        }
    }

    public void markMessagesAsRead(SparseArray<Long> inbox, SparseArray<Long> outbox, HashMap<Integer, Integer> encryptedMessages, boolean useQueue) {
        if (useQueue) {
            this.storageQueue.postRunnable(new AnonymousClass73(inbox, outbox, encryptedMessages));
        } else {
            markMessagesAsReadInternal(inbox, outbox, encryptedMessages);
        }
    }

    public void markMessagesAsDeletedByRandoms(ArrayList<Long> messages) {
        if (!messages.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass74(messages));
        }
    }

    private void markMessagesAsDeletedInternal(ArrayList<Integer> messages, int channelId) {
        String ids;
        long did;
        int unread_count = 0;
        if (channelId != 0) {
            try {
                StringBuilder builder = new StringBuilder(messages.size());
                for (int a = 0; a < messages.size(); a++) {
                    long messageId = ((long) ((Integer) messages.get(a)).intValue()) | (((long) channelId) << 32);
                    if (builder.length() > 0) {
                        builder.append(',');
                    }
                    builder.append(messageId);
                }
                ids = builder.toString();
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                return;
            }
        }
        ids = TextUtils.join(",", messages);
        Object[] objArr = new Object[0];
        SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, data, read_state FROM messages WHERE mid IN(%s)", new Object[]{ids}), r0);
        ArrayList<File> filesToDelete = new ArrayList();
        while (cursor.next()) {
            try {
                did = cursor.longValue(0);
                if (channelId != 0 && cursor.intValue(2) == 0) {
                    unread_count++;
                }
                if (((int) did) == 0) {
                    NativeByteBuffer data = cursor.byteBufferValue(1);
                    if (data != null) {
                        Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                        data.reuse();
                        if (message != null) {
                            File file;
                            if (message.media instanceof TL_messageMediaPhoto) {
                                Iterator i$ = message.media.photo.sizes.iterator();
                                while (i$.hasNext()) {
                                    file = FileLoader.getPathToAttach((PhotoSize) i$.next());
                                    if (file != null && file.toString().length() > 0) {
                                        filesToDelete.add(file);
                                    }
                                }
                            } else {
                                if (message.media instanceof TL_messageMediaDocument) {
                                    file = FileLoader.getPathToAttach(message.media.document);
                                    if (file != null && file.toString().length() > 0) {
                                        filesToDelete.add(file);
                                    }
                                    file = FileLoader.getPathToAttach(message.media.document.thumb);
                                    if (file != null && file.toString().length() > 0) {
                                        filesToDelete.add(file);
                                    }
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } catch (Throwable e2) {
                FileLog.m13e("tmessages", e2);
            }
        }
        cursor.dispose();
        FileLoader.getInstance().deleteFiles(filesToDelete, 0);
        if (!(channelId == 0 || unread_count == 0)) {
            did = (long) (-channelId);
            SQLitePreparedStatement state = this.database.executeFast("UPDATE dialogs SET unread_count = ((SELECT unread_count FROM dialogs WHERE did = ?) - ?) WHERE did = ?");
            state.requery();
            state.bindLong(1, did);
            state.bindInteger(2, unread_count);
            state.bindLong(3, did);
            state.step();
            state.dispose();
        }
        this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM bot_keyboard WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM messages_seq WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast("DELETE FROM media_counts_v2 WHERE 1").stepThis().dispose();
        BotQuery.clearBotKeyboard(0, messages);
    }

    private void updateDialogsWithDeletedMessagesInternal(ArrayList<Integer> messages, int channelId) {
        if (Thread.currentThread().getId() != this.storageQueue.getId()) {
            throw new RuntimeException("wrong db thread");
        }
        try {
            String ids;
            SQLiteCursor cursor;
            if (messages.isEmpty()) {
                ids = TtmlNode.ANONYMOUS_REGION_ID + (-channelId);
            } else {
                SQLitePreparedStatement state;
                ArrayList<Long> dialogsToUpdate = new ArrayList();
                if (channelId != 0) {
                    dialogsToUpdate.add(Long.valueOf((long) (-channelId)));
                    state = this.database.executeFast("UPDATE dialogs SET last_mid = (SELECT mid FROM messages WHERE uid = ? AND date = (SELECT MAX(date) FROM messages WHERE uid = ? )) WHERE did = ?");
                } else {
                    ids = TextUtils.join(",", messages);
                    cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT did FROM dialogs WHERE last_mid IN(%s)", new Object[]{ids}), new Object[0]);
                    while (cursor.next()) {
                        dialogsToUpdate.add(Long.valueOf(cursor.longValue(0)));
                    }
                    cursor.dispose();
                    state = this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0, last_mid = (SELECT mid FROM messages WHERE uid = ? AND date = (SELECT MAX(date) FROM messages WHERE uid = ? AND date != 0)) WHERE did = ?");
                }
                this.database.beginTransaction();
                for (int a = 0; a < dialogsToUpdate.size(); a++) {
                    long did = ((Long) dialogsToUpdate.get(a)).longValue();
                    state.requery();
                    state.bindLong(1, did);
                    state.bindLong(2, did);
                    state.bindLong(3, did);
                    state.step();
                }
                state.dispose();
                this.database.commitTransaction();
                ids = TextUtils.join(",", dialogsToUpdate);
            }
            messages_Dialogs dialogs = new messages_Dialogs();
            ArrayList<EncryptedChat> encryptedChats = new ArrayList();
            ArrayList<Integer> usersToLoad = new ArrayList();
            ArrayList<Integer> chatsToLoad = new ArrayList();
            ArrayList<Integer> encryptedToLoad = new ArrayList();
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT d.did, d.last_mid, d.unread_count, d.date, m.data, m.read_state, m.mid, m.send_state, m.date, d.pts, d.inbox_max, d.outbox_max FROM dialogs as d LEFT JOIN messages as m ON d.last_mid = m.mid WHERE d.did IN(%s)", new Object[]{ids}), new Object[0]);
            while (cursor.next()) {
                TL_dialog dialog = new TL_dialog();
                dialog.id = cursor.longValue(0);
                dialog.top_message = cursor.intValue(1);
                dialog.read_inbox_max_id = cursor.intValue(10);
                dialog.read_outbox_max_id = cursor.intValue(11);
                dialog.unread_count = cursor.intValue(2);
                dialog.last_message_date = cursor.intValue(3);
                dialog.pts = cursor.intValue(9);
                dialog.flags = channelId == 0 ? 0 : 1;
                dialogs.dialogs.add(dialog);
                NativeByteBuffer data = cursor.byteBufferValue(4);
                if (data != null) {
                    Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                    data.reuse();
                    MessageObject.setUnreadFlags(message, cursor.intValue(5));
                    message.id = cursor.intValue(6);
                    message.send_state = cursor.intValue(7);
                    int date = cursor.intValue(8);
                    if (date != 0) {
                        dialog.last_message_date = date;
                    }
                    message.dialog_id = dialog.id;
                    dialogs.messages.add(message);
                    addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                }
                int lower_id = (int) dialog.id;
                int high_id = (int) (dialog.id >> 32);
                if (lower_id == 0) {
                    if (!encryptedToLoad.contains(Integer.valueOf(high_id))) {
                        encryptedToLoad.add(Integer.valueOf(high_id));
                    }
                } else if (high_id == 1) {
                    if (!chatsToLoad.contains(Integer.valueOf(lower_id))) {
                        chatsToLoad.add(Integer.valueOf(lower_id));
                    }
                } else if (lower_id <= 0) {
                    if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                        chatsToLoad.add(Integer.valueOf(-lower_id));
                    }
                } else if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                    usersToLoad.add(Integer.valueOf(lower_id));
                }
            }
            cursor.dispose();
            if (!encryptedToLoad.isEmpty()) {
                getEncryptedChatsInternal(TextUtils.join(",", encryptedToLoad), encryptedChats, usersToLoad);
            }
            if (!chatsToLoad.isEmpty()) {
                getChatsInternal(TextUtils.join(",", chatsToLoad), dialogs.chats);
            }
            if (!usersToLoad.isEmpty()) {
                getUsersInternal(TextUtils.join(",", usersToLoad), dialogs.users);
            }
            if (!dialogs.dialogs.isEmpty() || !encryptedChats.isEmpty()) {
                MessagesController.getInstance().processDialogsUpdate(dialogs, encryptedChats);
            }
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
    }

    public void updateDialogsWithDeletedMessages(ArrayList<Integer> messages, boolean useQueue, int channelId) {
        if (!messages.isEmpty() || channelId != 0) {
            if (useQueue) {
                this.storageQueue.postRunnable(new AnonymousClass75(messages, channelId));
            } else {
                updateDialogsWithDeletedMessagesInternal(messages, channelId);
            }
        }
    }

    public void markMessagesAsDeleted(ArrayList<Integer> messages, boolean useQueue, int channelId) {
        if (!messages.isEmpty()) {
            if (useQueue) {
                this.storageQueue.postRunnable(new AnonymousClass76(messages, channelId));
            } else {
                markMessagesAsDeletedInternal(messages, channelId);
            }
        }
    }

    private void fixUnsupportedMedia(Message message) {
        if (message != null) {
            if (message.media instanceof TL_messageMediaUnsupported_old) {
                if (message.media.bytes.length == 0) {
                    message.media.bytes = new byte[1];
                    message.media.bytes[0] = (byte) 53;
                }
            } else if (message.media instanceof TL_messageMediaUnsupported) {
                message.media = new TL_messageMediaUnsupported_old();
                message.media.bytes = new byte[1];
                message.media.bytes[0] = (byte) 53;
                message.flags |= MessagesController.UPDATE_MASK_SELECT_DIALOG;
            }
        }
    }

    private void doneHolesInTable(String table, long did, int max_id) throws Exception {
        if (max_id == 0) {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM " + table + " WHERE uid = %d", new Object[]{Long.valueOf(did)})).stepThis().dispose();
        } else {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM " + table + " WHERE uid = %d AND start = 0", new Object[]{Long.valueOf(did)})).stepThis().dispose();
        }
        SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO " + table + " VALUES(?, ?, ?)");
        state.requery();
        state.bindLong(1, did);
        state.bindInteger(2, 1);
        state.bindInteger(3, 1);
        state.step();
        state.dispose();
    }

    public void doneHolesInMedia(long did, int max_id, int type) throws Exception {
        SQLitePreparedStatement state;
        if (type == -1) {
            if (max_id == 0) {
                this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d", new Object[]{Long.valueOf(did)})).stepThis().dispose();
            } else {
                this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND start = 0", new Object[]{Long.valueOf(did)})).stepThis().dispose();
            }
            state = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
            for (int a = 0; a < 5; a++) {
                state.requery();
                state.bindLong(1, did);
                state.bindInteger(2, a);
                state.bindInteger(3, 1);
                state.bindInteger(4, 1);
                state.step();
            }
            state.dispose();
            return;
        }
        if (max_id == 0) {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d", new Object[]{Long.valueOf(did), Integer.valueOf(type)})).stepThis().dispose();
        } else {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = 0", new Object[]{Long.valueOf(did), Integer.valueOf(type)})).stepThis().dispose();
        }
        state = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
        state.requery();
        state.bindLong(1, did);
        state.bindInteger(2, type);
        state.bindInteger(3, 1);
        state.bindInteger(4, 1);
        state.step();
        state.dispose();
    }

    public void closeHolesInMedia(long did, int minId, int maxId, int type) throws Exception {
        SQLiteCursor cursor;
        if (type < 0) {
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT type, start, end FROM media_holes_v2 WHERE uid = %d AND type >= 0 AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[]{Long.valueOf(did), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId)}), new Object[0]);
        } else {
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT type, start, end FROM media_holes_v2 WHERE uid = %d AND type = %d AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[]{Long.valueOf(did), Integer.valueOf(type), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId)}), new Object[0]);
        }
        ArrayList<Hole> holes = null;
        while (cursor.next()) {
            if (holes == null) {
                holes = new ArrayList();
            }
            int holeType = cursor.intValue(0);
            int start = cursor.intValue(1);
            int end = cursor.intValue(2);
            if (start != end || start != 1) {
                holes.add(new Hole(holeType, start, end));
            }
        }
        cursor.dispose();
        if (holes != null) {
            for (int a = 0; a < holes.size(); a++) {
                Hole hole = (Hole) holes.get(a);
                SQLiteDatabase sQLiteDatabase;
                Object[] objArr;
                if (maxId >= hole.end - 1 && minId <= hole.start + 1) {
                    sQLiteDatabase = this.database;
                    objArr = new Object[4];
                    objArr[0] = Long.valueOf(did);
                    objArr[1] = Integer.valueOf(hole.type);
                    objArr[2] = Integer.valueOf(hole.start);
                    objArr[3] = Integer.valueOf(hole.end);
                    sQLiteDatabase.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = %d AND end = %d", objArr)).stepThis().dispose();
                } else if (maxId >= hole.end - 1) {
                    if (hole.end != minId) {
                        try {
                            sQLiteDatabase = this.database;
                            objArr = new Object[5];
                            objArr[0] = Integer.valueOf(minId);
                            objArr[1] = Long.valueOf(did);
                            objArr[2] = Integer.valueOf(hole.type);
                            objArr[3] = Integer.valueOf(hole.start);
                            objArr[4] = Integer.valueOf(hole.end);
                            sQLiteDatabase.executeFast(String.format(Locale.US, "UPDATE media_holes_v2 SET end = %d WHERE uid = %d AND type = %d AND start = %d AND end = %d", objArr)).stepThis().dispose();
                        } catch (Throwable e) {
                            try {
                                FileLog.m13e("tmessages", e);
                            } catch (Throwable e2) {
                                FileLog.m13e("tmessages", e2);
                                return;
                            }
                        }
                    }
                    continue;
                } else if (minId > hole.start + 1) {
                    sQLiteDatabase = this.database;
                    objArr = new Object[4];
                    objArr[0] = Long.valueOf(did);
                    objArr[1] = Integer.valueOf(hole.type);
                    objArr[2] = Integer.valueOf(hole.start);
                    objArr[3] = Integer.valueOf(hole.end);
                    sQLiteDatabase.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = %d AND end = %d", objArr)).stepThis().dispose();
                    SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, hole.type);
                    state.bindInteger(3, hole.start);
                    state.bindInteger(4, minId);
                    state.step();
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, hole.type);
                    state.bindInteger(3, maxId);
                    state.bindInteger(4, hole.end);
                    state.step();
                    state.dispose();
                } else if (hole.start != maxId) {
                    try {
                        sQLiteDatabase = this.database;
                        objArr = new Object[5];
                        objArr[0] = Integer.valueOf(maxId);
                        objArr[1] = Long.valueOf(did);
                        objArr[2] = Integer.valueOf(hole.type);
                        objArr[3] = Integer.valueOf(hole.start);
                        objArr[4] = Integer.valueOf(hole.end);
                        sQLiteDatabase.executeFast(String.format(Locale.US, "UPDATE media_holes_v2 SET start = %d WHERE uid = %d AND type = %d AND start = %d AND end = %d", objArr)).stepThis().dispose();
                    } catch (Throwable e22) {
                        FileLog.m13e("tmessages", e22);
                    }
                } else {
                    continue;
                }
            }
        }
    }

    private void closeHolesInTable(String table, long did, int minId, int maxId) throws Exception {
        SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT start, end FROM " + table + " WHERE uid = %d AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[]{Long.valueOf(did), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId)}), new Object[0]);
        ArrayList<Hole> holes = null;
        while (cursor.next()) {
            if (holes == null) {
                holes = new ArrayList();
            }
            int start = cursor.intValue(0);
            int end = cursor.intValue(1);
            if (start != end || start != 1) {
                holes.add(new Hole(start, end));
            }
        }
        cursor.dispose();
        if (holes != null) {
            for (int a = 0; a < holes.size(); a++) {
                Hole hole = (Hole) holes.get(a);
                SQLiteDatabase sQLiteDatabase;
                Locale locale;
                String str;
                Object[] objArr;
                if (maxId >= hole.end - 1 && minId <= hole.start + 1) {
                    sQLiteDatabase = this.database;
                    locale = Locale.US;
                    str = "DELETE FROM " + table + " WHERE uid = %d AND start = %d AND end = %d";
                    objArr = new Object[3];
                    objArr[1] = Integer.valueOf(hole.start);
                    objArr[2] = Integer.valueOf(hole.end);
                    sQLiteDatabase.executeFast(String.format(locale, str, objArr)).stepThis().dispose();
                } else if (maxId >= hole.end - 1) {
                    if (hole.end != minId) {
                        try {
                            sQLiteDatabase = this.database;
                            locale = Locale.US;
                            str = "UPDATE " + table + " SET end = %d WHERE uid = %d AND start = %d AND end = %d";
                            objArr = new Object[4];
                            objArr[2] = Integer.valueOf(hole.start);
                            objArr[3] = Integer.valueOf(hole.end);
                            sQLiteDatabase.executeFast(String.format(locale, str, objArr)).stepThis().dispose();
                        } catch (Throwable e) {
                            FileLog.m13e("tmessages", e);
                        }
                    } else {
                        continue;
                    }
                } else if (minId > hole.start + 1) {
                    sQLiteDatabase = this.database;
                    locale = Locale.US;
                    str = "DELETE FROM " + table + " WHERE uid = %d AND start = %d AND end = %d";
                    objArr = new Object[3];
                    objArr[1] = Integer.valueOf(hole.start);
                    objArr[2] = Integer.valueOf(hole.end);
                    sQLiteDatabase.executeFast(String.format(locale, str, objArr)).stepThis().dispose();
                    SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO " + table + " VALUES(?, ?, ?)");
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, hole.start);
                    state.bindInteger(3, minId);
                    state.step();
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, maxId);
                    state.bindInteger(3, hole.end);
                    state.step();
                    state.dispose();
                } else if (hole.start != maxId) {
                    try {
                        sQLiteDatabase = this.database;
                        locale = Locale.US;
                        str = "UPDATE " + table + " SET start = %d WHERE uid = %d AND start = %d AND end = %d";
                        objArr = new Object[4];
                        objArr[2] = Integer.valueOf(hole.start);
                        objArr[3] = Integer.valueOf(hole.end);
                        sQLiteDatabase.executeFast(String.format(locale, str, objArr)).stepThis().dispose();
                    } catch (Throwable e2) {
                        try {
                            FileLog.m13e("tmessages", e2);
                        } catch (Throwable e22) {
                            FileLog.m13e("tmessages", e22);
                            return;
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }

    public void putMessages(messages_Messages messages, long dialog_id, int load_type, int max_id, boolean createDialog) {
        this.storageQueue.postRunnable(new AnonymousClass77(messages, load_type, dialog_id, max_id, createDialog));
    }

    public static void addUsersAndChatsFromMessage(Message message, ArrayList<Integer> usersToLoad, ArrayList<Integer> chatsToLoad) {
        int a;
        if (message.from_id != 0) {
            if (message.from_id > 0) {
                if (!usersToLoad.contains(Integer.valueOf(message.from_id))) {
                    usersToLoad.add(Integer.valueOf(message.from_id));
                }
            } else if (!chatsToLoad.contains(Integer.valueOf(-message.from_id))) {
                chatsToLoad.add(Integer.valueOf(-message.from_id));
            }
        }
        if (!(message.via_bot_id == 0 || usersToLoad.contains(Integer.valueOf(message.via_bot_id)))) {
            usersToLoad.add(Integer.valueOf(message.via_bot_id));
        }
        if (message.action != null) {
            if (!(message.action.user_id == 0 || usersToLoad.contains(Integer.valueOf(message.action.user_id)))) {
                usersToLoad.add(Integer.valueOf(message.action.user_id));
            }
            if (!(message.action.channel_id == 0 || chatsToLoad.contains(Integer.valueOf(message.action.channel_id)))) {
                chatsToLoad.add(Integer.valueOf(message.action.channel_id));
            }
            if (!(message.action.chat_id == 0 || chatsToLoad.contains(Integer.valueOf(message.action.chat_id)))) {
                chatsToLoad.add(Integer.valueOf(message.action.chat_id));
            }
            if (!message.action.users.isEmpty()) {
                for (a = 0; a < message.action.users.size(); a++) {
                    Integer uid = (Integer) message.action.users.get(a);
                    if (!usersToLoad.contains(uid)) {
                        usersToLoad.add(uid);
                    }
                }
            }
        }
        if (!message.entities.isEmpty()) {
            for (a = 0; a < message.entities.size(); a++) {
                MessageEntity entity = (MessageEntity) message.entities.get(a);
                if (entity instanceof TL_messageEntityMentionName) {
                    usersToLoad.add(Integer.valueOf(((TL_messageEntityMentionName) entity).user_id));
                } else if (entity instanceof TL_inputMessageEntityMentionName) {
                    usersToLoad.add(Integer.valueOf(((TL_inputMessageEntityMentionName) entity).user_id.user_id));
                }
            }
        }
        if (!(message.media == null || message.media.user_id == 0 || usersToLoad.contains(Integer.valueOf(message.media.user_id)))) {
            usersToLoad.add(Integer.valueOf(message.media.user_id));
        }
        if (message.fwd_from != null) {
            if (!(message.fwd_from.from_id == 0 || usersToLoad.contains(Integer.valueOf(message.fwd_from.from_id)))) {
                usersToLoad.add(Integer.valueOf(message.fwd_from.from_id));
            }
            if (!(message.fwd_from.channel_id == 0 || chatsToLoad.contains(Integer.valueOf(message.fwd_from.channel_id)))) {
                chatsToLoad.add(Integer.valueOf(message.fwd_from.channel_id));
            }
        }
        if (message.ttl < 0 && !chatsToLoad.contains(Integer.valueOf(-message.ttl))) {
            chatsToLoad.add(Integer.valueOf(-message.ttl));
        }
    }

    public void getDialogs(int offset, int count) {
        this.storageQueue.postRunnable(new AnonymousClass78(offset, count));
    }

    public static void createFirstHoles(long did, SQLitePreparedStatement state5, SQLitePreparedStatement state6, int messageId) throws Exception {
        int i;
        state5.requery();
        state5.bindLong(1, did);
        if (messageId == 1) {
            i = 1;
        } else {
            i = 0;
        }
        state5.bindInteger(2, i);
        state5.bindInteger(3, messageId);
        state5.step();
        for (int b = 0; b < 5; b++) {
            state6.requery();
            state6.bindLong(1, did);
            state6.bindInteger(2, b);
            if (messageId == 1) {
                i = 1;
            } else {
                i = 0;
            }
            state6.bindInteger(3, i);
            state6.bindInteger(4, messageId);
            state6.step();
        }
    }

    private void putDialogsInternal(messages_Dialogs dialogs) {
        try {
            Message message;
            this.database.beginTransaction();
            HashMap<Long, Message> new_dialogMessage = new HashMap();
            int a = 0;
            while (true) {
                if (a >= dialogs.messages.size()) {
                    break;
                }
                message = (Message) dialogs.messages.get(a);
                new_dialogMessage.put(Long.valueOf(message.dialog_id), message);
                a++;
            }
            if (!dialogs.dialogs.isEmpty()) {
                SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)");
                SQLitePreparedStatement state2 = this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                SQLitePreparedStatement state3 = this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                SQLitePreparedStatement state4 = this.database.executeFast("REPLACE INTO dialog_settings VALUES(?, ?)");
                SQLitePreparedStatement state5 = this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
                SQLitePreparedStatement state6 = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                a = 0;
                while (true) {
                    if (a >= dialogs.dialogs.size()) {
                        break;
                    }
                    TL_dialog dialog = (TL_dialog) dialogs.dialogs.get(a);
                    if (dialog.id == 0) {
                        if (dialog.peer.user_id != 0) {
                            dialog.id = (long) dialog.peer.user_id;
                        } else {
                            if (dialog.peer.chat_id != 0) {
                                dialog.id = (long) (-dialog.peer.chat_id);
                            } else {
                                dialog.id = (long) (-dialog.peer.channel_id);
                            }
                        }
                    }
                    int messageDate = 0;
                    message = (Message) new_dialogMessage.get(Long.valueOf(dialog.id));
                    if (message != null) {
                        messageDate = Math.max(message.date, 0);
                        if (isValidKeyboardToSave(message)) {
                            BotQuery.putBotKeyboard(dialog.id, message);
                        }
                        fixUnsupportedMedia(message);
                        NativeByteBuffer data = new NativeByteBuffer(message.getObjectSize());
                        message.serializeToStream(data);
                        long messageId = (long) message.id;
                        if (message.to_id.channel_id != 0) {
                            messageId |= ((long) message.to_id.channel_id) << 32;
                        }
                        state.requery();
                        state.bindLong(1, messageId);
                        state.bindLong(2, dialog.id);
                        state.bindInteger(3, MessageObject.getUnreadFlags(message));
                        state.bindInteger(4, message.send_state);
                        state.bindInteger(5, message.date);
                        state.bindByteBuffer(6, data);
                        state.bindInteger(7, MessageObject.isOut(message) ? 1 : 0);
                        state.bindInteger(8, 0);
                        state.bindInteger(9, (message.flags & MessagesController.UPDATE_MASK_PHONE) != 0 ? message.views : 0);
                        state.bindInteger(10, 0);
                        state.step();
                        if (SharedMediaQuery.canAddMessageToMedia(message)) {
                            state3.requery();
                            state3.bindLong(1, messageId);
                            state3.bindLong(2, dialog.id);
                            state3.bindInteger(3, message.date);
                            state3.bindInteger(4, SharedMediaQuery.getMediaType(message));
                            state3.bindByteBuffer(5, data);
                            state3.step();
                        }
                        data.reuse();
                        createFirstHoles(dialog.id, state5, state6, message.id);
                    }
                    long topMessage = (long) dialog.top_message;
                    if (dialog.peer.channel_id != 0) {
                        topMessage |= ((long) dialog.peer.channel_id) << 32;
                    }
                    state2.requery();
                    state2.bindLong(1, dialog.id);
                    state2.bindInteger(2, messageDate);
                    state2.bindInteger(3, dialog.unread_count);
                    state2.bindLong(4, topMessage);
                    state2.bindInteger(5, dialog.read_inbox_max_id);
                    state2.bindInteger(6, dialog.read_outbox_max_id);
                    state2.bindLong(7, 0);
                    state2.bindInteger(8, 0);
                    state2.bindInteger(9, dialog.pts);
                    state2.bindInteger(10, 0);
                    state2.step();
                    if (dialog.notify_settings != null) {
                        state4.requery();
                        state4.bindLong(1, dialog.id);
                        state4.bindInteger(2, dialog.notify_settings.mute_until != 0 ? 1 : 0);
                        state4.step();
                    }
                    a++;
                }
                state.dispose();
                state2.dispose();
                state3.dispose();
                state4.dispose();
                state5.dispose();
                state6.dispose();
            }
            putUsersInternal(dialogs.users);
            putChatsInternal(dialogs.chats);
            this.database.commitTransaction();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
    }

    public void putDialogs(messages_Dialogs dialogs) {
        if (!dialogs.dialogs.isEmpty()) {
            this.storageQueue.postRunnable(new AnonymousClass79(dialogs));
        }
    }

    public int getDialogReadMax(boolean outbox, long dialog_id) {
        Semaphore semaphore = new Semaphore(0);
        Integer[] max = new Integer[]{Integer.valueOf(0)};
        getInstance().getStorageQueue().postRunnable(new AnonymousClass80(outbox, dialog_id, max, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return max[0].intValue();
    }

    public int getChannelPtsSync(int channelId) {
        Semaphore semaphore = new Semaphore(0);
        Integer[] pts = new Integer[]{Integer.valueOf(0)};
        getInstance().getStorageQueue().postRunnable(new AnonymousClass81(channelId, pts, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return pts[0].intValue();
    }

    public User getUserSync(int user_id) {
        Semaphore semaphore = new Semaphore(0);
        User[] user = new User[1];
        getInstance().getStorageQueue().postRunnable(new AnonymousClass82(user, user_id, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return user[0];
    }

    public Chat getChatSync(int user_id) {
        Semaphore semaphore = new Semaphore(0);
        Chat[] chat = new Chat[1];
        getInstance().getStorageQueue().postRunnable(new AnonymousClass83(chat, user_id, semaphore));
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
        }
        return chat[0];
    }

    public User getUser(int user_id) {
        try {
            ArrayList<User> users = new ArrayList();
            getUsersInternal(TtmlNode.ANONYMOUS_REGION_ID + user_id, users);
            if (users.isEmpty()) {
                return null;
            }
            return (User) users.get(0);
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
            return null;
        }
    }

    public ArrayList<User> getUsers(ArrayList<Integer> uids) {
        ArrayList<User> users = new ArrayList();
        try {
            getUsersInternal(TextUtils.join(",", uids), users);
        } catch (Throwable e) {
            users.clear();
            FileLog.m13e("tmessages", e);
        }
        return users;
    }

    public Chat getChat(int chat_id) {
        try {
            ArrayList<Chat> chats = new ArrayList();
            getChatsInternal(TtmlNode.ANONYMOUS_REGION_ID + chat_id, chats);
            if (chats.isEmpty()) {
                return null;
            }
            return (Chat) chats.get(0);
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
            return null;
        }
    }

    public EncryptedChat getEncryptedChat(int chat_id) {
        try {
            ArrayList<EncryptedChat> encryptedChats = new ArrayList();
            getEncryptedChatsInternal(TtmlNode.ANONYMOUS_REGION_ID + chat_id, encryptedChats, null);
            if (encryptedChats.isEmpty()) {
                return null;
            }
            return (EncryptedChat) encryptedChats.get(0);
        } catch (Throwable e) {
            FileLog.m13e("tmessages", e);
            return null;
        }
    }
}
