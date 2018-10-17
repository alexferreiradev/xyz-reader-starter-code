package com.example.xyzreader.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.xyzreader.R;
import com.example.xyzreader.data.loader.ArticleLoader;
import com.example.xyzreader.ui.presenter.ArticleDetailContract;
import com.example.xyzreader.ui.presenter.ArticleDetailsFragmentPresenter;
import com.example.xyzreader.ui.view.ArticleDetailActivity;
import com.example.xyzreader.ui.view.ArticleListActivity;
import com.example.xyzreader.ui.view.helper.ArticleHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
		ArticleDetailContract.FragmentView {
	public static final String ARG_ITEM_ID = "item_id";
	public static final String ARG_CURSOR_POS = "cursor_pos";
	public static final int TOTAL_TO_ADD_BODY_PART = 500;
	public static final int DEAD_LINE = 7;
	private static final String TAG = "ArticleDetailFragment";
	View rootView;
	@BindView(R.id.pb_details_fragment)
	ProgressBar progressBar;
	@BindView(R.id.tv_article_date)
	TextView dateTV;
	@BindView(R.id.tv_article_author)
	TextView authorTV;
	@BindView(R.id.tv_article_body)
	TextView bodyTV;
	@BindView(R.id.sv_body)
	NestedScrollView articleSV;
	private ArticleDetailContract.PresenterFragment presenter;
	private ArticleDetailContract.FragmentListener listener;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());
	private SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());
	private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);// Most time functions can only handle 1902 - 2037

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ArticleDetailFragment() {
	}

	public static ArticleDetailFragment newInstance(long itemId, int position) {
		Bundle arguments = new Bundle();
		arguments.putLong(ARG_ITEM_ID, itemId);
		arguments.putInt(ARG_CURSOR_POS, position);
		ArticleDetailFragment fragment = new ArticleDetailFragment();
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID) && getArguments().containsKey(ARG_CURSOR_POS)) {
			long mItemId = getArguments().getLong(ARG_ITEM_ID);
			int cursorPosition = getArguments().getInt(ARG_CURSOR_POS);
			Log.d(TAG, "Criando fragment: " + mItemId);
			presenter = new ArticleDetailsFragmentPresenter(requireContext(), this, mItemId, cursorPosition);
		} else {
			throw new IllegalStateException("Não foi passado itemId ou posicao de cursor");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentActivity activity = getActivity();
		try {
			listener = (ArticleDetailContract.FragmentListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, "Activity que utilizar fragment deve implementar listener: " + e.getMessage());
			throw new IllegalStateException("Activity que utilizar fragment deve implementar listener", e);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "Iniciando loader no frag: ");
		// noinspection deprecation
		requireActivity().getSupportLoaderManager().initLoader(ArticleDetailActivity.ALL_ARTICLES_LOADER_ID, null, presenter);
	}

	@Override
	public void onUpButtonFloorChanged(long itemId) {
		listener.onUpBuutonFloorChanged(itemId, getUpButtonFloor());
	}

	@Override
	public int getUpButtonFloor() {
		return 0;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
		ButterKnife.bind(this, rootView);
		initView();
		Log.d(TAG, "Criando view para fragment com id: " + presenter.getArticleId());

		return rootView;
	}

	@Override
	public void updateStatusBar() {
	}

	@Override
	public void addBodyTextPart(Spanned aditionalBody) {
		bodyTV.append(aditionalBody);
	}

	private Date parsePublishedDate(Cursor cursor) {
		try {
			String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
			return dateFormat.parse(date);
		} catch (ParseException ex) {
			Log.e(TAG, ex.getMessage());
			Log.i(TAG, "passing today's date");

			return new Date();
		}
	}

	@Override
	public boolean isFragmentAdded() {
		return isAdded();
	}

	@Override
	public void bindView(Cursor cursor) {
		Log.d(TAG, "Iniciando bind de id: " + presenter.getArticleId());
		setPublishedDate(cursor);
		setAuthorName(cursor);
		setInitialBodyText();
		articleSV.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				int scrollY = articleSV.getScrollY();
				int height = articleSV.getHeight(); // ok, mas impreciso
				int maxScrollAmount = articleSV.getMaxScrollAmount(); // ok
				int scroolPos = height - scrollY;
				int lastVisiblePos = (maxScrollAmount / scroolPos) * 10;
				if (scrollY > 0) {
					if (lastVisiblePos > DEAD_LINE) {
						System.out.println("Novo body");
						System.out.printf("Ultima pos: %d\ny: %d\nheight: %d\nmaxSc: %d\n", lastVisiblePos, scrollY, height, maxScrollAmount);
					}
				}
			}
		});

		Log.d(TAG, "Fim bind de id: " + presenter.getArticleId());
	}

	private void setInitialBodyText() {
		final String articleBody = presenter.getCompletedBodyText();
		Spanned bodyInHtml = ArticleHelper.getBodyPartText(articleBody, TOTAL_TO_ADD_BODY_PART);
		bodyTV.setText(bodyInHtml);
	}

	private void setAuthorName(Cursor cursor) {
		String authorName = cursor.getString(ArticleLoader.Query.AUTHOR);
		authorTV.setText(String.format("by %s", authorName));
	}

	private void setPublishedDate(Cursor cursor) {
		Date publishedDate = parsePublishedDate(cursor);
		if (!publishedDate.before(START_OF_EPOCH.getTime())) {
			dateTV.setText(DateUtils.getRelativeTimeSpanString(
					publishedDate.getTime(),
					System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_ALL).toString());

		} else { // If date is before 1902, just show the string
			dateTV.setText(outputFormat.format(publishedDate));
		}
	}

	private void initView() {
		Log.d(TAG, "Iniciando bind null");
		setProgressBarVisibility(true);
		dateTV.setText(getString(R.string.loading_info));
		authorTV.setText(getString(R.string.loading_info));
		bodyTV.setText(getString(R.string.loading_info));
		bodyTV.setMovementMethod(new LinkMovementMethod());
		bodyTV.setTypeface(Typeface.SANS_SERIF);
	}

	@Override
	public void setProgressBarVisibility(boolean visible) {
		if (visible) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
		}
	}

}
