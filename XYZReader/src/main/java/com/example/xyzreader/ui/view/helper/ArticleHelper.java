package com.example.xyzreader.ui.view.helper;

import android.text.Html;
import android.text.Spanned;

public final class ArticleHelper {

	public static Spanned getBodyPartText(String articleBody, int finalPosition) {
		String bodyToStart;
		if (articleBody.length() > finalPosition) {
			bodyToStart = articleBody.substring(0, finalPosition);
		} else {
			bodyToStart = articleBody;
		}

		return Html.fromHtml(bodyToStart.replaceAll("(\r\n|\n)", "<br />"));
	}
}
