/*
 * Copyright [2013] [Nazmul Idris]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zen.core.db;

import android.content.*;
import zen.core.*;
import zen.utlis.*;

import java.util.*;

/**
 * This class collects all the database related stuff in one place. this class has to be instantiated
 * in order to be used (it's not static). You can reference it via {@link AppData#dbManager}
 * instance variable.
 * <p/>
 * The resource IDs (R.id) for
 * blobs {@link AppData.ID_Types#Database_BLOB} &
 * kvps {@link AppData.ID_Types#Database_KVP}
 * make it really easy to declare your desired
 * databases, and these will all be created when this class is instantiated by it's constructor.
 * <p/>
 * All the lifecycle stuff is tied to {@link AppData} and it takes care of creation and destruction of
 * all the database resources.
 */
public class DBManager implements DBConstantsIF {

protected final String[] db_blob_IDs;
protected final String[] db_kvp_IDs;
private final   AppData  data;
/** stores db connections to all dbs declared in R.id blobs {@link AppData.ID_Types#Database_BLOB} */
private HashMap<String, DB_blob> DB_blob_map = new HashMap<String, DB_blob>();
/** stores db connections to all dbs declared in R.id kvps {@link AppData.ID_Types#Database_KVP} */
private HashMap<String, DB_kvp>  DB_kvp_map  = new HashMap<String, DB_kvp>();

/**
 * create all the declared dbs (kvp & blob) in the R.id for
 * blobs {@link AppData.ID_Types#Database_BLOB} &
 * kvps {@link AppData.ID_Types#Database_KVP}
 */
public DBManager(Context ctx, AppData data) {

  this.data = data;

  db_blob_IDs = data.getResourceIds(AppData.ID_Types.Database_BLOB);
  for (String dbBlob_Name : db_blob_IDs) {
    DB_blob_map.put(dbBlob_Name, new DB_blob(ctx, dbBlob_Name, DbVersion));
  }

  db_kvp_IDs = data.getResourceIds(AppData.ID_Types.Database_KVP);
  for (String dbKVP_name : db_kvp_IDs) {
    DB_kvp_map.put(dbKVP_name, new DB_kvp(ctx, dbKVP_name, DbVersion));
  }

}

/**
 * get a reference to the {@link DB_blob} that's bound to this in the R.id for
 * blobs {@link AppData.ID_Types#Database_BLOB}
 */
public DB_blob getDB_BLOB(int id) {
  return DB_blob_map.get(data.getResourceName(id));
}

/**
 * get a reference to the {@link DB_blob} that's bound to this in the R.id for
 * blobs {@link AppData.ID_Types#Database_BLOB}
 */
public DB_blob getDB_BLOB(String dbName) {
  return DB_blob_map.get(dbName);
}

/**
 * get a reference to the {@link DB_kvp} that's bound to this in the R.id for
 * kvps {@link AppData.ID_Types#Database_KVP}
 */
public DB_kvp getDB_KVP(String dbName) {
  return DB_kvp_map.get(dbName);
}

/**
 * get a reference to the {@link DB_kvp} that's bound to this in the R.id for
 * kvps {@link AppData.ID_Types#Database_KVP}
 */
public DB_kvp getDB_KVP(int id) {
  return DB_kvp_map.get(data.getResourceName(id));
}

/**
 * test all the declared dbs (kvp & blob)
 */
public void test() {
  for (String dbEnum : db_kvp_IDs) {
    getDB_KVP(dbEnum).test();
  }

  for (String dbEnum : db_blob_IDs) {
    getDB_BLOB(dbEnum).test();
  }
}

/**
 * shutdown all the declared dbs (kvp & blob) in the R.id for
 * blobs {@link AppData.ID_Types#Database_BLOB} &
 * kvps {@link AppData.ID_Types#Database_KVP}
 * <p/>
 * This is deprecated because the maps are no longer static. The initial implementation
 * used static maps, which is why there was this explicit release mechanism; this is
 * due to Android persisting the value of static objects between app lifecycle instances,
 * as long as the underlying Linux process was not destroyed.
 */
@Deprecated
public void shutdown() {

  for (String dbEnum : db_kvp_IDs) {
    getDB_KVP(dbEnum).shutdown();
  }

  for (String dbEnum : db_blob_IDs) {
    getDB_BLOB(dbEnum).shutdown();
  }

  DB_kvp_map.clear();
  DB_blob_map.clear();

  AndroidUtils.log(IconPaths.System,
                   "DBManager.shutdown - cleared all static objects");

}

}//end class DBManager