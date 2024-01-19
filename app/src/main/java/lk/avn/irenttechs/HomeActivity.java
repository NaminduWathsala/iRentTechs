package lk.avn.irenttechs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.squareup.picasso.Picasso;

import java.util.List;

import lk.avn.irenttechs.dto.UserDTO;
import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.*;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getName();
    private SmoothBottomBar bottomBar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private MenuItem activeMenuItem;
    private TextView toolbar_name;
    private ImageButton cart, wishlist, search;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar_name = findViewById(R.id.toolbar_name);
        wishlist = findViewById(R.id.wishlist_home);
        cart = findViewById(R.id.cart_home);
        search = findViewById(R.id.search_producrs);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_LOW");
        MyBroadcastReceiver mbr = new MyBroadcastReceiver();
        registerReceiver(mbr, intentFilter);

        IntentFilter intentFilter2 = new IntentFilter("lk.avn.irenttechs.CUSTOM_INTENT");
        MyBroadcastReceiver mbr2 = new MyBroadcastReceiver();
        registerReceiver(mbr2, intentFilter2);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(HomeActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        toolbar.setNavigationIcon(R.drawable.menu);

        SharedPreferences preferences = getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        View headerView = navigationView.getHeaderView(0);

        TextView tv_name_nav = headerView.findViewById(R.id.side_nav_name);
        TextView tv_email_nav = headerView.findViewById(R.id.side_nav_email);
        ShapeableImageView image = headerView.findViewById(R.id.profile_img);
        String name = preferences.getString("NAME", null);
        String email = preferences.getString("EMAIL", null);

        if (name != null) {
            tv_name_nav.setText(name);
        }

        if (email != null) {
            tv_email_nav.setText(email);
        }
        storage = FirebaseStorage.getInstance();

        try {

            StorageReference imageRef = storage.getReference("user_images/" + email);

            imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get()
                                    .load(uri)
                                    .resize(200, 200)
                                    .centerCrop()
                                    .into(image);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Error Image");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "No Image");
                }
            });


        } catch (Exception e) {
            Log.i(TAG, e.getMessage());

        }



        Menu menu = navigationView.getMenu();

        if ("namiduwathsala@gmail.com".equals(email)) {
//            menu.setGroupVisible(R.id.adminAddProducts, true);
        }

        wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar_name.setText("Wishlist");

                WishListFragment wishListFragment = new WishListFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, wishListFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartFragment cartFragment = new CartFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, cartFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment searchFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem itemId) {

                if (activeMenuItem != null) {
                    activeMenuItem.setChecked(false);
                }

                if (itemId.getItemId() == R.id.sideNavHome) {
                    toolbar_name.setText("HOME");
                    addFragment(new HomeFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                    bottomBar.setItemActiveIndex(1);
                } else if (itemId.getItemId() == R.id.sideNavProfile) {
                    toolbar_name.setText("PROFILE");
                    addFragment(new ProfileFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    bottomBar.setItemActiveIndex(0);
                    drawerLayout.close();
                } else if (itemId.getItemId() == R.id.sideNavCart) {
                    toolbar_name.setText("Cart");
                    addFragment(new CartFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                } else if (itemId.getItemId() == R.id.sideNavWishlist) {
                    toolbar_name.setText("Wishlist");
                    addFragment(new WishListFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                } else if (itemId.getItemId() == R.id.sideNavOrders) {
                    toolbar_name.setText("Orders");
                    addFragment(new OrdersFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                } else if (itemId.getItemId() == R.id.sideNavSettings) {
                    toolbar_name.setText("Settings");
                    addFragment(new SettingsFragment());
//                    addFragment(new SettingsFragment());
                    activeMenuItem = itemId;
                    bottomBar.setItemActiveIndex(2);
                    itemId.setChecked(true);
                    drawerLayout.close();
                } else if (itemId.getItemId() == R.id.sideNavCategoryList) {
                    toolbar_name.setText("Categories");
                    addFragment(new AllCategoryListFragment());
//                    activeMenuItem = itemId;
//                    itemId.setChecked(true);
                    drawerLayout.close();
                }

                return true;
            }
        });

        bottomBar = findViewById(R.id.bottomBar);
        addFragment(new HomeFragment());
        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public boolean onItemSelect(int selectedItem) {
                switch (selectedItem) {
                    case 0:
                        toolbar_name.setText("PROFILE");
                        addFragment(new ProfileFragment());
                        break;
                    case 1:
                        toolbar_name.setText("HOME");
                        addFragment(new HomeFragment());
                        break;
                    case 2:
                        toolbar_name.setText("SETTINGS");
                        addFragment(new SettingsFragment());
                        break;

                }
                return true;
            }
        });

    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layouts, fragment);
        transaction.commit();
    }

}