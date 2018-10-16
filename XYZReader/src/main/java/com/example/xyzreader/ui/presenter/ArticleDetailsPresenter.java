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
	private Context context;
	private ArticleDetailContract.View view;
	private long curretItemId;
	private Cursor allArticlesCursor;
	private boolean findSelectedPosition = false;
	private int currentPosition = 0;
	private int selectedPos;

	public ArticleDetailsPresenter(Context context, ArticleDetailContract.View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	public void restoreSavedState(Bundle savedInstanceState) {

	}

	@Override
	public void savePositionState(Bundle outState, int verticalScrollbarPosition) {

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
		findSelectedPosition = selectedPos < 0;
		this.selectedPos = selectedPos;
	}

	@Override
	public boolean onPageChange(int position) {
		if (allArticlesCursor != null) {
			if (selectedPos != position) { // Somente depois de ter iniciado a primeira vez
				boolean moved = allArticlesCursor.moveToPosition(position);
				curretItemId = allArticlesCursor.getLong(ArticleLoader.Query._ID);
				currentPosition = position;
				view.bindView(allArticlesCursor);

				return moved;
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
				int foundPosition = -1;
				if (findSelectedPosition) { // Activity iniciada por outro APP, nao temos a posicao do loader
					cursor.moveToFirst();
					foundPosition = findCurrentItemPosition(cursor);
					if (foundPosition == 0) {
						view.bindView(cursor);
					}
					findSelectedPosition = false;
				} else {
					cursor.moveToPosition(selectedPos);
					view.bindView(cursor);
				}
				view.createPagerAdapter(this.allArticlesCursor);
				if (foundPosition > 0) {
					view.setPagerPos(foundPosition);
				} else {
					view.setPagerPos(selectedPos);
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
