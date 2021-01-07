package edu.skku.jaehyeonpark.popuptest;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class BookshelfFragment extends Fragment {
    private ArrayList<String> fileNames = new ArrayList<String>();
    TextFileHandler textFileHandler;
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        readFileNames();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookshelf, container, false);
        getActivity().setTitle("E-book 리더");
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ListView listView = view.findViewById(R.id.bookList);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, fileNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = fileNames.get(position);
                Intent intent = new Intent(view.getContext(), ReaderActivity.class);
                intent.putExtra("fileName", fileName);
                startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookshelf_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_add_book);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                textFileHandler = new TextFileHandler(getActivity());
                textFileHandler.callFileOpenAcivity(BookshelfFragment.this);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        textFileHandler.onActivityResult(data.getData());
        readFileNames();
        adapter.notifyDataSetChanged();
    }

    private void readFileNames(){
        File dir = getActivity().getFilesDir();
        String [] temp = dir.list();
        fileNames.clear();
        for(String i : temp){
            if(i.equals("instant-run") || i.equals("log.txt")){continue;}
            fileNames.add(i);
        }
        //fileNames.remove(0);
    }
}
