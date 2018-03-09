package es.guadaltech.odoo.misc;

import org.json.JSONException;

import android.app.ListActivity;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public abstract class GenericList extends ListActivity {

	protected Integer mFirstVisibleItem = -1;
	protected Integer pagination = 0;

	protected abstract void getServerItems() throws JSONException;

	protected abstract void getOfflineItems(Integer pagination);

	public class EndlessScrollListener implements OnScrollListener {

		private Integer visibleThreshold = 4;
		private Integer previousTotal = 0;
		private boolean loading = true;

		public EndlessScrollListener() {
		}

		public EndlessScrollListener(Integer visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			// Si no se están mostrando más de 30 items, no habrá nada que
			// recargar dinámicamente
			if (totalItemCount < 30)
				return;

			if (loading) {
				if (totalItemCount > previousTotal) {
					loading = false;
					previousTotal = totalItemCount;
				}
			}
			if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				getOfflineItems(pagination++);
				loading = true;
			}

			// if (firstVisibleItem > mFirstVisibleItem) {
			// toggleBottomVisibility(false);
			// } else if (firstVisibleItem < mFirstVisibleItem) {
			// toggleBottomVisibility(true);
			// }
			mFirstVisibleItem = firstVisibleItem;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}
	}
	


}
