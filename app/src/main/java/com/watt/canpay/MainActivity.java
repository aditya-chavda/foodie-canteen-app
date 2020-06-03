package com.watt.canpay;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watt.canpay.adapter.MenuItemAdapter;
import com.watt.canpay.model.MenuItem;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements MenuItemAdapter.MenuItemListener {

    private TextView txvTotalPrice;
    private Button btnConfirm;

    private RecyclerView recyclerView;

    ProgressDialog progressDialog;

    private int totalPrice = 0;

    private ArrayList<MenuItem> menuItemArrayList = new ArrayList<>();
    private HashSet<MenuItem> selectedMenuItems = new HashSet<>();

    public static final String DB_NAME ="Food_Items";
    DatabaseReference databaseReference;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        txvTotalPrice = findViewById(R.id.total_id);
        btnConfirm = findViewById(R.id.con_btn);

        recyclerView = findViewById(R.id.recyclerview);

        databaseReference = FirebaseDatabase.getInstance().getReference(DB_NAME);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching food for you.......Please Wait :)");
        progressDialog.show();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    MenuItem menuItem = snapshot.getValue(MenuItem.class);

                    menuItemArrayList.add(menuItem);
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                recyclerView.setAdapter(new MenuItemAdapter(MainActivity.this, menuItemArrayList));
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("MENU_ITEMS", selectedMenuItems);
                Intent intent = new Intent(MainActivity.this,OrderConfirm.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void showSettings(View view) {
        startActivity(new Intent(MainActivity.this,Settings.class));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onMenuItemChanged(MenuItem menuItem) {
        if(menuItem.getAdded()!=null){
            if(menuItem.getAdded()){
                totalPrice+=Integer.valueOf(menuItem.getItemPrice());
            }else{
                totalPrice-=Integer.valueOf(menuItem.getItemPrice());
            }
            txvTotalPrice.setText(String.format("₹%d", totalPrice));
            if(menuItem.getItemQuantity() == 0) {
                selectedMenuItems.remove(menuItem);
            }else {
                selectedMenuItems.add(menuItem);
            }
        }
    }
}