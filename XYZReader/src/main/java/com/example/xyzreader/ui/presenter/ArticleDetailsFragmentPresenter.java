package com.example.xyzreader.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.text.Spanned;
import android.util.Log;
import com.example.xyzreader.data.loader.ArticleLoader;
import com.example.xyzreader.ui.fragment.ArticleDetailFragment;
import com.example.xyzreader.ui.task.ArticleBodyLoad;

public class ArticleDetailsFragmentPresenter implements ArticleDetailContract.PresenterFragment {
	private static final String TAG = ArticleDetailsFragmentPresenter.class.toString();

	private Context context;
	private ArticleDetailContract.FragmentView view;
	private long itemId;
	private int cursorPosition;
	private Cursor allArticleCursor;
	private int currentScroolY = 0;
	private int currentOffset = ArticleDetailFragment.INITIAL_BODY_SIZE + 1;
	private boolean isLoadingScrool = false;

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
	public void onScroolChanged(int mScrollY, int height) {
		boolean callLoad = false;
		if (isLoadingScrool) {
			return;
		}

		if (currentScroolY == 0) { // Carregamento inicial
			callLoad = true;
		} else if (mScrollY > currentScroolY) {
			callLoad = true;
		}

		if (callLoad) {
			Log.i(TAG, "Carregando mais body para scrool.");
			currentScroolY = mScrollY;
			new ArticleBodyLoad(this).execute(String.valueOf(currentOffset));
			isLoadingScrool = true;
		}
	}

	@Override
	public String getCompletedBodyText() {
		if (allArticleCursor != null) {
			if (allArticleCursor.isClosed()) {
				Log.w(TAG, "Cursor já foi fechado.");
				return "";
			}
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
		if (aditionalBody == null || aditionalBody.length() == 0) { // Não tem mais a ser carregado
			return;
		} else {
			currentOffset += aditionalBody.length();
			view.addBodyTextPart(aditionalBody);
		}
		isLoadingScrool = false;
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
