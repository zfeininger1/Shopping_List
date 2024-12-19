package edu.uga.cs.shoppinglist;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PurchasedItemsAdapter extends RecyclerView.Adapter<PurchasedItemsAdapter.ViewHolder>{
    private List<BasketItem> basketItemList;
    public PurchasedItemsAdapter(List<BasketItem> basketItemList) {
        this.basketItemList = basketItemList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_items, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BasketItem basketItem = basketItemList.get(position);
        holder.itemName.setText(basketItem.getItemName());
        holder.itemPrice.setText("Price: $" + basketItem.getItemPrice());

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("previousList");
                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("shoppingList");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot parentSnapshot : dataSnapshot.getChildren()) {
                            String key = parentSnapshot.getKey();
                            DataSnapshot basketItemsSnapshot = parentSnapshot.child("basketItems");
                            if (String.valueOf(basketItemsSnapshot.getChildrenCount()).equals("1")) {
                                DatabaseReference nodeToDeleteRef = FirebaseDatabase.getInstance().getReference("previousList").child(key);
                                nodeToDeleteRef.removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("SUCCESS", "Node deleted successfully: " + key);
                                                Toast.makeText(view.getContext(), "Item deleted: " + basketItem.getItemName(), Toast.LENGTH_SHORT).show();
                                                Item item = new Item(basketItem.getItemName().toString());
                                                databaseReference1.push().setValue(item);

                                            }
                                        });
                                return;
                            }
                            for (DataSnapshot itemSnapshot : basketItemsSnapshot.getChildren()) {
                                String itemName = (String) itemSnapshot.child("itemName").getValue();
                                if (itemName != null && itemName.equals(basketItem.getItemName())) {
                                    Log.d("Match found", "Node key: " + key + ",Basket Item Index: " + itemSnapshot.getKey());
                                    holder.basketItemIndex = itemSnapshot.getKey();
                                    Query query = databaseReference.orderByChild("basketItems/" + holder.basketItemIndex + "/itemName").equalTo(basketItem.getItemName());
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                // Log data based on the query
                                                Log.d("Query", "Data matching the query: " + dataSnapshot.getValue());
                                            } else {
                                                Log.d("Query", "No data matching the query");
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("FirebaseDatabase", "Database write cancelled. Details: " + error.getMessage());
                                        }
                                    });

                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                List<Map<String, Object>> basketItems = (List<Map<String, Object>>) snapshot.child("basketItems").getValue();
                                                Iterator<Map<String, Object>> iterator = basketItems.iterator();
                                                while(iterator.hasNext()) {
                                                    Map<String, Object> itemMap = iterator.next();
                                                    String itemName = (String) itemMap.get("itemName");
                                                    if (itemName != null && itemName.equals(basketItem.getItemName())) {
                                                        iterator.remove();
                                                        break;
                                                    }
                                                }
                                                snapshot.child("basketItems").getRef().setValue(basketItems)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Log.d("DELETE", "Item deleted: " + basketItem.getItemName());
                                                                Toast.makeText(view.getContext(), "Item deleted: " + basketItem.getItemName(), Toast.LENGTH_SHORT).show();
                                                                Item item = new Item(basketItem.getItemName().toString());
                                                                databaseReference1.push().setValue(item);
                                                                DatabaseReference specificNodeRef = FirebaseDatabase.getInstance().getReference("previousList").child(key);
                                                                specificNodeRef.child("totalPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.exists()) {
                                                                            String totalPrice = dataSnapshot.getValue(String.class);
                                                                            double totalPriceHolder = (Double.parseDouble(totalPrice) / 1.04) - Double.parseDouble(basketItem.getItemPrice().toString());
                                                                            totalPrice = String.valueOf((totalPriceHolder * 0.04) + totalPriceHolder);
                                                                            specificNodeRef.child("totalPrice").setValue(totalPrice);
                                                                            Log.d("Total Price", totalPrice);
                                                                        } else {
                                                                            Log.d("Total Price", "does not exits");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e("DELETE", "Failed to delete item: " + basketItem.getItemName(), e);
                                                                Toast.makeText(view.getContext(), "Failed to delete item: " + basketItem.getItemName(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("FirebaseDatabase", "Database write cancelled. Details: " + error.getMessage());
                                        }
                                    });

                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("previousList");
                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("shoppingList");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot parentSnapshot : dataSnapshot.getChildren()) {
                            String key = parentSnapshot.getKey();
                            DataSnapshot basketItemsSnapshot = parentSnapshot.child("basketItems");
                            for (DataSnapshot itemSnapshot : basketItemsSnapshot.getChildren()) {
                                String itemName = (String) itemSnapshot.child("itemName").getValue();
                                if (itemName != null && itemName.equals(basketItem.getItemName())) {
                                    Log.d("Match found", "Node key: " + key + ",Basket Item Index: " + itemSnapshot.getKey());
                                    holder.basketItemIndex = itemSnapshot.getKey();
                                    Query query = databaseReference.orderByChild("basketItems/" + holder.basketItemIndex + "/itemName").equalTo(basketItem.getItemName());
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                // Log data based on the query
                                                Log.d("Query", "Data matching the query: " + dataSnapshot.getValue());
                                            } else {
                                                Log.d("Query", "No data matching the query");
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("FirebaseDatabase", "Database write cancelled. Details: " + error.getMessage());
                                        }
                                    });

                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                List<Map<String, Object>> basketItems = (List<Map<String, Object>>) snapshot.child("basketItems").getValue();
                                                Iterator<Map<String, Object>> iterator = basketItems.iterator();
                                                while(iterator.hasNext()) {
                                                    Map<String, Object> itemMap = iterator.next();
                                                    String itemPrice = (String) itemMap.get("itemPrice");
                                                    if (itemPrice != null && itemPrice.equals(basketItem.getItemPrice())) {
                                                        itemMap.put("itemPrice", holder.editText.getText().toString());
                                                        break;
                                                    }
                                                }
                                                snapshot.child("basketItems").getRef().setValue(basketItems)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Log.d("DELETE", "Item deleted: " + basketItem.getItemName());
                                                                Toast.makeText(view.getContext(), "Item Price replaced: " + basketItem.getItemName(), Toast.LENGTH_SHORT).show();
                                                                DatabaseReference specificNodeRef = FirebaseDatabase.getInstance().getReference("previousList").child(key);
                                                                specificNodeRef.child("totalPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.exists()) {
                                                                            String totalPrice = dataSnapshot.getValue(String.class);
                                                                            double totalPriceHolder = (Double.parseDouble(totalPrice) / 1.04) - Double.parseDouble(basketItem.getItemPrice().toString());
                                                                            totalPriceHolder = totalPriceHolder + Double.parseDouble(holder.editText.getText().toString());
                                                                            totalPrice = String.valueOf((totalPriceHolder * 0.04) + totalPriceHolder);
                                                                            specificNodeRef.child("totalPrice").setValue(totalPrice);
                                                                            Log.d("Total Price", totalPrice);
                                                                        } else {
                                                                            Log.d("Total Price", "does not exits");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e("DELETE", "Failed to delete item: " + basketItem.getItemName(), e);
                                                                Toast.makeText(view.getContext(), "Failed to delete item: " + basketItem.getItemName(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("FirebaseDatabase", "Database write cancelled. Details: " + error.getMessage());
                                        }
                                    });

                                    break;
                                }
                            }
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
    public int getItemCount() {return basketItemList.size();}

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemPrice;
        Button deleteButton;
        Button updateButton;
        EditText editText;
        String basketItemIndex;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.tvfirstNameBasket);
            itemPrice = itemView.findViewById(R.id.PriceText);
            deleteButton = itemView.findViewById(R.id.buttonBasket4);
            updateButton = itemView.findViewById(R.id.button7);
            editText = itemView.findViewById(R.id.editText);
        }
    }
}
