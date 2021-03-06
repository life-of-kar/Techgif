package com.google.android.gms.ads.internal.overlay;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.ads.internal.InterstitialAdParameterParcel;
import com.google.android.gms.ads.internal.util.client.VersionInfoParcel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.googlecode.mp4parser.boxes.microsoft.XtraBox;
import org.telegram.messenger.exoplayer.MediaCodecAudioTrackRenderer;
import org.telegram.messenger.exoplayer.MediaCodecVideoTrackRenderer;
import org.telegram.messenger.exoplayer.extractor.ExtractorSampleSource;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper;
import org.telegram.ui.Components.VideoPlayer;

public class zzf implements Creator<AdOverlayInfoParcel> {
    static void zza(AdOverlayInfoParcel adOverlayInfoParcel, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, adOverlayInfoParcel.versionCode);
        zzb.zza(parcel, 2, adOverlayInfoParcel.zzEA, i, false);
        zzb.zza(parcel, 3, adOverlayInfoParcel.zzfs(), false);
        zzb.zza(parcel, 4, adOverlayInfoParcel.zzft(), false);
        zzb.zza(parcel, 5, adOverlayInfoParcel.zzfu(), false);
        zzb.zza(parcel, 6, adOverlayInfoParcel.zzfv(), false);
        zzb.zza(parcel, 7, adOverlayInfoParcel.zzEF, false);
        zzb.zza(parcel, 8, adOverlayInfoParcel.zzEG);
        zzb.zza(parcel, 9, adOverlayInfoParcel.zzEH, false);
        zzb.zza(parcel, 10, adOverlayInfoParcel.zzfx(), false);
        zzb.zzc(parcel, 11, adOverlayInfoParcel.orientation);
        zzb.zzc(parcel, 12, adOverlayInfoParcel.zzEJ);
        zzb.zza(parcel, 13, adOverlayInfoParcel.url, false);
        zzb.zza(parcel, 14, adOverlayInfoParcel.zzrl, i, false);
        zzb.zza(parcel, 15, adOverlayInfoParcel.zzfw(), false);
        zzb.zza(parcel, 17, adOverlayInfoParcel.zzEM, i, false);
        zzb.zza(parcel, 16, adOverlayInfoParcel.zzEL, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzg(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzz(i);
    }

    public AdOverlayInfoParcel zzg(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        int i = 0;
        AdLauncherIntentInfoParcel adLauncherIntentInfoParcel = null;
        IBinder iBinder = null;
        IBinder iBinder2 = null;
        IBinder iBinder3 = null;
        IBinder iBinder4 = null;
        String str = null;
        boolean z = false;
        String str2 = null;
        IBinder iBinder5 = null;
        int i2 = 0;
        int i3 = 0;
        String str3 = null;
        VersionInfoParcel versionInfoParcel = null;
        IBinder iBinder6 = null;
        String str4 = null;
        InterstitialAdParameterParcel interstitialAdParameterParcel = null;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case MediaCodecVideoTrackRenderer.MSG_SET_SURFACE /*1*/:
                    i = zza.zzg(parcel, zzat);
                    break;
                case MediaCodecAudioTrackRenderer.MSG_SET_PLAYBACK_PARAMS /*2*/:
                    adLauncherIntentInfoParcel = (AdLauncherIntentInfoParcel) zza.zza(parcel, zzat, AdLauncherIntentInfoParcel.CREATOR);
                    break;
                case VideoPlayer.STATE_BUFFERING /*3*/:
                    iBinder = zza.zzq(parcel, zzat);
                    break;
                case VideoPlayer.STATE_READY /*4*/:
                    iBinder2 = zza.zzq(parcel, zzat);
                    break;
                case VideoPlayer.STATE_ENDED /*5*/:
                    iBinder3 = zza.zzq(parcel, zzat);
                    break;
                case ExtractorSampleSource.DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE /*6*/:
                    iBinder4 = zza.zzq(parcel, zzat);
                    break;
                case ConnectionResult.NETWORK_ERROR /*7*/:
                    str = zza.zzp(parcel, zzat);
                    break;
                case XtraBox.MP4_XTRA_BT_UNICODE /*8*/:
                    z = zza.zzc(parcel, zzat);
                    break;
                case ConnectionResult.SERVICE_INVALID /*9*/:
                    str2 = zza.zzp(parcel, zzat);
                    break;
                case ConnectionResult.DEVELOPER_ERROR /*10*/:
                    iBinder5 = zza.zzq(parcel, zzat);
                    break;
                case ConnectionResult.LICENSE_CHECK_FAILED /*11*/:
                    i2 = zza.zzg(parcel, zzat);
                    break;
                case Atom.FULL_HEADER_SIZE /*12*/:
                    i3 = zza.zzg(parcel, zzat);
                    break;
                case ConnectionResult.CANCELED /*13*/:
                    str3 = zza.zzp(parcel, zzat);
                    break;
                case ConnectionResult.TIMEOUT /*14*/:
                    versionInfoParcel = (VersionInfoParcel) zza.zza(parcel, zzat, VersionInfoParcel.CREATOR);
                    break;
                case ConnectionResult.INTERRUPTED /*15*/:
                    iBinder6 = zza.zzq(parcel, zzat);
                    break;
                case ItemTouchHelper.START /*16*/:
                    str4 = zza.zzp(parcel, zzat);
                    break;
                case ConnectionResult.SIGN_IN_FAILED /*17*/:
                    interstitialAdParameterParcel = (InterstitialAdParameterParcel) zza.zza(parcel, zzat, InterstitialAdParameterParcel.CREATOR);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
        }
        if (parcel.dataPosition() == zzau) {
            return new AdOverlayInfoParcel(i, adLauncherIntentInfoParcel, iBinder, iBinder2, iBinder3, iBinder4, str, z, str2, iBinder5, i2, i3, str3, versionInfoParcel, iBinder6, str4, interstitialAdParameterParcel);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public AdOverlayInfoParcel[] zzz(int i) {
        return new AdOverlayInfoParcel[i];
    }
}
