package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.exoplayer.C0747C;
import org.telegram.ui.Components.LayoutHelper;

public class HeaderCell extends FrameLayout {
    private TextView textView;

    public HeaderCell(Context context) {
        int i;
        int i2 = 5;
        super(context);
        this.textView = new TextView(getContext());
        this.textView.setTextSize(1, 15.0f);
        this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.textView.setTextColor(-12676913);
        TextView textView = this.textView;
        if (LocaleController.isRTL) {
            i = 5;
        } else {
            i = 3;
        }
        textView.setGravity(i | 16);
        View view = this.textView;
        if (!LocaleController.isRTL) {
            i2 = 3;
        }
        addView(view, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, i2 | 48, 17.0f, 15.0f, 17.0f, 0.0f));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(38.0f), C0747C.ENCODING_PCM_32BIT));
    }

    public void setText(String text) {
        this.textView.setText(text);
    }
}
