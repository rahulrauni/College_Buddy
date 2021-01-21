package com.printhub.printhub.collab;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.printhub.printhub.IndividualProduct;
import com.printhub.printhub.R;
import com.printhub.printhub.clubEvents.EventsClass;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.printhub.printhub.HomeScreen.MainnewActivity.cityName;
import static com.printhub.printhub.HomeScreen.MainnewActivity.collegeName;


public class collabAdapter extends RecyclerView.Adapter<collabAdapter.ViewHolder> {

    List<collabClass> collab_list;
    Context context;
    private FirebaseFirestore db;
    public collabAdapter( List<collabClass> collab_list,Context context) {
        this.collab_list=collab_list;
        this.context= context;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.collab_cardlayout,parent,false);
        db= FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       // String desc_data=blog_list.get(position).getDescription();
       // holder.setDescText(desc_data);
        holder.setIsRecyclable(false);
        String postkey =  collab_list.get(position).getPostkey();
        holder.description.setText(collab_list.get(position).getDescription());
        holder.domain.setText("Domain:"+" "+collab_list.get(position).getDomain());
        String postuser_id=collab_list.get(position).getUserid();
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(cityName).document(collegeName).collection("users").document(postuser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String userName=task.getResult().getString("name");
                    String imageurl = task.getResult().getString("imageLink").toString().trim();
                    holder.username.setText(userName);
                    Picasso.with(context).load(imageurl).into(holder.userimage);
                }
            }
        });
        //countlike
        db.collection(cityName).document(collegeName).collection("collab").document(postkey).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots,FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    holder.text_action.setText(count+" Interested");
                }else{
                    holder.text_action.setText(0 +" Interested");
                }

            }
        });
        //getlike
        db.collection(cityName).document(collegeName).collection("collab").document(postkey).collection("Likes").document(userid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot,FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.interest.setImageDrawable(context.getDrawable(R.drawable.like_red));
                }else{
                    holder.interest.setImageDrawable(context.getDrawable(R.drawable.like_grey));

                }

            }
        });



        holder.interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //holder.interest.playAnimation();
                collabClass collabClass = new collabClass(collab_list.get(position).getDomain(),collab_list.get(position).getDescription(),collab_list.get(position).getMobileNo(),collab_list.get(position).getWhatsApp(),collab_list.get(position).getGithubId(),collab_list.get(position).getLinkedinId(),collab_list.get(position).getStatus(),collab_list.get(position).getUserid(),collab_list.get(position).getTimestamp(),postkey);

                db.collection(cityName).document(collegeName).collection("collab").document(postkey).collection("Likes").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            db.collection(cityName).document(collegeName).collection("users").document(userid).collection("interest").document(postkey).set(collabClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toasty.success(context, "Added to Interest").show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toasty.success(context, "Failed Try Again").show();
                                }
                            });
                            Map<String,Object> likemap = new HashMap<>();
                            likemap.put("timestamp", FieldValue.serverTimestamp());
                            db.collection(cityName).document(collegeName).collection("collab").document(postkey).collection("Likes").document(userid).set(likemap);
                        }else{
                            db.collection(cityName).document(collegeName).collection("users").document(userid).collection("interest").document(postkey).delete();
                            db.collection(cityName).document(collegeName).collection("collab").document(postkey).collection("Likes").document(userid).delete();
                        }

                    }
                });


            }
        });

        long milliseconds=collab_list.get(position).getTimestamp().getTime();
        String dateString= DateFormat.format("dd/MM/yyyy",new Date(milliseconds)).toString();
        holder.postdate.setText(dateString);


    }

    @Override
    public int getItemCount() {
        return collab_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        CircleImageView userimage;
        ImageView github,linkedin,whatsapp,call;
        ImageView interest;
        private TextView username,postdate,domain,description,text_action;
        public ViewHolder(@NonNull  View itemView) {

            super(itemView);
            mView=itemView;
            userimage= mView.findViewById(R.id.userimage);
            github= mView.findViewById(R.id.github);
            linkedin= mView.findViewById(R.id.linkedin);
            whatsapp= mView.findViewById(R.id.whatsapp);
            call= mView.findViewById(R.id.call);
            interest= mView.findViewById(R.id.interest);
            username= mView.findViewById(R.id.username);
            postdate= mView.findViewById(R.id.postdate);
            domain= mView.findViewById(R.id.domain);
            description= mView.findViewById(R.id.description);
            text_action = mView.findViewById(R.id.text_action);



            //descView=mView.findViewById(R.id.blog_desc);
        }

//        public void setDescText(String descText){
//            descView=mView.findViewById(R.id.blog_desc);
//            descView.setText(descText);
//        }

    }
}