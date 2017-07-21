package joseph.com.mealplan;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import joseph.com.mealplan.model.Grocery;

public class GroceryListFragment extends Fragment {
    List<HashMap<String, String>> listItems;
    // Use Map as the type
    HashMap<String, String> resultsMap = new HashMap<>();
    SimpleAdapter adapter;
    // You don't need Hashtable unless you are using threads
    Hashtable<String, Integer> valid = new Hashtable<String, Integer>();
    MainActivity mainActivity;

    @BindView(R.id.lvGrocery)
    ListView resultsListView;
    @BindView(R.id.txAdd)
    EditText txAdd;
    @BindView(R.id.btAdd)
    Button btAdd;

    private Realm realm = Realm.getDefaultInstance();

    public static GroceryListFragment newInstance(MainActivity mainActivity) {
        GroceryListFragment fragment = new GroceryListFragment();
        fragment.mainActivity = mainActivity;
        return fragment;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_list, container, false);
        ButterKnife.bind(this, view);
        super.onCreate(savedInstanceState);

        //Creates a hash table of valid grocery items
        // Use Arrays.listOf and then an iterator
        String[] nameArray = {"Ham", "Cheese", "Pineapple", "Milk", "Bread", "Kiwi", "Butter", "Rice", "Pasta",
                "Tomato", "Steak", "French fries", "Avocado", "Cookies", "Cake", "Water", "Onions", "Carrots",
                "Garlic", "Spinach", "Ramen", "Chicken", "Cheesecake"};
        int[] number = {5, 1, 3, 1, 2, 3, 1, 4, 4, 3, 5, 1, 3, 2, 2, 1, 3, 3, 3, 3, 4, 5, 2};
        for(int i = 0; i != 23; i++){
            valid.put(nameArray[i], number[i]);
        }

        //Sets up adapter to allow for Aisle #: grocery layout
        listItems = new ArrayList<>();


        adapter = new SimpleAdapter(getContext(), listItems, R.layout.item_grocery,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.txAisle, R.id.txGroc});
        resultsListView.setAdapter(adapter);
        setupListViewListener();

        for (Grocery grocery : realm.where(Grocery.class).findAll()) {
            showItem(grocery.getName());
        }

        return view;
    }



    private void setupListViewListener(){
        // Use TAG. This is not MainActivity
        Log.i("MainActivity", "Setting Up List View");
        resultsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            //If list entry is long clicked, delete entry
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("MainActivity", "Item Removed:" + i);
                resultsMap = listItems.get(i);
                listItems.remove(i);
                adapter.notifyDataSetChanged();
                View.OnClickListener undoDelete = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { //Re-adds the deleted entry
                        listItems.add(0, resultsMap);
                        adapter.notifyDataSetChanged();
                    }
                };
                //Displays snackbar, which allows for undoing the delete
                Snackbar.make(resultsListView, "Removed Aisle", Snackbar.LENGTH_LONG)
                        .setAction("Undo", undoDelete)
                        .show();
                return true;
            }
        });

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemText = txAdd.getText().toString();
                String capitalized = itemText.substring(0, 1).toUpperCase() + itemText.substring(1).toLowerCase();
                if (valid.containsKey(capitalized)) {
                    saveItem(capitalized);
                    showItem(capitalized);
                } else {
                    Toast.makeText(getContext(), "Not a valid grocery item.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveItem(final String itemText) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insert(new Grocery(itemText));
            }
        });
    }


    public void showItem(String itemText) {
        // Use Map and the IDE says that initializing this is redundant.
        HashMap<String, String> resultsMap = new HashMap<>();

        // Never use try catch for normal control flow. That is like using GOTO
        // This is what if statements are for
        try{ //Case #1: The aisle # for the provided input has already been created, so update the values under
            // Use an iterator.
            for(int i = 0; i != listItems.size() + 1; i++){
                resultsMap = listItems.get(i);
                String aisle = resultsMap.get("First Line");
                if(aisle.equals("Aisle #" + valid.get(itemText))){
                    String old = resultsMap.get("Second Line");
                    resultsMap.put("Second Line", old + "-" + itemText+ "\n");
                    adapter.notifyDataSetChanged();
                    txAdd.setText("");
                    break;
                }
            }
        }

        catch(IndexOutOfBoundsException e){ //Case #2: The aisle # for the input provided doesn't exist yet, so create it
            resultsMap = new HashMap<>();
            resultsMap.put("First Line", "Aisle #" + valid.get(itemText));
            resultsMap.put("Second Line", "-" + itemText + "\n");
            listItems.add(resultsMap);
            adapter.notifyDataSetChanged();
            txAdd.setText("");
        }
    }
}