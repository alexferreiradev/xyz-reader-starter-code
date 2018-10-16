package com.example.xyzreader.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import com.example.xyzreader.data.loader.ArticleLoader;

public class ArticleDetailsFragmentPresenter implements ArticleDetailContract.PresenterFragment {
	private static final String TAG = ArticleDetailsFragmentPresenter.class.toString();

	private Context context;
	private ArticleDetailContract.FragmentView view;
	private Cursor mCursor;
	private long itemId;
	private int cursorPosition;

	public ArticleDetailsFragmentPresenter(Context context, ArticleDetailContract.FragmentView view, long itemId, int cursorPosition) {
		this.context = context;
		this.view = view;
		this.itemId = itemId;
		this.cursorPosition = cursorPosition;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
		return ArticleLoader.newAllArticlesInstance(context, this);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
		if (!view.isFragmentAdded()) {
			if (cursor != null) {
				cursor.close();
			}
			return;
		}

		view.setProgressBarVisibility(false);
		cursor.moveToPosition(cursorPosition);
		mCursor = cursor;
		view.bindView(cursor);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		mCursor = null;
		view.bindView(null);
	}

	@Override
	public void onScroolChanged(int mScrollY) {
	}

	@Override
	public long getArticleId() {
		return itemId;
	}

	@Override
	public void onStartLoad() {
		view.setProgressBarVisibility(true);
	}

	@Override
	public void onFinishLoad() {
		view.setProgressBarVisibility(false);
	}
}
