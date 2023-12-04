package com.telebroad.teleconsole.controller;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.telebroad.teleconsole.DialPadView;
import com.telebroad.teleconsole.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DialPadSegment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialPadSegment extends Fragment {

    private int callAction;
    public DialPadSegment() {
        // Required empty public constructor
    }

    public static DialPadSegment newInstance(int callAction) {
        DialPadSegment fragment = new DialPadSegment();
        fragment.callAction = callAction;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialpad_segment, container, false);
        DialPadView dialPadView = view.findViewById(R.id.fragment_segment_dialpad_view);
        dialPadView.setCallAction(callAction);
        dialPadView.setActivity(this.getActivity());
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
