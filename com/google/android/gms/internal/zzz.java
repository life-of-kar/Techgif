package com.google.android.gms.internal;

import com.google.android.gms.common.ConnectionResult;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.telegram.messenger.exoplayer.MediaCodecAudioTrackRenderer;
import org.telegram.messenger.exoplayer.MediaCodecVideoTrackRenderer;
import org.telegram.messenger.exoplayer.extractor.ExtractorSampleSource;
import org.telegram.messenger.volley.toolbox.HttpClientStack.HttpPatch;
import org.telegram.ui.Components.VideoPlayer;

public class zzz implements zzy {
    private final zza zzaE;
    private final SSLSocketFactory zzaF;

    public interface zza {
        String zzh(String str);
    }

    public zzz() {
        this(null);
    }

    public zzz(zza com_google_android_gms_internal_zzz_zza) {
        this(com_google_android_gms_internal_zzz_zza, null);
    }

    public zzz(zza com_google_android_gms_internal_zzz_zza, SSLSocketFactory sSLSocketFactory) {
        this.zzaE = com_google_android_gms_internal_zzz_zza;
        this.zzaF = sSLSocketFactory;
    }

    private HttpURLConnection zza(URL url, zzk<?> com_google_android_gms_internal_zzk_) throws IOException {
        HttpURLConnection zza = zza(url);
        int zzt = com_google_android_gms_internal_zzk_.zzt();
        zza.setConnectTimeout(zzt);
        zza.setReadTimeout(zzt);
        zza.setUseCaches(false);
        zza.setDoInput(true);
        if ("https".equals(url.getProtocol()) && this.zzaF != null) {
            ((HttpsURLConnection) zza).setSSLSocketFactory(this.zzaF);
        }
        return zza;
    }

    private static HttpEntity zza(HttpURLConnection httpURLConnection) {
        InputStream inputStream;
        HttpEntity basicHttpEntity = new BasicHttpEntity();
        try {
            inputStream = httpURLConnection.getInputStream();
        } catch (IOException e) {
            inputStream = httpURLConnection.getErrorStream();
        }
        basicHttpEntity.setContent(inputStream);
        basicHttpEntity.setContentLength((long) httpURLConnection.getContentLength());
        basicHttpEntity.setContentEncoding(httpURLConnection.getContentEncoding());
        basicHttpEntity.setContentType(httpURLConnection.getContentType());
        return basicHttpEntity;
    }

    static void zza(HttpURLConnection httpURLConnection, zzk<?> com_google_android_gms_internal_zzk_) throws IOException, zza {
        switch (com_google_android_gms_internal_zzk_.getMethod()) {
            case VideoPlayer.TRACK_DISABLED /*-1*/:
                byte[] zzm = com_google_android_gms_internal_zzk_.zzm();
                if (zzm != null) {
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.addRequestProperty("Content-Type", com_google_android_gms_internal_zzk_.zzl());
                    DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                    dataOutputStream.write(zzm);
                    dataOutputStream.close();
                }
            case VideoPlayer.TRACK_DEFAULT /*0*/:
                httpURLConnection.setRequestMethod("GET");
            case MediaCodecVideoTrackRenderer.MSG_SET_SURFACE /*1*/:
                httpURLConnection.setRequestMethod("POST");
                zzb(httpURLConnection, com_google_android_gms_internal_zzk_);
            case MediaCodecAudioTrackRenderer.MSG_SET_PLAYBACK_PARAMS /*2*/:
                httpURLConnection.setRequestMethod("PUT");
                zzb(httpURLConnection, com_google_android_gms_internal_zzk_);
            case VideoPlayer.STATE_BUFFERING /*3*/:
                httpURLConnection.setRequestMethod("DELETE");
            case VideoPlayer.STATE_READY /*4*/:
                httpURLConnection.setRequestMethod("HEAD");
            case VideoPlayer.STATE_ENDED /*5*/:
                httpURLConnection.setRequestMethod("OPTIONS");
            case ExtractorSampleSource.DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE /*6*/:
                httpURLConnection.setRequestMethod("TRACE");
            case ConnectionResult.NETWORK_ERROR /*7*/:
                httpURLConnection.setRequestMethod(HttpPatch.METHOD_NAME);
                zzb(httpURLConnection, com_google_android_gms_internal_zzk_);
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void zzb(HttpURLConnection httpURLConnection, zzk<?> com_google_android_gms_internal_zzk_) throws IOException, zza {
        byte[] zzq = com_google_android_gms_internal_zzk_.zzq();
        if (zzq != null) {
            httpURLConnection.setDoOutput(true);
            httpURLConnection.addRequestProperty("Content-Type", com_google_android_gms_internal_zzk_.zzp());
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(zzq);
            dataOutputStream.close();
        }
    }

    protected HttpURLConnection zza(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    public HttpResponse zza(zzk<?> com_google_android_gms_internal_zzk_, Map<String, String> map) throws IOException, zza {
        String zzh;
        String url = com_google_android_gms_internal_zzk_.getUrl();
        HashMap hashMap = new HashMap();
        hashMap.putAll(com_google_android_gms_internal_zzk_.getHeaders());
        hashMap.putAll(map);
        if (this.zzaE != null) {
            zzh = this.zzaE.zzh(url);
            if (zzh == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
        }
        zzh = url;
        HttpURLConnection zza = zza(new URL(zzh), (zzk) com_google_android_gms_internal_zzk_);
        for (String zzh2 : hashMap.keySet()) {
            zza.addRequestProperty(zzh2, (String) hashMap.get(zzh2));
        }
        zza(zza, (zzk) com_google_android_gms_internal_zzk_);
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        if (zza.getResponseCode() == -1) {
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        HttpResponse basicHttpResponse = new BasicHttpResponse(new BasicStatusLine(protocolVersion, zza.getResponseCode(), zza.getResponseMessage()));
        basicHttpResponse.setEntity(zza(zza));
        for (Entry entry : zza.getHeaderFields().entrySet()) {
            if (entry.getKey() != null) {
                basicHttpResponse.addHeader(new BasicHeader((String) entry.getKey(), (String) ((List) entry.getValue()).get(0)));
            }
        }
        return basicHttpResponse;
    }
}