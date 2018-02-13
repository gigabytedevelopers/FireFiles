package com.gigabytedevelopersinc.app.explorer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gigabytedevelopersinc.app.explorer.pro.R;
import com.gigabytedevelopersinc.app.explorer.cursor.RootCursorWrapper;
import com.gigabytedevelopersinc.app.explorer.misc.IconColorUtils;
import com.gigabytedevelopersinc.app.explorer.misc.IconHelper;
import com.gigabytedevelopersinc.app.explorer.model.DocumentInfo;
import com.gigabytedevelopersinc.app.explorer.model.DocumentsContract;
import com.gigabytedevelopersinc.app.explorer.model.DocumentsContract.Document;
import com.gigabytedevelopersinc.app.explorer.setting.SettingsActivity;

import static com.gigabytedevelopersinc.app.explorer.model.DocumentInfo.getCursorInt;
import static com.gigabytedevelopersinc.app.explorer.model.DocumentInfo.getCursorString;

public class RecentsAdapter extends CursorRecyclerViewAdapter<RecentsAdapter.ViewHolder> {

    private final IconHelper mIconHelper;
    private final int mDefaultColor;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public RecentsAdapter(Context context, Cursor cursor){
        super(context, cursor);
        mContext = context;
        mIconHelper = new IconHelper(context);
        mDefaultColor = SettingsActivity.getPrimaryColor();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.setData(cursor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent, parent, false);
        return new ViewHolder(itemView);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener(){
        return onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(ViewHolder item, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconMime;
        private final ImageView iconThumb;
        private final View iconMimeBackground;
        private Cursor mCursor;
        public DocumentInfo mDocumentInfo;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(ViewHolder.this, getLayoutPosition());
                }
            });
            iconMime = (ImageView) v.findViewById(R.id.icon_mime);
            iconThumb = (ImageView) v.findViewById(R.id.icon_thumb);
            iconMimeBackground = v.findViewById(R.id.icon_mime_background);
        }

        public void setData(Cursor cursor){
            mDocumentInfo = DocumentInfo.fromDirectoryCursor(cursor);
            final String docAuthority = getCursorString(cursor, RootCursorWrapper.COLUMN_AUTHORITY);
            final String docId = getCursorString(cursor, Document.COLUMN_DOCUMENT_ID);
            final String docMimeType = getCursorString(cursor, Document.COLUMN_MIME_TYPE);
            final String docPath = getCursorString(cursor, Document.COLUMN_PATH);
            final String docDisplayName = getCursorString(cursor, Document.COLUMN_DISPLAY_NAME);
            final int docIcon = getCursorInt(cursor, Document.COLUMN_ICON);
            final int docFlags = getCursorInt(cursor, Document.COLUMN_FLAGS);

            mIconHelper.stopLoading(iconThumb);

            iconMimeBackground.setVisibility(View.VISIBLE);
            iconMimeBackground.setBackgroundColor(IconColorUtils.loadMimeColor(mContext, docMimeType, docAuthority, docId, mDefaultColor));
            iconThumb.animate().cancel();
            iconThumb.setAlpha(0f);

            final Uri uri = DocumentsContract.buildDocumentUri(docAuthority, docId);
            mIconHelper.loadThumbnail(uri, docPath, docMimeType, docFlags, docIcon, iconMime, iconThumb, iconMimeBackground);
        }
    }
}