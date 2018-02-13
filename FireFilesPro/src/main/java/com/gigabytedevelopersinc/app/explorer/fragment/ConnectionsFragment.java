package com.gigabytedevelopersinc.app.explorer.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.gigabytedevelopersinc.app.explorer.BaseActivity;
import com.gigabytedevelopersinc.app.explorer.DialogFragment;
import com.gigabytedevelopersinc.app.explorer.DocumentsActivity;
import com.gigabytedevelopersinc.app.explorer.DocumentsApplication;
import com.gigabytedevelopersinc.app.explorer.pro.R;
import com.gigabytedevelopersinc.app.explorer.adapter.ConnectionsAdapter;
import com.gigabytedevelopersinc.app.explorer.misc.AnalyticsManager;
import com.gigabytedevelopersinc.app.explorer.misc.RootsCache;
import com.gigabytedevelopersinc.app.explorer.misc.Utils;
import com.gigabytedevelopersinc.app.explorer.model.RootInfo;
import com.gigabytedevelopersinc.app.explorer.network.NetworkConnection;
import com.gigabytedevelopersinc.app.explorer.provider.ExplorerProvider;
import com.gigabytedevelopersinc.app.explorer.provider.NetworkStorageProvider;
import com.gigabytedevelopersinc.app.explorer.setting.SettingsActivity;
import com.gigabytedevelopersinc.app.explorer.ui.CompatTextView;
import com.gigabytedevelopersinc.app.explorer.ui.FloatingActionButton;
import com.gigabytedevelopersinc.app.explorer.ui.MaterialProgressBar;

import static com.gigabytedevelopersinc.app.explorer.DocumentsApplication.isTelevision;
import static com.gigabytedevelopersinc.app.explorer.model.DocumentInfo.getCursorInt;
import static com.gigabytedevelopersinc.app.explorer.network.NetworkConnection.SERVER;

public class ConnectionsFragment extends ListFragment implements View.OnClickListener{

    public static final String TAG = "ConnectionsFragment";

    private ListView mListView;

    private ConnectionsAdapter mAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    private final int mLoaderId = 42;
    private MaterialProgressBar mProgressBar;
    private CompatTextView mEmptyView;
    private FloatingActionButton fab;
    private RootInfo mConnectionsRoot;
    private int mLastShowAccentColor;

    public static void show(FragmentManager fm) {
        final ConnectionsFragment fragment = new ConnectionsFragment();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container_directory, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static ConnectionsFragment get(FragmentManager fm) {
        return (ConnectionsFragment) fm.findFragmentByTag(TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(isTelevision());
        mConnectionsRoot = DocumentsApplication.getRootsCache(getActivity()).getConnectionsRoot();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_connections,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final Resources res = getActivity().getResources();

        fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        if(isTelevision()){
            fab.setVisibility(View.GONE);
        }

        mProgressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        mEmptyView = (CompatTextView)view.findViewById(android.R.id.empty);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setOnItemClickListener(mItemListener);
        if(isTelevision()) {
            mListView.setOnItemLongClickListener(mItemLongClickListener);
        }
        fab.attachToListView(mListView);

        // Indent our list divider to align with text
        final Drawable divider = mListView.getDivider();
        final boolean insetLeft = res.getBoolean(R.bool.list_divider_inset_left);
        final int insetSize = res.getDimensionPixelSize(R.dimen.list_divider_inset);
        if (insetLeft) {
            mListView.setDivider(new InsetDrawable(divider, insetSize, 0, 0, 0));
        } else {
            mListView.setDivider(new InsetDrawable(divider, 0, 0, insetSize, 0));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int accentColor = SettingsActivity.getAccentColor();
        if ((mLastShowAccentColor != 0 && mLastShowAccentColor == accentColor))
            return;
        fab.setBackgroundTintList(ColorStateList.valueOf(accentColor));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getActivity();

        mAdapter = new ConnectionsAdapter(this);
        mCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri contentsUri = ExplorerProvider.buildConnection();

                String selection = null;
                String[] selectionArgs = null;
                if(!Utils.hasWiFi(getActivity())){
                    selection = ExplorerProvider.ConnectionColumns.TYPE + "!=? " ;
                    selectionArgs = new String[]{SERVER};
                }

                return new CursorLoader(context, contentsUri, null, selection, selectionArgs, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor result) {
                if (!isAdded())
                    return;

                mAdapter.swapResult(result);
                mEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);

                if (isResumed()) {
                    setListShown(true);
                } else {
                    setListShownNoAnimation(true);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapResult(null);
            }
        };
        setListAdapter(mAdapter);
        setListShown(false);
        // Kick off loader at least once
        getLoaderManager().restartLoader(mLoaderId, null, mCallbacks);

    }

    public void reload(){
        getLoaderManager().restartLoader(mLoaderId, null, mCallbacks);
        RootsCache.updateRoots(getActivity(), NetworkStorageProvider.AUTHORITY);
    }

    private AdapterView.OnItemClickListener mItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Cursor cursor = mAdapter.getItem(position);
            if (cursor != null) {
                NetworkConnection connection = NetworkConnection.fromConnectionsCursor(cursor);
                openConnectionRoot(connection);
            }
        }
    };

    private AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            showPopupMenu(view, position);
            return false;
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.connections_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add:
                addConnection();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()){
            case R.id.fab:
                addConnection();
                break;
            case R.id.button_popup:
                final int position = mListView.getPositionForView(view);
                if (position != ListView.INVALID_POSITION) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            showPopupMenu(view, position);
                        }
                    });
                }
                break;
        }
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu popup = new PopupMenu(getActivity(), view);

        popup.getMenuInflater().inflate(R.menu.popup_connections, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onPopupMenuItemClick(menuItem, position);
            }
        });
        popup.show();
    }

    public boolean onPopupMenuItemClick(MenuItem item, int position) {
        final Cursor cursor = mAdapter.getItem(position);
        int connection_id = getCursorInt(cursor, BaseColumns._ID);
        NetworkConnection networkConnection = NetworkConnection.fromConnectionsCursor(cursor);
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_edit:
                editConnection(connection_id);
                return true;
            case R.id.menu_delete:
                if(!networkConnection.type.equals(SERVER)) {
                    deleteConnection(connection_id);
                } else {
                    ((BaseActivity)getActivity())
                            .showSnackBar("Default server connection can't be deleted",
                                    Snackbar.LENGTH_SHORT);
                }
                return true;
            default:
                return false;
        }
    }

    private void addConnection() {
        CreateConnectionFragment.show(((DocumentsActivity)getActivity()).getSupportFragmentManager());
        AnalyticsManager.logEvent("connection_add");
    }

    private void editConnection(int connection_id) {
        CreateConnectionFragment.show(((DocumentsActivity)getActivity()).getSupportFragmentManager(), connection_id);
        AnalyticsManager.logEvent("connection_edit");
    }

    private void deleteConnection(final int connection_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete connection?").setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int did) {
                dialog.dismiss();
                boolean success = NetworkConnection.deleteConnection(getActivity(), connection_id);
                if(success){
                    reload();
                }
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int did) {
                dialog.dismiss();
            }
        });
        DialogFragment.showThemedDialog(builder);
        AnalyticsManager.logEvent("connection_delete");
    }

    public void openConnectionRoot(NetworkConnection connection) {
        DocumentsActivity activity = ((DocumentsActivity)getActivity());
        activity.onRootPicked(activity.getRoots().getRootInfo(connection), mConnectionsRoot);
    }
}