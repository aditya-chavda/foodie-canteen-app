package com.watt.canpay;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watt.canpay.model.MenuItem;

public class Settings extends AppCompatActivity {

    Button addBal, checkBal, logout, addItm, remItm;

    FirebaseAuth firebaseAuth;
    Firebase firebase;

    DatabaseReference databaseReference;

    NfcAdapter nfcAdapter;

    public static final String FIREBASE_SERVER_URL ="https://foodie-project1911.firebaseio.com/";
    public static final String DB_PATH ="Food_Items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        addBal = findViewById(R.id.add_bal);
        checkBal = findViewById(R.id.check_bal);
        logout = findViewById(R.id.logout);
        addItm = findViewById(R.id.addItem);
        remItm = findViewById(R.id.remItem);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        firebaseAuth = FirebaseAuth.getInstance();

        Firebase.setAndroidContext(Settings.this);
        firebase = new Firebase(FIREBASE_SERVER_URL);
        databaseReference = FirebaseDatabase.getInstance().getReference(DB_PATH);

        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        @SuppressLint("InflateParams")
        View mView = getLayoutInflater().inflate(R.layout.dialog_item, null);
        final EditText fn = mView.findViewById(R.id.itemName);
        final EditText fp = mView.findViewById(R.id.itemPrice);
        Button mfAb = mView.findViewById(R.id.btnaddFood);
        builder.setView(mView);
        final AlertDialog dialog = builder.create();

        addItm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        mfAb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = fn.getText().toString();
                String fprice = fp.getText().toString();

                if(fname.equals("")) {
                    fn.setError("Fill this field!");
                }
                if(fprice.equals("")) {
                    fp.setError("Fill this field!");
                }
                if(!fname.equals("") && !fprice.equals("")) {
                    MenuItem menuItem = new MenuItem(fname, fprice);
                    String DBfoodID = databaseReference.push().getKey();
                    databaseReference.child(DBfoodID).setValue(menuItem);
                    Toast.makeText(Settings.this, "Food Item added Successfully", Toast.LENGTH_SHORT).show();
                    fn.setText("");
                    fn.setError(null);
                    fp.setText("");
                    fp.setError(null);
                }
            }
        });

        AlertDialog.Builder builder2 = new AlertDialog.Builder(Settings.this);
        @SuppressLint("InflateParams")
        View mView2 = getLayoutInflater().inflate(R.layout.dialog_ritem, null);
        final EditText fn2 = mView2.findViewById(R.id.fn);
        Button mfRb = mView2.findViewById(R.id.btnRfood);
        builder2.setView(mView2);
        final AlertDialog dialog2 = builder2.create();

        remItm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.show();
            }
        });

        mfRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String txtFN = fn2.getText().toString().trim().toLowerCase();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!txtFN.equals("") ) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String dbFN = snapshot.child("itemName").getValue().toString().toLowerCase();
                                if(txtFN.equals(dbFN)) {
                                    snapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            Toast.makeText(Settings.this, "Food Item removed successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            fn2.setError(null);
                        }else {
                            fn2.setError("Fill this field!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Settings.this, "DB Error", Toast.LENGTH_SHORT).show();
                    }
                });

                fn2.setText("");
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(Settings.this, Login.class));
                Toast.makeText(Settings.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, Settings.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        if(nfcAdapter!=null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "Card Detected!", Toast.LENGTH_SHORT).show();

            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            final Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            @SuppressLint("InflateParams")
            View mView = getLayoutInflater().inflate(R.layout.dialog_bal, null);
            final EditText ed = mView.findViewById(R.id.amount);
            Button mAb = mView.findViewById(R.id.btnadd);
            builder.setView(mView);
            final AlertDialog dialog = builder.create();

            addBal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                }
            });

            mAb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                        Toast.makeText(Settings.this, "No Card Detected!", Toast.LENGTH_SHORT).show();
                    }
                    if (parcelables != null && parcelables.length > 0) {
                        int payload = Integer.parseInt(readNdefMessage((NdefMessage) parcelables[0]));
                        if(payload > 0) {
                            int exrs = Integer.parseInt(ed.getText().toString()) + payload;
                            byte[] bpayload = Integer.toString(exrs).getBytes();
                            NdefRecord ndefRecord = NdefRecord.createExternal("Foodie", "externaltype", bpayload);
                            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
                            writeNdefMessage(tag, ndefMessage);
                            dialog.dismiss();
                        }else if(readNdefMessage((NdefMessage) parcelables[0])==null){
                            byte[] bpayload = ed.getText().toString().getBytes();
                            NdefRecord ndefRecord = NdefRecord.createExternal("Foodie", "externaltype", bpayload);
                            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
                            writeNdefMessage(tag, ndefMessage);
                            dialog.dismiss();
                        }else {
                            byte[] bpayload = ed.getText().toString().getBytes();
                            NdefRecord ndefRecord = NdefRecord.createExternal("Foodie", "externaltype", bpayload);
                            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
                            writeNdefMessage(tag, ndefMessage);
                            dialog.dismiss();
                        }
                    }
                }
            });

            checkBal.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {

                    if (!intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                        Toast.makeText(Settings.this, "No Card Detected!", Toast.LENGTH_SHORT).show();
                    }

                    if (parcelables != null && parcelables.length > 0) {
                        String payload=readNdefMessage((NdefMessage) parcelables[0]);
                        Toast.makeText(Settings.this, "Balance is: ₹" + payload, Toast.LENGTH_LONG).show();
                    } else {
                        //s.setText("No data found!");
                        Toast.makeText(Settings.this, "No Balance!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage) {
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);

            if (ndefFormatable == null) {
                Toast.makeText(this, "Invalid Tag!", Toast.LENGTH_LONG).show();
                return;
            }

            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            Toast.makeText(this, "Balance Updated!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("formatTag", e.getMessage());
        }

    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {

        try {

            if (tag == null) {
                Toast.makeText(this, "Invalid Tag!", Toast.LENGTH_LONG).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {
                // format tag with the ndef format and writes the message.
                formatTag(tag, ndefMessage);
            } else {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Invalid Tag!", Toast.LENGTH_LONG).show();

                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "Balance Updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("writeNdefMessage", e.getMessage());
        }
    }

    public String readNdefMessage(NdefMessage ndefMessage) {
        if (ndefMessage.getRecords().length > 0) {
            NdefRecord ndefRecord = ndefMessage.getRecords()[0];
            return new String(ndefRecord.getPayload());
        }else{
            return null;
        }
    }

    /*public boolean checkRep(String gv) {
        final String gvf = gv;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!gvf.equals("") ) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String dbFN = snapshot.child("itemName").getValue().toString().toLowerCase();
                        if(gvf.equals(dbFN)) {

                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }*/

}
