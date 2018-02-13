package com.gigabytedevelopersinc.app.explorer.model;

import java.util.List;

import com.gigabytedevelopersinc.app.explorer.fragment.RootsFragment.*;

/**
 * Created by HaKr on 07/08/16.
 */

public class GroupInfo {
    public String label;
    public List<Item> itemList;

    public GroupInfo(String text, List<Item> list){
        label = text;
        itemList = list;
    }
}
