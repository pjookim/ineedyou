package kr.camelsoft.ineedyou;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.bumptech.glide.Glide;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.data.DataHolder;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FIREBASE_POST_URL = "https://ineedyou-57d36.firebaseio.com//Posts";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MyAdapter mAdapter;
    static final List<Post> mPosts = new ArrayList<>();
    static final List<Post> mPosts2 = new ArrayList<>();
    static final List<String> mKeys = new ArrayList<>();
    private Query mRef;
    private static final int REQUEST_WRITE = 0;

    FloatingSearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main);

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                //get suggestions based on newQuery

                //pass them on to the search view
                Snackbar snackbar = Snackbar
                        .make(mRecyclerView, newQuery, Snackbar.LENGTH_SHORT);
                snackbar.show();

                mAdapter.mSearch(newQuery);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView background;
        TextView timeText;
        TextView commentCount;
        CardView cardView;
        TextView location;

        public MyViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            background = (ImageView) itemView.findViewById(R.id.background);
            timeText = (TextView) itemView.findViewById(R.id.timeText);
            commentCount = (TextView) itemView.findViewById(R.id.commentCount);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            location = (TextView) itemView.findViewById(R.id.location);
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
            holder.location.setText(post.getLocation());
            //holder.commentCount.setText("" + post.getCommentMap().size());
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

        public void mSearch(String charText) {
            if (charText.length() == 0) {
                MainActivity.mPosts2.addAll(mPosts);
            } else {
                for (Post wp : mPosts2) {
                    if (wp.getText().contains(charText)) {
                        MainActivity.mPosts2.add(wp);
                    }
                }
            }
            notifyDataSetChanged();
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
