package edu.uga.cs.shoppinglist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ShoppingListFragment extends Fragment {
    private EditText editTextItem;
    private Button button;

    RecyclerView recyclerView;

    DatabaseReference database;

    ShoppingListAdapter shoppingListAdapter;

    ArrayList<Item> list;


    public ShoppingListFragment() {
        // Required empty public constructor
    }

    public static ShoppingListFragment newInstance(String param1, String param2) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        recyclerView = v.findViewById(R.id.shoppingListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        editTextItem = v.findViewById(R.id.editText);
        button = v.findViewById(R.id.button3);

        database = FirebaseDatabase.getInstance().getReference("shoppingList");
        recyclerView.setHasFixedSize(true);

        list = new ArrayList<>();
        shoppingListAdapter = new ShoppingListAdapter(getContext(),list);
        recyclerView.setAdapter(shoppingListAdapter);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Item item = dataSnapshot.getValue(Item.class);
                    list.add(item);


                }
                shoppingListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextItem.getText().toString().equals("") || editTextItem.getText().toString().equals(null)) {
                    return;
                } else {
                    Item item = new Item(editTextItem.getText().toString());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("shoppingList");
                    myRef.push().setValue(item)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), editTextItem.getText().toString() + " has been added to the Shopping List!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        return v;
    }
}



