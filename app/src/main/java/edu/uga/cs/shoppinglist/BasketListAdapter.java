package edu.uga.cs.shoppinglist;
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

import java.util.ArrayList;

public class BasketListAdapter extends RecyclerView.Adapter<BasketListAdapter.MyViewHolder> {

    Context context;

    ArrayList<BasketItem> list;

    public BasketListAdapter(Context context, ArrayList<BasketItem> list) {
        this.context = context;
        this.list = list;


    }

    public BasketListAdapter(ArrayList<BasketItem> list) {
        list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.basket,parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BasketItem item = list.get(position);
        holder.firstName.setText(item.getItemName());
        holder.price.setText(item.getItemPrice());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("shoppingBasket");
                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("shoppingList");
                Query query = databaseReference.orderByChild("itemName").equalTo(item.getItemName());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            BasketItem basketItem = snapshot.getValue(BasketItem.class);
                            snapshot.getRef().removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("DELETE", "Item deleted: " + item.getItemName());
                                            Toast.makeText(view.getContext(), "Item deleted: " + item.getItemName(), Toast.LENGTH_SHORT).show();
                                            Item item = new Item(basketItem.getItemName().toString());
                                            databaseReference1.push().setValue(item);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView price;
        TextView firstName;
        Button deleteButton;
        Button updateButton;
        EditText editText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.tvfirstNameBasket);
            deleteButton = itemView.findViewById(R.id.buttonBasket4);
            price = itemView.findViewById(R.id.PriceText);

        }
    }

}


