package com.av7bible.av7bibleappv3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.TextView;

//this class allows images to be intermixed with text in the layout xmls
public class TextViewWithImages extends TextView {

    public TextViewWithImages(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }
    public TextViewWithImages(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TextViewWithImages(Context context) {
        super(context);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable s = getTextWithImages(getContext(), text, this.getLineHeight());
        super.setText(s, BufferType.SPANNABLE);
    }

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private static boolean addImages(Context context, Spannable spannable, float height) {
        Pattern refImg = Pattern.compile("\\Q[img src=\\E([a-zA-Z0-9_]+?)\\Q/]\\E");
        boolean hasChanges = false;

        Matcher matcher = refImg.matcher(spannable);
        while (matcher.find()) {
            boolean set = true;
            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()
                        ) {
                    spannable.removeSpan(span);
                } else {
                    set = false;
                    break;
                }
            }
            String resName = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim();
            int id = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
            Drawable mDrawable = context.getResources().getDrawable(id);
            mDrawable.setBounds(0, 0, (int)height, (int)height);
            if (set) {
                hasChanges = true;
                spannable.setSpan(  new ImageSpan(mDrawable),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        return hasChanges;
    }
    private static Spannable getTextWithImages(Context context, CharSequence text, float height) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addImages(context, spannable, height);
        return spannable;
    }


}