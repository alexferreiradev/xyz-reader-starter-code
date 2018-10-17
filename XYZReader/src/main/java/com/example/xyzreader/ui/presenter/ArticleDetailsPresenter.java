package com.example.xyzreader.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.View;
import com.example.xyzreader.data.loader.ArticleLoader;
import com.example.xyzreader.ui.view.ArticleDetailActivity;

public class ArticleDetailsPresenter implements ArticleDetailContract.Presenter {
	public static final int INVALID_POSITION = -1;
	private static final String ARTICLE_POS_SAVED_KEY = "articlePosSavedKey";

	private Context context;
	private ArticleDetailContract.View view;
	private Cursor allArticlesCursor;
	private long curretItemId;
	private boolean activityStartedFromOtherApp = false;
	private int currentPosition = INVALID_POSITION;
	private int selectedPos = INVALID_POSITION;

	public ArticleDetailsPresenter(Context context, ArticleDetailContract.View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	public void restoreSavedState(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey(ARTICLE_POS_SAVED_KEY)) {
			currentPosition = savedInstanceState.getInt(ARTICLE_POS_SAVED_KEY);
		}
	}

	@Override
	public void saveState(Bundle outState, int verticalScrollbarPosition) {
		outState.putInt(ARTICLE_POS_SAVED_KEY, currentPosition);
	}

	@Override
	public void shareArticle(View view) {
		if (allArticlesCursor == null) {
			this.view.showErrorMsg("Não pode ser compartilhado um artigo ainda não carregado");
			this.view.showErrorMsg("Espere carregar o artigo para tentar compartilhar");
		} else {
			allArticlesCursor.moveToPosition(currentPosition);
			this.view.startShareView(view, allArticlesCursor);
		}
	}

	@Override
	public void setSelectedPos(int selectedPos) {
		activityStartedFromOtherApp = selectedPos == INVALID_POSITION;
		this.selectedPos = selectedPos;
	}

	@Override
	public boolean onPageChange(int position) {
		if (allArticlesCursor != null) {
			if (position != currentPosition) { // Somente quando faz troca de posicao
				boolean moved = allArticlesCursor.moveToPosition(position);
				curretItemId = allArticlesCursor.getLong(ArticleLoader.Query._ID);
				currentPosition = position;
				view.bindView(allArticlesCursor);

				return moved;
			} else {
				return true;
			}
		}

		return false;
	}

	@Override
	public long getArticleIdByCursor() {
		allArticlesCursor.moveToPosition(currentPosition);
		return allArticlesCursor.getLong(ArticleLoader.Query._ID);
	}

	@Override
	public void setStartId(long mStartId) {
		this.curretItemId = mStartId;
	}

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
		return ArticleLoader.newAllArticlesInstance(context, this);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
		view.setProgressBarVisibity(false);
		switch (loader.getId()) {
			case ArticleDetailActivity.ALL_ARTICLES_LOADER_ID:
				this.allArticlesCursor = cursor;
				if (currentPosition == INVALID_POSITION) { // Iniciando Activity de item selecionado
					currentPosition = selectedPos;
				} else if (activityStartedFromOtherApp) {
					cursor.moveToFirst();
					currentPosition = findCurrentItemPosition(cursor);
				}

				if (currentPosition != INVALID_POSITION) {
					cursor.moveToPosition(currentPosition);
					view.bindView(cursor);
				}

				view.createPagerAdapter(this.allArticlesCursor);

				if (currentPosition != INVALID_POSITION) {
					view.setPagerPos(currentPosition);
				}

				break;
		}
	}

	private int findCurrentItemPosition(Cursor cursor) {
		while (!cursor.isAfterLast()) {
			if (cursor.getLong(ArticleLoader.Query._ID) == curretItemId) {
				return cursor.getPosition();
			}
			cursor.moveToNext();
		}

		return -1;
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		view.swapCursor(null);
		view.notifyViewPagerThatDataChanged();
		allArticlesCursor = null;
	}

	@Override
	public void onStartLoad() {
		view.setProgressBarVisibity(true);
	}

	@Override
	public void onFinishLoad() {
		view.setProgressBarVisibity(false);
	}
}
