package com.google.android.gms.internal;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import com.google.android.gms.C0159R;
import com.google.android.gms.ads.internal.zzr;
import java.util.Map;

@zzhb
public class zzfm extends zzfs {
    private final Context mContext;
    private String zzCU;
    private long zzCV;
    private long zzCW;
    private String zzCX;
    private String zzCY;
    private final Map<String, String> zzxA;

    /* renamed from: com.google.android.gms.internal.zzfm.1 */
    class C02701 implements OnClickListener {
        final /* synthetic */ zzfm zzCZ;

        C02701(zzfm com_google_android_gms_internal_zzfm) {
            this.zzCZ = com_google_android_gms_internal_zzfm;
        }

        public void onClick(DialogInterface dialog, int which) {
            zzr.zzbC().zzb(this.zzCZ.mContext, this.zzCZ.createIntent());
        }
    }

    /* renamed from: com.google.android.gms.internal.zzfm.2 */
    class C02712 implements OnClickListener {
        final /* synthetic */ zzfm zzCZ;

        C02712(zzfm com_google_android_gms_internal_zzfm) {
            this.zzCZ = com_google_android_gms_internal_zzfm;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.zzCZ.zzam("Operation denied by user.");
        }
    }

    public zzfm(zzjp com_google_android_gms_internal_zzjp, Map<String, String> map) {
        super(com_google_android_gms_internal_zzjp, "createCalendarEvent");
        this.zzxA = map;
        this.mContext = com_google_android_gms_internal_zzjp.zzhP();
        zzeK();
    }

    private String zzaj(String str) {
        return TextUtils.isEmpty((CharSequence) this.zzxA.get(str)) ? TtmlNode.ANONYMOUS_REGION_ID : (String) this.zzxA.get(str);
    }

    private long zzak(String str) {
        String str2 = (String) this.zzxA.get(str);
        if (str2 == null) {
            return -1;
        }
        try {
            return Long.parseLong(str2);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void zzeK() {
        this.zzCU = zzaj("description");
        this.zzCX = zzaj("summary");
        this.zzCV = zzak("start_ticks");
        this.zzCW = zzak("end_ticks");
        this.zzCY = zzaj("location");
    }

    @TargetApi(14)
    Intent createIntent() {
        Intent data = new Intent("android.intent.action.EDIT").setData(Events.CONTENT_URI);
        data.putExtra("title", this.zzCU);
        data.putExtra("eventLocation", this.zzCY);
        data.putExtra("description", this.zzCX);
        if (this.zzCV > -1) {
            data.putExtra("beginTime", this.zzCV);
        }
        if (this.zzCW > -1) {
            data.putExtra("endTime", this.zzCW);
        }
        data.setFlags(268435456);
        return data;
    }

    public void execute() {
        if (this.mContext == null) {
            zzam("Activity context is not available.");
        } else if (zzr.zzbC().zzM(this.mContext).zzdo()) {
            Builder zzL = zzr.zzbC().zzL(this.mContext);
            zzL.setTitle(zzr.zzbF().zzd(C0159R.string.create_calendar_title, "Create calendar event"));
            zzL.setMessage(zzr.zzbF().zzd(C0159R.string.create_calendar_message, "Allow Ad to create a calendar event?"));
            zzL.setPositiveButton(zzr.zzbF().zzd(C0159R.string.accept, "Accept"), new C02701(this));
            zzL.setNegativeButton(zzr.zzbF().zzd(C0159R.string.decline, "Decline"), new C02712(this));
            zzL.create().show();
        } else {
            zzam("This feature is not available on the device.");
        }
    }
}
