package kr.camelsoft.ineedyou;

import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WriteActivity extends AppCompatActivity {
    private EditText mInput;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private List<String> bgUrls = new ArrayList<>();
    private final String DEFAULT_BG_URL = "http://postfiles1.naver.net/20151124_256/akj61300_1448357467095Vrg0b_JPEG/bg1.jpg?type=w2";
    private ImageView mWriteBackground;
    private int mCardBgPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // assets 에 json 에서 배경이미지 목록을 불러옴
        try {
            InputStream stream = getAssets().open("bg_urls.json");
            String[] bgArray = new Gson().fromJson(new InputStreamReader(stream), String[].class);
            if (bgArray != null && bgArray.length > 0) {
                bgUrls.addAll(Arrays.asList(bgArray));
            }
            if (bgUrls.isEmpty()) {
                bgUrls.add(DEFAULT_BG_URL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mInput = (EditText) findViewById(R.id.input);
        mWriteBackground = (ImageView) findViewById(R.id.writeBackground);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(new MyImageAdapter());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = mInput.getText().toString();
                if(TextUtils.isEmpty(text)){
                    return;
                }
                Post post = new Post();
                post.setText(text);
                post.setBgUrl(bgUrls.get(mCardBgPosition));
                post.setWriterId(getMyId());
                post.setWriteTime(System.currentTimeMillis());

                new Firebase(MainActivity.FIREBASE_POST_URL).push().setValue(post);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    class MyImageAdapter extends RecyclerView.Adapter<MyImageViewHolder> {

        @Override
        public MyImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.card_image, null);
            MyImageViewHolder myImageViewHolder = new MyImageViewHolder(itemView);
            return myImageViewHolder;
        }

        @Override
        public void onBindViewHolder(MyImageViewHolder holder, final int position) {
            final String imgSrc = bgUrls.get(position);
            Glide.with(WriteActivity.this).load(imgSrc).centerCrop().into(holder.background);
            holder.background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Glide.with(WriteActivity.this).load(imgSrc).centerCrop().into(mWriteBackground);
                    mCardBgPosition = position;
                }
            });
        }

        @Override
        public int getItemCount() {
            return bgUrls.size();
        }
    }

    class MyImageViewHolder extends RecyclerView.ViewHolder {
        ImageView background;

        public MyImageViewHolder(View itemView) {
            super(itemView);
            background = (ImageView) itemView.findViewById(R.id.background);
        }
    }

    private String getMyId(){
        return Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}