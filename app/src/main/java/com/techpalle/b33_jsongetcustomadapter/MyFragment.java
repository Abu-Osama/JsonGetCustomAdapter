package com.techpalle.b33_jsongetcustomadapter;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    //STEP 10 : DECLARE ALL VARIABLES
    Button b;
    ListView lv;
    ArrayList<Contacts> al;
    MyAdapter ma;
    MyTask m;

    //STEP 9 : CREATE 2 INNER CLASSES
    //1 FOR ASYNC TASK
    public class MyTask extends AsyncTask<String, Void, String>{
        //declare all variables
        URL myurl;
        HttpURLConnection connection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        //13. implement do in background, connect to server, get JSON and return
        @Override
        protected String doInBackground(String... p1) {
            try {
                myurl = new URL(p1[0]);
                connection = (HttpURLConnection) myurl.openConnection();
                inputStream = connection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                result = new StringBuilder();
                line = bufferedReader.readLine();
                while(line != null){
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("B33", "Message .."+e.getMessage());
                Log.d("B33", "Cause.."+e.getCause());
                e.printStackTrace(); //prints complete info about error.
            } finally {
                //clean impo resources - eg: closing all network connections
                if(connection != null){
                    connection.disconnect();
                    if(inputStream != null){
                        try {
                            inputStream.close();
                            if (inputStreamReader != null){
                                inputStreamReader.close();
                                if(bufferedReader != null){
                                    bufferedReader.close();
                                }
                            }
                        } catch (IOException e) {
                            Log.d("B33","PROBLEM IN CLOSING CONNECTION");
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        //STEP 14 : ONPOST EXECUTE FOR PARSING JSON
        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                Toast.makeText(getActivity(), "NETWORK ISSUE, FIX",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //START JSON PARSING
            try {
                JSONObject j = new JSONObject(s);
                JSONArray arr = j.getJSONArray("contacts");
                for (int i=0; i<arr.length(); i++){
                    JSONObject temp = arr.getJSONObject(i);
                    String name = temp.getString("name");
                    String email = temp.getString("email");
                    JSONObject phone = temp.getJSONObject("phone");
                    String mobile = phone.getString("mobile");
                    //with this we got one object
                    Contacts c = new Contacts();
                    c.setCno(""+(i+1));
                    c.setCname(name);
                    c.setCemail(email);
                    c.setCmobile(mobile);
                    //PUSH TO ARRAY LIST
                    al.add(c);
                    //NOTIFY ADAPTER
                    ma.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Log.d("B33","JSON EXCEPTION.."+e.getMessage());
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }

    }
    //1 FOR CUSTOM ADAPTER
    public class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return al.size();
        }
        @Override
        public Object getItem(int i) {
            return al.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //a. based on pos read contacts object from arraylist
            Contacts c = al.get(i);
            //b. load row.xml and all other views
            View v = getActivity().getLayoutInflater().inflate(R.layout.row, null);
            TextView tv1 = (TextView) v.findViewById(R.id.textView1);
            TextView tv2 = (TextView) v.findViewById(R.id.textView2);
            TextView tv3 = (TextView) v.findViewById(R.id.textView3);
            TextView tv4 = (TextView) v.findViewById(R.id.textView4);
            //c. fill data onto text views - USING GETTERS
            tv1.setText(c.getCno());
            tv2.setText(c.getCname());
            tv3.setText(c.getCemail());
            tv4.setText(c.getCmobile());
            //d. return row.xml that is view v
            return v;
        }
    }

    public MyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //STEP 11 : INITIALIZE VARIABLES AND BUTOTN CLICK LISTENER
        View v = inflater.inflate(R.layout.fragment_my, container, false);
        b = (Button) v.findViewById(R.id.button1);
        lv = (ListView) v.findViewById(R.id.listView1);
        al = new ArrayList<Contacts>();
        ma = new MyAdapter();
        m = new MyTask();
        lv.setAdapter(ma);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInternet() == true){
                    m.execute("http://api.androidhive.info/contacts/");
                }else{
                    //DISPLAY A DIALOG SAYING "NO INTERNET, PLZ CHECK"
                    Toast.makeText(getActivity(),
                            "NO INTERNET", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }

    //CHECK INTERNET METHOD
    public boolean checkInternet(){
        ConnectivityManager conn = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conn != null){
            NetworkInfo info = conn.getActiveNetworkInfo();
            if(info != null && info.isConnected()){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
