package com.telebroad.teleconsole.controller.dashboard;
import androidx.recyclerview.widget.RecyclerView;

public interface ScrollableFragment {
     RecyclerView recyclerView();

    default void scrollToTop() {
        if (recyclerView() != null){
            recyclerView().scrollToPosition(0);
        }
    }

}
