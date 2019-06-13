package com.integrals.inlens.InlensGallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.integrals.inlens.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class InlensGalleryActivity extends AppCompatActivity {

    private List<String> AllCommunityImages;
    private static final String FILE_NAME = "UserInfo.ser";
    private String[] UserInfo;
    private RecyclerView GallerGridView;
    private FloatingActionButton GalleryRefreshFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inlens_gallery);

        GalleryRefreshFab = findViewById(R.id.gallery_refresh_fab);
        GallerGridView = findViewById(R.id.gallery_recyclerview);
        GallerGridView.setHasFixedSize(true);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;



        GallerGridView.setLayoutManager(new GridLayoutManager(this, (int) (dpWidth/100)+1));
        UserInfo = new String[4];
        String data = GetFileData();
        UserInfo = data.split("\n");
        AllCommunityImages = new ArrayList<>();


        if(UserInfo[1].equals("Not Available"))
        {
            Toast.makeText(this, "No community currently active.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            AllCommunityImages = getAllShownImagesPath(Long.parseLong(UserInfo[2]));
            ImageAdapter adapter = new ImageAdapter(getApplicationContext(),AllCommunityImages);
            GallerGridView.setAdapter(adapter);
        }



        GalleryRefreshFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GalleryRefreshFab.clearAnimation();
                GalleryRefreshFab.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gallery_fab_rotate_360));
                GalleryRefreshFab.getAnimation().start();

                AllCommunityImages = getAllShownImagesPath(Long.parseLong(UserInfo[2]));
                ImageAdapter adapter = new ImageAdapter(getApplicationContext(),AllCommunityImages);
                GallerGridView.setAdapter(adapter);

            }
        });

        GallerGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && GalleryRefreshFab.getVisibility() == View.VISIBLE) {

                    GalleryRefreshFab.hide();
                    GalleryRefreshFab.setEnabled(false);
                } else if (dy < 0 && GalleryRefreshFab.getVisibility() != View.VISIBLE) {
                    GalleryRefreshFab.show();
                    GalleryRefreshFab.setEnabled(true);
                }
            }
        });

    }

    private String GetFileData() {


        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder builder = new StringBuilder();
            String text;
            while ((text = bufferedReader.readLine()) != null) {
                builder.append(text).append("\n");
            }

            return builder.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "no data";
    }

    private ArrayList<String> getAllShownImagesPath(long starttime) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            File img = new File(absolutePathOfImage);
            if(img.lastModified() > starttime && !absolutePathOfImage.toLowerCase().contains("screenshot") &&  !absolutePathOfImage.toLowerCase().contains("whatsapp"))
            {
                listOfAllImages.add(absolutePathOfImage);

            }
        }
        return listOfAllImages;
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        private Context context;
        private List<String> ImageList;

        public ImageAdapter(Context context, List<String> imageList) {
            this.context = context;
            ImageList = imageList;
        }


        @NonNull
        @Override
        public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageAdapter.ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item_card,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {


            Glide.with(context).load(ImageList.get(position))
                    .apply(new RequestOptions().placeholder(R.drawable.ic_photo_camera).centerCrop())
                    .into(holder.GallerImage);

        }

        @Override
        public int getItemCount() {
            return ImageList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView GallerImage;
            public ImageViewHolder(View itemView) {
                super(itemView);

                GallerImage = itemView.findViewById(R.id.gallery_item_imageview);


            }
        }
    }
}
