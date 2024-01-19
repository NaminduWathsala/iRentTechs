package lk.avn.irenttechs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.avn.irenttechs.adapter.CartAdapter;
import lk.avn.irenttechs.adapter.WishlistAdapter;
import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.model.Cart;
import lk.avn.irenttechs.model.Invoice;
import lk.avn.irenttechs.model.Products;

public class CartFragment extends Fragment {
    private static final String TAG = CartFragment.class.getName();
    private static String errorName, successName, successtitle, warning_name, availableQTY1, qty_item_invoice;
    Bundle args;
    private String CategoryValue, email, product_id;
    private static FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private static CustomLoading customLoading;
    private static CartAdapter cartAdapter;
    private double overallTotal = 0.0;
    private TextView totalTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        if (login_preferences.getString("ID", null) == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return null;
        } else {
            return inflater.inflate(R.layout.fragment_cart, container, false);
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
                t_name.setText("Cart");
                MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
                toolbar.setNavigationIcon(null);
                ImageButton backbtn = ha.findViewById(R.id.back_btn);
                backbtn.setVisibility(View.VISIBLE);
                backbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                            toolbar.setNavigationIcon(R.drawable.menu);
                            backbtn.setVisibility(View.GONE);
                        }
                    }
                });
            }

            totalTextView = fragment.findViewById(R.id.cart_product_total);


            RecyclerView productView = fragment.findViewById(R.id.cart_rv);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            cartAdapter = new CartAdapter(products, getContext(), email, totalTextView, new CartAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    String selectedProductId = cartAdapter.getDocumentId(position);

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
                    String selectedProductId = cartAdapter.getDocumentId(position);
                    warning_name = "Do you really want to delete these records? This process cannot be undone";
                    new CartFragment.WarningDialog(email, selectedProductId, position).show(getActivity().getSupportFragmentManager(), "Error");
                    onViewCreated(fragment, savedInstanceState);
                }

                @Override
                public void onChangeQty(int position) {
                    String selectedProductId = cartAdapter.getDocumentId(position);
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

                    new CartFragment.cardQTYDialog(email, selectedProductId, requireActivity().getSupportFragmentManager()).show(getActivity().getSupportFragmentManager(), "Success");
                }
            });
            productView.setLayoutManager(linearLayoutManager);
            productView.setAdapter(cartAdapter);


            CollectionReference wishlistCollection = firestore.collection("Cart");
            Query wishlist_query = wishlistCollection.whereEqualTo("user_email", email).whereEqualTo("status", "1");
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

                                cartAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });


            Button cart_buy_now = fragment.findViewById(R.id.cart_buy_now);
            cart_buy_now.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(email, products, totalTextView, requireActivity().getSupportFragmentManager());
                    datePickerDialog.show(getActivity().getSupportFragmentManager(), "MyDatePickerDialog");

                }
            });

        }
    }


    public static void BuyNowInvoice(TextView totalTextView, ArrayList<Products> products, String email, String checkInDate, String checkOutDate, FragmentManager fragmentManager) {

        if (checkInDate == null || checkInDate.isEmpty()) {
            errorName = "Please Add Check In Date";
            new CartFragment.ErrorDialog().show(fragmentManager, "Error");
        } else if (checkOutDate == null || checkOutDate.isEmpty()) {
            errorName = "Please Add Check Out Date";
            new CartFragment.ErrorDialog().show(fragmentManager, "Error");
        } else {

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date checkIn = sdf.parse(checkInDate);
                Date checkOut = sdf.parse(checkOutDate);
                Date currentDate = new Date();

                if (checkIn.before(currentDate)) {
                    errorName = "Check In Date cannot be before the current date";
                    new CartFragment.ErrorDialog().show(fragmentManager, "Error");
                } else if (checkOut.before(checkIn)) {
                    errorName = "Check Out Date must be later than Check In Date";
                    new CartFragment.ErrorDialog().show(fragmentManager, "Error");
                } else {
                    String totalvalue = totalTextView.getText().toString();
                    String final_total = totalvalue.replaceAll("[^0-9.]", "");
                    final_total = final_total.replaceFirst("^\\.", "");

                    Invoice invoice = new Invoice();
                    invoice.setUser_email(email);
                    invoice.setDatetime(String.valueOf(new Date()));
                    invoice.setStatus("pending");
                    invoice.setCheckInDate(checkInDate);
                    invoice.setCheckOutDate(checkOutDate);
                    invoice.setTotal_price(final_total);

                    List<Invoice.InvoiceItem> invoiceItems = new ArrayList<>();

                    for (Products product : products) {
                        Log.i(TAG, product.getDocumentId());
                        CollectionReference wishlistCollection1 = firestore.collection("Cart");
                        Query wishlist_query1 = wishlistCollection1.whereEqualTo("product_id", product.getDocumentId())
                                .whereEqualTo("user_email", email)
                                .whereEqualTo("status", "1");

                        wishlist_query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                for (QueryDocumentSnapshot doc : value) {
                                    String qty_item_invoice = doc.getString("qty");
                                    Log.i(TAG, qty_item_invoice);

                                    Invoice.InvoiceItem item = new Invoice.InvoiceItem();
                                    item.setProduct_id(product.getDocumentId());
                                    item.setQty(qty_item_invoice);
                                    invoiceItems.add(item);

                                    if (invoiceItems.size() == products.size()) {
                                        invoice.setProducts(invoiceItems);
                                        customLoading.show();
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        CollectionReference invoiceCollection = db.collection("Invoice");
                                        invoiceCollection.add(invoice).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                customLoading.dismiss();
                                                if (task.isSuccessful()) {
                                                    String generatedDocumentId = task.getResult().getId();
                                                    Log.d(TAG, "Generated Document ID: " + generatedDocumentId);

                                                    invoiceCollection.document(generatedDocumentId).update("documentId", generatedDocumentId);

                                                    for (Products product : products) {
                                                        updateCartStatus(product.getDocumentId(), email);
                                                    }

                                                    successtitle = "Success!";
                                                    successName = "Your Rental has been successful. We will let you know about your order confirmation in the next 24 Hours. Thank you for renting. Have a nice day.";
                                                    new SuccessDialog().show(fragmentManager, "Success");

                                                    if (fragmentManager.findFragmentByTag("MyDatePickerDialog") != null) {
                                                        ((MyDatePickerDialog) fragmentManager.findFragmentByTag("MyDatePickerDialog")).dismiss();
                                                    }

                                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                    transaction.replace(R.id.frame_layouts, new HomeFragment());
                                                    transaction.commit();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                customLoading.dismiss();

                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateCartStatus(String productId, String userEmail) {
        CollectionReference cartCollection = firestore.collection("Cart");
        Query query = cartCollection.whereEqualTo("product_id", productId)
                .whereEqualTo("user_email", userEmail)
                .whereEqualTo("status", "1");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String documentId = doc.getId();
                        cartCollection.document(documentId).update("status", "2");
                    }
                }
            }
        });
    }

    public static void deleteWishlist(String email, String productValue, int position, FragmentManager fragmentManager) {
        CollectionReference wishlistCollection = firestore.collection("Cart");
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
                                cartAdapter.removeItem(position);


                                CartFragment cartFragment = new CartFragment();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.frame_layouts, cartFragment)
                                        .addToBackStack(null)
                                        .commit();
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
                    deleteWishlist(email, productId, position, getParentFragmentManager());
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

    public static void cartUpdateQTY(String p_email, String p_id, String p_qty, FragmentManager fragmentManager) {

        customLoading.show();

        CollectionReference cartCollection = firestore.collection("Cart");

        Query query = cartCollection.whereEqualTo("user_email", p_email).whereEqualTo("product_id", p_id);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        DocumentReference documentRef = cartCollection.document(documentId);
                        documentRef.update("qty", p_qty)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        customLoading.dismiss();

                                        CartFragment cartFragment = new CartFragment();
                                        fragmentManager.beginTransaction()
                                                .replace(R.id.frame_layouts, cartFragment)
                                                .addToBackStack(null)
                                                .commit();
                                        Log.i(TAG, "Update successful");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        customLoading.dismiss();

                                        Log.i(TAG, "Something went wrong");
                                    }
                                });

                    }
                }
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

            cartButton.setText("Update");
            cartButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);


            cartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String qty_number = numberPicker.getText().toString();

                    if (qty_number.equals("0") || qty_number.isEmpty()) {
                        errorName = "Please Add your Quantity";
                        new CartFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else if (Integer.parseInt(qty_number) > Integer.parseInt(availableQTY1)) {
                        errorName = "We Don't have that much products sorry !";
                        new CartFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else {
                        cartUpdateQTY(email, productId, qty_number, requireActivity().getSupportFragmentManager());
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

    public static class MyDatePickerDialog extends DialogFragment {

        private EditText cartDateCheckIn;
        private EditText cartDateCheckOut;
        private String pd_email;
        private ArrayList<Products> pd_product;
        private TextView pd_totalTextView;
        private FragmentManager fragmentManager;


        public MyDatePickerDialog(String pd_email, ArrayList<Products> pd_product, TextView pd_totalTextView, FragmentManager fragmentManager) {
            this.pd_email = pd_email;
            this.pd_product = pd_product;
            this.pd_totalTextView = pd_totalTextView;
            this.fragmentManager = fragmentManager;
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.cart_date_picker, null);
            cartDateCheckIn = customView.findViewById(R.id.cart_date_check_in);
            cartDateCheckOut = customView.findViewById(R.id.cart_date_check_out);

            cartDateCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialog(cartDateCheckIn);
                }
            });

            cartDateCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialog(cartDateCheckOut);
                }
            });


            Button buyNowButton = customView.findViewById(R.id.cart_cancel_button);
            buyNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            Button buyNow_btn = customView.findViewById(R.id.cart_product_buy_now);
            buyNow_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String DateCheckIn = cartDateCheckIn.getText().toString();
                    String DateCheckOut = cartDateCheckOut.getText().toString();
                    BuyNowInvoice(pd_totalTextView, pd_product, pd_email, DateCheckIn, DateCheckOut, fragmentManager);
//                    dismiss();

                }
            });

            builder.setView(customView);
            return builder.create();
        }

        private void showDatePickerDialog(final EditText dateEditText) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                            dateEditText.setText(selectedDate);
                        }
                    },
                    year, month, day
            );

            datePickerDialog.show();
        }
    }


}
