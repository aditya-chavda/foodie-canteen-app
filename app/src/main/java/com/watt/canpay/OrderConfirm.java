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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.watt.canpay.model.MenuItem;

import java.util.HashSet;

public class OrderConfirm extends AppCompatActivity {

    Button cash, card;
    TextView total_tv;
    NfcAdapter nfcAdapter;
    Integer rs,result;

    int total;
    private HashSet<MenuItem> selectedMenuItems;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        cash = findViewById(R.id.cash_pay);
        card = findViewById(R.id.card_pay);
        total_tv=findViewById(R.id.totalTv);

        selectedMenuItems = (HashSet<MenuItem>)getIntent().getSerializableExtra("MENU_ITEMS");

        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(OrderConfirm.this,"Thanks for your order :)",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OrderConfirm.this,MainActivity.class));
                selectedMenuItems.clear();
                finish();
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        order_summary();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, OrderConfirm.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        if(nfcAdapter!=null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    public void showSettings(View view) {
        startActivity(new Intent(OrderConfirm.this, Settings.class));
    }

    @SuppressLint("SetTextI18n")
    public void order_summary() {
        TextView ol = findViewById(R.id.order_list);
        ol.setText("");
        for(MenuItem menuItem : selectedMenuItems){
            if(menuItem.getItemQuantity() > 0) {
                ol.append(menuItem.getItemName().concat(" ₹").concat(menuItem.getItemPrice()).concat("\t")
                        .concat(" (").concat(String.valueOf(menuItem.getItemQuantity())).concat(")").concat("\n"));
                total += Integer.valueOf(menuItem.getItemPrice()) * menuItem.getItemQuantity();
            }
        }
        total_tv.setText("Total payable amount: ₹".concat(String.valueOf(total)));
    }


    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "Card Detected!", Toast.LENGTH_SHORT).show();

            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (parcelables != null && parcelables.length > 0) {
                readNdefMessage((NdefMessage) parcelables[0]);

                result=rs-total;

                if(result >= 0) {

                    card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            byte[] payload = result.toString().getBytes();
                            NdefRecord ndefRecord = NdefRecord.createExternal("com.watt.canpay", "externaltype", payload);
                            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
                            writeNdefMessage(tag, ndefMessage);
                            startActivity(new Intent(OrderConfirm.this,MainActivity.class));
                            selectedMenuItems.clear();
                            finish();
                        }
                    });

                }else{
                    Toast.makeText(OrderConfirm.this, "Not Sufficient Balance!", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(OrderConfirm.this, "No Balance!", Toast.LENGTH_SHORT).show();
            }
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
            Toast.makeText(this, "Payment Successful! Remaining Balance: ₹" + result.toString(), Toast.LENGTH_SHORT).show();

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
                Toast.makeText(this, "Payment Successful! Remaining Balance: ₹" + result.toString(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("writeNdefMessage", e.getMessage());
        }
    }

    public void readNdefMessage(NdefMessage ndefMessage) {
        if (ndefMessage.getRecords().length > 0) {
            NdefRecord ndefRecord = ndefMessage.getRecords()[0];
            String payload = new String(ndefRecord.getPayload());
            rs=Integer.parseInt(payload);
        }
    }
}