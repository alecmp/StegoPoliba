package alessandro.datasecurity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import alessandro.datasecurity.activities.decrypt.DecryptActivity;
import alessandro.datasecurity.activities.decrypt.DecryptResultActivity;
import alessandro.datasecurity.utils.Constants;
import alessandro.datasecurity.utils.Database;
import alessandro.datasecurity.utils.DividerItemDecoration;
import butterknife.ButterKnife;


public class InboxFragment extends Fragment implements MessagesAdapter.MessageAdapterListener {
    public static List<MessageModel> messages = new ArrayList<>();


    private FirebaseUser user;
    private DatabaseReference myRef;
    static FirebaseDatabase database;
    private MessagesAdapter mAdapter;
    private String userId;
    private RecyclerView mRecyclerView;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;

    Query query;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity())
                .setActionBarTitle("Inbox");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        // Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        messages = new ArrayList<>();
        database = Database.getDatabase();
        myRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getUid() != null) {
            userId = user.getUid();
        }


        FloatingActionButton fab = view.findViewById(R.id.inbox_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ContactsActivity.class);
                startActivity(intent);

            }
        });
        mRecyclerView = view.findViewById(R.id.recycler_view);
      /*  query = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userId)
                .child("messages ")
                .getRef();*/

        ButterKnife.bind(getActivity());


        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }

        mAdapter = new MessagesAdapter(getContext(), messages, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        actionModeCallback = new ActionModeCallback();

        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("messages")
                .child(userId)
                .getRef();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    MessageModel mModel = eventSnapshot.getValue(MessageModel.class);
                    mModel.setColor(getRandomMaterialColor("400"));
                    if (mModel.isRead()) {
                        //Log.d("sharedpreff",sharedpreferences.getString(Long.toString(mModel.getId()), "Oggetto Criptato") );
                        mModel.setSubject(sharedpreferences.getString(Long.toString(mModel.getId())+"S", "Oggetto decriptato"));
                        mModel.setDisplayedSubject(mModel.getSubject());
                        mModel.setMessage(sharedpreferences.getString(Long.toString(mModel.getId())+"M", "Messaggio decriptato"));
                    }else{
                       mModel.setDisplayedSubject("Oggetto criptato");
                       mModel.setMessage("Messaggio criptato");
                    }

                    messages.add(mModel);

                }
                mAdapter.notifyDataSetChanged();
                //swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }


    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {

        int returnColor = Color.GRAY;
        int arrayId = 0;
        if(getActivity()!=null) {
            arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getActivity().getPackageName());
        }
        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Toast.makeText(getContext(), "Search...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onIconClicked(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        MessageModel message = messages.get(position);
        message.setImportant(!message.isImportant());
        messages.set(position, message);
        mAdapter.notifyDataSetChanged();

    }


    @Override
    public void onMessageRowClicked(int position) {
        myRef = FirebaseDatabase.getInstance().getReference();
        Query query = myRef.child("messages").child(userId).orderByChild("id").equalTo(messages.get(position).getId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().child("read").setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            MessageModel message = messages.get(position);

            if(!message.isRead()) {
                message.setRead(true);
                messages.set(position, message);
                mAdapter.notifyDataSetChanged();
                // Toast.makeText(getContext(), "Read: " + message.getMessage(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), DecryptActivity.class);
                intent.putExtra(Constants.EXTRA_SECRET_TEXT_RESULT, message.getId());
                intent.putExtra(Constants.EXTRA_SECRET_SUBJECT_RESULT, message.getSubject());
                intent.putExtra(Constants.EXTRA_STEGO_IMAGE_PATH, message.getPath());
                intent.putExtra(Constants.EXTRA_IS_RESULT, false);
                intent.putExtra("from", message.getFrom());
                intent.putExtra("id", Long.toString(message.getId()));
                startActivity(intent);
            }else{
                Intent intent = new Intent(getActivity(), DecryptResultActivity.class);
                intent.putExtra(Constants.EXTRA_SECRET_TEXT_RESULT, message.getMessage());
                intent.putExtra(Constants.EXTRA_SECRET_SUBJECT_RESULT, message.getSubject());
                intent.putExtra(Constants.EXTRA_STEGO_IMAGE_PATH, message.getPath());
                intent.putExtra(Constants.EXTRA_IS_RESULT, true);
                intent.putExtra("from", message.getFrom());
                intent.putExtra("id", Long.toString(message.getId()));
                startActivity(intent);

            }

        }
    }

    @Override
    public void onRowLongClicked(int position) {
        // long press is performed, enable action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
            //swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteMessages();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
//            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // deleting the messages from recycler view
    private void deleteMessages() {

        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {

            MessageModel message = messages.get(i);
            Query queryRef = FirebaseDatabase.getInstance().getReference().child("messages").child(userId).orderByChild("id").equalTo(message.getId());
            queryRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                    snapshot.getRef().setValue(null);
                }
            });




            mAdapter.removeData(selectedItemPositions.get(i));





        }
        mAdapter.notifyDataSetChanged();
    }

}
