/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.fog.test.perfeval;

import org.cloudbus.cloudsim.sdn.example.topogenerators.*;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import static org.fog.test.perfeval.SIoTSimulador.fogDevices;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author rafael
 */
public class criaJSON {

        
	public static List<Sensor> sensors;
        protected static List<FogDevice> fog = new ArrayList<FogDevice>();
        protected static List<FogDevice> cloud = new ArrayList<FogDevice>();

    
        public criaJSON(List<FogDevice> fogDevices, List<FogDevice> cloud, List<Sensor> sensors) {}
        
        //Aqui da nome ao JSON, passa as listas fog, cloud e sensores para serem criadas
        public void criaTopologia(List<FogDevice> fog, List<FogDevice> cloud, List<Sensor> sensors){
		String jsonFileName = "SIoT-Simulador.json";
                criaJSON reqg = new criaJSON(fog, cloud, sensors);
		reqg.createTopology(fog, cloud, sensors);
		reqg.wrtieJSON(jsonFileName);  
        }  

        //Aqui é onde é adicionado as Fogs, Clouds, Sensores e faz os Links
	public void createTopology(List<FogDevice> fog,List<FogDevice> cloud, List<Sensor> sensors) {
                String name; 
                int level;   
                String tupleType; 
                double latency;  

            //Adiciona as Clouds
            for(FogDevice x : cloud){
                name = x.getNodeName();
                level = x.getLevel();
                addCloud(name, level);
             }
            
            //Adiciona as Fogs
            for(FogDevice y : fog){
                name = y.getNodeName();
                level = y.getLevel();
                addFog(name, level);
            }

            //Adiciona os Sensores
            for(Sensor x : sensors){
                    name = x.getName();
                    tupleType = x.getTupleType();
                    latency = x.getLatency();
                    addSensor(name, tupleType, latency);
            }
          
            //Links Cloud-Fog
            for(FogDevice x : cloud){
                for(FogDevice y : fog){
                    if(x.getId() == y.getParentId()){
                        addLink(y.getNodeName(),x.getNodeName(),y.getUplinkLatency());
                    }
                }
            }
            
            //Links Sensores-Fog
            for(FogDevice y : fog){
                for(Sensor x : sensors){
                    if(y.getNodeName() == x.getTupleType())
                        addLink(x.getName(),y.getNodeName(),x.getLatency());
                }
            }   
	}
	
	
	private List<FogSpec> fogs = new ArrayList<FogSpec>();
        private List<CloudSpec> clouds = new ArrayList<CloudSpec>();
	private List<SensorSpec> sensores = new ArrayList<SensorSpec>();
        private List<LinkSpec> links = new ArrayList<LinkSpec>();
        
	public FogSpec addFog(String name, int level) {
		FogSpec f = new FogSpec();
                
                f.name = name;
                f.level = level;

		fogs.add(f);
		return f;
	}
        
        public CloudSpec addCloud(String name, int level) {
		CloudSpec c = new CloudSpec();
                
                c.name = name;
                c.level = level;

		clouds.add(c);
		return c;
	}
        

        
        public SensorSpec addSensor(String name, String tupleType,double latency) {
		SensorSpec ss = new SensorSpec();
                
                ss.name = name;
                ss.tupleType = tupleType;
                ss.latency = latency;
		
                sensores.add(ss);
		return ss;
	}

        
        public LinkSpec addLink(String nameSource, String nameDest, double latency) {
		LinkSpec l = new LinkSpec();
                l.source = nameSource;
                l.destination = nameDest;
                l.latency = latency;
                links.add(l);
                return l;
	}
        

	class NodeSpec {
		String name;
                int level;
	}

	class FogSpec extends NodeSpec {
		
		@SuppressWarnings("unchecked")
		JSONObject toJSON() {
			FogSpec o = this;
			JSONObject obj = new JSONObject();
                        obj.put("level", o.level);			
                        obj.put("name", o.name);
			return obj;
		}
	}
        
        class CloudSpec extends NodeSpec {
		
		@SuppressWarnings("unchecked")
		JSONObject toJSON() {
			CloudSpec o = this;
			JSONObject obj = new JSONObject();
			obj.put("level", o.level);
                        obj.put("name", o.name);
			return obj;
		}
	}
        
        class SensorSpec extends NodeSpec {
                String tupleType; 
                double latency;  
		
                @SuppressWarnings("unchecked")
		JSONObject toJSON() {
			SensorSpec o = this;
			JSONObject obj = new JSONObject();
                        obj.put("tupleType", o.tupleType);
                        obj.put("latency", o.latency);
                        obj.put("name", o.name);
			return obj;
		}
	}

        
        class LinkSpec extends NodeSpec {
		String source;
		String destination;
		double latency;

		@SuppressWarnings("unchecked")
		JSONObject toJSON() {
			LinkSpec link = this;
			JSONObject obj = new JSONObject();
			obj.put("source", link.source);
			obj.put("destination", link.destination);
			obj.put("latency", link.latency);
			return obj;
		}
	}
     
	
	int vmId = 0;
	
	@SuppressWarnings("unchecked")
	public void wrtieJSON(String jsonFileName) {
		JSONObject obj = new JSONObject();

		JSONArray nodeList = new JSONArray();
		JSONArray linkList = new JSONArray();

		for(FogSpec o:fogs) {
			nodeList.add(o.toJSON());
                }
                
                for(CloudSpec o:clouds) {
			nodeList.add(o.toJSON());
                }
                
                for(SensorSpec o:sensores) {
			nodeList.add(o.toJSON());
                }
                
                for(LinkSpec link:links) {
			linkList.add(link.toJSON());
		}
                        
		obj.put("nodes", nodeList);
		obj.put("links", linkList);
	 
		try {
	 
			FileWriter file = new FileWriter(jsonFileName);
			file.write(obj.toJSONString().replaceAll(",", ",\n"));
			file.flush();
			file.close();
                        System.out.println(jsonFileName+" criado com sucesso!!!!");
	 
		} catch (IOException e) {
                        System.out.println("Erro ao criado JSON!!");
                        e.printStackTrace();
		}
                
                //Aqui exibe no console o JSON
		//System.out.println(obj);
	}
        }

