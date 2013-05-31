package com.gw2.main;


import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.gw2.pojo.Events;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, KeyManagementException {
    	
    	Connection conn = null;
    	List list = null;
    	try {
			Class.forName("org.sqlite.JDBC");
		    conn = DriverManager.getConnection("jdbc:sqlite:db/guildwars2.db");
		    Statement stat = conn.createStatement();
		    PreparedStatement prep = conn.prepareStatement("insert into events values (?, ?, ?, ?);");
		    //123
		    list = getJSONList("https://api.guildwars2.com/v1/events.json?world_id=1001");
		    for(int i=0;i<list.size();i++){
//		    	JsonObject jsonobj = JsonObject.
		    	Events e = (Events)list.get(i);
		    	int world_id = e.getWorld_id();
		    	int map_id = e.getMap_id();
		    	String event_id = e.getEvent_id();
		    	String state = e.getState();
		    	prep.setInt(1, world_id);
		    	prep.setInt(2, map_id);
		    	prep.setString(3, event_id);
		    	prep.setString(4, state);
		    	prep.addBatch();
		    }
		    conn.setAutoCommit(false);
		    prep.executeBatch();
		    conn.setAutoCommit(true);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
    	
    }
    
    public static List getJSONList (String url){
    	HttpClient mHttpClient;
		try {
			mHttpClient = get();
	        HttpGet mGet = new HttpGet(url);
	        HttpResponse mResponse = mHttpClient.execute(mGet);
	//        System.out.println(mResponse.getStatusLine());
	//        System.out.println(mResponse.getEntity());
	        String json = EntityUtils.toString(mResponse.getEntity());
	        Gson gson = new Gson();
	        if(json.indexOf("[") != -1){
	        	json.replace("[", "");
	        }
	        if(json.indexOf("]") != -1){
	        	json.replace("]", "");
	        }
	        System.out.println(json);
	        List<Events> list = gson.fromJson(json, new TypeToken<List<Events>>(){}.getType());
	        return list;
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

    public static HttpClient get() throws NoSuchAlgorithmException, KeyManagementException {

        TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };


        SSLContext mContext = SSLContext.getInstance("TLS");
        mContext.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory sf = new SSLSocketFactory(mContext);

        SchemeRegistry mSchemeRegistry = new SchemeRegistry();
        mSchemeRegistry.register(new Scheme("https", 443, sf));
        return new DefaultHttpClient(new PoolingClientConnectionManager(mSchemeRegistry));

    }

}
