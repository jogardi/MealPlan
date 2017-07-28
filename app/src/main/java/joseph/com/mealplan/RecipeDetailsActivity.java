package joseph.com.mealplan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import joseph.com.mealplan.model.Recipe;

public class RecipeDetailsActivity extends AppCompatActivity {


    private String TAG = getClass().getName();

    private RecipeClient client;
    Recipe recipe;

    TextView tvRecipeName;
    ScaleImageView ivRecipeImage;
    TextView tvRecipeDirections;
    ImageButton favorites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        tvRecipeName = (TextView) findViewById(R.id.tvRecipeName);
        ivRecipeImage = (ScaleImageView) findViewById(R.id.ivRecipeImage);
        tvRecipeDirections = (TextView) findViewById(R.id.tvRecipeDirections);
        favorites = (ImageButton) findViewById(R.id.btFavorite);

        tvRecipeDirections.setMovementMethod(new ScrollingMovementMethod());

        // unwrap recipe passed in via intent
        recipe = Parcels.unwrap(getIntent().getParcelableExtra("recipe"));
        if (recipe.isFavorite()) {
            favorites.setVisibility(View.INVISIBLE);
        }
        tvRecipeName.setText(recipe.getTitle());
        final Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/DINAlternate-Bold.ttf");
        tvRecipeName.setTypeface(typeface);

        Picasso.with(this)
               .load(recipe.getImageUrl())
               .fit().centerCrop()
               .into(ivRecipeImage);

        ivRecipeImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(recipe.getSourceUrl()));
                startActivity(intent);
            }
        });

        client = new RecipeClient();

        client.getRecipe(recipe.getId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG, response.toString());
                recipe.addIngredients(response);
                String ingredientsList = "";
                for (int i = 0; i < recipe.getIngredients().size(); i++) {
                    ingredientsList += recipe.getIngredients().get(i).getName();
                    ingredientsList += "\n";
                }
                if (ingredientsList.isEmpty()) {
                    tvRecipeDirections.setText("No Ingredients to Show.");
                }

                else {
                    tvRecipeDirections.setText(ingredientsList);
                }

                tvRecipeDirections.setTypeface(typeface);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void goToUrl(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(recipe.getSourceUrl()));
        startActivity(intent);
    }

    public void addFavorites(View view) {
        Intent intent = new Intent("favorite");
        intent.putExtra("recipe", Parcels.wrap(Recipe.class, recipe));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        favorites.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(), "Recipe added to favorites", Toast.LENGTH_SHORT).show();
    }

    public void addMealPlan(View view) {
        final Intent intent = new Intent("plan");
        intent.putExtra("recipe", Parcels.wrap(Recipe.class, recipe));
        final Context context = view.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Which day would you like to duplicate this recipe?");
        builder.setItems(new CharSequence[]
                        {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Back"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                intent.putExtra("day", "Sunday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 1:
                                intent.putExtra("day", "Monday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 2:
                                intent.putExtra("day", "Tuesday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 3:
                                intent.putExtra("day", "Wednesday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 4:
                                intent.putExtra("day", "Thursday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 5:
                                intent.putExtra("day", "Friday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 6:
                                intent.putExtra("day", "Saturday");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                finish();
                                break;
                            case 7:
                                return;
                        }
                    }
                });
        builder.create().show();
    }
}
