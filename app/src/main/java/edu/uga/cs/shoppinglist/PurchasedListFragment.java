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
import android.widget.TextView;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PurchasedListFragment extends Fragment {

    RecyclerView recyclerView;
    PurhcasedListAdapter purhcasedListAdapter;
    ArrayList<PreviousListItem> list;
    DatabaseReference database;
    Button settleButton;
    TextView textView;
    String settle = "";
    double totalSum;

    public PurchasedListFragment() {
        // Required empty public constructor
    }

    public static PurchasedListFragment newInstance(String param1, String param2) {
        PurchasedListFragment fragment = new PurchasedListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_purchased_list, container, false);
        recyclerView = v.findViewById(R.id.shoppingListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseDatabase.getInstance().getReference("previousList");
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        purhcasedListAdapter = new PurhcasedListAdapter(getContext(), list);
        recyclerView.setAdapter(purhcasedListAdapter);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PreviousListItem item = dataSnapshot.getValue(PreviousListItem.class);
                    list.add(item);
                }
                purhcasedListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        settleButton = v.findViewById(R.id.button8);
        DatabaseReference previousListRef = FirebaseDatabase.getInstance().getReference("previousList");
        settleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        totalSum = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String totalPriceString = snapshot.child("totalPrice").getValue(String.class);
                            String associatedUser = snapshot.child("email").getValue(String.class);
                            Log.d("Total", associatedUser + " " + totalPriceString);
                            settle = settle + associatedUser + " payed: $" + totalPriceString + ". ";
                            if (!totalPriceString.equals(null)) {
                                double totalPrice = Double.parseDouble(totalPriceString);
                                totalSum += totalPrice;
                            }
                            snapshot.getRef().removeValue();
                        }
                        settle = settle + "*Total spent: $" + String.valueOf(totalSum) + ". ";
                        DatabaseReference userCountRef = FirebaseDatabase.getInstance().getReference("message");
                        userCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Integer currentValue = dataSnapshot.getValue(Integer.class);
                                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                                settle = settle + "Each Individual Owes: $" + String.valueOf(decimalFormat.format(totalSum / currentValue)) + ".*";
                                textView = v.findViewById(R.id.textView4);
                                textView.setText(settle);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return v;
    }
}