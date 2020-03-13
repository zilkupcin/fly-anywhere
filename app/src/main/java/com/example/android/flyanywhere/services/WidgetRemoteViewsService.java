package com.example.android.flyanywhere.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.example.android.flyanywhere.adapters.WidgetDealAdapter;

/**
 * Created by baroc on 21/05/2018.
 */

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDealAdapter(getApplicationContext(), intent);
    }
}
