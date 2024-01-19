package lk.avn.irenttechs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import lk.avn.irenttechs.adapter.OrderFragmentAdapter;


public class OrdersFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        if (login_preferences.getString("ID", null) == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return null;
        } else {
            return inflater.inflate(R.layout.fragment_orders, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);
        SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        if (login_preferences.getString("ID", null) == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } else {
            tabLayout = fragment.findViewById(R.id.tabLayout);
            viewPager = fragment.findViewById(R.id.viewPager);

            OrderFragmentAdapter orderFragmentAdapter = new OrderFragmentAdapter(this);
            orderFragmentAdapter.addFragment(new OrderPendingFragment(), "Pending Orders");
            orderFragmentAdapter.addFragment(new OrderCancelPendingFragment(), "Cancel Pending");
            orderFragmentAdapter.addFragment(new OrderOngoingFragment(), "Ongoing Orders");
            orderFragmentAdapter.addFragment(new OrderCancelFragment(), "Canceled Orders");
            orderFragmentAdapter.addFragment(new OrderFinishedFragment(), "Finished Orders");

            viewPager.setAdapter(orderFragmentAdapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(orderFragmentAdapter.getPageTitle(position));
            }).attach();

        }
    }
}