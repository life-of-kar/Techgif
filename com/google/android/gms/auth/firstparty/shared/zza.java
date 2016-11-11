package com.google.android.gms.auth.firstparty.shared;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.safeparcel.zzb;
import org.telegram.messenger.exoplayer.MediaCodecAudioTrackRenderer;
import org.telegram.messenger.exoplayer.MediaCodecVideoTrackRenderer;
import org.telegram.messenger.exoplayer.extractor.ExtractorSampleSource;
import org.telegram.ui.Components.VideoPlayer;

public class zza implements Creator<FACLConfig> {
    static void zza(FACLConfig fACLConfig, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, fACLConfig.version);
        zzb.zza(parcel, 2, fACLConfig.zzYm);
        zzb.zza(parcel, 3, fACLConfig.zzYn, false);
        zzb.zza(parcel, 4, fACLConfig.zzYo);
        zzb.zza(parcel, 5, fACLConfig.zzYp);
        zzb.zza(parcel, 6, fACLConfig.zzYq);
        zzb.zza(parcel, 7, fACLConfig.zzYr);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzW(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzaT(i);
    }

    public FACLConfig zzW(Parcel parcel) {
        boolean z = false;
        int zzau = com.google.android.gms.common.internal.safeparcel.zza.zzau(parcel);
        String str = null;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        int i = 0;
        while (parcel.dataPosition() < zzau) {
            int zzat = com.google.android.gms.common.internal.safeparcel.zza.zzat(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.zza.zzca(zzat)) {
                case MediaCodecVideoTrackRenderer.MSG_SET_SURFACE /*1*/:
                    i = com.google.android.gms.common.internal.safeparcel.zza.zzg(parcel, zzat);
                    break;
                case MediaCodecAudioTrackRenderer.MSG_SET_PLAYBACK_PARAMS /*2*/:
                    z5 = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat);
                    break;
                case VideoPlayer.STATE_BUFFERING /*3*/:
                    str = com.google.android.gms.common.internal.safeparcel.zza.zzp(parcel, zzat);
                    break;
                case VideoPlayer.STATE_READY /*4*/:
                    z4 = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat);
                    break;
                case VideoPlayer.STATE_ENDED /*5*/:
                    z3 = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat);
                    break;
                case ExtractorSampleSource.DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE /*6*/:
                    z2 = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat);
                    break;
                case ConnectionResult.NETWORK_ERROR /*7*/:
                    z = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.zza.zzb(parcel, zzat);
                    break;
            }
        }
        if (parcel.dataPosition() == zzau) {
            return new FACLConfig(i, z5, str, z4, z3, z2, z);
        }
        throw new com.google.android.gms.common.internal.safeparcel.zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public FACLConfig[] zzaT(int i) {
        return new FACLConfig[i];
    }
}
