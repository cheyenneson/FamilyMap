package hawkes.fmc.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeMap;

import hawkes.fmc.R;
import hawkes.fmc.model.Filter;
import hawkes.fmc.model.Model;

public class FilterActivity extends AppCompatActivity {

    RecyclerView mEventsRecyclerView;
    LinearLayoutManager mEventsLinearLayoutManager;
    ItemAdapter mEventsTestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Get Recycler View by id from layout file
        mEventsRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_filters);

        // Create Linear Layout Manager which defines how it will be shown on the screen
        mEventsLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mEventsLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Set Layout Manager in the RecyclerView
        mEventsRecyclerView.setLayoutManager(mEventsLinearLayoutManager);

        // Create Adapter object from the data by calling default Constructor
        mEventsTestAdapter = new ItemAdapter(getFilters());

        // Set RecyclerView Adapter
        mEventsRecyclerView.setAdapter(mEventsTestAdapter);

    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ListItemViewHolder> {

        private ArrayList<Filter> filtersList;

        public ItemAdapter(ArrayList<Filter> filtersList) {
            this.filtersList = filtersList;
        }

        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View itemView = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.filter_recyclerview_viewholder, parent, false);

            return new ListItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {
            Filter filter = filtersList.get(position);
            String topText = filter.getFilterType();
            holder.topText.setText(topText);

            holder.bottomText.setText("Filter by " + filter.getFilterType());

            boolean checkedState = Model.getModel().getFilters().get(filter.getFilterType()).isOn();
            holder.filterSwitch.setChecked(checkedState);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return filtersList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            //public IconTextView mIcon;
            public TextView topText;
            public TextView bottomText;
            public Switch filterSwitch;

            public ListItemViewHolder(View itemView) {
                super(itemView);

                //mIcon = (ImageView) itemView.findViewById(R.id.child_list_item_icon);
                topText = (TextView) itemView.findViewById(R.id.filterTitle);
                bottomText = (TextView) itemView.findViewById(R.id.filterSubtitle);

                filterSwitch = (Switch) itemView.findViewById(R.id.filterSwitch);

                //itemView.setOnClickListener(this);
                filterSwitch.setOnCheckedChangeListener(this);
            }

            /**
             * Called when the checked state of a compound button has changed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Filter clickedFilter = filtersList.get(getAdapterPosition());
                Model model = Model.getModel();
                model.getFilters().get(clickedFilter.getFilterType()).setOn(isChecked);
            }
        }
    }

    private ArrayList<Filter> getFilters() {

        Model model = Model.getModel();

        ArrayList<Filter> filtersInOrder = new ArrayList<>();
        for (Filter f : model.getFilters().values()) {
            filtersInOrder.add(f);
        }
        return filtersInOrder;
    }
}
