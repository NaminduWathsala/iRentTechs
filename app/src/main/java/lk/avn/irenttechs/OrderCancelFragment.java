package lk.avn.irenttechs;

import android.animation.LayoutTransition;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import lk.avn.irenttechs.adapter.OrderCancelAdapter;
import lk.avn.irenttechs.adapter.OrderPendingAdapter;
import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.custom.CustomTransition;
import lk.avn.irenttechs.model.Invoice;

public class OrderCancelFragment extends Fragment {
    private static final String TAG = OrderCancelFragment.class.getName();
    Bundle args;
    private static String errorName, successName, successtitle, warning_name;
    private static FirebaseFirestore firestore;
    private ArrayList<Invoice> invoices;
    private String email, invoice_id;
    private static CustomLoading customLoading;
    private static OrderCancelAdapter orderCancelAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_cancel, container, false);
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

        RecyclerView invoiceView = fragment.findViewById(R.id.order_cancel_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        orderCancelAdapter = new OrderCancelAdapter(invoices, getContext(), new OrderCancelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onCardClick(int position, LinearLayout order_card_gone_click, LinearLayout visiblelayout, LinearLayout full_layout) {
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

        });

        invoiceView.setLayoutManager(linearLayoutManager);
        invoiceView.setAdapter(orderCancelAdapter);

        CollectionReference wishlistCollection = firestore.collection("Invoice");
        Query wishlist_query = wishlistCollection.whereEqualTo("user_email", email).whereEqualTo("status","canceled");
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

                            orderCancelAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

    }
}