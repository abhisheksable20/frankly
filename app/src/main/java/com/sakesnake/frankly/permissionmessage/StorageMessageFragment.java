package com.sakesnake.frankly.permissionmessage;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sakesnake.frankly.R;

public class StorageMessageFragment extends DialogFragment
{
    private View rootView;
    private TextView allowPermission,cancelPermission;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_storage_message, container, false);

        getDialog().setCanceledOnTouchOutside(true);

        //init dialog views
        allowPermission = (TextView) rootView.findViewById(R.id.allow_permission_text_view);
        cancelPermission = (TextView) rootView.findViewById(R.id.cancel_permission_text_view);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        StoragePermissionViewModel viewModel = new ViewModelProvider(requireActivity()).get(StoragePermissionViewModel.class);

        allowPermission.setOnClickListener(v ->
        {
            viewModel.setStoragePermission(true);
            getDialog().dismiss();
        });

        cancelPermission.setOnClickListener(v ->
        {
            viewModel.setStoragePermission(false);
            getDialog().dismiss();
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        allowPermission = null;
        cancelPermission = null;
    }
}