package com.example.iagolast.dnifirma;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import de.tsenger.androsmex.mrtd.DG1_Dnie;
import es.gob.jmulticard.jse.provider.DnieKeyStore;
import es.gob.jmulticard.jse.provider.DnieProvider;
import es.gob.jmulticard.jse.provider.MrtdKeyStoreImpl;

public class MainActivity extends AppCompatActivity {

    // Put your CAN number here !!!!
    private static final String CAN_NUMBER = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // If no tag is found, skip all the process.
        if (tag == null) {
            return;
        }
        try {
            readDniData(tag);
        } catch (Exception e) {
            e.printStackTrace();
            if (CAN_NUMBER == null) {
                Log.e("ERROR", "Invalid Card Access Number (CAN), please provide a valid 6 digit number.");
            }
        }
    }

    private void readDniData(Tag tag) throws CertificateException, NoSuchAlgorithmException, IOException {
        // Configure DnieProvider
        final DnieProvider dnieProvider = new DnieProvider();
        dnieProvider.setProviderTag(tag);
        dnieProvider.setProviderCan(CAN_NUMBER);
        Security.insertProviderAt(dnieProvider, 1);

        // Improve read speed
        System.setProperty("es.gob.jmulticard.fastmode", "true");

        // Create DnieKeyStore
        KeyStoreSpi keyStoreSpi = new MrtdKeyStoreImpl();
        DnieKeyStore dnieKeyStore = new DnieKeyStore(keyStoreSpi, dnieProvider, "MRTD");

        // Load DNI data, this can throw exceptions and must be async to prevent blocking the UI thread.
        dnieKeyStore.load(null, null);

        // Read first data group.
        DG1_Dnie datagroup1 = dnieKeyStore.getDatagroup1();

        // Get name from datagroup and say hello.
        String name = datagroup1.getName();
        sayHello(name);
    }

    private void sayHello(String name) {
        TextView myAwesomeTextView = (TextView) findViewById(R.id.text_view_0);
        myAwesomeTextView.setText("Hello " + name);
    }
}
