package com.tc.nfc.core.interfaces;

import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.SearchBookResult;

import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/7/29.
 */
public interface SearchAction {
    public void bookSearch(String text, String page, ActionCallbackListener<List<SearchBookResult>> listener);
    public void getImage(String isbnGroups, ActionCallbackListener<Map<String, String>> listener);
}
