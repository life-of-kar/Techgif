package com.google.android.gms.ads.internal.overlay;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.googlecode.mp4parser.boxes.microsoft.XtraBox;
import org.telegram.messenger.exoplayer.MediaCodecAudioTrackRenderer;
import org.telegram.messenger.exoplayer.MediaCodecVideoTrackRenderer;
import org.telegram.messenger.exoplayer.extractor.ExtractorSampleSource;
import org.telegram.ui.Components.VideoPlayer;

public class zzb implements Creator<AdLauncherIntentInfoParcel> {
    static void zza(AdLauncherIntentInfoParcel adLauncherIntentInfoParcel, Parcel parcel, int i) {
        int zzav = com.google.android.gms.common.internal.safeparcel.zzb.zzav(parcel);
        com.google.android.gms.common.internal.safeparcel.zzb.zzc(parcel, 1, adLauncherIntentInfoParcel.versionCode);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 2, adLauncherIntentInfoParcel.intentAction, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 3, adLauncherIntentInfoParcel.url, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 4, adLauncherIntentInfoParcel.mimeType, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 5, adLauncherIntentInfoParcel.packageName, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 6, adLauncherIntentInfoParcel.zzDK, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 7, adLauncherIntentInfoParcel.zzDL, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 8, adLauncherIntentInfoParcel.zzDM, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 9, adLauncherIntentInfoParcel.intent, i, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzf(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzv(i);
    }

    public AdLauncherIntentInfoParcel zzf(Parcel parcel) {
        Intent intent = null;
        int zzau = zza.zzau(parcel);
        int i = 0;
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String str7 = null;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case MediaCodecVideoTrackRenderer.MSG_SET_SURFACE /*1*/:
                    i = zza.zzg(parcel, zzat);
                    break;
                case MediaCodecAudioTrackRenderer.MSG_SET_PLAYBACK_PARAMS /*2*/:
                    str7 = zza.zzp(parcel, zzat);
                    break;
                case VideoPlayer.STATE_BUFFERING /*3*/:
                    str6 = zza.zzp(parcel, zzat);
                    break;
                case VideoPlayer.STATE_READY /*4*/:
                    str5 = zza.zzp(parcel, zzat);
                    break;
                case VideoPlayer.STATE_ENDED /*5*/:
                    str4 = zza.zzp(parcel, zzat);
                    break;
                case ExtractorSampleSource.DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE /*6*/:
                    str3 = zza.zzp(parcel, zzat);
                    break;
                case ConnectionResult.NETWORK_ERROR /*7*/:
                    str2 = zza.zzp(parcel, zzat);
                    break;
                case XtraBox.MP4_XTRA_BT_UNICODE /*8*/:
                    str = zza.zzp(parcel, zzat);
                    break;
                case ConnectionResult.SERVICE_INVALID /*9*/:
                    intent = (Intent) zza.zza(parcel, zzat, Intent.CREATOR);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
        }
        if (parcel.dataPosition() == zzau) {
            return new AdLauncherIntentInfoParcel(i, str7, str6, str5, str4, str3, str2, str, intent);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public AdLauncherIntentInfoParcel[] zzv(int i) {
        return new AdLauncherIntentInfoParcel[i];
    }
}
