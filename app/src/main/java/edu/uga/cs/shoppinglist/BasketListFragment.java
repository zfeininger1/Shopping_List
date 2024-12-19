package edu.uga.cs.shoppinglist;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.widget.Toast;

public class BasketListFragment extends Fragment {

    private Spinner spinner;
    private Button button;
    private EditText editText;
    RecyclerView recyclerView;

    DatabaseReference database;

    BasketListAdapter BasketListAdapter;

    ArrayList<BasketItem> list;
    private Button purchaseButton;


    public BasketListFragment() {
        // Required empty public constructor
    }

    public static BasketListFragment newInstance(String param1, String param2) {
        BasketListFragment fragment = new BasketListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_basket, container, false);

        // Initialize Spinner
        recyclerView = v.findViewById(R.id.basketListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        editText = v.findViewById(R.id.editText);
        button = v.findViewById(R.id.button_basket);
        purchaseButton = v.findViewById(R.id.button6);

        database = FirebaseDatabase.getInstance().getReference("shoppingBasket");
        recyclerView.setHasFixedSize(true);

        list = new ArrayList<>();
        BasketListAdapter = new BasketListAdapter(getContext(),list);
        recyclerView.setAdapter(BasketListAdapter);

        spinner = v.findViewById(R.id.spinner);
        button = v.findViewById(R.id.button_basket);
        editText = v.findViewById(R.id.editText);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    BasketItem item = dataSnapshot.getValue(BasketItem.class);
                    list.add(item);


                }
                BasketListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Load data from Firebase to Spinner
        loadDataFromFirebase();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d("TEST", "clicked " + spinner.getSelectedItem().toString() + " " + editText.getText().toString());
                if (editText.getText().toString().equals("") || editText.getText().toString().equals(null)) {
                    Toast.makeText(getContext(), "ENTER A PRICE VALUE", Toast.LENGTH_SHORT).show();
                    return;
                }
                BasketItem basketItem = new BasketItem(spinner.getSelectedItem().toString(), editText.getText().toString());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("shoppingBasket");
                myRef.push().setValue(basketItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getContext(), spinner.getSelectedItem().toString() + " has been added to shopping Basket", Toast.LENGTH_SHORT).show();
                                DatabaseReference shoppingListRef = database.getReference("shoppingList");
                                shoppingListRef.orderByChild("itemName").equalTo(spinner.getSelectedItem().toString())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        });
            }
        });

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
//                Log.d("TEST", user.getEmail().toString());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference shoppingBasketRef = database.getReference("shoppingBasket");
                DatabaseReference previousListRef = database.getReference("previousList");
                shoppingBasketRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double totalPrice = 0;
                        if (dataSnapshot.exists()) {
                            List<BasketItem> shoppingBasketItems = new ArrayList<>();
                            for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                                BasketItem basketItem = itemSnapshot.getValue(BasketItem.class);
                                if (basketItem != null) {
                                    shoppingBasketItems.add(basketItem);
                                    itemSnapshot.getRef().removeValue();
                                    totalPrice = totalPrice + Double.parseDouble(basketItem.getItemPrice().toString());
                                }
                            }
                            totalPrice = totalPrice + (totalPrice * 0.04);
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            PreviousListItem previousListItem = new PreviousListItem(timestamp, user.getEmail().toString(), String.valueOf(totalPrice), shoppingBasketItems);
//                            Log.d("TOTAL PRICE", String.valueOf(totalPrice));
                            previousListRef.push().setValue(previousListItem)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getContext(), "Items purchased and moved to previousList", Toast.LENGTH_SHORT).show();
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




        return v;
    }


    private void loadDataFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("shoppingList"); // Replace with your actual data path

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the data from Firebase
                List<String> dataList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Handle the case where each item is a HashMap
                    Map<String, Object> itemMap = (Map<String, Object>) snapshot.getValue();
                    if (itemMap != null) {
                        // Assuming your item has a field named "itemName"
                        String itemName = (String) itemMap.get("itemName");
                        if (itemName != null) {
                            dataList.add(itemName);
                        }
                    }
                }

                // Populate the Spinner with the retrieved data
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dataList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }
}


