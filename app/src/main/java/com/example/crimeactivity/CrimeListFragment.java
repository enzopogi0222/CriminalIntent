package com.example.crimeactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int updatedCrimePosition = -1;
    private static final int REQUEST_CRIME = 1;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CRIME) {
            if (data == null) {
                return;
            }

            UUID crimeId = (UUID) data.getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
            updatedCrimePosition = -1;

            CrimeLab crimeLab = CrimeLab.get(getActivity());
            List<Crime> crimes = crimeLab.getCrimes();
            for (int i = 0; i < crimes.size(); i++) {
                if (crimes.get(i).getId().equals(crimeId)) {
                    updatedCrimePosition = i;
                    break;
                }
            }
        }
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            if (updatedCrimePosition != -1) {
                mAdapter.notifyItemChanged(updatedCrimePosition);
                updatedCrimePosition = -1; // Reset the position
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class NormalCrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public NormalCrimeHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }
        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            String formattedDate = DateFormat.format("EEEE, MMM dd, yyyy", mCrime.getDate()).toString();
            mDateTextView.setText(formattedDate);
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View view){
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivityForResult(intent, REQUEST_CRIME);
        }

    }

    private class SeriousCrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Button mContactPoliceButton;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public SeriousCrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mContactPoliceButton = itemView.findViewById(R.id.contact_police_button);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
            mContactPoliceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactPolice();
                }
            });
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            String formattedDate = DateFormat.format("EEEE, MMM dd, yyyy", mCrime.getDate()).toString();
            mDateTextView.setText(formattedDate);
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.INVISIBLE);
            if (crime.isSolved()){
                mContactPoliceButton.setVisibility(View.GONE);
            } else {
                mContactPoliceButton.setVisibility(View.VISIBLE);
            }
        }

        private void contactPolice() {
            mCrime.setSolved(true);
            CrimeLab.get(getActivity()).updateCrime(mCrime);
            Toast.makeText(getActivity(), "Calling Police!", Toast.LENGTH_SHORT).show();
            mContactPoliceButton.setVisibility(View.GONE);
            mSolvedImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivityForResult(intent, REQUEST_CRIME);
        }
    }


    private class CrimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Crime> mCrimes;
        private static final int NORMAL_ROW = 0;
        private static final int SERIOUS_CRIME_ROW = 1;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == SERIOUS_CRIME_ROW) {
                View view = layoutInflater.inflate(R.layout.list_item_serious_crime, parent, false);
                return new SeriousCrimeHolder(view);
            } else {
                View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
                return new NormalCrimeHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Crime crime = mCrimes.get(position);

            if (holder.getItemViewType() == SERIOUS_CRIME_ROW){
                ((SeriousCrimeHolder) holder).bind(crime);
            } else{
                ((NormalCrimeHolder) holder).bind(crime);
            }
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public int getItemViewType(int position){
            Crime crime = mCrimes.get(position);
            if (crime.isRequiresPolice()){
                return SERIOUS_CRIME_ROW;
            } else {
                return NORMAL_ROW;
            }
        }
    }
}
