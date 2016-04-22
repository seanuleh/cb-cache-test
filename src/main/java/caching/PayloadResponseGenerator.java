package caching;

import org.mule.api.MuleEvent;

import com.mulesoft.mule.cache.responsegenerator.ResponseGenerator;

/*
 *  This is currently unused
 */

public class PayloadResponseGenerator implements ResponseGenerator {    
    @Override    
    public MuleEvent create(MuleEvent request, MuleEvent cachedResponse) {
    	logger("[PayloadResponseGenerator.create]");
    	logger("[PayloadResponseGenerator.create] requestPayload: " + request.getMessage().getPayload());
    	logger("[PayloadResponseGenerator.create] responsePayload: " + cachedResponse.getMessage().getPayload());
        request.getMessage().setPayload(cachedResponse.getMessage().getPayload());
        return request;    
    }
    
    public void logger(String str){
    	System.out.println(str);
    }
}