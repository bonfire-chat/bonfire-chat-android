package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

public class SearchUserActivity extends Activity {
    private ContactsAdapter adapter;

    ListView contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        List<Contact> contacts = new ArrayList<Contact>();
        contactsList = (ListView) findViewById(R.id.contactsList);
        adapter = new ContactsAdapter(this, contacts);
        contactsList.setAdapter(adapter);

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = adapter.getItem(position);
                BonfireData db = BonfireData.getInstance(SearchUserActivity.this);
                db.createContact(contact);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_user, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ContactSearch contactSearch = new ContactSearch();
                contactSearch.execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class ContactSearch extends AsyncTask<String, Object, Contact[]> {
        @Override
        protected Contact[] doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();

            String keyword = "%"+params[0]+"%";
	
            try {
                HttpGet httppost = new HttpGet(BonfireData.API_ENDPOINT + "/search.php?nickname=" + URLEncoder.encode(keyword, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                java.util.Scanner s = new java.util.Scanner(response.getEntity().getContent()).useDelimiter("\\A");
                JSONTokener tokener = new JSONTokener(s.next());
                JSONArray array = (JSONArray) tokener.nextValue();
                Contact[] d = new Contact[array.length()];
                for(int i = 0; i < d.length; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    d[i] = new Contact(obj.getString("nickname"), obj.getString("nickname"), "",
                            obj.getString("phone"), obj.getString("publickey"), obj.getString("xmppid"), "", "", 0);
                }
                return d;

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Contact[] contacts) {
            adapter.clear();
            for(Contact contact : contacts) {
                adapter.add(contact);
            }
        }
    }

}
