package com.example.bonnie.picturesque;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TextActivity extends AppCompatActivity {
    private EditText tagEditText;
    private EditText valueEditText;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference textReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        tagEditText = findViewById(R.id.tagEditText);
        valueEditText = findViewById(R.id.valueEditText);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            tagEditText.setText(b.getString("tag"));
        }

        configuraFirebase();
        valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                salvarNoFirebase();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                carregaNaTela();
                //salvarNoFirebase();

            }
        });


        FloatingActionButton gallery = (FloatingActionButton) findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = tagEditText.getEditableText().toString();
                Intent i = new Intent(TextActivity.this, PhotoActivity.class);
                i.putExtra("tag",tag);
                startActivity(i);

            }
        });
    }

    private void configuraFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        textReference = firebaseDatabase.getReference("textos");
    }

    private void salvarNoFirebase() {
        String tag = tagEditText.getEditableText().toString();
        String texto = valueEditText.getEditableText().toString();
        textReference.child(tag).setValue(texto);
    }

    private void carregaNaTela() {
        String tag = tagEditText.getEditableText().toString();
        textReference.child(tag).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String texto = dataSnapshot.getValue(String.class);
                valueEditText.setText(texto);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



}
