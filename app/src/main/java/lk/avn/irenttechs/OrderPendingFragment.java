package lk.avn.irenttechs;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import lk.avn.irenttechs.adapter.OrderPendingAdapter;
import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.custom.CustomTransition;
import lk.avn.irenttechs.model.Invoice;
import lk.avn.irenttechs.model.Products;

public class OrderPendingFragment extends Fragment {

    private static final String TAG = OrderPendingFragment.class.getName();
    Bundle args;
    private static String errorName, successName, successtitle, warning_name;
    private static FirebaseFirestore firestore;
    private ArrayList<Invoice> invoices;
    private String email, invoice_id;
    private static CustomLoading customLoading;
    private static OrderPendingAdapter orderPendingAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_pending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        args = getArguments();
        firestore = FirebaseFirestore.getInstance();
        invoices = new ArrayList<>();
        customLoading = new CustomLoading(getActivity());

        SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", getContext().MODE_PRIVATE);
        email = preferences.getString("EMAIL", null);

        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("Orders");
        }

        RecyclerView invoiceView = fragment.findViewById(R.id.Order_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        orderPendingAdapter = new OrderPendingAdapter(invoices, getContext(), new OrderPendingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onCardClick(int position ,LinearLayout order_card_gone_click,LinearLayout visiblelayout ,LinearLayout full_layout) {
                full_layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

                order_card_gone_click.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int i = (visiblelayout.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

                        Transition customTransition2 = new CustomTransition();
                        TransitionManager.beginDelayedTransition(full_layout, customTransition2);
                        visiblelayout.setVisibility(i);
                    }
                });
            }

            @Override
            public void onCancelClick(int position) {
                String selectedProductId = orderPendingAdapter.getDocumentId(position);
                warning_name = "Do you really want to Cancel this rental products?";
                new OrderPendingFragment.WarningDialog(email, selectedProductId, position ,selectedProductId).show(getActivity().getSupportFragmentManager(), "Error");
            }

            @Override
            public void onTrackOrderClick(int position) {

            }
        });
        invoiceView.setLayoutManager(linearLayoutManager);
        invoiceView.setAdapter(orderPendingAdapter);

        CollectionReference wishlistCollection = firestore.collection("Invoice");
        Query wishlist_query = wishlistCollection.whereEqualTo("user_email", email).whereEqualTo("status","pending");
        wishlist_query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    invoice_id = doc.getString("documentId");

                    Log.i(TAG, invoice_id);

                    CollectionReference products_list = firestore.collection("Invoice");

                    Query query = products_list.whereEqualTo("documentId", invoice_id);

                    query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            for (DocumentChange change : value.getDocumentChanges()) {
                                Invoice invoice = change.getDocument().toObject(Invoice.class);
                                switch (change.getType()) {
                                    case ADDED:
                                        invoices.add(invoice);
                                        break;
                                    case MODIFIED:
                                        Invoice old = invoices.stream().filter(i -> i.getDocumentId().equals(invoice.getDocumentId())).findFirst().orElse(null);
                                        if (old != null) {
                                            old.setCheckInDate(invoice.getCheckInDate());
                                            old.setCheckOutDate(invoice.getCheckOutDate());
                                            old.setTotal_price(invoice.getTotal_price());
                                            old.setDatetime(invoice.getDatetime());
                                        }
                                        break;
                                    case REMOVED:
                                        invoices.removeIf(p -> p.getDocumentId().equals(invoice.getDocumentId()));
                                        break;

                                }
                            }

                            orderPendingAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

    }

    public static void deleteInvoiceList(String email, int position, String selectedInvoiceId) {
        CollectionReference invoiceCollection = firestore.collection("Invoice");
        customLoading.show();

        Query query = invoiceCollection.whereEqualTo("documentId", selectedInvoiceId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String documentId = doc.getId();
                            if (documentId != null) {
                                invoiceCollection.document(documentId).update("status", "cancel_pending")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> updateTask) {
                                                if (updateTask.isSuccessful()) {
                                                    customLoading.dismiss();
                                                    orderPendingAdapter.removeItem(position);
                                                    Log.d(TAG, "Document updated successfully. Removed item at position: " + position);
                                                } else {
                                                    customLoading.dismiss();
                                                    Log.e(TAG, "Error updating document status", updateTask.getException());
                                                }
                                            }
                                        });
                            }
                        }
                    }
                } else {
                    customLoading.dismiss();

                    Log.e(TAG, "Error querying documents", task.getException());
                }
            }
        });
    }


    public static class WarningDialog extends DialogFragment {
        private String email;
        private String productId;
        private String selectedInvoiceId;
        private int position;

        public WarningDialog(String email, String productId, int position ,String selectedInvoiceId) {
            this.email = email;
            this.productId = productId;
            this.position = position;
            this.selectedInvoiceId = selectedInvoiceId;
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
                    deleteInvoiceList(email, position,selectedInvoiceId);
                    dismiss();
                }
            });

            deleteButton.setText("Cancel");

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
}