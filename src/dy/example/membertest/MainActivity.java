package dy.example.membertest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	protected final static int REQ_PICK_FROM_ALBUM = 1997;
	
	EditText et_id;
	EditText et_pwd;
	
	Button btn_select;
	Button btn_join;
	
	ImageView iv_image;
	
	String filepath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		et_id = (EditText)findViewById(R.id.et_id);
		et_pwd = (EditText)findViewById(R.id.et_pwd);
		
		btn_select = (Button)findViewById(R.id.btn_select);
		btn_join = (Button)findViewById(R.id.btn_join);
		
		iv_image = (ImageView)findViewById(R.id.iv_image);
		
		btn_select.setOnClickListener(btnClickListener);
		btn_join.setOnClickListener(btnClickListener);
	}
		
	Button.OnClickListener btnClickListener = new View.OnClickListener() {
			
		@Override
		public void onClick(View v) {
		// TODO Auto-generated method stub
			
			switch (v.getId()) {
			case R.id.btn_select:
				// Activity Action: Pick an item from the data, returning what was selected.
				// Input: getData() is URI containing a directory of data (vnd.android.cursor.dir/*) from which to pick an item.
				// Output: The URI of the item that was picked.
				// Constant Value: "android.intent.action.PICK"
				
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
				
				// startActivityForResult
				startActivityForResult(intent, REQ_PICK_FROM_ALBUM);

				break;
				
			case R.id.btn_join:
				
				doFileUpload(filepath);
				
				break;
			}
		}
	};
	
	public void doFileUpload(String filepath) {
		httpFileUpload("http://110.12.75.91:8080/ServerProg/mmember.do", "", filepath);
	}
	
	public void httpFileUpload(String urlString, String params, String filename) {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";	
		
		try {
			
			// An URLConnection for HTTP (RFC 2616) used to send and receive data over the web.
			// Data may be of any type and length. 
			// This class may be used to send and receive streaming data 
			// whose length is not known in advance.

			// Uses of this class follow a pattern:

			// 1. Obtain a new HttpURLConnection by calling 
			// URL.openConnection() and casting the result to HttpURLConnection.

			// 2. Prepare the request. The primary property of a request is its URI. 
			// Request headers may also include metadata such as
			// credentials, preferred content types, and session cookies.

			// 3. Optionally upload a request body. 
			// Instances must be configured with setDoOutput(true) 
			// if they include a request body. 
			// Transmit data by writing to the stream returned by getOutputStream().
			
			// 4. Read the response. Response headers typically include metadata 
			// such as the response body's content type and length, modified dates and session cookies. 
			// The response body may be read from the stream returned by getInputStream().
			// If the response has no body, that method returns an empty stream.
			
			// 5. Disconnect. Once the response body has been read, 
			//the HttpURLConnection should be closed by calling disconnect().
			// Disconnecting releases the resources held by a connection so they may be closed or reused.
			
			URL connectURL = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
			
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			conn.setUseCaches(false);
			
			// POST 방식으로 요청.
			conn.setRequestMethod("POST");
			
			// Header 설정.
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			
			// byte[]
			// twoHyphens + boundary + lineEnd 구분자
			// Output 			
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
						
			// 첫 번째 파라미터(String) 
			dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd + lineEnd + et_id.getText());
								
			// 두 번째 파라미터(String) 
			dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"pwd\"" + lineEnd + lineEnd + et_pwd.getText());
						
			// cmd=insert
			dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"cmd\"" + lineEnd + lineEnd + "insert");
						
			dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"upimage\"; filename=\"" + filename + "\"" + lineEnd);
			dos.writeBytes("Content-Type: application/octet-stream" + lineEnd + lineEnd);
						
			FileInputStream fileInputStream = new FileInputStream(filename);
						
			int bytesAvailable = fileInputStream.available();
			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
						
			byte[] buffer = new byte[bufferSize];

			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
						
			while (bytesRead > 0) {
				// Upload file part(s)
				DataOutputStream dataWrite = new DataOutputStream(conn.getOutputStream());
				
				dataWrite.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available(); 
				bufferSize = Math.min(bytesAvailable, maxBufferSize); 
				bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
			} 
						
			fileInputStream.close();
									
			// twoHyphens + boundary + lineEnd 구분자. 반드시 작성.
			dos.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);
						
			dos.flush();
			dos.close();
						
			// 결과 반환.			
			BufferedReader rd = null;
			  
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			
			String line = null;
			
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		switch (requestCode) {
		case REQ_PICK_FROM_ALBUM:
			Uri imagrUri = data.getData();
			iv_image.setImageURI(imagrUri);
			
			filepath = getRealImagePath(imagrUri);
		}
	}    
	
	/**
	 * 이미지 URI로 부터 실제 파일 경로를 가져온다.
	 * @param uriPath : URI
	 * @return path : 실제 파일 경로
	 */
	
	public String getRealImagePath(Uri uriPath) {
		
		String[] proj = {MediaStore.Images.Media.DATA};
		
		Cursor cursor = managedQuery(uriPath, proj, null, null, null);
		
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		
		cursor.moveToFirst();
		
		// uri example content://media/external/images/media/18
		
		String path = cursor.getString(index);
		// path example /mnt/sdcard/Cymera/CYMERA_20121001_191332.jpg
		
		path = path.substring(5);
		// path example sdcard/Cymera/CYMERA_20121001_191332.jpg
		
		cursor.close();
		
		return path;				
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
