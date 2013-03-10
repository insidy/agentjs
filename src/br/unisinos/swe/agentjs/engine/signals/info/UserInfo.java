package br.unisinos.swe.agentjs.engine.signals.info;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import br.unisinos.swe.agentjs.engine.EngineContext;

public class UserInfo {
	
	private SharedPreferences _settings;
	
	private String _name;
	private String _id;
	private String _gender;
	private String _facebookId;
	private String _facebookName;
	
	public UserInfo() {
		_settings = EngineContext.instance().getPreferences();
		
		_name = _settings.getString("user.name", "");
		_id = _settings.getString("user.id", "");
		_gender = _settings.getString("user.gender", "");
		
		_facebookId = _settings.getString("user.facebook.id", "");
		_facebookName = _settings.getString("user.facebook.name", "");
		
		if(_name.equals("")) {
			if(!_facebookName.equals("")) {
				fetchDataFromFacebook(_facebookName);
			}
		}
		
	}
	
	private void fetchDataFromFacebook(String id) {
		// GET http://graph.facebook.com/{id}?fields=id,name,gender
		
		//ex: http://graph.facebook.com/paulocesar.buttenbender?fields=id,name,gender

		saveToPreferences();
	}

	public void saveToPreferences() {
		SharedPreferences.Editor _editor = _settings.edit();
		if(!_name.equals("")) {
			_editor.putString("user.name", _name);
		}
		
		if(!_id.equals("")) {
			_editor.putString("user.id", _id);
		}
		
		if(!_facebookId.equals("")) {
			_editor.putString("user.facebook.id", _facebookId);
		}
		_editor.commit();
	}
	
	public void setName(String name) {
		this._name = name;
	}
	
	public void setId(String id) {
		this._id = id;
	}
	
	public void setFacebookId(String facebookId) {
		this._facebookId = facebookId;
	}
	
	public void setGender(String gender) {
		this._gender = gender;
	}
	
	public String getName() {
		return this._name;
	}
	
	public String getId() {
		return this._id;
	}
	
	public String getFacebookId() {
		return this._facebookId;
	}
	
	public String getFacebookName() {
		return this._facebookName;
	}
	
	public String getGender() {
		return this._gender;
	}

	public JSONObject toJson() {
		JSONObject selfJson = new JSONObject();
		
		try {
			selfJson.put("name", this.getName());
			selfJson.put("id", this.getId());
			selfJson.put("gender", this.getGender());
			
			selfJson.put("facebookId", _facebookId);
			selfJson.put("facebookNameId", _facebookName);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating json object of User Info");
		}
		
		return selfJson;
	}

}
