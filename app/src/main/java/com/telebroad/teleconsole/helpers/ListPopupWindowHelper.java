package com.telebroad.teleconsole.helpers;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.SimpleAdapter;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;

import java.util.HashMap;
import java.util.List;

public class ListPopupWindowHelper {
    public static void setupListPopupWindow(Context context, View anchorView, List<HashMap<String, Object>> data, View openView, AdapterView.OnItemClickListener onItemClickListener) {
        ListPopupWindow listPopupWindow = new ListPopupWindow(context);
        listPopupWindow.setAnchorView(anchorView);
        ListAdapter adapter = new SimpleAdapter(context, data, R.layout.new_message_item, new String[]{"title", "icon"}, new int[]{R.id.new_message_title, R.id.new_message_icon});
        listPopupWindow.setAdapter(adapter);
        if (openView != null){
            openView.setVisibility(View.GONE);
            listPopupWindow.setOnDismissListener(() -> {
                new Handler().postDelayed(() -> {
                    openView.setVisibility(View.VISIBLE);
                }, 125);
            });
        }
        int maxWidth = getMaxWidth(adapter, context);
        listPopupWindow.setContentWidth(maxWidth);
      //  listPopupWindow.setHeight(getHeight(context, adapter));
        listPopupWindow.setOnItemClickListener( (parent, view, position, id) -> {
            listPopupWindow.dismiss();
            onItemClickListener.onItemClick(parent,view,position,id);
        });
        listPopupWindow.setDropDownGravity(Gravity.END | Gravity.TOP);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.listPopupWindowMargin);
        listPopupWindow.show();
        //listPopupWindow.getListView().setPadding(0, margin, 0, margin);
    }

    @NonNull
    public static HashMap<String, Object> addItem(String title, @DrawableRes int icon) {
        HashMap<String, Object> testOne = new HashMap<>();
        testOne.put("title", title);
        testOne.put("icon", icon);
        return testOne;
    }

    @NonNull
    public static HashMap<String, Object> addItem(@StringRes int title, @DrawableRes int icon){
        return addItem(AppController.getInstance().getString(title), icon);
    }

    public static int getHeight(Context context, ListAdapter adapter) {
        int itemHeight = context.getResources().getDimensionPixelSize(R.dimen.listPopupWindowItemHeight);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.listPopupWindowMargin);
        return (itemHeight * adapter.getCount()) + (margin * 4);
    }

    public static int getMaxWidth(ListAdapter adapter, Context context) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        int x = 0;
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            if (mMeasureParent == null) {
                mMeasureParent = new ConstraintLayout(context);
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();



            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }
       // android.util.Log.d("PopupWindow", "times run " + x);
        return maxWidth;
    }

}
