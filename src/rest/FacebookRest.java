package rest;

import java.util.*;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.scribe.exceptions.OAuthException;

import java.io.*;

import java.net.URLEncoder;
import socnet1.Post;

public class FacebookRest
{
	private static final String NETWORK_NAME = "Facebook";
	private static final Token EMPTY_TOKEN = null;

	public Token accessToken;
	public String authUrl;

	private static String apiKey = "119935244835357";
	private static String apiSecret = "8b6eeb6d25e17eaf31d1f9e61bca493d";
	private static final String USER_INFO_URL = "https://graph.facebook.com/me";
	public OAuthService service;
	private String userID;
	private String userName;
	private Long start_time;
	private Boolean initedService;

	public FacebookRest()
	{
		service = new ServiceBuilder()
		.provider(FacebookApi.class)
		.apiKey(FacebookRest.apiKey)
		.apiSecret(FacebookRest.apiSecret)
		.callback("http://eden.dei.uc.pt/~amaf/echo.php") // Do not change this.
		.scope("publish_stream,read_stream")
		.build();

		//initedService = false;
		authUrl = service.getAuthorizationUrl(FacebookRest.EMPTY_TOKEN);
	}

	public String getAuthUrl(){
		return authUrl;
	}

	public void initService(String authCode){
		Verifier verifier = new Verifier(authCode);
		this.accessToken = service.getAccessToken(FacebookRest.EMPTY_TOKEN, verifier);
		this.start_time = convToFBTime( (new Date()).getTime() );
		System.out.println("start_time = "+start_time);

		// get user info
		JSONObject userInfo = getUserInfo();
		this.userID = (String) userInfo.get("id");
		//initedService = true;
		this.userName = (String) userInfo.get("name");

	}

	public JSONObject getUserInfo(){
		OAuthRequest request = new OAuthRequest(Verb.GET, USER_INFO_URL);
		service.signRequest(accessToken, request);		
    	Response response = request.send();

    	if(response.getCode()==200){
			JSONParser parser = new JSONParser();
			try{
				Object obj = parser.parse(response.getBody());
				return (JSONObject) obj;
			}catch(ParseException e){
				System.out.println(e);
			}		
		}
		return null;
	}

	public Boolean getInitedService(){
		return initedService;
	}


	//// REST
	public String addPost(String post) {
		try{
			post = URLEncoder.encode(post, "UTF-8");
		}
		catch(java.io.UnsupportedEncodingException e){System.out.println(e);}
		// returns id of new post
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/stream.publish?message="+post+"&format=json");
		service.signRequest(accessToken, request);
		Response response = request.send();

		if (response.getCode()==200){
			String id = response.getBody();
			id=id.substring(1,id.length()-1);
			return id;
		}

		System.out.println("Error inserting post on facebook: "+response.getCode());
		return null;
	}

	public boolean removePost(String id_post){;
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/stream.remove?post_id="+id_post+"&format=json");
		service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println("Got it! Lets see what we found...");

		if(response.getCode()==200){
			if((response.getBody()).equals("true")){
				return true;
			}
		}
		System.out.println(response.getBody());      

		return false;
	}

	public String addComment(String id_post, String content){
		System.out.println("Adding comment to Facebook");
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/stream.addComment?post_id="+id_post+"&comment="+content+"&format=json&uid="+this.userID);
		service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println("Got it! Lets see what we found...");

		if(response.getCode()==200){
			String id = response.getBody();
			id=id.substring(1,id.length()-1);
			return id;
		}

		System.out.println("Error inserting reply on facebook: "+response.getCode());
		return null;
	}	

	public ArrayList<Post> getPosts(){
		Long start_time_ = start_time - 60;   // defer date to an older date (assure facebook server clocks get new posts)
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/stream.get?filter_key=nf&format=json&source_ids="+this.userID+"&start_time="+start_time_);
		// OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/stream.getFilters");
		service.signRequest(accessToken, request);
		Response response = request.send();

		if(response.getCode()==200){
			JSONParser parser = new JSONParser();
			ArrayList<Post> posts = new ArrayList<Post>();
			try{
				//System.out.println( response.getBody() );
				Object obj = parser.parse(response.getBody());
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray arr = (JSONArray) jsonObject.get("posts");

				for(int i=0; i< arr.size(); i++) {
					JSONObject item = (JSONObject) arr.get(i);

					String text = (String) item.get("message");
					String idFace = (String) item.get("post_id");
					String wallID = this.userID;
					Long date = (Long) item.get("created_time");
					String source = this.userName;

					System.out.println(i+": - '"+text+"' on "+date);

					if( start_time < date )
						posts.add( new Post(source, text, convFBTimeToDate(date), idFace, wallID) );
				}

				if(posts.size()>0)
					start_time = convToFBTime(posts.get(0).getSentDate().getTime());

				System.out.println("start_time = "+start_time);

				return posts;
			}catch(ParseException e){
				System.out.println(e);
			}
		}

		return null;
	}

	public void setStartTime(Long time){
		this.start_time = convToFBTime(time);
		System.out.println("####\nSET start_time = "+start_time+"\n####");
	}

	private Long convToFBTime(Long date){
		date = date / 1000 + ((date % 1000 >= 500)?1:0);  // from milliseconds to seconds
		return date;
	}
	private Date convFBTimeToDate(long date){
		Date d = new Date();
		d.setTime(date * 1000 );
		return d;
	}

}