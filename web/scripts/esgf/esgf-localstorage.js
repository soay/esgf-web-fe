

//general functions for localStorage

Storage.prototype.setObject = function(key,value) {
	this.setItem(key,JSON.stringify(value));
}

Storage.prototype.getObject = function(key) {
	return JSON.parse(this.getItem(key));
}


ESGF.localStorage.toString = function(category) {
	
	
	if($.browser.msie) {
		//alert('IE toString - need to create a local objects');
		//alert('tempStore: ' + ESGF.setting.tempStore['esgf_queryString']);

		var map = ESGF.setting.tempStore[category];
		
		//var map = JSON.parse(localStorage.getItem(category));
		
		return JSON.stringify(map);

		
		
		
	} else {
		
		
		var map = localStorage.getObject(category);
		//var map = localStorage[category];
		
		
		return JSON.stringify(map);
	}
	
	
	
}

ESGF.localStorage.search = function(category, searchTerm) {
	
	if($.browser.msie) {
		//alert('IE search');
		
		var map = ESGF.setting.tempStore[category];
		
		//var map = JSON.parse(localStorage.getItem(category));
		
		
		var found = false;
		
		//search through the keys
		for(var key in map) {
			if(key == searchTerm) {
				found = true;
			}
		}
		return found;
		
		
		
		
		
	} else {
		
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
			//localStorage[category] = {'' : ''};
		}
		//var map = localStorage[category];
		var map = localStorage.getObject(category);
		//CTRL Z TO HERE
		
		var found = false;
		
		//search through the keys
		for(var key in map) {
			if(key == searchTerm) {
				found = true;
			}
		}
		return found;
		
	}
	
	
};


ESGF.localStorage.get = function(category, key) {
	
	if($.browser.msie) {
		//alert('IE get for category ' + category);
		
		var map = ESGF.setting.tempStore[category];
		//var map = JSON.parse(localStorage.getItem(category));
		
		
		return map[key];
		
		
		
	} else {
		
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		var map = localStorage.getObject(category);
		return map[key];
		
	}
	
	
};

ESGF.localStorage.getAll = function(category) {
	
	if($.browser.msie) {
		
		var map = ESGF.setting.tempStore[category];
		//var obj = localStorage.getItem(category);
		//var jsontext = '{"firstname":"Jesper","surname":"Aaberg","phone":["555-0100","555-0120"]}';
		//var contact = JSON.parse(obj);
		return map;
		
	} else {
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
			
		}
		var map = localStorage.getObject(category);
		
		return map;
	}
	
};

ESGF.localStorage.put = function(category, key, value) {
	if($.browser.msie) {
			//alert('IE put for category ' + category);

		
		//var map = JSON.parse(localStorage.getItem(category));
		var map = ESGF.setting.tempStore[category];
		
		
		var canPut = true;
		for (var mapKey in map) {
			if (key == mapKey) {
				canPut = false;
			}
		}
		//If there is no duplicate,
		//add item to the map and place it back in the localStorage category map
		if(canPut) {
			//alert('map ' + map);
			//alert('put ... key ' + key + ' value ' + value);
			map[key] = value;		
			ESGF.setting.tempStore[category] = map;
			//localStorage.setItem(category,JSON.stringify(map));
			
			
		}
		
		
		
		
	} else {
		
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		//var map = localStorage.getObject(category);
		var map = JSON.parse(localStorage.getItem(category));
		var canPut = true;
		for (var mapKey in map) {
			if (key == mapKey) {
				canPut = false;
			}
		}
		//If there is no duplicate,
		//add item to the map and place it back in the localStorage category map
		if(canPut) {
			map[key] = value;		
			//localStorage.setObject(category,map);
			localStorage.setItem(category,JSON.stringify(map));
			
		}
		
	}
	
	
};

ESGF.localStorage.update = function(category, key, value) {
	
	if($.browser.msie) {
		//alert('IE update');
		var map = ESGF.setting.tempStore[category];
		//var map = JSON.parse(localStorage.getItem(category));
		var canUpdate = false;
		for (var mapKey in map) {
			if (key == mapKey) {
				canUpdate = true;
			}
		}
		if(canUpdate) {
			map[key] = value;
			ESGF.setting.tempStore[category] = map;

			//localStorage.setItem(category,JSON.stringify(map));
		}

		
	} else {
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		var map = localStorage.getObject(category);
		var canUpdate = false;
		for (var mapKey in map) {
			if (key == mapKey) {
				canUpdate = true;
			}
		}
		if(canUpdate) {
			map[key] = value;
			localStorage.setObject(category,map);
		}
		
	}
	
	
};

ESGF.localStorage.removeAll = function(category) {
	
	if($.browser.msie) {
		//alert('IE removeAll');
		var map = ESGF.setting.tempStore[category];

		//var map = JSON.parse(localStorage.getItem(category));
		
		//delete localStorage[category];
		delete map;
		
	} else {
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		delete localStorage[category];
	}
	
	
};

ESGF.localStorage.remove = function(category, key) {
	
	//alert(localStorage);
	
	if($.browser.msie) {
		//alert('IE remove for category ' + category);
		
		//get the map
		var map = ESGF.setting.tempStore[category];

		//var map = JSON.parse(localStorage.getItem(category));
		
		//alert('mapstring: ' + JSON.stringify(map));
		
		for(var mapKey in map) {
			if (key == mapKey) {
				canRemove = true;
			}
		}
		if(canRemove) {
			delete map[key];
			ESGF.setting.tempStore[category] = map;
			//localStorage.setItem(category,JSON.stringify(map));
			
		}
		
		
		
		
	} else {
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		var map = localStorage.getObject(category);

		var canRemove = false;
		for (var mapKey in map) {
			if (key == mapKey) {
				canRemove = true;
			}
		}
		if(canRemove) {
			delete map[key];
			localStorage.setObject(category,map);
		}
		
		
		
	}
	
	
	
};

ESGF.localStorage.append = function(category, key, value) {
	
	if($.browser.msie) {
		//alert('IE append');

		var map = ESGF.setting.tempStore[category];
		//var map = JSON.parse(localStorage.getItem(category));
		
		
		if(map[key] == undefined) {
			map[key] = value;
		} else {
			var keyStr = map[key];
			if(keyStr.search(value) != -1) {
				keyStr = value + ';' + keyStr;
				map[key] = keyStr;
			}
		}
		//ESGF.setting.tempStore[category] = map;
		localStorage.setItem(category,JSON.stringify(map));
		
		
		
	} else {
		
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		var map = localStorage.getObject(category);
		if(map[key] == undefined) {
			map[key] = value;
		} else {
			var keyStr = map[key];
			if(keyStr.search(value) != -1) {
				keyStr = value + ';' + keyStr;
				map[key] = keyStr;
			}
		}
		localStorage.setObject(category,map);
	}
	
	
};

ESGF.localStorage.removeFromValue = function(category, key, value) {
	
	if($.browser.msie) {
		//alert('IE removeFromValue');
		
		var map = ESGF.setting.tempStore[category];
		//var map = JSON.parse(localStorage.getItem(category));
		
		if(map != undefined) {
			var map = localStorage.getObject(category);
			if(map[key] == undefined) {
				map[key] = value;
			} else {
				var keyStr = map[key];
				if(keyStr.search(value) != -1) {
					var newStr = keyStr.replace(value,"");
					map[key] = newStr;
				}
			}

			ESGF.setting.tempStore[category] = map;
			//localStorage.setItem(category,JSON.stringify(map));
			
		}
		
		
		
	} else {
		if(localStorage[category] != undefined) {
			var map = localStorage.getObject(category);
			if(map[key] == undefined) {
				map[key] = value;
			} else {
				var keyStr = map[key];
				if(keyStr.search(value) != -1) {
					var newStr = keyStr.replace(value,"");
					map[key] = newStr;
				}
			}
			localStorage.setObject(category,map);
		}
	}
	
	
}; 


ESGF.localStorage.toKeyArr = function(category) {
	
	if($.browser.msie) {
		
		
		var dataCartMap = ESGF.localStorage.getAll(category);

		var arr = new Array();
		
		for(var key in dataCartMap) {
			
			if(key != '') {       			
	    		arr.push(key);
			}
		}
		return arr;
		

	} else {
		
		if(localStorage[category] == undefined) {
			localStorage.setObject(category,{'' : ''});
		}
		
		var arr = new Array();
		
		var dataCartMap = ESGF.localStorage.getAll(category);
		for(var key in dataCartMap) {
			if(key != '') {       			
	    		arr.push(key);
			}
		}
		return arr;
		
		
	}
	
	
};

ESGF.localStorage.printMap = function(category) {
	
	if($.browser.msie) {
		//alert('IE printMap');
		
		var map = ESGF.localStorage.getAll(category);

		
		LOG.debug("*****Map of " + category + "****");
		for(var key in map) {
			LOG.debug("key: " + key + " -> " + map[key]);
		}
		LOG.debug("*****End Map of " + category + "****");
		
	} else {
		var map = ESGF.localStorage.getAll(category);
		LOG.debug("*****Map of " + category + "****");
		for(var key in map) {
			LOG.debug("key: " + key + " -> " + map[key]);
		}
		LOG.debug("*****End Map of " + category + "****");
		
	}
	
	
} 



