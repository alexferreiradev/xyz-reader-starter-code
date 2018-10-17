package com.example.xyzreader.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.text.Spanned;
import com.example.xyzreader.data.loader.ArticleLoader;

public class ArticleDetailsFragmentPresenter implements ArticleDetailContract.PresenterFragment {
	private static final String TAG = ArticleDetailsFragmentPresenter.class.toString();

	private Context context;
	private ArticleDetailContract.FragmentView view;
	private long itemId;
	private int cursorPosition;
	private Cursor allArticleCursor;

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
		view.setProgressBarVisibility(false);
		allArticleCursor = cursor;
		cursor.moveToPosition(cursorPosition);
		view.bindView(cursor);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
	}

	@Override
	public void onScroolChanged(int mScrollY) {
	}

	@Override
	public String getCompletedBodyText() {
		if (allArticleCursor != null) {
			allArticleCursor.moveToPosition(cursorPosition);

			return allArticleCursor.getString(ArticleLoader.Query.BODY);
		}

		return "";
	}

	@Override
	public void startLoadBody() {
		view.setProgressBarVisibility(true);
	}

	@Override
	public void loadedAditionalBody(Spanned aditionalBody) {
		view.setProgressBarVisibility(false);
		view.addBodyTextPart(aditionalBody);
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
