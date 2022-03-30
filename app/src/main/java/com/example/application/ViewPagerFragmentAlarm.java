package com.example.application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

public class ViewPagerFragmentAlarm extends FragmentStateAdapter {
    //CÁC THUỘC TÍNH PRIVATE
    private List<Fragment> listFragment;

    //HÀM KHỞI TẠO
    public ViewPagerFragmentAlarm(@NonNull FragmentActivity fragmentActivity, List<Fragment> listFragment) {
        super(fragmentActivity);
        this.listFragment = listFragment;
    }

    //HÀM TẠO FRAGMENT
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return listFragment.get(position);
    }

    //TRẢ VỀ SỐ LƯỢNG FRAGMENT
    @Override
    public int getItemCount() {
        return listFragment.size();
    }

}
