package com.example.xyzreader.ui.task;

import android.os.AsyncTask;
import android.text.Spanned;
import com.example.xyzreader.ui.fragment.ArticleDetailFragment;
import com.example.xyzreader.ui.presenter.ArticleDetailContract;
import com.example.xyzreader.ui.view.helper.ArticleHelper;

public class ArticleBodyLoad extends AsyncTask<String, Integer, Spanned> {

	private ArticleDetailContract.PresenterFragment presenter;

	public ArticleBodyLoad(ArticleDetailContract.PresenterFragment presenter) {
		this.presenter = presenter;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		presenter.startLoadBody();
	}

	@Override
	protected Spanned doInBackground(String... strings) {
		String offset = strings[0];

		int intOffset = Integer.parseInt(offset);
		String completeBodyText = presenter.getCompletedBodyText();

		return ArticleHelper.getBodyTextPart(completeBodyText, intOffset, ArticleDetailFragment.TOTAL_TO_ADD_BODY_PART);
	}

	@Override
	protected void onPostExecute(Spanned aditionalBody) {
		presenter.loadedAditionalBody(aditionalBody);
	}
}
