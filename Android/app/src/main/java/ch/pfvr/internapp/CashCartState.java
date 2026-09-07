package ch.pfvr.internapp;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Durable representation of the Vereinsbeiz shopping cart. */
final class CashCartState {
    private CashCartState() {}

    static Set<String> encode(Map<String,Integer> cart){
        Set<String> out=new LinkedHashSet<>();
        if(cart==null)return out;
        for(Map.Entry<String,Integer> entry:cart.entrySet()){
            String id=cleanId(entry.getKey());
            int quantity=entry.getValue()==null?0:entry.getValue();
            if(id.isEmpty()||quantity<=0)continue;
            out.add(id+"="+Math.min(99,quantity));
        }
        return out;
    }

    static Map<String,Integer> decode(Set<String> stored){
        Map<String,Integer> out=new LinkedHashMap<>();
        if(stored==null)return out;
        for(String value:stored){
            if(value==null)continue;
            int separator=value.lastIndexOf('=');
            if(separator<=0||separator>=value.length()-1)continue;
            String id=cleanId(value.substring(0,separator));
            if(id.isEmpty())continue;
            try{
                int quantity=Integer.parseInt(value.substring(separator+1));
                if(quantity>0)out.put(id,Math.min(99,quantity));
            }catch(NumberFormatException ignored){}
        }
        return out;
    }

    private static String cleanId(String id){
        if(id==null)return "";
        String clean=id.trim();
        if(clean.isEmpty()||clean.length()>120||clean.indexOf('\n')>=0||clean.indexOf('\r')>=0||clean.indexOf('=')>=0)return "";
        return clean;
    }
}
