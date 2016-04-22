package caching;

import java.io.Serializable;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.lifecycle.Callable;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.transport.PropertyScope;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.SerializableDocument;
import com.couchbase.client.java.document.json.JsonObject;

public class CouchbaseObjectStoreJson<T extends Serializable> implements ObjectStore<T> {
	private Cluster cluster;
	private String bucketId;
	private String bucketPassword;
	private Bucket bucket;
	private Integer valueEvictionTime;
	
	public Integer getValueEvictionTime() {
		return valueEvictionTime;
	}

	public void setValueEvictionTime(Integer valueEvictionTime) {
		this.valueEvictionTime = valueEvictionTime;
	}

	public String getBucketId() {
		return bucketId;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public void setBucketPassword(String bucketPassword) {
		this.bucketPassword = bucketPassword;
	}

	public CouchbaseObjectStoreJson(){
		System.out.println("TRYING TO CREATE THE OBJECT STORE");
		
		cluster = CouchbaseCluster.create("127.0.0.1");
		valueEvictionTime = 60;
		
		// Open the bucket
		if (bucketId != null && bucketPassword != null) {
			bucket = cluster.openBucket(bucketId, bucketPassword);
		} else if (bucketId != null) {
			bucket = cluster.openBucket(bucketId);
		} else {
			bucket = cluster.openBucket();
		}
		
		MuleEvent event = RequestContext.getEvent( );
	}

	@Override
	public boolean contains(Serializable key) throws ObjectStoreException {
		// TODO Auto-generated method stub
		System.out.println("Does CB contain " + key);
		return false;
	}

	@Override
	public void store(Serializable key, Serializable value)
			throws ObjectStoreException {
		
		MuleEvent event = (MuleEvent) value;
		String json;
		try {
			json = event.getMessage().getPayloadAsString();
			JsonDocument doc = JsonDocument.create(key.toString(), valueEvictionTime, JsonObject.fromJson(json));
			bucket.upsert(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public T retrieve(Serializable key) throws ObjectStoreException {
		// TODO Auto-generated method stub
		System.out.println("CB pls find this for me: " + key);
		System.out.println("BTW key is of type: " + key.getClass());
		
		JsonDocument responseDocument = bucket.get(key.toString());
		
		if (responseDocument != null) {
			System.out.println("the response document was not null: " + responseDocument.content());
			System.out.println("The content is of class: " + responseDocument.content().getClass());
			
			return (T) responseDocument.content();
		}
		else return null;
	}

	@Override
	public T remove(Serializable key) throws ObjectStoreException {
		// TODO Auto-generated method stub
		System.out.println("CB get rid of this shit: " + key);
		
		return null;
	}

	@Override
	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() throws ObjectStoreException {
		// TODO Auto-generated method stub
		
	}
	/*
	protected MuleEvent createEvent(Object payload)
		      throws Exception {
		    FlowConstruct parentFlow = MuleTestUtils.getTestFlow("MainService", muleContext);
		    return new DefaultMuleEvent(muleMessageWithPayload(payload), MessageExchangePattern.REQUEST_RESPONSE,
		                                parentFlow);
		  }
*/


}
