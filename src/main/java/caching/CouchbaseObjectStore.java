package caching;

import java.io.Serializable;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.context.MuleContextAware;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.mule.util.store.DeserializationPostInitialisable;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.SerializableDocument;

public class CouchbaseObjectStore<T extends Serializable> implements ObjectStore<T>, MuleContextAware {
	private Cluster cluster;
	private String bucketId;
	private String bucketPassword;
	private String clusterNode;
	private Bucket bucket;
	private Integer valueEvictionTime;
	private MuleContext muleContext;

	public CouchbaseObjectStore(){
	}
	
	private void initCache(){
		if (cluster == null) {
			logger("[CouchbaseObjectStore.initCache] Attempting to connect to: " + clusterNode);
			
			cluster = CouchbaseCluster.create(clusterNode);
			
			// Open the bucket
			if (bucketId != null && bucketPassword != null) {
				bucket = cluster.openBucket(bucketId, bucketPassword);
			} else if (bucketId != null) {
				bucket = cluster.openBucket(bucketId);
			} else {
				bucket = cluster.openBucket();
			}
		}
	}

	@Override
	public boolean contains(Serializable key) throws ObjectStoreException {
		logger("[ObjectStore.contains]: Key " + key);
		
		return false;
	}

	@Override
	public void store(Serializable key, Serializable value)
			throws ObjectStoreException {
		logger("[ObjectStore.store]: Storing [" + key + "] -> " + value);
		
		SerializableDocument doc = SerializableDocument.create(key.toString(), valueEvictionTime, value);
		bucket.upsert(doc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T retrieve(Serializable key) throws ObjectStoreException {
		initCache();
		
		logger("[ObjectStore.retrieve] Key: " + key);
		
		SerializableDocument responseDocument = bucket.get(key.toString(), SerializableDocument.class);
		MuleEvent event = null;
		
		if (responseDocument != null) {
			logger("[ObjectStore.retrieve]: " + responseDocument.content());
			
			Object val = (MuleEvent) responseDocument.content();
			
			if (val instanceof MuleEvent) {
				event = (MuleEvent) val;	
				if (event.getMuleContext() == null) {
					try {
						DeserializationPostInitialisable.Implementation.init(event, this.muleContext);
					} catch (Exception e) {
						throw new ObjectStoreException(e);
					}					
				}				
			}			
		}
		
		return (T) event;
	}

	@Override
	public T remove(Serializable key) throws ObjectStoreException {
		logger("[ObjectStore.remove] Key: " + key);
		
		return null;
	}

	@Override
	public boolean isPersistent() {
		logger("[ObjectStore.isPersistent]");
		
		return false;
	}

	@Override
	public void clear() throws ObjectStoreException {
		logger("[ObjectStore.clear]");
	}
	
	public void logger(String str){
		System.out.println(str);
	}
	
	/* 
	 *  Getters & Setters
	 */

	@Override
	public void setMuleContext(MuleContext context) {
		// TODO Auto-generated method stub
		muleContext = context;
	}
	
	public void setValueEvictionTime(Integer valueEvictionTime) {
		this.valueEvictionTime = valueEvictionTime;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public void setBucketPassword(String bucketPassword) {
		this.bucketPassword = bucketPassword;
	}

	public void setClusterNode(String clusterNode) {
		this.clusterNode = clusterNode;
	}

}
