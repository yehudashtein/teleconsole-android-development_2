package com.telebroad.teleconsole.controller.dashboard;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.common.base.Strings;
import com.telebroad.teleconsole.DialPadView;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.viewmodels.FullPhoneViewModel;


public class DialPadFragment extends BottomSheetDialogFragment {
    private DialPadView dialPadView;
    private TextView myCallerID;
    private static DialPadFragment instance;
    private float y1, y2;
    private NestedScrollView scrollView;
    private static final String ARG_NUMBER_TO_DIAL = "com.telebroad.teleconsole.dialpadfragment.number.to.dial";
    public DialPadFragment() {}

    public static DialPadFragment getInstance() {
        return getInstance(null);
//        DialPadFragment instance = new DialPadFragment();
//        Bundle args = new Bundle();
//        instance.setArguments(args);
//        android.util.Log.d("DpFragment", "Getting Instance...");
//        return instance;
    }
    public static DialPadFragment getInstance(String numberToDial) {
        if (instance == null) {
            instance =new DialPadFragment();
            Bundle args = new Bundle();
            if (!Strings.isNullOrEmpty(numberToDial)) {
                args.putString(ARG_NUMBER_TO_DIAL, numberToDial);
            }
            instance.setArguments(args);
           // android.util.Log.d("DpFragment", "Getting Instance...");
            return instance;
        }else return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //android.util.Log.d("DpFragment", "Constructing...");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        ((BottomSheetDialog)dialog).getBehavior().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                //android.util.Log.d("BSSC", "new state is " + newState);
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //In the EXPANDED STATE apply a new MaterialShapeDrawable with rounded cornes
                    MaterialShapeDrawable newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet);
                    ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable);
                }
            }
            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return dialog;
    }

    @NonNull
    private MaterialShapeDrawable createMaterialShapeDrawable(@NonNull View bottomSheet) {
        ShapeAppearanceModel shapeAppearanceModel =
                //Create a ShapeAppearanceModel with the same shapeAppearanceOverlay used in the style
                ShapeAppearanceModel.builder(getContext(), 0, R.style.CustomShapeAppearanceBottomSheetDialog).build();
        //Create a new MaterialShapeDrawable (you can't use the original MaterialShapeDrawable in the BottoSheet)
        MaterialShapeDrawable currentMaterialShapeDrawable = (MaterialShapeDrawable) bottomSheet.getBackground();
        MaterialShapeDrawable newMaterialShapeDrawable = new MaterialShapeDrawable((shapeAppearanceModel));
        //Copy the attributes in the new MaterialShapeDrawable
        newMaterialShapeDrawable.initializeElevationOverlay(getContext());
        newMaterialShapeDrawable.setFillColor(currentMaterialShapeDrawable.getFillColor());
        newMaterialShapeDrawable.setTintList(currentMaterialShapeDrawable.getTintList());
        newMaterialShapeDrawable.setElevation(currentMaterialShapeDrawable.getElevation());
        newMaterialShapeDrawable.setStrokeWidth(currentMaterialShapeDrawable.getStrokeWidth());
        newMaterialShapeDrawable.setStrokeColor(currentMaterialShapeDrawable.getStrokeColor());
        return newMaterialShapeDrawable;
    }

    private boolean isScrollViewAtBottom(ScrollView scrollView) {
        View child = scrollView.getChildAt(0);
        if (child == null) {
            return false;
        }
        int diff = (child.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        return diff == 0; // If difference is zero, then the bottom has been reached
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dial_pad, container, false);
       // android.util.Log.d("DpFragment", "Creating...");
        //scrollView = view.findViewById(R.id.scrollView);
        dialPadView = view.findViewById(R.id.fragment_dialpad_view);
        dialPadView.setCallAction(DialPadView.FIRST_CALL);
        dialPadView.setActivity(this.getActivity());
        String numberToDial = getArguments() == null ? null : getArguments().getString(ARG_NUMBER_TO_DIAL);
        if (!Strings.isNullOrEmpty(numberToDial)){
            dialPadView.setPresetNumber(numberToDial);
        }
        myCallerID = view.findViewById(R.id.myCallerID);
        FullPhoneViewModel phoneViewModel = new ViewModelProvider(this).get(FullPhoneViewModel.class);
        phoneViewModel.fullPhone.observe(getViewLifecycleOwner(), (fullPhone) -> {
           // android.util.Log.d("LiveData", "Changed");
            String callerID = fullPhone != null ? PhoneNumber.getPhoneNumber(fullPhone.getCalleridExternal()).formatted() : getActivity().getString(R.string.unknown);
            myCallerID.setText(getActivity().getString(R.string.caller_id, callerID));
//            if (fullPhone != null){
//                myCallerID.setText(new PhoneNumber(fullPhone.getCalleridExternal()).formatted());
//            }else{
//                myCallerID.setText(getActivity().getString(R.string.unknown));
//            }
        });
        Utils.hideKeyboard(getContext(), view);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        myCallerID.setText("Loading...");
        return view;
    }

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
//    if (view.getParent() instanceof CoordinatorLayout) {
//        View bottomSheetInternal = ((View) view.getParent());
//        try {
//            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
//            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//                @Override
//                public void onStateChanged(@NonNull View bottomSheet, int newState) {}
//                @Override
//                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                    // If the NestedScrollView isn't at the top, prevent dragging the bottom sheet.
//                    if (scrollView.getScrollY() > 0) {
//                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                    }
//                }
//            });
//        } catch (IllegalArgumentException e) {}
//    }
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
