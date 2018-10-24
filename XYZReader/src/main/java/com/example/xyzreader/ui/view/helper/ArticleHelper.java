package com.example.xyzreader.ui.view.helper;

import android.text.Html;
import android.text.Spanned;

public final class ArticleHelper {

	public static Spanned getBodyPartText(String articleBody, int offset, int incremment) {
		String bodyToStart;

		int textSize = articleBody.length();
		int processedOffset = offset + 1;
		int processedfinalPos = processedOffset + incremment;
		if (processedfinalPos > textSize) {
			processedfinalPos = textSize;
		}
		if (processedOffset > textSize) {
			processedOffset = textSize;
		}

		bodyToStart = articleBody.substring(processedOffset, processedfinalPos);

		return Html.fromHtml(bodyToStart.replaceAll("(\r\n|\n)", "<br />"));
	}
}
