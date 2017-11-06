package kr.camelsoft.ineedyou;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by pjookim on 05/11/2017.
 */

public class DetailActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mText;
    private ImageView mCardImage;
    private String mPostId;
    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mText = (TextView) findViewById(R.id.text);
        mCardImage = (ImageView) findViewById(R.id.cardImage);

        mPost = (Post) getIntent().getSerializableExtra("post");
        mPostId = getIntent().getStringExtra("postId");

        Glide.with(this).load(mPost.getBgUrl()).centerCrop().into(mCardImage);
        mText.setText(mPost.getText());

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(mPost.getText());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(new MyCommentAdapter(mPost.getCommentMap()));
    }

    private class MyCommentAdapter extends RecyclerView.Adapter<MyCommentViewHolder> {
        List<Comment> comments = new ArrayList<>();
        String[] keys;

        MyCommentAdapter(Map<String, Comment> commentMap) {
            List<Comment> list = new ArrayList<>(commentMap.values());
            Collections.sort(list, new Comparator<Comment>() {
                public int compare(Comment o1, Comment o2) {
                    Collator collator = Collator.getInstance();
                    String strLhs = String.format("%030d", o1.getWriteTime());
                    String strRhs = String.format("%030d", o2.getWriteTime());
                    return collator.compare(strRhs, strLhs);
                }
            });
            comments.addAll(list);
        }

        @Override
        public MyCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.card_comment, null);
            MyCommentViewHolder viewHolder = new MyCommentViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyCommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            Glide.with(DetailActivity.this).load(comment.getBgUrl()).centerCrop().into(holder.background);
            holder.commentText.setText(comment.getText());
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }
    }

    class MyCommentViewHolder extends RecyclerView.ViewHolder {
        ImageView background;
        TextView commentText;

        public MyCommentViewHolder(View itemView) {
            super(itemView);
            background = (ImageView) itemView.findViewById(R.id.background);
            commentText = (TextView) itemView.findViewById(R.id.commentText);
        }
    }
}