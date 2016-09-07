package com.upc.fib.racopocket.Fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.upc.fib.racopocket.Activities.MainMenuActivity;
import com.upc.fib.racopocket.Models.NotificationModel;
import com.upc.fib.racopocket.R;
import com.upc.fib.racopocket.Utils.Constants;
import com.upc.fib.racopocket.Utils.FileUtils;
import com.upc.fib.racopocket.Utils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class NotificationsMainMenu extends Fragment
{
    ImageButton update;
    ExpandableListView expListViewNotifications;
    ProgressBar progressBar;

    String mySubjects, myNotifications;
    List<String> listDataHeader;

    OAuthConsumer consumer = new DefaultOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainMenuActivity) getActivity()).setActionBarDesign(getResources().getString(R.string.nav_notifications));
        View rootView = inflater.inflate(R.layout.notifications_main_menu, container, false);

        update = (ImageButton) rootView.findViewById(R.id.updateNotifications);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBarNotifications);
        expListViewNotifications = (ExpandableListView) rootView.findViewById(R.id.expListViewNotifications);

        String token = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN");
        String secret = PreferencesUtils.recoverStringPreference(getContext().getApplicationContext(), "OAUTH_TOKEN_SECRET");
        consumer.setTokenWithSecret(token, secret);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        expListViewNotifications.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                //TODO: Implement onClickListener
                return true;
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new GetNotifications().execute(true);
            }
        });

        new GetNotifications().execute(false);
    }

    private class GetNotifications extends AsyncTask<Boolean, Void, Pair<Integer, String>>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Pair<Integer, String> doInBackground(Boolean... params)
        {
            Boolean forceUpdate = params[0];
            int statusCode = 200;

            if (forceUpdate || !FileUtils.fileExists(getContext().getApplicationContext(), "avisos.json"))
                statusCode = FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
            else {
                if (PreferencesUtils.preferenceExists(getContext().getApplicationContext(), "enableAutomaticUpdates"))
                    if (PreferencesUtils.recoverBooleanPreference(getContext().getApplicationContext(), "enableAutomaticUpdates"))
                        statusCode = FileUtils.fetchAndStoreFile(getContext().getApplicationContext(), consumer, "https://raco.fib.upc.edu/api-v1/avisos.json", "avisos.json");
            }

            return new Pair<>(statusCode, FileUtils.readFileToString(getContext().getApplicationContext(), "avisos.json"));
        }

        @Override
        protected void onPostExecute(Pair response)
        {
            if (!response.first.toString().equals("200"))
                Toast.makeText(getContext().getApplicationContext(), getResources().getString(R.string.connection_problems), Toast.LENGTH_SHORT).show();

            myNotifications = response.second.toString();
            mySubjects = FileUtils.readFileToString(getContext().getApplicationContext(), "assignatures.json");
            listDataHeader = new ArrayList<>();
            try {
                JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
                for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                    JSONObject subjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                    listDataHeader.add(subjectJSONObject.getString("idAssig"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // TODO: Implement file download
            HashMap<String, List<NotificationModel>> listDataChild = new HashMap<>();
            try {
                JSONObject subjectsNotificationsJSONObject = new JSONObject(myNotifications);
                for (int i = 0; i < listDataHeader.size(); i++) {
                    // Each subject
                    List<NotificationModel> dataChild = new ArrayList<>();
                    // If subject has notifications we display the subject's title
                    // Otherwise we don't display the subject's title
                    if (subjectsNotificationsJSONObject.has(listDataHeader.get(i))) {
                        JSONArray iSubjectNotificationsJSONArray = subjectsNotificationsJSONObject.getJSONArray(listDataHeader.get(i));
                        for (int j = 0; j < iSubjectNotificationsJSONArray.length(); j++) {
                            // Each subject notifications
                            JSONObject subjectJSONObject = iSubjectNotificationsJSONArray.getJSONObject(j);
                            int idNotification = subjectJSONObject.getInt("id");
                            String title = subjectJSONObject.getString("title");
                            String pubDate = subjectJSONObject.getString("pubDate");
                            JSONArray attachmentsListJSONArray = subjectJSONObject.getJSONArray("attachments");
                            List<Pair<Integer, String>> attachmentsList = new ArrayList<>();
                            for (int k = 0; k < attachmentsListJSONArray.length(); k++) {
                                JSONObject attachmentJSONObject = attachmentsListJSONArray.getJSONObject(k);
                                Integer id = attachmentJSONObject.getInt("id");
                                String filename = attachmentJSONObject.getString("fileName");
                                attachmentsList.add(new Pair<>(id, filename));
                            }
                            String description = subjectJSONObject.getString("description");
                            pubDate = pubDate.substring(5, pubDate.length() - 6);
                            dataChild.add(new NotificationModel(idNotification, title, pubDate, attachmentsList, description));
                        }
                        listDataChild.put(listDataHeader.get(i), dataChild);
                    } else {
                        listDataHeader.remove(i--);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Set and display the Subjects Array
            ExpandableListAdapter expandableListAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);
            expListViewNotifications.setAdapter(expandableListAdapter);

            expListViewNotifications.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private List<String> listDataHeader;
        private HashMap<String, List<NotificationModel>> listDataChild;

        public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<NotificationModel>> listChildData) {
            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listChildData;
        }

        @Override
        public NotificationModel getChild(int groupPosition, int childPosition) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.notifications_item_list, parent, false);
            }

            TextView titleNotifications = (TextView) convertView.findViewById(R.id.titleNotifications);
            TextView dateNotifications = (TextView) convertView.findViewById(R.id.dateNotifications);

            NotificationModel element = getChild(groupPosition, childPosition);
            titleNotifications.setText(element.getTitle());
            dateNotifications.setText(element.getPubDate());

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.notifications_group_list, parent, false);
            }

            TextView lblListHeader = (TextView) convertView.findViewById(R.id.notificationListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}