package com.example.bonnie.picturesque;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

//Senta toma um café , não repara a bagunça



public class PhotoActivity extends AppCompatActivity {

    private FloatingActionsMenu fac;
    private EditText tagEditText2;
    // constantes
    private static final int REQUEST_TAKE_PICTURE = 1001;
    private static final int REQUEST_PERMISSION_CAMERA = 2001;
    private RecyclerView photosRecyclerView;
    private List<Bitmap> photos;
    private Photo photo;
    private List<Photo> infoPhoto;
    private RecyclerView recyclerView;
    private ImageView photoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);


        fac = (FloatingActionsMenu) findViewById(R.id.multiple_actions_left);
        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        FloatingActionButton fabText = (FloatingActionButton) findViewById(R.id.fabText);
        fabCamera.setOnClickListener(listenerCamera);
        fabText.setOnClickListener(listenerText);
        tagEditText2 = (EditText) findViewById(R.id.tagEditText2);
        android.support.design.widget.FloatingActionButton fabTag = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fabTag);
        fabTag.setOnClickListener(listenerTag);
        //RecyclerView
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        photos = new ArrayList<Bitmap>();
        photosRecyclerView.setAdapter(new PhotoAdapter(this, photos));
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            tagEditText2.setText(bundle.getString("tag"));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        configurarOFirebase();
    }


    private static class PhotosViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView photoImageView;
        private PhotosViewHolder.ClickListener mClickListener;


        public PhotosViewHolder(View view) {
            super(view);
            this.view = view;
            this.photoImageView = view.findViewById(R.id.photoImageView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });

        }

        public interface ClickListener {
            public void onItemClick(View view, int position);

            public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(PhotosViewHolder.ClickListener clickListener) {
            mClickListener = clickListener;
        }
    }



    private class PhotoAdapter extends RecyclerView.Adapter<PhotosViewHolder>{
       // pega o inflador de Layout
       private Context context;
       private List<Bitmap> photos;

       public PhotoAdapter (Context context, List<Bitmap> photos){
           this.context = context;
           this.photos = photos;
       }
        @Override
        public int getItemCount() {
            return photos.size();
        }


        @NonNull
        @Override
        public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.photos_layout, parent,
                    false);

            PhotosViewHolder viewHolder = new PhotosViewHolder(v);
            viewHolder.setOnClickListener(new PhotosViewHolder.ClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //Toast.makeText(PhotoActivity.this, "Item clicked at " + position, Toast.LENGTH_SHORT).show();
                   Intent intent = new Intent();
                   intent.setAction(Intent.ACTION_VIEW);
                   Uri image = Uri.parse(infoPhoto.get(position).getUrl());
                   intent.setDataAndType(image, "image/*");
                   startActivity(intent);
//

                }

                @Override
                public void onItemLongClick(View view, int position) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this);
                    builder.setMessage(R.string.dialog)
                        .setPositiveButton(R.string.cancelar, new CancelarButtonListener(infoPhoto.get(position)))
                        .setNegativeButton(R.string.excluir, new ExcluirButtonListener(infoPhoto.get(position),position));

                builder.create().show();
                }
            });

            return viewHolder;
        }



        public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
            Bitmap foto = photos.get(position);
            holder.photoImageView.setImageBitmap(foto);
        }
    }

    private void configuraMinhaRecyclerView() {
        //busca na árvore
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        //inicializa a fonte de dados
        photos = new ArrayList<Bitmap>();
        //vincula o adapter
        photosRecyclerView.setAdapter(new PhotoAdapter(this, photos));
        //diz qual o gerenciador de layout
        //neste caso temos um GridLayout com 2 colunas
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private View.OnClickListener listenerTag = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tag = tagEditText2.getEditableText().toString();

            if (tag != null && !tag.equals("")){
                photos.clear();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                DatabaseReference urlsReference = firebaseDatabase.getReference("images").child(tag);
                urlsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //limpa fotos anteriores
                        photos.clear();
                        infoPhoto = new ArrayList<Photo>();
                        photosRecyclerView.getAdapter().notifyDataSetChanged();
                        //para cada foto
                        for (DataSnapshot filho : dataSnapshot.getChildren()) {
                            String url = filho.getValue() + PNG_EXTENSION;
                            StorageReference aux = firebaseStorage.getReferenceFromUrl(url);
                            photo = new Photo();
                            photo.setId( filho.getKey());
                            photo.setUrl(url);
                            String tag = tagEditText2.getEditableText().toString();
                            photo.setTag(tag);


                            // Por Carregar as fotos de forma assincrona ao apertar o botão de atualizar na tela varias vezes muito rápido
                            // As fotos são duplicadas, triplicadas etc. Seja delicado e paciente ao apertar o botão :D


                            aux.getBytes(UM_MEGA).addOnSuccessListener(new MyOnSuccessListener(photo))
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //se falhar
                                    Toast.makeText(PhotoActivity.this, getString(R.string.problemas_com_download),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        //se não der certo a conexão inicial
                        Toast.makeText(PhotoActivity.this, getString(R.string.problemas_ao_conectar_firebase),
                                Toast.LENGTH_SHORT).show();
                    }


                });
            } else {
                Toast.makeText(PhotoActivity.this,getString(R.string.informa_tag),
                        Toast.LENGTH_SHORT).show();

            }

        }
    };

    class MyOnSuccessListener implements OnSuccessListener<byte[]>{
        Photo photo;
        MyOnSuccessListener(Photo photo){
            this.photo = photo;
        }
        @Override
        public void onSuccess(byte[] bytes) {
            updateRecyclerViewList(Utils.toBitmap(bytes));
            infoPhoto.add(this.photo);
        }
    }

    // Bloco do Botão da Camera Permissões da Mesma Etc----
    private View.OnClickListener listenerCamera = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tag = tagEditText2.getEditableText().toString();
            if (tag != null && !tag.equals("")) {

                if (ActivityCompat.checkSelfPermission(PhotoActivity.this, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PhotoActivity.this,
                            android.Manifest.permission.CAMERA)) {
                        Toast.makeText(PhotoActivity.this,
                                getString(R.string.permissaoCamera), Toast.LENGTH_SHORT).show();
                    }
                    //se não tem permissão, pede
                    ActivityCompat.requestPermissions(PhotoActivity.this, new String[]
                            {Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                } else {
                    //se já tem, tira foto
                    goCamera();
                }

            }

            else {
                Toast.makeText(PhotoActivity.this,getString(R.string.informa_tag),
                        Toast.LENGTH_SHORT).show();
            }

        }

    };

    @Override //Quando a pessoa clicou em sim ou não no balão pedindo permissão
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA:
                //usuário disse sim
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goCamera();
                }
                //usuário disse não
                else {
                    Toast.makeText(this, getString(R.string.permissaoCamera),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //Quando volta da aplicação da camera
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PICTURE:
                //usuário tirou foto
                if (resultCode == Activity.RESULT_OK) {
                    //pega a foto
                    Bitmap foto = (Bitmap) data.getExtras().get("data");
                    //faz upload para o Firebase Storage
                    uploadImage(foto);
                } else {
                    Toast.makeText(this, getString(R.string.foto_nao_tirada), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void goCamera() {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicIntent, REQUEST_TAKE_PICTURE);
    }

    private void updateRecyclerViewList(Bitmap foto){
        photos.add(foto);
        photosRecyclerView.getAdapter().notifyDataSetChanged();
    }


    private void uploadImage(final Bitmap image) {
        //gera um nomoe aleatório
        final String chave = this.fileNameGenerator.push().getKey();
        //armazena no storage com extensão
        StorageReference storageReference = this.imagesReference.child(chave + PNG_EXTENSION);
        byte[] data = Utils.toByteArray(image);
        UploadTask task = storageReference.putBytes(data);
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                updateRecyclerViewList(image);
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                //no database a chave tem que ser sem extensão, por causa do ponto
                String tag = tagEditText2.getEditableText().toString();
                saveURLForDownload(downloadUrl, chave, tag);
                Toast.makeText(PhotoActivity.this, getString(R.string.sucesso),
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PhotoActivity.this, getString(R.string.falha),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });}

    private void saveURLForDownload (Uri downloadURL, String name, String tag){
        this.urlsReference.child(tag).child(name).setValue(downloadURL.toString());


    }


    //Configurações do Firebase

    //referência ao diretório images
    private StorageReference imagesReference;
    //referência para gerar nomes de arquivos únicos
    private DatabaseReference fileNameGenerator;
    //referência para guardar as urls das fotos cujo upload foi realizado
    private DatabaseReference urlsReference;
    //Constantes
    private static final String PNG_EXTENSION = ".png";
    private static final long UM_MEGA = 1024 * 1024;

    public void configurarOFirebase() {
        //singleton do Storage cara que representa seu espaço todo lá no firebase
        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        //referência para a raiz do Storage
        final StorageReference storageRootReference = firebaseStorage.getReference();
        //referência para a pasta images, onde todas as fotos ficarão
        this.imagesReference = storageRootReference.child("images");
        //referência para o singleton do Database realtime
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        //referência para o gerador de nomes de arquivos
        this.fileNameGenerator = firebaseDatabase.getReference("image_names");
        //referência para onde os nomes ficam associados a urls
        this.urlsReference = firebaseDatabase.getReference("images");

    }

    // Ida para tela de textos -----------------------------------------------------------

    private View.OnClickListener listenerText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tag = tagEditText2.getEditableText().toString();
            Intent i = new Intent(PhotoActivity.this, TextActivity.class);
            i.putExtra("tag",tag);
            startActivity(i);

        }
    };

    //--------------------------------------------------------------------------------

    public class CancelarButtonListener implements DialogInterface.OnClickListener {


        public CancelarButtonListener(Photo photo){
           // photo = photo;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(PhotoActivity.this,getString(R.string.cancelado), Toast.LENGTH_SHORT).show();
        }
    }

    public class ExcluirButtonListener implements DialogInterface.OnClickListener {
        Photo photo;
        long indice;

        public ExcluirButtonListener(Photo photo, long indice){
            this.photo = photo;
            this.indice = indice;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

            firebaseDatabase.getReference("images").child(this.photo.getTag()).child(this.photo.getId()).removeValue();
            firebaseStorage.getReference("images").child(this.photo.getId() + PNG_EXTENSION).delete();

            photos.remove((int)indice);
            infoPhoto.remove((int)indice);
            photosRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }



}
