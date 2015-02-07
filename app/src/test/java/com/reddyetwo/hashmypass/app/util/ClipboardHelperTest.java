package com.reddyetwo.hashmypass.app.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClipData.class, Toast.class})
public class ClipboardHelperTest {

    @Mock
    private Context context;

    @Mock
    private ClipboardManager clipboardManager;

    @Mock
    private ClipData clip;

    @Mock
    private Toast toast;

    private String label = "LABEL";

    private String text = "TEXT";

    @StringRes
    private int toastMessageId = 1;

    @Before
    public void setUp() throws Exception {
        mockStatic(ClipData.class);
        when(ClipData.newPlainText(label, text)).thenReturn(clip);

        when(context.getSystemService(Context.CLIPBOARD_SERVICE)).thenReturn(clipboardManager);
    }

    @Test
    public void copyToClipboardShouldSetPrimaryClip() {
        // Given

        // When
        ClipboardHelper.copyToClipboard(context, label, text, 0);

        // Then
        verify(clipboardManager).setPrimaryClip(clip);
    }

    @Test
    public void copyToClipboardShouldShowToastWhenIdIsGreaterThanZero() {
        // Given
        mockStatic(Toast.class);
        when(Toast.makeText(context, toastMessageId, Toast.LENGTH_LONG)).thenReturn(toast);

        // When
        ClipboardHelper.copyToClipboard(context, label, text, toastMessageId);

        // Then
        verify(toast).show();
    }

    @Test
    public void copyToClipboardShouldNotShownToastWhenIdIsZero() {
        // Given
        mockStatic(Toast.class);
        when(Toast.makeText(context, toastMessageId, Toast.LENGTH_LONG)).thenReturn(toast);

        // When
        ClipboardHelper.copyToClipboard(context, label, text, 0);

        // Then
        verify(toast, never()).show();
    }
}