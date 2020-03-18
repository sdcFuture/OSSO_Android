package com.futureelectronics.osso;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.futureelectronics.osso.data.ScannedDevice;
import com.futureelectronics.osso.databinding.ListItemScannedOssoBinding;

import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Kyle Harman on 12/11/2018.
 */
public class ScannedAdapter extends RecyclerView.Adapter<ScannedAdapter.ScannedViewHolder> {

    private final List<ScannedDevice> mDeviceList = new ArrayList<>();

    private List<String> mCurrentOssoAddrList;

    @Nullable
    private final ScannedDevClickCallback mClickCallback;

    public ScannedAdapter(@Nullable ScannedDevClickCallback callback) {
        mClickCallback = callback;
        setHasStableIds(true);
    }

    public void addScanResult(ScanResult bleScanResult) {
        // Not the best way to ensure distinct devices, just for sake on the demo.
        if(bleScanResult.getBleDevice().getName() == null || !bleScanResult.getBleDevice().getName().toUpperCase().contains("BLUENRG")){
            return;
        }

        String macAddr = bleScanResult.getBleDevice().getMacAddress();

        // Check if this Osso has already been added
        if(mCurrentOssoAddrList != null){
            for (String addr : mCurrentOssoAddrList) {
                if(macAddr.equalsIgnoreCase(addr)){
                    return;
                }
            }
        }

        for (int i = 0; i < mDeviceList.size(); i++) {

            if (mDeviceList.get(i).getAddress().equalsIgnoreCase(macAddr)) {

                int oldRssi = mDeviceList.get(i).getRssi();
                mDeviceList.get(i).updateInfo(bleScanResult.getRssi(), bleScanResult.getScanRecord().getBytes(), bleScanResult.getTimestampNanos());
                if(oldRssi != bleScanResult.getRssi()) {
                    notifyItemChanged(i);
                }
                return;
            }
        }

        ScannedDevice scannedDevice = new ScannedDevice(bleScanResult.getBleDevice());
        scannedDevice.updateInfo(bleScanResult.getRssi(), bleScanResult.getScanRecord().getBytes(), bleScanResult.getTimestampNanos());
        scannedDevice.setName("OSSO");
        mDeviceList.add(scannedDevice);
        notifyDataSetChanged();
    }

    void clearScanResults() {
        mDeviceList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ScannedViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ListItemScannedOssoBinding binding = ListItemScannedOssoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        binding.setCallback(mClickCallback);
        return new ScannedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ScannedViewHolder holder, int position)
    {
        holder.binding.setDevice(mDeviceList.get(position));
        holder.binding.executePendingBindings();
    }

    public ScannedDevice getScannedDevice(String address)
    {
        for(int i=0; i<mDeviceList.size(); i++){
            if(mDeviceList.get(i).getAddress().equalsIgnoreCase(address)){
                return mDeviceList.get(i);
            }
        }

        return null;
    }

    public void setCurrentOssoAddrList(List<String> addrList){
        mCurrentOssoAddrList = addrList;
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ScannedViewHolder extends RecyclerView.ViewHolder {
        final ListItemScannedOssoBinding binding;

        public ScannedViewHolder(ListItemScannedOssoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
