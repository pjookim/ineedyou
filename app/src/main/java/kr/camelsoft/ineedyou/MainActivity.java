package kr.camelsoft.ineedyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String FIREBASE_POST_URL = "https://ineedyou-57d36.firebaseio.com//Posts";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MyAdapter mAdapter;
    private List<Post> mPosts = new ArrayList<>();
    private List<String> mKeys = new ArrayList<>();
    private Query mRef;
    private static final int REQUEST_WRITE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main);

        final SwipeRefreshLayout swipeRefreshLayout =
                (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.pink, R.color.indigo, R.color.lime);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLinearLayoutManager = new LinearLayoutManager(MainActivity.this);

                // 데이터의 순서를 역순으로 보여줌
                mLinearLayoutManager.setReverseLayout(true);
                mLinearLayoutManager.setStackFromEnd(true);

                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                mAdapter = new MyAdapter();
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WriteActivity.class);
                startActivityForResult(intent, REQUEST_WRITE);
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(this);

        // 데이터의 순서를 역순으로 보여줌
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // Firebase 연동 후 Child Event Listener 등록
        mRef = new Firebase(FIREBASE_POST_URL).orderByChild("writeTime");
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Post value = dataSnapshot.getValue(Post.class);
                String key = dataSnapshot.getKey();
                if (previousChildName == null) {
                    mPosts.add(0, value);
                    mKeys.add(0, key);
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mPosts.size()) {
                        mPosts.add(value);
                        mKeys.add(key);
                    } else {
                        mPosts.add(nextIndex, value);
                        mKeys.add(nextIndex, key);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Post value = dataSnapshot.getValue(Post.class);
                int index = mKeys.indexOf(key);
                mPosts.set(index, value);

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A model was removed from the list. Remove it from our list and the name mapping
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);

                mKeys.remove(index);
                mPosts.remove(index);

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // A model changed position in the list. Update our list accordingly
                String key = dataSnapshot.getKey();
                Post newModel = dataSnapshot.getValue(Post.class);
                int index = mKeys.indexOf(key);
                mPosts.remove(index);
                mKeys.remove(index);
                if (previousChildName == null) {
                    mPosts.add(0, newModel);
                    mKeys.add(0, key);
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mPosts.size()) {
                        mPosts.add(newModel);
                        mKeys.add(key);
                    } else {
                        mPosts.add(nextIndex, newModel);
                        mKeys.add(nextIndex, key);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.toException().printStackTrace();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView background;
        TextView timeText;
        TextView commentCount;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            background = (ImageView) itemView.findViewById(R.id.background);
            timeText = (TextView) itemView.findViewById(R.id.timeText);
            commentCount = (TextView) itemView.findViewById(R.id.commentCount);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.card_post, null);
            MyViewHolder myViewHolder = new MyViewHolder(itemView);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Post post = mPosts.get(position);
            holder.text.setText(post.getText());
            holder.commentCount.setText("" + post.getCommentMap().size());
            holder.timeText.setText(getDiffTimeText(post.getWriteTime()));
            Glide.with(MainActivity.this).load(post.getBgUrl()).centerCrop().into(holder.background);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("postId", mKeys.get(position));
                    intent.putExtra("post", post);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }
    }

    String getDiffTimeText(long targetTime) {
        DateTime curDateTime = new DateTime();
        DateTime targetDateTime = new DateTime().withMillis(targetTime);

        int diffDay = Days.daysBetween(curDateTime, targetDateTime).getDays();
        int diffHours = Hours.hoursBetween(targetDateTime, curDateTime).getHours();
        int diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).getMinutes();
        if (diffDay == 0) {
            if(diffHours == 0 && diffMinutes == 0){
                return "방금전";
            }
            if(diffHours > 0){
                return "" + diffHours + "시간 전";
            }
            return "" + diffMinutes + "분 전";

        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return format.format(new Date(targetTime));
        }
    }
}
