package com.example.xyzreader.ui.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.text.Spanned;
import com.example.xyzreader.data.loader.ArticleLoader;
import com.example.xyzreader.ui.view.BaseView;

public interface ArticleDetailContract {

	interface View extends BaseView, FragmentListener {

		void notifyViewPagerThatDataChanged();

		// TODO: 10/10/18 Verificar necessidade
		void updateUpBt(int position);

		void setPagerPos(int position);

		void startShareView(android.view.View view, Cursor cursor);

		void createPagerAdapter(Cursor cursor);

		void bindView(Cursor cursor);

		void swapCursor(Cursor cursor);
	}

	interface FragmentView {

		boolean isFragmentAdded();

		void bindView(Cursor mCursor);

		void setProgressBarVisibility(boolean visible);

		int getUpButtonFloor();

		void onUpButtonFloorChanged(long itemId);

		void updateStatusBar();

		void addBodyTextPart(Spanned aditionalBody);

		void setScroolPos(int scroolPosSaved);
	}

	interface FragmentListener {
		void shareArticle(android.view.View v);

		void onUpBuutonFloorChanged(long itemId, int upButtonFloor);
	}

	interface PresenterFragment extends LoaderManager.LoaderCallbacks<Cursor>, ArticleLoader.LoaderListeners {

		void onScroolChanged(int mScrollY, int height);

		long getArticleId();

		String getCompletedBodyText();

		void startLoadBody();

		void loadedAditionalBody(Spanned aditionalBody);

		void saveInstanceState(Bundle outState, int scrollY);

		void onRestoreView(Bundle savedInstanceState);
	}

	interface Presenter extends LoaderManager.LoaderCallbacks<Cursor>, ArticleLoader.LoaderListeners {

		void restoreSavedState(Bundle savedInstanceState);

		void saveState(Bundle outState, int verticalScrollbarPosition);

		void setStartId(long mStartId);

		long getArticleIdByCursor();

		void shareArticle(android.view.View view);

		boolean onPageChange(int position);

		void setSelectedPos(int selectedPos);
	}
}
