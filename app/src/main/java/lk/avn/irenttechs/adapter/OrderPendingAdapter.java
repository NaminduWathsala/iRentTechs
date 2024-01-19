package lk.avn.irenttechs.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.avn.irenttechs.R;
import lk.avn.irenttechs.model.Invoice;
import lk.avn.irenttechs.model.Products;

public class OrderPendingAdapter extends RecyclerView.Adapter<OrderPendingAdapter.ViewHolder> {

    private List<Invoice> invoices;
    private Context context;
    private OnItemClickListener listener;

    public OrderPendingAdapter(List<Invoice> invoices, Context context, OnItemClickListener listener) {
        this.invoices = invoices;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderPendingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_pending_view, parent, false);
        return new OrderPendingAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderPendingAdapter.ViewHolder holder, int position) {

        Invoice invoice = invoices.get(position);
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        String Order_date = invoice.getDatetime();
        String outputOrder_date;
        try {
            Date date = inputFormat.parse(Order_date);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            outputOrder_date = outputFormat.format(date);

            holder.order_id.setText("Order Id: " + invoice.getDocumentId());
            holder.order_date.setText("Order Date: "+outputOrder_date);
            holder.checkIn.setText(invoice.getCheckInDate());
            holder.checkout.setText(invoice.getCheckOutDate());
            holder.total_price.setText("Rs. " + invoice.getTotal_price());
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onCardClick(int position, LinearLayout order_card_gone_click, LinearLayout visiblelayout, LinearLayout full_layout);

        void onCancelClick(int position);

        void onTrackOrderClick(int position);
    }
    public void removeItem(int position) {
        invoices.remove(position);
        notifyItemRemoved(position);
    }

    public String getDocumentId(int position) {
        return invoices.get(position).getDocumentId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView order_id, order_date, checkIn, checkout, total_price;
        Button track_order, cancel_order;
        LinearLayout order_card_gone_click, visiblelayout, full_layout;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            order_id = itemView.findViewById(R.id.order_id);
            order_date = itemView.findViewById(R.id.order_date);
            checkIn = itemView.findViewById(R.id.checkIn);
            checkout = itemView.findViewById(R.id.checkout);
            track_order = itemView.findViewById(R.id.track_order);
            total_price = itemView.findViewById(R.id.total_price);
            cancel_order = itemView.findViewById(R.id.cancel_order);
            full_layout = itemView.findViewById(R.id.full_layout);
            order_card_gone_click = itemView.findViewById(R.id.order_card_gone);
            visiblelayout = itemView.findViewById(R.id.visiblelayout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            order_card_gone_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCardClick(position, order_card_gone_click, visiblelayout, full_layout);
                        }
                    }
                }
            });

            track_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onTrackOrderClick(position);
                        }
                    }
                }
            });

            cancel_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCancelClick(position);
                        }
                    }
                }
            });
        }
    }
}
