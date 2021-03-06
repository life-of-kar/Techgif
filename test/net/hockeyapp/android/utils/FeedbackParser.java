package net.hockeyapp.android.utils;

public class FeedbackParser {

    private static class FeedbackParserHolder {
        public static final FeedbackParser INSTANCE;

        private FeedbackParserHolder() {
        }

        static {
            INSTANCE = new FeedbackParser();
        }
    }

    private FeedbackParser() {
    }

    public static FeedbackParser getInstance() {
        return FeedbackParserHolder.INSTANCE;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public net.hockeyapp.android.objects.FeedbackResponse parseFeedbackResponse(java.lang.String r39) {
        /*
        r38 = this;
        r16 = 0;
        r10 = 0;
        if (r39 == 0) goto L_0x0283;
    L_0x0005:
        r23 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x02ad }
        r0 = r23;
        r1 = r39;
        r0.<init>(r1);	 Catch:{ JSONException -> 0x02ad }
        r36 = "feedback";
        r0 = r23;
        r1 = r36;
        r15 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x02ad }
        r11 = new net.hockeyapp.android.objects.Feedback;	 Catch:{ JSONException -> 0x02ad }
        r11.<init>();	 Catch:{ JSONException -> 0x02ad }
        r36 = "messages";
        r0 = r36;
        r25 = r15.getJSONArray(r0);	 Catch:{ JSONException -> 0x0289 }
        r24 = 0;
        r14 = 0;
        r36 = r25.length();	 Catch:{ JSONException -> 0x0289 }
        if (r36 <= 0) goto L_0x0207;
    L_0x002e:
        r24 = new java.util.ArrayList;	 Catch:{ JSONException -> 0x0289 }
        r24.<init>();	 Catch:{ JSONException -> 0x0289 }
        r19 = 0;
    L_0x0035:
        r36 = r25.length();	 Catch:{ JSONException -> 0x0289 }
        r0 = r19;
        r1 = r36;
        if (r0 >= r1) goto L_0x0207;
    L_0x003f:
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "subject";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r30 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "text";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r31 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "oem";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r28 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "model";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r26 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "os_version";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r29 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "created_at";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r8 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "id";
        r20 = r36.getInt(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "token";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r32 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "via";
        r35 = r36.getInt(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "user_string";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r34 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "clean_text";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r7 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "name";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r27 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "app_id";
        r36 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r2 = r36.toString();	 Catch:{ JSONException -> 0x0289 }
        r0 = r25;
        r1 = r19;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "attachments";
        r22 = r36.optJSONArray(r37);	 Catch:{ JSONException -> 0x0289 }
        r13 = java.util.Collections.emptyList();	 Catch:{ JSONException -> 0x0289 }
        if (r22 == 0) goto L_0x01bb;
    L_0x0135:
        r13 = new java.util.ArrayList;	 Catch:{ JSONException -> 0x0289 }
        r13.<init>();	 Catch:{ JSONException -> 0x0289 }
        r21 = 0;
    L_0x013c:
        r36 = r22.length();	 Catch:{ JSONException -> 0x0289 }
        r0 = r21;
        r1 = r36;
        if (r0 >= r1) goto L_0x01bb;
    L_0x0146:
        r0 = r22;
        r1 = r21;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "id";
        r4 = r36.getInt(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r22;
        r1 = r21;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "feedback_message_id";
        r5 = r36.getInt(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r22;
        r1 = r21;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "file_name";
        r18 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r22;
        r1 = r21;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "url";
        r33 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r22;
        r1 = r21;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "created_at";
        r3 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r0 = r22;
        r1 = r21;
        r36 = r0.getJSONObject(r1);	 Catch:{ JSONException -> 0x0289 }
        r37 = "updated_at";
        r6 = r36.getString(r37);	 Catch:{ JSONException -> 0x0289 }
        r12 = new net.hockeyapp.android.objects.FeedbackAttachment;	 Catch:{ JSONException -> 0x0289 }
        r12.<init>();	 Catch:{ JSONException -> 0x0289 }
        r12.setId(r4);	 Catch:{ JSONException -> 0x0289 }
        r12.setMessageId(r5);	 Catch:{ JSONException -> 0x0289 }
        r0 = r18;
        r12.setFilename(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r33;
        r12.setUrl(r0);	 Catch:{ JSONException -> 0x0289 }
        r12.setCreatedAt(r3);	 Catch:{ JSONException -> 0x0289 }
        r12.setUpdatedAt(r6);	 Catch:{ JSONException -> 0x0289 }
        r13.add(r12);	 Catch:{ JSONException -> 0x0289 }
        r21 = r21 + 1;
        goto L_0x013c;
    L_0x01bb:
        r14 = new net.hockeyapp.android.objects.FeedbackMessage;	 Catch:{ JSONException -> 0x0289 }
        r14.<init>();	 Catch:{ JSONException -> 0x0289 }
        r14.setAppId(r2);	 Catch:{ JSONException -> 0x0289 }
        r14.setCleanText(r7);	 Catch:{ JSONException -> 0x0289 }
        r14.setCreatedAt(r8);	 Catch:{ JSONException -> 0x0289 }
        r0 = r20;
        r14.setId(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r26;
        r14.setModel(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r27;
        r14.setName(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r28;
        r14.setOem(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r29;
        r14.setOsVersion(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r30;
        r14.setSubjec(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r31;
        r14.setText(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r32;
        r14.setToken(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r34;
        r14.setUserString(r0);	 Catch:{ JSONException -> 0x0289 }
        r0 = r35;
        r14.setVia(r0);	 Catch:{ JSONException -> 0x0289 }
        r14.setFeedbackAttachments(r13);	 Catch:{ JSONException -> 0x0289 }
        r0 = r24;
        r0.add(r14);	 Catch:{ JSONException -> 0x0289 }
        r19 = r19 + 1;
        goto L_0x0035;
    L_0x0207:
        r0 = r24;
        r11.setMessages(r0);	 Catch:{ JSONException -> 0x0289 }
        r36 = "name";
        r0 = r36;
        r36 = r15.getString(r0);	 Catch:{ JSONException -> 0x0284 }
        r36 = r36.toString();	 Catch:{ JSONException -> 0x0284 }
        r0 = r36;
        r11.setName(r0);	 Catch:{ JSONException -> 0x0284 }
    L_0x021d:
        r36 = "email";
        r0 = r36;
        r36 = r15.getString(r0);	 Catch:{ JSONException -> 0x028f }
        r36 = r36.toString();	 Catch:{ JSONException -> 0x028f }
        r0 = r36;
        r11.setEmail(r0);	 Catch:{ JSONException -> 0x028f }
    L_0x022e:
        r36 = "id";
        r0 = r36;
        r36 = r15.getInt(r0);	 Catch:{ JSONException -> 0x0294 }
        r0 = r36;
        r11.setId(r0);	 Catch:{ JSONException -> 0x0294 }
    L_0x023b:
        r36 = "created_at";
        r0 = r36;
        r36 = r15.getString(r0);	 Catch:{ JSONException -> 0x0299 }
        r36 = r36.toString();	 Catch:{ JSONException -> 0x0299 }
        r0 = r36;
        r11.setCreatedAt(r0);	 Catch:{ JSONException -> 0x0299 }
    L_0x024c:
        r17 = new net.hockeyapp.android.objects.FeedbackResponse;	 Catch:{ JSONException -> 0x0289 }
        r17.<init>();	 Catch:{ JSONException -> 0x0289 }
        r0 = r17;
        r0.setFeedback(r11);	 Catch:{ JSONException -> 0x02a3 }
        r36 = "status";
        r0 = r23;
        r1 = r36;
        r36 = r0.getString(r1);	 Catch:{ JSONException -> 0x029e }
        r36 = r36.toString();	 Catch:{ JSONException -> 0x029e }
        r0 = r17;
        r1 = r36;
        r0.setStatus(r1);	 Catch:{ JSONException -> 0x029e }
    L_0x026b:
        r36 = "token";
        r0 = r23;
        r1 = r36;
        r36 = r0.getString(r1);	 Catch:{ JSONException -> 0x02a8 }
        r36 = r36.toString();	 Catch:{ JSONException -> 0x02a8 }
        r0 = r17;
        r1 = r36;
        r0.setToken(r1);	 Catch:{ JSONException -> 0x02a8 }
    L_0x0280:
        r10 = r11;
        r16 = r17;
    L_0x0283:
        return r16;
    L_0x0284:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ JSONException -> 0x0289 }
        goto L_0x021d;
    L_0x0289:
        r9 = move-exception;
        r10 = r11;
    L_0x028b:
        r9.printStackTrace();
        goto L_0x0283;
    L_0x028f:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ JSONException -> 0x0289 }
        goto L_0x022e;
    L_0x0294:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ JSONException -> 0x0289 }
        goto L_0x023b;
    L_0x0299:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ JSONException -> 0x0289 }
        goto L_0x024c;
    L_0x029e:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ JSONException -> 0x02a3 }
        goto L_0x026b;
    L_0x02a3:
        r9 = move-exception;
        r10 = r11;
        r16 = r17;
        goto L_0x028b;
    L_0x02a8:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ JSONException -> 0x02a3 }
        goto L_0x0280;
    L_0x02ad:
        r9 = move-exception;
        goto L_0x028b;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.hockeyapp.android.utils.FeedbackParser.parseFeedbackResponse(java.lang.String):net.hockeyapp.android.objects.FeedbackResponse");
    }
}
