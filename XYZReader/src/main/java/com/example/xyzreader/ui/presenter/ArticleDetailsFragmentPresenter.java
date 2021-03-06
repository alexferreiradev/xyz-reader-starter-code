package com.example.xyzreader.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.text.Spanned;
import android.util.Log;
import com.example.xyzreader.R;
import com.example.xyzreader.data.loader.ArticleLoader;
import com.example.xyzreader.ui.task.ArticleBodyLoad;

public class ArticleDetailsFragmentPresenter implements ArticleDetailContract.PresenterFragment {
	private static final String TAG = ArticleDetailsFragmentPresenter.class.toString();
	private static final String SCROOL_POS_SAVED_KEY = "scrool pos saved key";

	private Context context;
	private ArticleDetailContract.FragmentView view;
	private long itemId;
	private int cursorPosition;
	private Cursor allArticleCursor;
	private int currentScroolY = 0;
	private int currentOffset = 0;
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
		if (!view.isFragmentAdded()) {
			return;
		}

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
			Log.d(TAG, "Ignorando scrool: " + mScrollY + " devido já estar carregando um");
			return;
		}

		if (currentScroolY == 0) { // Carregamento inicial
			callLoad = true;
		} else if (mScrollY > currentScroolY) {
			callLoad = true;
		} else {
			Log.d(TAG, "Ignorando scrool: " + mScrollY);
		}

		if (callLoad) {
			int incremmentText = context.getResources().getInteger(R.integer.article_body_text_incremment_value);
			currentScroolY = mScrollY;
			new ArticleBodyLoad(this).execute(String.valueOf(currentOffset), String.valueOf(incremmentText));
			isLoadingScrool = true;
			Log.i(TAG, String.format("Carregando mais body para scrool. Offset: %d, increment: %d", currentOffset, incremmentText));
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
	public void onRestoreView(Bundle savedInstanceState) {
		int scroolPosSaved = savedInstanceState.getInt(SCROOL_POS_SAVED_KEY, -1);
		if (scroolPosSaved > 0) {
			view.setScroolPos(scroolPosSaved);
		}
	}

	@Override
	public void saveInstanceState(Bundle outState, int scrollY) {
		if (scrollY > 0) {
			outState.putInt(SCROOL_POS_SAVED_KEY, scrollY);
		}
	}

	@Override
	public void loadedAditionalBody(Spanned aditionalBody) {
		view.setProgressBarVisibility(false);
		isLoadingScrool = false;
		if (aditionalBody != null && aditionalBody.length() != 0) {
			Log.d(TAG, "Texto adicionado: " + aditionalBody.length());
			currentOffset += aditionalBody.length() + 1;
			view.addBodyTextPart(aditionalBody);
		} else { // Não tem mais a ser carregado
			Log.d(TAG, "Não tem mais texto para carregar");
		}
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
