package edu.uga.cs.shoppinglist;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class PurhcasedListAdapter extends RecyclerView.Adapter<PurhcasedListAdapter.MyViewHolder> {
    Context context;
    ArrayList<PreviousListItem> list;

    public PurhcasedListAdapter(Context context, ArrayList<PreviousListItem> list) {
        this.context = context;
        this.list = list;
    }
    public PurhcasedListAdapter(ArrayList<PreviousListItem> list) {list = list;}

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.previous_list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PreviousListItem item = list.get(position);
        List<BasketItem> basketItemList = item.getBasketItems();
        holder.firstName.setText(item.getEmail().toString());
        PurchasedItemsAdapter adapter = new PurchasedItemsAdapter(basketItemList);
        holder.recyclerView.setAdapter(adapter);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setHasFixedSize(true);
        holder.list = new ArrayList<>();
    }

    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView firstName;
        RecyclerView recyclerView;
        ArrayList<BasketItem> list;
        DatabaseReference database;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.tvfirstName);
            recyclerView = itemView.findViewById(R.id.innerRecycler);
        }
    }
}

