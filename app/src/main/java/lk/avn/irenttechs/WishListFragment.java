package lk.avn.irenttechs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;

import lk.avn.irenttechs.adapter.WishlistAdapter;
import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.model.Cart;
import lk.avn.irenttechs.model.Products;

public class WishListFragment extends Fragment {
    private static final String TAG = WishListFragment.class.getName();
    Bundle args;
    private static String errorName, successName, successtitle,availableQTY1,clickProductId;
    private static FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private String CategoryValue, email, product_id;
    private static String warning_name;
    private static CustomLoading customLoading;

    private static WishlistAdapter wishlistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        if (login_preferences.getString("ID", null) == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return null;
        } else {
            return inflater.inflate(R.layout.fragment_wish_list, container, false);
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
            args = getArguments();
            firestore = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();

            products = new ArrayList<>();

            customLoading = new CustomLoading(getActivity());

            SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", getContext().MODE_PRIVATE);
            email = preferences.getString("EMAIL", null);

            if (getActivity() instanceof HomeActivity) {
                HomeActivity ha = (HomeActivity) getActivity();
                ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
                TextView t_name = ha.findViewById(R.id.toolbar_name);
                t_name.setText("Wishlist");
                MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
                toolbar.setNavigationIcon(null);
                ImageButton backbtn = ha.findViewById(R.id.back_btn);
                backbtn.setVisibility(View.VISIBLE);
                backbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                });
            }

            RecyclerView productView = fragment.findViewById(R.id.wishlist_fragment_rv);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            wishlistAdapter = new WishlistAdapter(products, getContext(), new WishlistAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    String selectedProductId = wishlistAdapter.getDocumentId(position);

                    Log.i(TAG, "Selected Product ID: " + selectedProductId);

                    Bundle bundle = new Bundle();
                    bundle.putString("product_id", selectedProductId);

                    SingleProductViewFragment singleProductViewFragment = new SingleProductViewFragment();
                    singleProductViewFragment.setArguments(bundle);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.frame_layouts, singleProductViewFragment)
                            .addToBackStack(null)
                            .commit();
                }

                @Override
                public void onDeleteClick(int position) {
                    String selectedProductId = wishlistAdapter.getDocumentId(position);
                    warning_name = "Do you really want to delete these records? This process cannot be undone";
                    new WishListFragment.WarningDialog(email, selectedProductId, position).show(getActivity().getSupportFragmentManager(), "Error");
                }

                @Override
                public void onAddToCartClick(int position) {
                    String selectedProductId = wishlistAdapter.getDocumentId(position);
                    CollectionReference wishlistCollection1 = firestore.collection("Products");
                    Query wishlist_query1 = wishlistCollection1.whereEqualTo("documentId", selectedProductId);
                    wishlist_query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            for (QueryDocumentSnapshot doc : value) {
                                availableQTY1 = doc.getString("qty");
                                Log.i(TAG, availableQTY1);
                            }
                        }
                    });

                    new WishListFragment.cardQTYDialog(email, selectedProductId, requireActivity().getSupportFragmentManager()).show(getActivity().getSupportFragmentManager(), "Success");
                }
            });
            productView.setLayoutManager(linearLayoutManager);
            productView.setAdapter(wishlistAdapter);


            CollectionReference wishlistCollection = firestore.collection("Wishlist");
            Query wishlist_query = wishlistCollection.whereEqualTo("user_email", email);
            wishlist_query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        product_id = doc.getString("product_id");

                        Log.i(TAG, product_id);

                        CollectionReference products_list = firestore.collection("Products");

                        Query query = products_list.whereEqualTo("documentId", product_id).whereEqualTo("status", "1");

                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                for (DocumentChange change : value.getDocumentChanges()) {
                                    Products product = change.getDocument().toObject(Products.class);
                                    switch (change.getType()) {
                                        case ADDED:
                                            products.add(product);
                                            break;
                                        case MODIFIED:
                                            Products old = products.stream().filter(i -> i.getDocumentId().equals(product.getDocumentId())).findFirst().orElse(null);
                                            if (old != null) {
                                                old.setName(product.getName());
                                                old.setBrand(product.getBrand());
                                                old.setPrice(product.getPrice());
                                                old.setQty(product.getQty());
                                                old.setProduct_image(product.getProduct_image());
                                            }
                                            break;
                                        case REMOVED:
                                            products.removeIf(p -> p.getDocumentId().equals(product.getDocumentId()));
                                            break;

                                    }
                                }

                                wishlistAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }

    }

    public static void deleteWishlist(String email, String productValue, int position) {
        CollectionReference wishlistCollection = firestore.collection("Wishlist");
        customLoading.show();
        Query query = wishlistCollection.whereEqualTo("user_email", email)
                .whereEqualTo("product_id", productValue);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                customLoading.dismiss();
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String documentId = doc.getString("documentId");
                            if (documentId != null) {
                                wishlistCollection.document(documentId).delete();
                                wishlistAdapter.removeItem(position);
                            }
                        }
                    }
                }
            }
        });
    }


    public static class WarningDialog extends DialogFragment {
        private String email;
        private String productId;
        private int position;

        public WarningDialog(String email, String productId, int position) {
            this.email = email;
            this.productId = productId;
            this.position = position;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.warning_message, null);


            TextView messageTextView = customView.findViewById(R.id.warning_dialog_message);
            messageTextView.setText(warning_name);

            Button deleteButton = customView.findViewById(R.id.warning_dialog_ok_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteWishlist(email, productId, position);
                    dismiss();
                }
            });

            Button cancelButton = customView.findViewById(R.id.warning_dialog_cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    public static void productAddToCart(String p_email, String p_id, String p_qty, FragmentManager fragmentManager) {
        Date date = new Date();

        Cart cart = new Cart(p_email, p_id, p_qty, date.toString(), "1");

        customLoading.show();

        CollectionReference cartCollection = firestore.collection("Cart");
        cartCollection.add(cart).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                customLoading.dismiss();
                if (task.isSuccessful()) {
                    String generatedDocumentId = task.getResult().getId();
                    Log.d(TAG, "Generated Document ID: " + generatedDocumentId);

                    cartCollection.document(generatedDocumentId).update("documentId", generatedDocumentId);

                    successtitle = "Success!";
                    successName = "Your product has been added to the Cart ";
                    new WishListFragment.SuccessDialog().show(fragmentManager, "Success");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                customLoading.dismiss();
                Log.d(TAG, "Something Went wrong");
            }
        });
    }


    public static class cardQTYDialog extends DialogFragment {
        private String email;
        private String productId;
        FragmentManager fragmentManager;

        public cardQTYDialog(String email, String productId, FragmentManager fragmentManager) {
            this.email = email;
            this.productId = productId;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.cart_qty_message, null);

            EditText numberPicker = customView.findViewById(R.id.numberPicker);
            Button minusButton = customView.findViewById(R.id.minusButton);
            Button plusButton = customView.findViewById(R.id.plusButton);


            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(numberPicker.getText().toString());
                    if (value > 0) {
                        value--;
                        numberPicker.setText(String.valueOf(value));
                    }
                }
            });

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(numberPicker.getText().toString());
                    value++;
                    numberPicker.setText(String.valueOf(value));
                }
            });





            Button cartButton = customView.findViewById(R.id.cart_product_add_to_card);
            cartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String qty_number = numberPicker.getText().toString();

                    if (qty_number.equals("0") || qty_number.isEmpty()) {
                        errorName = "Please Add your Quantity";
                        new WishListFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else if (Integer.parseInt(qty_number) > Integer.parseInt(availableQTY1)) {
                        errorName = "We Don't have that much products sorry !";
                        new WishListFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else {
                        productAddToCart(email, productId, qty_number, requireActivity().getSupportFragmentManager());
                        dismiss();
                    }
                }
            });

            Button cancelButton = customView.findViewById(R.id.cart_cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }


    public static class ErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.error_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(errorName);

            Button okButton = customView.findViewById(R.id.success_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    public static class SuccessDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.success_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(successName);

            TextView messageTextView2 = customView.findViewById(R.id.success_dialog_title);
            messageTextView2.setText(successtitle);

            Button okButton = customView.findViewById(R.id.success_dialog_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }


}