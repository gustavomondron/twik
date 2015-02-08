package com.reddyetwo.hashmypass.app.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Toast.class)
public class HelpToastOnLongPressClickListenerTest {

    @Mock
    private View view;

    @Mock
    private CharSequence text;

    @Mock
    private Context context;

    @Mock
    private Toast toast;

    @Before
    public void setUp() throws Exception {
        mockStatic(Toast.class);
        when(view.getContext()).thenReturn(context);
        when(Toast.makeText(context, text, Toast.LENGTH_SHORT)).thenReturn(toast);
        PowerMockito.doNothing().when(toast).show();
    }

    @Test
    public void shouldShowToastOnLongClick() {
        // Given
        when(view.getContentDescription()).thenReturn(text);
        when(text.length()).thenReturn(1);

        // When
        new HelpToastOnLongPressClickListener().onLongClick(view);

        // Then
        verify(toast).show();
    }

    @Test
    public void shouldNotShowToastOnLongClickWhenContentIsNull() {
        // Given

        // When
        new HelpToastOnLongPressClickListener().onLongClick(view);

        // Then
        verify(toast, never()).show();
    }

    @Test
    public void shouldNotShowToastOnLongClickWhenContentIsEmpty() {
        // Given
        when(view.getContentDescription()).thenReturn(text);
        when(text.length()).thenReturn(0);

        // When
        new HelpToastOnLongPressClickListener().onLongClick(view);

        // Then
        verify(toast, never()).show();
    }
}