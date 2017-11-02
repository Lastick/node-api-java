package club.karbo.karbolightwallet;

// Copyright (c) 2017 The Karbowanec developers
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class KarboNode {

  private final String host_default = "127.0.0.1";
  private final int port_default = 32348;
  private final String rpc_v = "2.0";
  private final String id_conn = "EWF8aIFX0y9w";
  private final String log_tag = "KarboLightWallet";
  private String host = null;
  private int port = 0;
  private Boolean service_status = false;
  private Boolean oper_status = false;

  public KarboNode(String host, int port){
    if (host != null && port != 0){
      this.host = host;
      this.port = port;
      } else {
      this.host = this.host_default;
      this.port = this.port_default;
    }
  }
  
  private String doServiceNode(String req, String url_path, Boolean mode){
    String buff = "";
    this.service_status = false;
    URL url;
    HttpURLConnection conn = null;
    try {
      url = new URL("http://" + this.host + ":" + this.port + "/" + url_path);
      conn = (HttpURLConnection) url.openConnection();
      conn.setReadTimeout(15000);
      conn.setConnectTimeout(15000);
      if (mode){
        conn.setRequestMethod("POST");
        } else {
        conn.setRequestMethod("GET");
        req = "";
      }
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      OutputStream os = conn.getOutputStream();
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
      writer.write(req);
      writer.flush();
      writer.close();
      os.close();
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpsURLConnection.HTTP_OK){
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = br.readLine()) != null){
          buff += line;
        }
        br.close();
        this.service_status = true;
      }
      } catch (Exception e){
        e.printStackTrace();
      } finally {
        if (conn != null){
          conn.disconnect();
        }
    }
    return buff;
  }

  public String getFeeAddress(){
    String result = "";
    String buff = "";
    this.oper_status = false;
    buff = this.doServiceNode(null, "feeaddress", false);
    if (this.service_status){
      try {
        JSONObject root = new JSONObject(buff);
        if (root.isNull("error")){
          if (root.has("fee_address")){
            result = root.getString("fee_address");
            this.oper_status = true;
          }
        }
	    } catch (JSONException e){
		  e.printStackTrace();
      }
    }
    if (this.oper_status){
      Log.d(this.log_tag, "node: success");
      } else {
      Log.d(this.log_tag, "node: fail");
    }
    return result;
  }

  public String[] getPeers(){
    String[] result = new String[0];
    String buff = "";
    this.oper_status = false;
    buff = this.doServiceNode(null, "peers", false);
    if (this.service_status){
      try {
        JSONObject root = new JSONObject(buff);
        if (root.isNull("error")){
          if (root.has("peers") && root.has("status")){
            if (root.getString("status").equals("OK")){
              JSONArray peers_obj = root.getJSONArray("peers");
              int size = peers_obj.length();
              if(size > 0){
            	result = new String[size];
                for (int i = 0; i < size; i++){
                  result[i] = peers_obj.getString(i);
                }
              }
              this.oper_status = true;
            }
          }
        }
	    } catch (JSONException e){
		  e.printStackTrace();
      }
    }
    if (this.oper_status){
      Log.d(this.log_tag, "node: success");
      } else {
      Log.d(this.log_tag, "node: fail");
    }
    return result;
  }

  public Boolean getOperStatus(){
    return this.oper_status;
  }

}
